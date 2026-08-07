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

## マルチプロジェクト化の検討経緯

lib複合ビルド解決の不具合が続けて見つかったことを受け、`lib`と`demo`をGradleマルチプロジェクト化(単一`settings.gradle`配下への統合)するべきかユーザーと協議した。両不具合の修正後は複合ビルドのままで問題なく動作することを確認できたため、`requirements.md`のFR6.4(`demo`独自の`rootProject.name`)を維持する形で、**マルチプロジェクト化は見送り**とした。
