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
