# Unit 2(demo) - Code Generation Summary

## 対応FR/NFR

FR6(デモアプリ新設)、FR7(コメント充実、demo分)、NFR5(JSpecifyベースのNullability規約統一、demo分)。

## 新規作成モジュール: demo(`cherry-testtool-demo`)

`lib`と同階層(リポジトリ直下)に新設。`lib`への依存はGradle複合ビルド(`includeBuild('../lib')`)で解決する独立したGradleプロジェクト。

### 変更ファイル一覧(新規作成)

- `demo/settings.gradle`、`demo/build.gradle`、`demo/.gitignore`、Gradle Wrapper一式
- `demo/src/main/java/cherry/testtool/demo/DemoApplication.java` — エントリポイント
- `demo/src/main/java/cherry/testtool/demo/SampleService.java` — サンプル業務サービス(`lib/src/test`の`ToolTester`をベースに、レビュー時のユーザー指示で`SampleService`へ改名)
- `demo/src/main/java/cherry/testtool/demo/SampleController.java`(レビュー時にユーザー指示で追加) — `SampleService`の`toBeStubbed`系メソッドを通常のREST API(`/api/sample/**`)として公開
- `demo/src/main/java/cherry/testtool/demo/aspect/StubAspect.java` — `lib/src/test/aspect`の`StubAspect`をベースに、pointcutを`cherry.testtool.demo.SampleService`へ調整
- `demo/src/main/java/cherry/testtool/demo/aspect/TraceAspect.java`(レビュー時にユーザー指示で追加) — `lib/src/test/aspect`の`TraceAspect`をベースに、pointcutを`cherry.testtool.demo`配下(`aspect`パッケージ自身は除外)へ調整
- `demo/src/main/java/cherry/testtool/demo/package-info.java`、`demo/src/main/java/cherry/testtool/demo/aspect/package-info.java` — `@NullMarked`
- `demo/src/main/resources/application.yml` — `server.port: 8080`(FR6.1)、ログ設定
- `demo/src/test/java/cherry/testtool/demo/DemoApplicationTests.java` — コンテキストロード確認
- `demo/src/test/java/cherry/testtool/demo/SampleControllerTest.java`(レビュー時にユーザー指示で追加) — スタブ登録前後の挙動差をHTTP経由で検証
- `demo/README.md` — スタブ組み込み方の手引書(Application Design Q1「組み込み方の手引書と共にリファレンス実装として提供する」に対応)

### lib側への影響

- `lib/build.gradle`にリンク`group`/`version`を追加(複合ビルド解決に必要)
- 詳細は[lib-unit-summary.md](../../lib/code/lib-unit-summary.md)の「Unit 2(demo)着手時に発覚した追加修正」を参照。以下2件の不具合をlib側で修正した。
  1. `io.spring.dependency-management`のBOM/バージョン管理は複合ビルドを跨いで伝播しないため、該当依存にバージョンを直接明記
  2. `lib`の自動構成登録がSpring Boot 4.1.0で機能しない旧形式`spring.factories`のままだったため、新形式`AutoConfiguration.imports`へ置換

### 「移管」の実際の扱い

`requirements.md`のFR6.2は`ToolTester`/`StubAspect`等の「移管」を許容しているが、`lib`自身の6テストクラスがこれらに依存しているため、`lib/src/test`のフィクスチャは維持したまま、`demo`には独立したコピー(改名・パッケージ変更・JSpecify対応込み)を新規作成する方式を取った。

## ビルド検証

`./gradlew clean test`を実行し、`demo`の全2テスト(`DemoApplicationTests`、`SampleControllerTest`)が成功することを確認済み。`lib`の複合ビルド経由の自動構成解決(`InvokerService`・`StubResolver`等のBean、`TesttoolController`のREST API)が正しく機能していることも合わせて確認した。

## build.gradleのKotlin DSL化(レビュー時にユーザー指示)

`demo/build.gradle`・`demo/settings.gradle`をそれぞれ`demo/build.gradle.kts`・`demo/settings.gradle.kts`へ変換した(Groovy版は削除、`lib`と同時に対応)。変換後、`./gradlew clean test`で全2テスト成功、警告無しを確認済み。

## マルチプロジェクト化の検討経緯

lib複合ビルド解決の不具合が続けて見つかったことを受け、`lib`と`demo`をGradleマルチプロジェクト化(単一`settings.gradle`配下への統合)するべきかユーザーと協議した。両不具合の修正後は複合ビルドのままで問題なく動作することを確認できたため、`requirements.md`のFR6.4(`demo`独自の`rootProject.name`)を維持する形で、**マルチプロジェクト化は見送り**とした。

**(2026-08-09追記)**: その後、IntelliJ IDEAで`lib`の`includeBuild`起因のビルドスクリプト解析競合が判明し、判断を改めてマルチプロジェクト化を実施した。`demo`の`lib`依存は`includeBuild`経由の座標参照から`implementation(project(":lib"))`へ変更している。詳細は[lib-unit-summary.md](../../lib/code/lib-unit-summary.md)「Gradleマルチプロジェクト化」を参照。

## スタブサンプル(stub-samples/)の追加(2026-08-09、レビュー時にユーザー指示)

「デモに設定するスタブのサンプルをどこかに置いておきたい。`client/webconsole`と`client/cli`の両方をデモできるのが良い」という要望を受け、`demo/stub-samples/cherry.testtool.demo.SampleService/`配下に3件のスタブスクリプトサンプル(`toBeStubbed1.0.js`(BigDecimal版、`12345.67`)、`toBeStubbed1.1.js`(Integer版、`9999`)、`toBeStubbed2.js`(LocalDateTime、`2030-01-01T12:00:00`)を新規作成した。`client/cli`の走査規約(`{className}/{methodName}[.methodIndex].js`)に沿った構造とすることで、`client/cli`へそのまま渡せると同時に、`client/webconsole`の`/stubconfig`画面へ貼り付けるスクリプト片としても流用できる、両クライアントで共用可能な単一の置き場所とした。

`demo`を実際に起動し、`client/cli stubconfig register/show/clear`で3件とも意図通りの値が返ること(register後の`curl`結果が期待値と一致、clear後に元の値へ戻る)を確認済み。`demo/README.md`に配置構造・`client/cli`/`client/webconsole`双方での使い方を追記した。

## 呼出しサンプル(invoke-samples/)の追加(2026-08-09、レビュー時にユーザー指示)

「invokerのサンプルも欲しい」との要望を受け、`stub-samples/`と同じ考え方で`demo/invoke-samples/cherry.testtool.demo.SampleService/`配下に、`SampleService`の`toBeInvoked0`〜`toBeInvoked6`(オーバーロードの`toBeInvoked6`は`.0`/`.1`の2ファイル)、計8件の引数生成スクリプトサンプルを新規作成した。`toBeInvoked3`以降(`LocalDate`/`LocalTime`、ネストしたrecord`Dto1`/`Dto2`)は、GraalVM JSの`Java.type(...)`で対象の型を直接参照しその場でインスタンスを生成する方式とした(`Dto1`/`Dto2`はネストしたrecordのためバイナリ名`cherry.testtool.demo.SampleService$Dto1`等で参照)。

`demo`を実際に起動し、`client/cli invoke demo/invoke-samples`で8件全てが意図通りの結果(`toBeInvoked1`→`7`、`toBeInvoked4`→`val1:8, val2:10`、`toBeInvoked6.0`(int,int)→`-7`、`toBeInvoked6.1`(long,long)→`7`等)を返すことを確認済み。`demo/README.md`に配置構造・両クライアントでの使い方を追記した。

## StubAutoLoadRunnerの追加(2026-08-09、レビュー時にユーザー指示)

「デモにStubConfigLoaderを追加できる？設定でON/OFFできるように、読み込み先を設定できるように。」との要望を受けて調査したところ、`lib`には`StubConfigLoader`(ディレクトリ配下のスクリプトを一括読込みし`StubRepository`へ登録するクラス)が既に存在し`TesttoolConfiguration`でBean登録もされていたが、`load()`を呼び出す側が無く未使用のままだったことが判明した。

`demo/src/main/java/cherry/testtool/demo/StubAutoLoadRunner.java`(`ApplicationRunner`)を新規作成し、起動時に`StubConfigLoader.load(...)`を呼び出す構成とした。

- `@ConditionalOnProperty(prefix = "demo.stub-loader", name = "enabled", havingValue = "true")`でON/OFFを制御(既定は無効。既存の`SampleControllerTest`が「スタブ未登録の状態」を前提としているため、デフォルト動作を変えないよう無効を既定とした)
- 読込み先ディレクトリ(`demo.stub-loader.directory`、既定`stub-samples`)・対象拡張子(`demo.stub-loader.ext`、既定`.js`)を`@Value`で設定可能にした
- `demo/src/main/resources/application.yml`に既定値(無効)を明記
- `demo/src/test/java/cherry/testtool/demo/StubAutoLoadRunnerTest.java`を新規作成。`@TestPropertySource(properties = "demo.stub-loader.enabled=true")`で有効化し、既存の`stub-samples/cherry.testtool.demo.SampleService/toBeStubbed1.1.js`が起動時に自動登録され、`/api/sample/stubbed1/int`が明示登録無しでスタブ値(`9999`)を返すことを検証
- `demo/README.md`の「スタブのサンプル」節に「起動時の自動読込み(StubAutoLoadRunner)」を追記

`./gradlew clean test`で全52テスト(demo 3件[新規`StubAutoLoadRunnerTest`含む]、lib 31件、client:webconsole 3件、client:cli 15件)成功を確認済み。
