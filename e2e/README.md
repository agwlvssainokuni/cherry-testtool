# cherry-testtool-e2e

`demo`+`client/cli`+`client/webconsole`をまたぐ一気通貫(E2E)の自動テスト。依存ライブラリのバージョンアップによる挙動変化を検知する目的で追加した(FR12)。Gradleマルチプロジェクトには含めない、独立したnpmプロジェクト(`@playwright/test`)。

## 構成

- `playwright.config.ts` — Playwright Test設定。`globalSetup`/`globalTeardown`でdemo・webconsoleを自動起動・停止する
- `global-setup.ts` / `global-teardown.ts` — demo(`8080`)・webconsole(`9090`)をビルド済みjarから起動し、起動確認後にテストを開始する。終了時に両プロセスを停止する
- `support/` — 共通設定(`config.ts`)・プロセス起動/停止ヘルパー(`processes.ts`)・cli実行ヘルパー(`cli.ts`)
- `tests/` — テストシナリオ本体
  - `cli.spec.ts` — cliからdemoへ直接(`http://localhost:8080`)、`invoke`・`stubconfig register/show/clear`とスタブ効果を検証
  - `webconsole-ui.spec.ts` — 実ブラウザ操作でwebconsole(`http://localhost:9090`)のSPAを検証(Home→Invoker→Stubconfig、実行、スタブ登録とその効果)
  - `webconsole-api.spec.ts` — ブラウザを介さず、webconsoleの`/testtool/**`プロキシ層をHTTPレベルで検証
  - `webconsole-api-key-mismatch.spec.ts` — demo/webconsoleそれぞれの`api-key`設定が食い違うケースを検証。global-setupとは独立した専用ポート(`8081`/`9091`)でdemo/webconsoleを自己完結的に起動・停止する

## テスト一覧

| ファイル | テスト | 内容 | 実行パス |
|---|---|---|---|
| `cli.spec.ts` | invoke: toBeInvoked系メソッドの一括呼出しが成功する | cliから`demo`へ直接、`invoke-samples`配下のスクリプトを一括実行し成功を確認 | 両方 |
| `cli.spec.ts` | stubconfig: register→スタブ効果反映→show→clear→復元 | スタブ未登録時の値(`1234`)→`register`→スタブ値(`9999`)に変化→`show`で内容確認→`clear`(空スクリプト再登録)→元の値に復元、を一気通貫で確認 | 両方 |
| `cli.spec.ts` | サーバー側キー設定時、クライアントがヘッダ無しだと拒否される | APIキー「ありなし」パターン | `with-key`のみ |
| `cli.spec.ts` | サーバー側キー未設定時、クライアントがヘッダ付きでも成功する | APIキー「なしあり」パターン | `no-key`のみ |
| `webconsole-ui.spec.ts` | Home→Invoker: クラス/メソッド解決から実行結果表示まで | 実ブラウザ操作。Home画面→Invoker遷移→クラス/メソッド名からのBean/オーバーロード自動解決→スクリプト実行→結果表示を確認 | 両方 |
| `webconsole-ui.spec.ts` | Home→Stubconfig: 登録・スタブ効果・クリア | 実ブラウザ操作。Home→Stubconfig遷移→スタブ登録→(Playwright requestで)`demo`側APIの返却値変化を確認→クリア→復元を確認 | 両方 |
| `webconsole-api.spec.ts` | resolve/bean: webconsole経由とdemo直接で同一結果 | ブラウザを介さないHTTPレベル検証。webconsoleのプロキシ層が`demo`と同一結果を返すことを確認 | 両方 |
| `webconsole-api.spec.ts` | invoker/invoke: webconsole経由でdemoのメソッドを呼び出せる | 同上、`invoke`エンドポイントのプロキシ動作確認 | 両方 |
| `webconsole-api.spec.ts` | セキュリティヘッダが付与される | `X-Frame-Options`等、`GatewayRouteConfig`が付与するセキュリティヘッダの確認 | 両方 |
| `webconsole-api-key-mismatch.spec.ts` | demoのみキー設定: webconsole経由のリクエストは401が伝播する | demo/webconsole自体の設定食い違い「ありなし」。専用ポート(`8081`/`9091`)で自己完結的に検証 | `no-key`のみ |
| `webconsole-api-key-mismatch.spec.ts` | webconsoleのみキー設定: demoはキー未要求のため成功する | 同上「なしあり」パターン | `no-key`のみ |

カバー範囲: cli直接呼出し、webconsole実ブラウザ操作、webconsoleのHTTPプロキシ層、スタブ効果の実地反映、APIキー保護の4パターン(なしなし/ありあり/ありなし/なしあり、cli側とwebconsole-demo間の両方)。

## 事前準備

demo・webconsole・cliのjarをビルドしておく必要がある(`e2e`自身はビルドを行わない)。

```bash
# リポジトリ直下で
./gradlew build
```

## 実行方法

```bash
cd e2e
npm install
npm run install:browsers   # 初回のみ(Playwrightのchromiumをダウンロード)

npm run test:e2e:no-key    # APIキー未設定の状態で実行
npm run test:e2e:with-key  # APIキー設定時の状態で実行(demo/webconsole起動時に自動設定)
npm run test:e2e           # 上記両方を順に実行
```

`test:e2e:with-key`は環境変数`E2E_API_KEY`をセットして実行され、`global-setup.ts`がdemo・webconsole起動時に`cherry.testtool.web.api-key`として同じ値を設定する(FR9/FR10のAPIキー保護機能を有効化した状態でのシナリオ検証)。

## 通常のビルドとの関係

`./gradlew build`/`check`には含めない。プロセスの起動を伴い時間がかかる・環境依存で不安定になりうるため、独立したnpm scriptとして実行する。CI(GitHub Actions、`.github/workflows/e2e.yml`)では`push`・`pull_request`・手動実行(`workflow_dispatch`)のタイミングで自動実行される。

既存の手動結合確認手順([client/cli](../client/cli)・[client/webconsole](../client/webconsole)の各README)は、開発時の即時確認用途としてこのE2Eテストと併存させる。
