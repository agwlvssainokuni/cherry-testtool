# Code Generation Plan: demo+クライアント(cli/webconsole)のE2Eテスト追加(FR12)

**Unit context**: Units Generationはスキップ済み(execution-plan.md参照)。既存4Unit(`lib`/`demo`/`client:cli`/`client:webconsole`)のコードは変更せず、リポジトリ直下に新規`e2e/`(独立npmプロジェクト)・`.github/workflows/`を追加する。

**依存事実確認済み**(Step 1で調査済み):
- 各jarパス: `demo/build/libs/cherry-testtool-demo.jar`・`client/webconsole/build/libs/cherry-testtool-webconsole.jar`・`client/cli/build/libs/cherry-testtool-cli.jar`(いずれもバージョンサフィックス無し)
- 既存フィクスチャ流用: `demo/invoke-samples/cherry.testtool.demo.SampleService/*.js`(`invoke`用)・`demo/stub-samples/cherry.testtool.demo.SampleService/*.js`(`stubconfig`用、`toBeStubbed1.1.js`は`9999`)
- スタブ効果の観測先: `GET /api/sample/stubbed1/int?p1=1030&p2=204`(`SampleController`、通常値`1234`、スタブ適用時`9999`)
- APIキー(FR9/FR10): webconsoleは`cherry.testtool.web.api-key`設定時、demoへのプロキシ時に自動でヘッダを付与し、ブラウザ側には要求しない(`GatewayRouteConfig`のコメント「SPA利用者(ブラウザ)には別途要求しない」)。cli直接(demoへ)は`RootCommand.effectiveHeaders()`により`cherry.testtool.web.api-key`環境変数(Spring Boot既定の環境変数マッピングで`CHERRY_TESTTOOL_WEB_APIKEY`)を設定すれば自動付与される
- SPAの`data-testid`属性(FR11で付与済み): `invoker-class-name-input`・`invoker-bean-name-select`・`invoker-method-name-input`・`invoker-method-index-select`・`invoker-script-textarea`・`invoker-invoke-button`・`invoker-result-textarea`、`stubconfig-*`(同様の命名)、`home-card-invoker`・`home-card-stubconfig`
- 稼働確認用エンドポイント: demoは`GET /api/sample/stubbed1/int?p1=0&p2=0`(200応答)、webconsoleは`GET /`(SPAの`index.html`、200応答)

## Step 1: 事前調査(完了)
- [x] jarの成果物名、既存フィクスチャ(`invoke-samples`/`stub-samples`)、`SampleController`のエンドポイント、APIキーの付与方式、SPAの`data-testid`属性を確認済み(上記「依存事実確認済み」参照)

## Step 2: `e2e/`ディレクトリの基本ファイル新設
- [ ] `e2e/package.json`(`name: "cherry-testtool-e2e"`、`private: true`、devDependencies: `@playwright/test`・`typescript`・`@types/node`。scripts: `test:e2e:no-key`・`test:e2e:with-key`・`test:e2e`(両方を順次実行)・`install:browsers`(`playwright install --with-deps chromium`))
- [ ] `e2e/tsconfig.json`(Node実行用、`module: "commonjs"`または`"esnext"`+`ts-node`不要な構成。Playwright Test標準の`@playwright/test`推奨tsconfigに準拠)
- [ ] `e2e/.gitignore`(`node_modules`・`test-results/`・`playwright-report/`・`playwright/.cache/`)
- [ ] `e2e/README.md`(目的、ローカル実行手順(`./gradlew build`要求含む)、ディレクトリ構成の概要。他モジュールREADMEと同じ書式)

## Step 3: 共通設定・ヘルパーモジュール
- [ ] `e2e/support/config.ts`: リポジトリルート(`path.resolve(__dirname, '..', '..')`)、各jarパス、`DEMO_URL = 'http://localhost:8080'`・`WEBCONSOLE_URL = 'http://localhost:9090'`、フィクスチャパス(`demo/invoke-samples`・`demo/stub-samples`)を定数化
- [ ] `e2e/support/processes.ts`: 子プロセス起動(`spawn('java', ['-jar', jarPath, ...args], { cwd })`)・HTTPポーリングによる起動待機(`waitForHttp(url, timeoutMs)`)・プロセス停止(SIGTERM、タイムアウト後SIGKILL)の共通関数を実装
- [ ] `e2e/support/cli.ts`: `runCli(args: string[], opts？: { apiKey？: string }): Promise<{ stdout: string; stderr: string; exitCode: number }>`。`child_process.execFile('java', ['-jar', CLI_JAR_PATH, ...args], { env: opts.apiKey ? { ...process.env, CHERRY_TESTTOOL_WEB_APIKEY: opts.apiKey } : process.env })`で実装

## Step 4: グローバルセットアップ/ティアダウン
- [ ] `e2e/global-setup.ts`: 環境変数`E2E_API_KEY`(未設定/空文字なら「APIキー無し」パス)を読み取り、設定時は`--cherry.testtool.web.api-key=${E2E_API_KEY}`引数を付与してdemoを起動(`cwd: demo/`)→`waitForHttp('http://localhost:8080/api/sample/stubbed1/int?p1=0&p2=0')`で起動確認→同様にwebconsoleを起動(`cwd: client/webconsole/`、同じAPIキー引数)→`waitForHttp('http://localhost:9090/')`で起動確認。起動した2プロセスのPIDを`e2e/.e2e-pids.json`へ書き出す
- [ ] `e2e/global-teardown.ts`: `e2e/.e2e-pids.json`を読み取り、両プロセスへSIGTERM(未終了なら追ってSIGKILL)を送信し、一時ファイルを削除する

## Step 5: Playwright設定
- [ ] `e2e/playwright.config.ts`: `testDir: './tests'`、`globalSetup`/`globalTeardown`を上記ファイルへ設定、`projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }]`(ブラウザはchromiumのみ、社内開発ツールのためクロスブラウザ検証は対象外)、`reporter: 'list'`、`timeout`・`expect.timeout`を複数プロセス起動を考慮した余裕のある値に設定、`fullyParallel: false`(demo/webconsoleの共有スタブ状態を扱うテストが多いため直列実行)

## Step 6: cliシナリオのテスト実装
- [ ] `e2e/tests/cli.spec.ts`を新設。以下をAPIキー無し/有りの両モードで共通実行できるよう、`process.env.E2E_API_KEY`を読み取って`runCli`へ渡すヘルパーを介して記述:
  - `invoke`: `runCli(['--url', DEMO_URL, 'invoke', INVOKE_SAMPLES_DIR])`を実行し、終了コード`0`・標準出力に`toBeInvoked1`等の実行結果が含まれることを確認
  - `stubconfig`のスタブ効果: (1) `GET {DEMO_URL}/api/sample/stubbed1/int?p1=1030&p2=204`が`1234`を返すことを確認 → (2) `runCli(['--url', DEMO_URL, 'stubconfig', 'register', STUB_SAMPLES_DIR])`でスタブ登録 → (3) 同じGETが`9999`を返すことを確認(スタブ適用) → (4) `runCli(['--url', DEMO_URL, 'stubconfig', 'show', STUB_SAMPLES_DIR])`で登録内容が表示されることを確認 → (5) `runCli(['--url', DEMO_URL, 'stubconfig', 'clear', STUB_SAMPLES_DIR])`でスタブ解除 → (6) 同じGETが`1234`に戻ることを確認

## Step 7: webconsole実ブラウザ操作シナリオのテスト実装
- [ ] `e2e/tests/webconsole-ui.spec.ts`を新設(`@playwright/test`の`page`フィクスチャ使用、`baseURL: WEBCONSOLE_URL`):
  - Home画面表示確認(見出し「テストツール」)→`home-card-invoker`クリックで`/invoker`へ遷移確認
  - Invoker画面: `invoker-class-name-input`に`cherry.testtool.demo.SampleService`を入力しblur、`invoker-bean-name-select`の選択肢が populated されるのを待つ→`invoker-method-name-input`に`toBeInvoked1`を入力しblur→`invoker-method-index-select`で`(long,long)`パターンを選択→`invoker-script-textarea`に`[3, 4]`を入力→`invoker-invoke-button`クリック→`invoker-result-textarea`に`7`を含む結果が表示されることを確認
  - Sidebarナビゲーションで`/stubconfig`へ遷移(`home-card-stubconfig`または直接ナビゲーション)
  - Stubconfig画面: 同様に`cherry.testtool.demo.SampleService`・`toBeStubbed1`(Integer overload)を指定、`stubconfig-script-textarea`に`9999`を入力し`stubconfig-register-button`クリック→登録成功を`stubconfig-result-textarea`で確認→別途Playwright `request`機能で`GET {DEMO_URL}/api/sample/stubbed1/int?p1=1030&p2=204`が`9999`を返すことを確認(スタブ効果の実地検証)→`stubconfig-clear-button`クリックで解除→同GETが`1234`に戻ることを確認

## Step 8: webconsole HTTPレベル(プロキシ層)シナリオのテスト実装
- [ ] `e2e/tests/webconsole-api.spec.ts`を新設。Playwrightの`request`フィクスチャ(`APIRequestContext`)を用い、ブラウザを介さず`WEBCONSOLE_URL`の`/testtool/**`へ直接リクエストする:
  - `GET {WEBCONSOLE_URL}/testtool/resolve/bean?...`が`{DEMO_URL}`への直接リクエストと同一結果を返すことを確認(プロキシが正しく機能していることの検証)
  - `POST {WEBCONSOLE_URL}/testtool/invoker/invoke`(`invoke-samples`の1件分のボディ)が想定通りのレスポンスを返すことを確認
  - レスポンスヘッダに`X-Frame-Options: DENY`等のセキュリティヘッダが付与されていることを確認(`GatewayRouteConfig`の`secureHeaders()`の検証)

## Step 9: GitHub Actionsワークフロー新設
- [ ] `.github/workflows/e2e.yml`を新設。トリガー: `push`(`branches: [main]`)・`pull_request`・`workflow_dispatch`。ジョブ手順: `actions/checkout`→JDKセットアップ(`actions/setup-java`、プロジェクトのJavaバージョンに合わせる)→Node.jsセットアップ(`actions/setup-node`)→`./gradlew build`(demo/webconsole/cliのjarをビルド)→`e2e/`で`npm ci`→`npx playwright install --with-deps chromium`→`npm run test:e2e`(APIキー無し・有り両方のパスを実行)

## Step 10: ローカル動作確認
- [ ] `./gradlew build`(全モジュール)を実行し既存ビルドに影響が無いことを確認
- [ ] `cd e2e && npm install && npx playwright install --with-deps chromium`
- [ ] `npm run test:e2e:no-key`を実行し全シナリオ成功を確認
- [ ] `npm run test:e2e:with-key`を実行し全シナリオ成功を確認(APIキー設定時の自動ヘッダ付与を含む)
- [ ] 不具合があれば該当ファイルを修正し再実行(このステップ内でループ)

## Step 11: ドキュメント整備
- [ ] `aidlc-docs/construction/e2e/code/e2e-test-summary.md`を新設し、実装内容・実行方法・確認結果のサマリーを記載(markdown、既存の`*-summary.md`と同じ形式)
- [ ] ルート`README.md`(存在する場合)にE2Eテストの実行方法への言及が必要か確認し、必要なら追記(既存READMEの構成次第で本ステップの要否を判断)

## Step 12: 最終確認・完了報告
- [ ] `git status`で新規・変更ファイル一覧を確認し、意図しないファイル(誤って生成された`node_modules`のコミット等)が無いことを確認
- [ ] 本Code Generation Planの全チェックボックスが完了していることを確認し、完了報告(標準2択メッセージ)を提示する
