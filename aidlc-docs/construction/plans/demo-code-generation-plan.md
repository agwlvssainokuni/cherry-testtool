# Code Generation Plan - demo(Unit 2)

## Unit Context

- **対応FR/NFR**: FR6(デモアプリ新設)、FR7(コメント充実、demo分)、NFR5(JSpecify、demo分)
- **依存Unit**: Unit 1(lib)にコンパイル依存(完了・承認済み)
- **依存される側**: Unit 3(webconsole)・Unit 4(cli)の結合確認(手動)対象
- **ワークスペースルート**: `~/Documents/project/git/cherry-testtool`(greenfield的な新設、ディレクトリは`demo/`をリポジトリ直下・`lib`と同階層に新設)

## 重要な設計判断: 「移管」の解釈

`requirements.md`のFR6.2は「`lib/src/test`の検証用フィクスチャ(`ToolTester`/`StubAspect`等)をデモアプリへ移管してよい」としているが、**`lib`自身の6テストクラス(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubRepositoryTest`、`StubAspectTest`、および`StubAspect`自体)が`ToolTester`に依存している**ため、`lib/src/test`から単純に削除する「移動」はできない(`lib`のテストが壊れる)。

そのため、**`lib/src/test`のフィクスチャはそのまま維持**し、**`demo`には独立したコピー(パッケージ`cherry.testtool.demo`、JSpecify対応、Javadoc充実)を新規作成**する方針とする。両者はコード的には類似するが、`lib`側はテストスコープの検証用、`demo`側は実行可能なリファレンスアプリの本体、という異なる役割を持つ。

## Steps

### Step 1: Project Structure Setup
- [ ] Step 1.1: `demo/settings.gradle`を新規作成する。`rootProject.name = "cherry-testtool-demo"`(FR6.4)、`includeBuild('../lib')`によりGradle複合ビルドで`lib`を参照する
- [ ] Step 1.2: `demo/build.gradle`を新規作成する。Spring Bootプラグイン(4.1.0)、Java 25 toolchain、依存として`cherry.testtool:cherry-testtool`(複合ビルド経由で`lib`を解決)、`spring-boot-starter-web`(`TesttoolController`の有効化に必要)、`spring-boot-starter-aspectj`(`StubAspect`のAOP自動プロキシに必要)を設定する
- [ ] Step 1.3: `lib/build.gradle`を修正し`group = 'cherry.testtool'`を追加する(複合ビルドでの依存解決に必要な最小限の変更)

### Step 2: Business Logic Generation
- [ ] Step 2.1: `demo/src/main/java/cherry/testtool/demo/DemoApplication.java`を新規作成する(`@SpringBootApplication`、`main`メソッド)
- [ ] Step 2.2: `demo/src/main/java/cherry/testtool/demo/ToolTester.java`を新規作成する。`lib/src/test`の`ToolTester`の内容をベースに、パッケージを`cherry.testtool.demo`とし、JSpecify対応(`@Nullable`)・Javadocを付与する
- [ ] Step 2.3: `demo/src/main/java/cherry/testtool/demo/aspect/StubAspect.java`を新規作成する。`lib/src/test/aspect`の`StubAspect`の内容をベースに、パッケージを`cherry.testtool.demo.aspect`、pointcutを`execution(* cherry.testtool.demo.ToolTester.*(..))`へ更新し、Javadocを付与する
- [ ] Step 2.4: `demo/src/main/java/cherry/testtool/demo/package-info.java`、`demo/src/main/java/cherry/testtool/demo/aspect/package-info.java`を新規作成し、`@NullMarked`を付与する
- [ ] Step 2.5: `demo/src/main/resources/application.yml`を新規作成する(`server.port: 8080`(FR6.1)、ログ設定)

### Step 3: Business Logic Unit Testing
- [ ] Step 3.1: `demo/src/test/java/cherry/testtool/demo/DemoApplicationTests.java`を新規作成する。`@SpringBootTest`によるコンテキストロード確認(lib複合ビルド経由の自動構成が正しく解決されることの検証)

### Step 4: Documentation Generation
- [ ] Step 4.1: `demo/README.md`を新規作成する。`StubAspect`によるスタブ組み込み方の手引書(Application Design Q1で決定した「組み込み方の手引書と共にリファレンス実装として提供する」に対応)。他プロジェクトで`lib`を組み込む際の`StubAspect`導入手順・pointcutのカスタマイズ方法を説明する
- [ ] Step 4.2: `aidlc-docs/construction/demo/code/demo-unit-summary.md`を作成し、Unit 2全体の変更内容をまとめる

## Deployment Artifacts
`demo`はSpring Bootアプリケーションのため、`./gradlew bootJar`で実行可能jarを生成できる(Spring Bootプラグインの既定機能、追加タスク定義は不要)。
