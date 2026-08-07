# Code Generation Plan - webconsole(Unit 3)

## Unit Context

- **対応FR**: FR1(gateway設定是正)、FR2.1-2.9(gateway+spa統合)、FR8.4(SPA側のAPI呼出し先追随修正)、FR7(コメント充実、webconsole分)、NFR5(JSpecify、webconsole分)
- **依存Unit**: ビルド時はUnit 1(lib)・Unit 2(demo)に非依存。結合確認(手動)にはUnit 2(demo、既定ポート8080)の起動が必要
- **依存される側**: Unit 4(cli)には依存しない
- **ワークスペースルート**: `~/Documents/project/git/cherry-testtool`(brownfield、既存`client/gateway`・`client/spa`を統合・置換して`client/webconsole`を新設)

## 主要な設計判断のおさらい(Application Design/Requirements Analysisで決定済み)

- Spring MVC(Servlet)+ Spring Cloud Gateway Servlet版(`spring-cloud-starter-gateway-server-webmvc`)
- プロキシ対象は`/testtool/**`のみ(`/**`ではない)。ルート定義はJava Functional Route
- SPAフォールバック(`SpaFallbackResourceResolver`)により、`/testtool/**`にも静的ファイルにも一致しないパスは`index.html`を返す
- CORSは設定しない(開発時はViteの`server.proxy`で同一オリジン化)
- `client/webconsole`配下にJavaプロジェクト本体と、旧`client/spa`を`frontend/`として同居させる。ビルド時にフロントエンドをビルドし静的リソースへ組み込む
- 待受ポート9090、`rootProject.name = cherry-testtool-webconsole`
- ビルドスクリプトはKotlin DSL(`lib`・`demo`と同じ方針、レビューでの指示を踏襲)

## Steps

### Step 1: Project Structure Setup
- [ ] Step 1.1: `client/webconsole/settings.gradle.kts`(`rootProject.name = "cherry-testtool-webconsole"`)、`client/webconsole/build.gradle.kts`(Spring Bootプラグイン、Java 25、`spring-cloud-starter-gateway-server-webmvc`、`spring-boot-starter-web`)を新規作成する。Gradle Wrapperは`lib`からコピーする
- [ ] Step 1.2: `client/webconsole/build.gradle.kts`に、フロントエンド(`frontend/`)を`npm install`→`npm run build`し、成果物(`frontend/dist`)を`processResources`で`static/`へ組み込むタスクを追加する(`Exec`タスク2つ+`processResources`への依存追加)
- [ ] Step 1.3: `git mv`で`client/spa`の内容を`client/webconsole/frontend/`へ移動する(`package.json`、`package-lock.json`、`vite.config.ts`、`tsconfig*.json`、`eslint.config.js`、`index.html`、`public/`、`src/`、`LICENSE`、`.gitignore`)。`.env`(`VITE_TESTTOOL_ROOT`)はFR2.9の相対パス化に伴い削除する
- [ ] Step 1.4: `client/webconsole/frontend/vite.config.ts`に`server.proxy`(`/testtool` → `http://localhost:9090`)を追加する

### Step 2: Business Logic Generation(バックエンド)
- [ ] Step 2.1: `client/webconsole/src/main/java/cherry/testtool/webconsole/WebconsoleApplication.java`を新規作成する(`@SpringBootApplication`)
- [ ] Step 2.2: `client/webconsole/src/main/java/cherry/testtool/webconsole/GatewayRouteConfig.java`を新規作成する。Spring Cloud Gateway Server MVCの`RouterFunction`による`/testtool/**` → `backend.uri`へのプロキシルート定義。既存`client/gateway`のセキュリティヘッダ付与・レスポンスヘッダ重複排除(`Vary`)相当のフィルタを設定する(**実装時に`spring-cloud-starter-gateway-server-webmvc`の実際のAPI(フィルタのクラス/メソッド名)をjarの中身で確認する**、Unit 1・Unit 2で判明したSpring Boot 4.x系APIの破壊的変更・新モジュール分離の前例に倣う)
- [ ] Step 2.3: `client/webconsole/src/main/java/cherry/testtool/webconsole/SpaFallbackResourceResolver.java`を新規作成する。`PathResourceResolver`を拡張し、リクエストされた静的リソースが存在しなければ`index.html`を返す
- [ ] Step 2.4: `client/webconsole/src/main/java/cherry/testtool/webconsole/WebConfig.java`を新規作成する。`WebMvcConfigurer`実装で`SpaFallbackResourceResolver`を静的リソースハンドラへ登録する
- [ ] Step 2.5: `client/webconsole/src/main/java/cherry/testtool/webconsole/package-info.java`を新規作成し`@NullMarked`を付与する
- [ ] Step 2.6: `client/webconsole/src/main/resources/application.yml`を新規作成する(`server.port: 9090`、`backend.protocol`/`backend.host`/`backend.port`/`backend.uri`(既定値はUnit2デモアプリの`8080`)、ログ設定)

### Step 3: フロントエンド調整(FR8.4対応)
- [ ] Step 3.1: `frontend/src/common.ts`を修正し、絶対URL解決(`VITE_TESTTOOL_ROOT`)から相対パス(`/testtool`固定)ベースへ簡素化する
- [ ] Step 3.2: `frontend/src/invoker/api.ts`・`frontend/src/stubconfig/api.ts`の`resolveBeanName`・`resolveMethod`の呼出し先を、廃止された`/invoker/bean`・`/invoker/method`・`/stubconfig/bean`・`/stubconfig/method`から、統合後の`/resolve/bean`・`/resolve/method`(lib Unit1のFR8対応)へ更新する

### Step 4: Business Logic Unit Testing
- [ ] Step 4.1: `client/webconsole/src/test/java/cherry/testtool/webconsole/WebconsoleApplicationTests.java`を新規作成する(`@SpringBootTest`によるコンテキストロード確認)
- [ ] Step 4.2: `client/webconsole/src/test/java/cherry/testtool/webconsole/SpaFallbackResourceResolverTest.java`を新規作成する。静的ファイルが存在する場合はそのまま返し、存在しない場合は`index.html`にフォールバックすることを検証する
- [ ] Step 4.3: 手動結合確認手順を`README.md`に記載する(Unit 2のデモアプリを起動した状態で、`client/webconsole`経由のプロキシ動作・SPA配信を確認する手順。自動テストでのプロキシ結合確認は複雑さに対して得られる保証が薄いため見送り、手動確認手順の整備で代替する)

### Step 5: Documentation Generation
- [ ] Step 5.1: `client/webconsole/README.md`を新規作成する(起動方法、開発時のVite dev server起動方法とproxy設定の説明、手動結合確認手順)
- [ ] Step 5.2: 旧`client/gateway`・`client/spa`ディレクトリを削除する(FR2.5)
- [ ] Step 5.3: ルート`README.md`のアーキテクチャ図・起動手順・ポート番号(`client/gateway`→`client/webconsole`、`8070`→`9090`)を更新する
- [ ] Step 5.4: `aidlc-docs/construction/webconsole/code/webconsole-unit-summary.md`を作成し、Unit 3全体の変更内容をまとめる

## Deployment Artifacts
`client/webconsole`はSpring Bootアプリケーションのため、`./gradlew bootJar`で実行可能jarを生成できる(フロントエンドのビルド成果物を静的リソースとして内包する)。
