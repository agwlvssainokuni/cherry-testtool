# Code Generation Summary: demo+クライアント(cli/webconsole)のE2Eテスト追加(FR12)

## 作成したファイル

### e2e/(新規、独立npmプロジェクト)
- `e2e/package.json` — `@playwright/test`・`@types/node`・`cross-env`・`typescript`をdevDependenciesとして追加。`test:e2e:no-key`・`test:e2e:with-key`・`test:e2e`(両方を順次実行)・`install:browsers`スクリプトを定義
- `e2e/tsconfig.json` — Node実行用(`module`/`moduleResolution`は`node16`、TypeScript 7の`node10`廃止に対応)
- `e2e/.gitignore` — `node_modules`・`test-results/`・`playwright-report/`・`playwright/.cache/`・`.e2e-pids.json`を除外
- `e2e/README.md` — 構成・実行方法を記載(他モジュールと同じ書式)
- `e2e/playwright.config.ts` — `testDir: './tests'`、`globalSetup`/`globalTeardown`、`projects: [chromium]`、`fullyParallel: false`(demo/webconsoleの共有状態を扱うため直列実行)、`reporter: [list, html]`
- `e2e/global-setup.ts` / `e2e/global-teardown.ts` — demo(`8080`)・webconsole(`9090`)をビルド済みjarから起動・停止。`E2E_API_KEY`環境変数が設定されていれば`--cherry.testtool.web.api-key`引数を付与して起動する。起動したプロセスのPIDは`.e2e-pids.json`へ一時保存し、teardownで読み取って停止する
- `e2e/support/config.ts` — jarパス・URL・フィクスチャパス等の定数
- `e2e/support/processes.ts` — 子プロセス起動・HTTPポーリングによる起動待機・プロセス停止の共通関数
- `e2e/support/cli.ts` — `runCli()`ヘルパー(cliのjarを子プロセス実行し、`CHERRY_TESTTOOL_WEB_APIKEY`環境変数でAPIキーを渡せる)

### テストシナリオ
- `e2e/tests/cli.spec.ts` — cliからdemoへ直接(`http://localhost:8080`)。`invoke`(`demo/invoke-samples`を使用)、`stubconfig register/show/clear`とスタブ効果(`demo/stub-samples`を使用、`GET /api/sample/stubbed1/int`で反映確認)。**(2026-08-15追記、レビュー時の追加依頼)** APIキーの不一致パターンも簡易確認: サーバー側キー設定時にクライアントがヘッダ無しだと拒否される(with-keyパスのみ実行、no-keyパスは`test.skip`)、サーバー側キー未設定時にクライアントがヘッダ付きでも成功する(no-keyパスのみ実行、with-keyパスは`test.skip`)
- `e2e/tests/webconsole-ui.spec.ts` — 実ブラウザ操作でwebconsole(`http://localhost:9090`)のSPAを検証。Home→Invoker(クラス/メソッド解決→実行→結果表示)、Home→Stubconfig(登録→スタブ効果→クリア(空スクリプトでの再登録))
- `e2e/tests/webconsole-api.spec.ts` — Playwrightの`request`機能でブラウザを介さずwebconsoleの`/testtool/**`プロキシ層を検証(demo直接との結果比較、invoke呼出し、セキュリティヘッダ)

### CI
- `.github/workflows/e2e.yml` — `push`(`main`)・`pull_request`・`workflow_dispatch`トリガー。JDK 25・Node.js(`lts/*`)セットアップ→`./gradlew build`→`e2e/`で`npm ci`→Playwright chromiumインストール→`npm run test:e2e`。失敗時はPlaywright HTMLレポートをアーティファクトとしてアップロード

### ドキュメント更新
- ルート`README.md`: アーキテクチャツリーへ`e2e/`追加、モジュールREADME一覧へ`e2e/README.md`追加、「テスト」節へ「E2Eテストを実行する」サブセクション追加

## 実装中に判明した既存仕様

- StubconfigPageの「クリア」ボタン(`stubconfig-clear-button`)はテキストエリアをローカルでクリアするのみでAPIを呼ばない(FR11のテストで既に確認済みの仕様)。サーバー側のスタブ解除には、クリア後に空スクリプトで「登録」ボタンを押す必要がある。当初`webconsole-ui.spec.ts`はこの点を誤解しテストが失敗したため、フローを修正した
- webconsoleはAPIキー設定時、ブラウザには要求せず内部でdemoへのプロキシ時にヘッダを自動付与する(`GatewayRouteConfig`のコメント通り)。一方cli・Playwrightの`request`機能によるdemo直接アクセスでは、APIキーヘッダを明示的に付与する必要がある(`webconsole-api.spec.ts`の`resolve/bean`比較テストで、demo直接呼び出し側にヘッダを付与するよう修正した)

## 確認結果

- `./gradlew build`(全モジュール): 成功
- `npx tsc --noEmit`(`e2e/`): 成功
- `npm run test:e2e:no-key`: 8成功・1スキップ(with-key専用の不一致パターンをスキップ)
- `npm run test:e2e:with-key`: 8成功・1スキップ(no-key専用の不一致パターンをスキップ、APIキー設定時の自動付与・明示付与とも動作確認)
