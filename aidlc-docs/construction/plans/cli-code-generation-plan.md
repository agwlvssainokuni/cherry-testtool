# Code Generation Plan - cli(Unit 4)

## Unit Context

- **対応FR**: FR5(`client/cli`のSpring Bootアプリ化)、FR7(コメント充実、cli分)、NFR5(JSpecify、cli分)
- **依存Unit**: ビルド時はUnit 1-3に非依存。結合確認(手動)にはUnit 2(demo、既定ポート8080)の起動が必要
- **Functional Design**: `aidlc-docs/construction/cli/functional-design/`(business-logic-model.md、business-rules.md、domain-entities.md)で確定済み

## 主要な設計判断のおさらい

- Picocli(`picocli-spring-boot-starter`)採用、`RootCommand`(`--url`/`--basic-auth`/`--header`をINHERITスコープで共通定義)配下に`invoke`・`stubconfig`(`register`/`clear`/`show`のサブサブコマンド)を配置
- `CliApplication`が`CommandLineRunner`+`ExitCodeGenerator`を実装。終了コードは0(全成功)/1(1件でも失敗)
- `TesttoolApiClient`(`@HttpExchange`)はSpring管理のprototype Bean。`ApiClientFactory.create(baseUri)`が`applicationContext.getBean(TesttoolApiClient.class, baseUri)`で取得
- ファイル単位の失敗は継続、最後にまとめて終了コードへ反映(BR4)
- Webサーバは起動しない(`spring.main.web-application-type=none`)。`RestClient`/`HttpServiceProxyFactory`は`spring-web`モジュールのみで足り、`spring-boot-starter-web`(Tomcat等)は不要
- `rootProject.name = cherry-testtool-cli`、ビルドスクリプトはKotlin DSL(lib/demo/webconsoleと同方針)
- 依存バージョンは実装前に確認する: `picocli-spring-boot-starter`最新版(Maven Central照会で`4.7.7`を確認済み)

## Steps

### Step 1: Project Structure Setup
- [x] Step 1.1: `client/cli/settings.gradle.kts`(`rootProject.name = "cherry-testtool-cli"`)、`client/cli/build.gradle.kts`(Spring Bootプラグイン、Java 25、`picocli-spring-boot-starter:4.7.7`、`spring-boot-starter`、`spring-web`、`jspecify`)を新規作成する。Gradle Wrapperは`lib`からコピーする
- [x] Step 1.2: 依存関係を解決し、`picocli-spring-boot-starter`の実際のAPI(`CommandLine.IFactory`実装Bean名・自動構成クラス)をjarの中身で確認する(Unit 1-3で判明したSpring Boot 4.x系API破壊的変更の前例に倣う)。`javap`で確認した結果、`PicocliAutoConfiguration`は`@Primary @Bean CommandLine.IFactory picocliSpringFactory(ApplicationContext)`を提供しており、`AutoConfiguration.imports`も新形式で問題なし(Unit1/3のような破壊的変更は無し)

### Step 2: ドメイン・共通コンポーネント
- [x] Step 2.1: `cherry.testtool.cli.ScriptFileEntry`(record)、`cherry.testtool.cli.FileProcessingResult`(record)、`cherry.testtool.cli.BatchResult`(record、`failureCount()`)を新規作成する(domain-entities.md準拠)。`ConnectionOptions`(record)も併せて作成
- [x] Step 2.2: `cherry.testtool.cli.ScriptFileScanner`を新規作成する(BR1・BR2: 再帰走査、パス文字列順ソート、className/methodName/methodIndex抽出。methodIndexが数値でない場合は`-1`のsentinel値とし例外は投げない)
- [x] Step 2.3: `cherry.testtool.cli.TesttoolApiClient`(`@HttpExchange`インタフェース、invoke/putStub/getStub)、`cherry.testtool.cli.ApiClientConfig`(`@Bean @Scope("prototype")`)、`cherry.testtool.cli.ApiClientFactory`、`cherry.testtool.cli.RequestHeaderBuilder`(BR6: BASIC認証・追加ヘッダ組み立て)を新規作成する

### Step 3: サービス層
- [x] Step 3.1: `cherry.testtool.cli.InvokeService`(`invokeAll`、BR4準拠の失敗時継続、BR5準拠の標準出力)を新規作成する
- [x] Step 3.2: `cherry.testtool.cli.StubConfigService`(`registerAll`/`clearAll`/`showAll`、BR7準拠のAPIマッピング)を新規作成する
- [x] Step 3.3: ヘッダ組み立て共通処理(BR6: `--basic-auth`のBase64エンコード、`--header`の`Name: Value`解析)を、`InvokeService`/`StubConfigService`共有のユーティリティとして実装する(Step2で作成済みの`RequestHeaderBuilder`を利用)

### Step 4: Picocliコマンド層・エントリポイント
- [x] Step 4.1: `cherry.testtool.cli.RootCommand`(共通オプション`--url`/`--basic-auth`/`--header`、`scope = ScopeType.INHERIT`、`subcommands = {InvokeCommand.class, StubConfigCommand.class}`)を新規作成する
- [x] Step 4.2: `cherry.testtool.cli.InvokeCommand`(`@Command("invoke")`、位置引数ディレクトリ群、`InvokeService`へ委譲)を新規作成する
- [x] Step 4.3: `cherry.testtool.cli.StubConfigCommand`(ディスパッチ専用、`subcommands`に`register`/`clear`/`show`)と、3つの葉サブコマンド(`StubConfigRegisterCommand`/`StubConfigClearCommand`/`StubConfigShowCommand`)を新規作成する
- [x] Step 4.4: `cherry.testtool.cli.CliApplication`(`@SpringBootApplication`、`CommandLineRunner`+`ExitCodeGenerator`実装、`CommandLine(rootCommand, picocliSpringFactory).execute(args)`)を新規作成する
- [x] Step 4.5: `cherry.testtool.cli.package-info.java`(`@NullMarked`)、`src/main/resources/application.yml`(`spring.main.web-application-type: none`、`spring.main.banner-mode: off`、ログ設定)を新規作成する
- [x] Step 4.6(計画外、実機確認で発覚): `demo`を起動しビルド済みjarで`invoke`/`stubconfig register,show,clear`を実行して手動結合確認したところ、`stubconfig show`が`Could not extract response: no suitable HttpMessageConverter found for response type List<String>`で失敗することを発見。`spring-boot-starter-web`に依存しない本モジュールではJSON用`HttpMessageConverter`が自動登録されないことが原因と判明し、`build.gradle.kts`へ`spring-boot-starter-json`を追加して解決。再ビルド後、`invoke`(成功/エラー応答とも)、`stubconfig register/show/clear`の全コマンドがdemo(8080)に対して実際に正しく動作することを確認

### Step 5: テスト
- [x] Step 5.1: `ScriptFileScannerTest`(BR1・BR2の抽出規則を検証、テスト用ディレクトリ構造をfixtureとして用意)を新規作成する。`RequestHeaderBuilderTest`(BR6)も併せて作成
- [x] Step 5.2: `InvokeService`・`StubConfigService`のテスト(`MockRestServiceServer`をバインドした実`TesttoolApiClient`実体+モック`ApiClientFactory`を用い、成功/失敗混在時の継続動作・`BatchResult`集計・出力を検証)を新規作成する。全15テスト成功を確認(`ScriptFileScannerTest`5件、`RequestHeaderBuilderTest`6件、`InvokeServiceTest`1件、`StubConfigServiceTest`3件)
- [x] Step 5.3: 手動結合確認手順を`README.md`に記載する(Unit 2のデモアプリを起動した状態で、`invoke`・`stubconfig register/show/clear`を実行し、旧`invoker.sh`/`stubconfig.sh`と同等の結果が得られることを確認する手順)。Step4完了時点で実機確認済みの手順をStep6.1のREADME作成時に転記する

### Step 6: ドキュメント・旧ファイル削除
- [x] Step 6.1: `client/cli/README.md`を新規作成する(ビルド・実行方法、コマンド一覧、旧シェルスクリプトからの移行ガイド)
- [x] Step 6.2: 旧`client/cli/invoker.sh`・`stubconfig.sh`を削除する(FR5.1)。既存の例示スクリプトディレクトリ(`invoker/`,`invoker2/`,`stubconfig/`配下の`.js`ファイル)は新CLIでも同じ規約で使えるため維持する(未追跡のためgit管理外のまま)
- [x] Step 6.3: ルート`README.md`のCLI起動方法・コマンド例を新Java CLIに合わせて更新する
- [x] Step 6.4: `aidlc-docs/construction/cli/code/cli-unit-summary.md`を作成し、Unit 4全体の変更内容をまとめる

## Deployment Artifacts
`client/cli`はSpring Bootアプリケーションのため、`./gradlew bootJar`で実行可能jarを生成できる(`java -jar cherry-testtool-cli.jar invoke {dirs}...`のように実行する)。
