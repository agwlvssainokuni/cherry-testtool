# Code Generation Plan - demo(Unit 2)

## Unit Context

- **対応FR/NFR**: FR6(デモアプリ新設)、FR7(コメント充実、demo分)、NFR5(JSpecify、demo分)
- **依存Unit**: Unit 1(lib)にコンパイル依存(完了・承認済み)
- **依存される側**: Unit 3(webconsole)・Unit 4(cli)の結合確認(手動)対象
- **ワークスペースルート**: `~/Documents/project/git/cherry-testtool`(greenfield的な新設、ディレクトリは`demo/`をリポジトリ直下・`lib`と同階層に新設)

## 重要な設計判断: 「移管」の解釈

`requirements.md`のFR6.2は「`lib/src/test`の検証用フィクスチャ(`ToolTester`/`StubAspect`等)をデモアプリへ移管してよい」としているが、**`lib`自身の6テストクラス(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubRepositoryTest`、`StubAspectTest`、および`StubAspect`自体)が`ToolTester`に依存している**ため、`lib/src/test`から単純に削除する「移動」はできない(`lib`のテストが壊れる)。

そのため、**`lib/src/test`のフィクスチャはそのまま維持**し、**`demo`には独立したコピー(パッケージ`cherry.testtool.demo`、JSpecify対応、Javadoc充実)を新規作成**する方針とする。両者はコード的には類似するが、`lib`側はテストスコープの検証用、`demo`側は実行可能なリファレンスアプリの本体、という異なる役割を持つ。

## 追加設計判断: SampleService + SampleController(レビュー時にユーザー指示で追加)

`ToolTester`は現状`/testtool/invoker/invoke`(リフレクション経由)でしか外部から呼び出せず、実アプリらしい「通常のREST API」経由での動作確認ができない。デモとしての説得力を高めるため、以下を追加する。

- クラス名を`ToolTester`から`SampleService`へ変更する(「テストツール自体を試すもの」ではなく「実アプリが持つであろうサンプル業務サービス」という位置づけを明確にするため)
- 新設する`SampleController`(通常の`@RestController`、`/testtool/**`とは独立したパス)は、`toBeStubbed`系メソッドのみを公開する(`toBeInvoked`系はオーバーロード等リフレクション呼出しの内部テスト用シグネチャのためREST化に不向き)
- `toBeStubbed1`はオーバーロード(`Integer`版/`BigDecimal`版)を持つため、URLパスを分けて公開する

## Steps

### Step 1: Project Structure Setup
- [x] Step 1.1: `demo/settings.gradle`を新規作成する。`rootProject.name = "cherry-testtool-demo"`(FR6.4)、`includeBuild('../lib')`によりGradle複合ビルドで`lib`を参照する
- [x] Step 1.2: `demo/build.gradle`を新規作成する。Spring Bootプラグイン(4.1.0)、Java 25 toolchain、依存として`cherry.testtool:cherry-testtool`(複合ビルド経由で`lib`を解決)、`spring-boot-starter-web`(`TesttoolController`の有効化に必要)、`spring-boot-starter-aspectj`(`StubAspect`のAOP自動プロキシに必要)を設定する
- [x] Step 1.3: `lib/build.gradle`を修正し`group = 'cherry.testtool'`を追加する(複合ビルドでの依存解決に必要な最小限の変更)

### Step 2: Business Logic Generation
- [x] Step 2.1: `demo/src/main/java/cherry/testtool/demo/DemoApplication.java`を新規作成する(`@SpringBootApplication`、`main`メソッド)
- [x] Step 2.2: `demo/src/main/java/cherry/testtool/demo/SampleService.java`を新規作成する。`lib/src/test`の`ToolTester`の内容をベースに、クラス名を`SampleService`、パッケージを`cherry.testtool.demo`とし、JSpecify対応(`@Nullable`)・Javadocを付与する
- [x] Step 2.3: `demo/src/main/java/cherry/testtool/demo/aspect/StubAspect.java`を新規作成する。`lib/src/test/aspect`の`StubAspect`の内容をベースに、パッケージを`cherry.testtool.demo.aspect`、pointcutを`execution(* cherry.testtool.demo.SampleService.*(..))`へ更新し、Javadocを付与する
- [x] Step 2.4: `demo/src/main/java/cherry/testtool/demo/aspect/TraceAspect.java`を新規作成する(レビュー時にユーザー指示で追加)。`lib/src/test/aspect`の`TraceAspect`の内容をベースに、パッケージを`cherry.testtool.demo.aspect`、pointcutを`execution(* cherry.testtool.demo..*.*(..)) && !within(cherry.testtool.demo.aspect..*)`へ更新する(`lib`版と同じ考え方でdemo自身のパッケージに絞り込み、`aspect`パッケージ自身は対象外)
- [x] Step 2.5: `demo/src/main/java/cherry/testtool/demo/SampleController.java`を新規作成する。通常の`@RestController`として、`SampleService`の`toBeStubbed`系メソッドを公開する。`toBeStubbed1`はオーバーロードのため`GET /api/sample/stubbed1/int`(Integer版)と`GET /api/sample/stubbed1/decimal`(BigDecimal版)の2エンドポイントに分け、`toBeStubbed2`は`GET /api/sample/stubbed2`とする
- [x] Step 2.6: `demo/src/main/java/cherry/testtool/demo/package-info.java`、`demo/src/main/java/cherry/testtool/demo/aspect/package-info.java`を新規作成し、`@NullMarked`を付与する
- [x] Step 2.7: `demo/src/main/resources/application.yml`を新規作成する(`server.port: 8080`(FR6.1)、ログ設定。`TraceAspect`の`trace.*`設定は`@Value`にデフォルト値を埋め込み済みのため追加設定は不要)

### Step 3: Business Logic Unit Testing
- [x] Step 3.1: `demo/src/test/java/cherry/testtool/demo/DemoApplicationTests.java`を新規作成する。`@SpringBootTest`によるコンテキストロード確認(lib複合ビルド経由の自動構成が正しく解決されることの検証)
- [x] Step 3.2: `demo/src/test/java/cherry/testtool/demo/SampleControllerTest.java`を新規作成する。`SampleController`のエンドポイントを、スタブ未登録時(実際の計算結果)→`StubRepository`へスタブ登録後(スタブされた結果)→解除後(元の結果)、という順で呼び出し、`StubAspect`による介入が実際にHTTP経由で観測できることを検証する(デモの中核的価値の検証)

### Step 4: Documentation Generation
- [x] Step 4.1: `demo/README.md`を新規作成する。`StubAspect`によるスタブ組み込み方の手引書(Application Design Q1で決定した「組み込み方の手引書と共にリファレンス実装として提供する」に対応)。他プロジェクトで`lib`を組み込む際の`StubAspect`導入手順・pointcutのカスタマイズ方法、および`SampleController`経由での動作確認手順を説明する
- [x] Step 4.2: `aidlc-docs/construction/demo/code/demo-unit-summary.md`を作成し、Unit 2全体の変更内容をまとめる

## Deployment Artifacts
`demo`はSpring Bootアプリケーションのため、`./gradlew bootJar`で実行可能jarを生成できる(Spring Bootプラグインの既定機能、追加タスク定義は不要)。
