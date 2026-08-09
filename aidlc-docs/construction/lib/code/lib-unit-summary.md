# Unit 1(lib) - Code Generation Summary

## 対応FR/NFR

FR3(意図的な例外処理へのコメント補足)、FR4(Interface/Impl分離の解消)、FR7(コメント充実、lib分)、FR8(Controller統合)、NFR5(JSpecifyベースのNullability規約統一、lib分)。

## 変更ファイル一覧

### 新規作成
- `lib/src/test/java/cherry/testtool/aspect/TraceAspect.java`(レビュー時にユーザー指示で追加。`reference/TraceAspect.java`を基に、パッケージ・pointcut・`@Value`のデフォルト値(`reference/application.properties`の値を`${prop:default}`形式で埋め込み)をこのプロジェクトへ適合させたもの。旧`appctx-trace.xml`のアノテーションベース版。当初`cherry.testtool`パッケージに作成後、`cherry.testtool.aspect`へ移動しpointcutも`execution(* cherry.testtool..*.*(..)) && !within(cherry.testtool.aspect..*)`へ絞り込み(自パッケージを対象外に))
- `lib/src/test/java/cherry/testtool/aspect/StubAspect.java`(レビュー時にユーザー指示で移動。旧`lib/src/test/java/cherry/testtool/StubAspect.java`を`aspect`パッケージへ移動、パッケージ宣言のみ変更)
- `lib/src/test/resources/application.yml`(レビュー時にユーザー指示で追加。旧`application.properties`のYAML変換版)
- `lib/src/test/java/cherry/testtool/stub/StubAspectTest.java`(レビュー時にユーザー指示で追加。廃止済み`StubInterceptorTest`と同等の検証を、正規のスタブ組み込み方式である`StubAspect`(`cherry.testtool.aspect`)を対象に実施。`stub`パッケージに配置(検証対象の`StubAspect`自体は`aspect`パッケージのまま))
- `lib/src/main/java/cherry/testtool/web/TesttoolController.java`
- `lib/src/test/java/cherry/testtool/web/TesttoolControllerTest.java`
- `lib/src/test/java/cherry/testtool/web/TestApplication.java`(レビュー時にユーザー指示で追加。`TestMain`廃止後、当初想定の`@WebMvcTest`+`@MockitoBean`方式へ切り替えるために新設した最小限のメイン設定クラス。`@WebMvcTest`と`@SpringBootApplication`は同一クラスに同時付与できないため分離)
- `lib/src/main/java/cherry/testtool/package-info.java`
- `lib/src/main/java/cherry/testtool/invoker/package-info.java`
- `lib/src/main/java/cherry/testtool/reflect/package-info.java`
- `lib/src/main/java/cherry/testtool/script/package-info.java`
- `lib/src/main/java/cherry/testtool/stub/package-info.java`
- `lib/src/main/java/cherry/testtool/util/package-info.java`
- `lib/src/main/java/cherry/testtool/web/package-info.java`

### 修正(上書き、Interface→具象クラス化)
- `lib/src/main/java/cherry/testtool/invoker/InvokerService.java`
- `lib/src/main/java/cherry/testtool/reflect/ReflectionResolver.java`
- `lib/src/main/java/cherry/testtool/script/ScriptProcessor.java`
- `lib/src/main/java/cherry/testtool/stub/StubRepository.java`
- `lib/src/main/java/cherry/testtool/stub/StubResolver.java`

### 修正(コメント・JSpecify対応、構造変更なし)
- `lib/src/main/java/cherry/testtool/TesttoolConfiguration.java`
- `lib/src/main/java/cherry/testtool/stub/StubConfigLoader.java`
- `lib/src/main/java/cherry/testtool/stub/StubConfig.java`
- `lib/src/main/java/cherry/testtool/stub/StubInvocation.java`
- `lib/src/main/java/cherry/testtool/util/ReflectionUtil.java`
- `lib/src/main/java/cherry/testtool/util/ToMapUtil.java`
- `lib/build.gradle`(`org.jspecify:jspecify`依存を追加)

### 修正(レビュー時にユーザー指示で追加、スタブ介入方式の見直し)
- `lib/src/main/java/cherry/testtool/stub/StubInterceptor.java` — `@Deprecated`を付与。推奨方式をアノテーションベースの`StubAspect`パターン(リファレンス実装はUnit 2のデモアプリ)へ変更したため後方互換目的で残置(詳細はrequirements.md「スタブ介入方式の見直し」参照)
- `lib/src/test/java/cherry/testtool/invoker/InvokerServiceTest.java`、`reflect/ReflectionResolverTest.java`、`script/ScriptProcessorTest.java`、`stub/StubRepositoryTest.java` — `@ImportResource`から削除済みの`appctx-stub.xml`参照を除去(`appctx-trace.xml`のみ残す)

### 修正(レビュー時にユーザー指示で追加、トレースアスペクトのアノテーション化)
- `lib/src/test/java/cherry/testtool/invoker/InvokerServiceTest.java`、`reflect/ReflectionResolverTest.java`、`script/ScriptProcessorTest.java`、`stub/StubRepositoryTest.java` — `@ImportResource`(`appctx-trace.xml`)を撤去し、`@SpringBootTest(classes = {...})`へ`TraceAspect.class`を追加

### 修正(レビュー時にユーザー指示で追加、ToolTester Interface統合)
- `lib/src/test/java/cherry/testtool/invoker/InvokerServiceTest.java`、`reflect/ReflectionResolverTest.java`、`script/ScriptProcessorTest.java`、`stub/StubRepositoryTest.java` — `ToolTesterImpl`参照を`ToolTester`へ更新。加えて、Bean名がクラス名から自動導出されるため`toolTesterImpl`→`toolTester`に変わり、ハードコードされていたBean名文字列リテラル(`InvokerServiceTest`5箇所、`ReflectionResolverTest`1箇所)も修正

### 削除
- `lib/src/main/java/cherry/testtool/invoker/InvokerServiceImpl.java`
- `lib/src/main/java/cherry/testtool/reflect/ReflectionResolverImpl.java`
- `lib/src/main/java/cherry/testtool/script/ScriptProcessorImpl.java`
- `lib/src/main/java/cherry/testtool/stub/StubRepositoryImpl.java`
- `lib/src/main/java/cherry/testtool/stub/StubResolverImpl.java`
- `lib/src/main/java/cherry/testtool/web/InvokerController.java`
- `lib/src/main/java/cherry/testtool/web/StubConfigController.java`
- `lib/src/test/java/cherry/testtool/TestMain.java`(レビュー時にユーザー指示で追加削除。デモアプリ新設(Unit 2)により手動起動用フィクスチャとしての役目を終えるため、Unit 2への移管ではなく廃止とした)
- `lib/src/test/java/cherry/testtool/stub/StubInterceptorTest.java`(レビュー時にユーザー指示で追加削除。`StubInterceptor`の`@Deprecated`化に伴い廃止)
- `lib/src/test/resources/spring/appctx-stub.xml`(レビュー時にユーザー指示で追加削除。`StubAspect`(アノテーションベース)と重複するXML AOP設定であったため廃止)
- `lib/src/test/resources/spring/appctx-trace.xml`(レビュー時にユーザー指示で追加削除。`TraceAspect`(アノテーションベース)へ置換したため廃止。空になった`lib/src/test/resources/spring/`ディレクトリも削除)
- `lib/src/test/resources/application.properties`(レビュー時にユーザー指示で追加削除。`application.yml`へ変換したため)
- `lib/src/test/java/cherry/testtool/TraceAspect.java`(レビュー時にユーザー指示で追加削除。`cherry.testtool.aspect`パッケージへ移動したため)
- `lib/src/test/java/cherry/testtool/StubAspect.java`(レビュー時にユーザー指示で追加削除。`cherry.testtool.aspect`パッケージへ移動したため)

### 修正(上書き、Interface→具象クラス化)
- `lib/src/test/java/cherry/testtool/ToolTester.java` — `ToolTester`(interface)と`ToolTesterImpl`を統合し、`Impl`無しの具象クラス`ToolTester`とした(libの他5組と同一方針)

### 削除(追加)
- `lib/src/test/java/cherry/testtool/ToolTesterImpl.java`

Unit 2(demo)への移管対象である`StubAspect`は本Unitでパッケージ移動(`cherry.testtool`→`cherry.testtool.aspect`)のみ実施し、移管自体はUnit 2で行う。

## 詳細サマリー

- [business-logic-summary.md](business-logic-summary.md) — Interface統合の詳細
- [api-layer-summary.md](api-layer-summary.md) — Controller統合の詳細

## ビルド検証(早期確認)

正式なBuild and Testフェーズ(全Unit完了後)とは別に、本Unit完了時点で`./gradlew compileJava compileTestJava test`を実行し、コンパイル成功・全テスト成功を都度確認済み。最終状態(`StubAspectTest`追加後)では31テスト成功(既存4クラス22件+`TesttoolControllerTest`7件+`StubAspectTest`2件)。`--tests`で個別実行しログ出力を確認し、`TraceAspect`が引き続きENTER/EXITトレースログを出力すること、`aspect`パッケージ自身(`TraceAspect`/`StubAspect`)がトレース対象から除外されていること、`StubAspect`が`StubInterceptor`(廃止前)と同等のスタブ介入(登録・解除・例外スロー)を実現できていることを確認済み。

この過程で判明した技術的事項に対応するため、計画外の追加修正を行った。

- `lib/build.gradle`: `spring-boot-starter-web`を`testImplementation`へ変更(テストコンパイル時にSpring MVCの型が必要なため)、`spring-boot-starter-webmvc-test`を追加(Spring Boot 4.xでの`@WebMvcTest`モジュール分離のため)
- `TesttoolControllerTest`: 当初`@WebMvcTest`+`@MockitoBean`で作成 → `TestMain`との衝突により`MockMvcBuilders.standaloneSetup`方式へ変更 → `TestMain`廃止後、`TestApplication`(最小限のメイン設定クラス)を新設した上で当初想定の`@WebMvcTest`+`@MockitoBean`方式へ最終的に復帰(詳細は[api-layer-summary.md](api-layer-summary.md)参照)

**Unit 3・Unit 4への申し送り**: Spring Boot 4.1.0では`@WebMvcTest`等のWebスライステストアノテーションのパッケージが`org.springframework.boot.webmvc.test.autoconfigure`へ変更されている(旧`org.springframework.boot.test.autoconfigure.web.servlet`)。Spring Bootのテストコードを新規作成する際は留意すること。

## Unit 2(demo)着手時に発覚した追加修正(2026-08-07)

Unit 2(demo)でlibを複合ビルド経由で実際に組み込んだところ、Unit 1完了時点では露呈していなかった2件の不具合が判明し、lib側を修正した。

1. **`io.spring.dependency-management`のBOM/バージョン管理が複合ビルド(`includeBuild`)を跨いで伝播しない**: `lib/build.gradle`の`dependencyManagement.dependencies`ブロックで管理していたバージョン(`commons-collections4`、`org.graalvm.js:js`/`js-scriptengine`、`jspecify`)が、`demo`側のビルドで解決できずエラーとなった。`dependencyManagement.dependencies`ブロックを廃止し、該当する`dependencies`宣言へバージョンを直接明記する形に変更した。
2. **`spring.factories`によるSpring Boot自動構成登録は、Spring Boot 4.1.0では完全に機能しない**: Spring Boot本体(`spring-boot-autoconfigure`)自身が新形式`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`のみを使用しており、旧形式`META-INF/spring.factories`の`EnableAutoConfiguration`キーはSpring Boot 4.xでは無視される。`lib`の`spring.factories`をこの新形式ファイルへ置き換えた。`lib`自身のテストは`@SpringBootTest(classes = {TesttoolConfiguration.class, ...})`で`TesttoolConfiguration`を明示指定していたため、この不具合はUnit 1完了時点では発覚しなかった(`demo`側の`@SpringBootTest`(classes明示無し、自動構成に依存)で初めて顕在化)。

両修正後、`lib`・`demo`とも全テスト成功を再確認した。この経緯を踏まえ、`lib`と`demo`をGradleマルチプロジェクト化するかどうかをユーザーと協議したが、両修正で複合ビルドのままでも問題なく動作することを確認できたため、**マルチプロジェクト化は見送り、複合ビルド(`includeBuild`)を維持**することで合意した。

## build.gradleのKotlin DSL化(2026-08-07、レビュー時にユーザー指示)

`lib/build.gradle`・`lib/settings.gradle`をそれぞれ`lib/build.gradle.kts`・`lib/settings.gradle.kts`へ変換した(Groovy版は削除)。

- `configurations { javaagent }`(Groovy)は、Kotlin DSLでは非推奨の`by configurations.creating`ではなく`configurations.create("javaagent")`を使用(Gradle 10非互換の警告を回避)
- その他は文字列リテラルのクォート方式変更・関数呼出し構文化が主で、依存関係やタスク設定の実質的な内容は変更していない
- 変換後、`./gradlew clean test`で全31テスト成功、警告無しを確認済み

## rootProject.nameの変更(2026-08-08、レビュー時にユーザー指示)

`lib/settings.gradle.kts`の`rootProject.name`を`cherry-testtool`から`cherry-testtool-core`へ変更した(`group`(`cherry.testtool`)は変更なし)。`demo/build.gradle.kts`の複合ビルド依存座標(`implementation("cherry.testtool:cherry-testtool:0.0.1-SNAPSHOT")`)も`cherry-testtool-core`へ追随修正。`requirements.md`のモジュール一覧表、`build-and-test/`配下のbuild-instructions.md・build-and-test-summary.mdも合わせて更新した。変更後、`lib`・`demo`とも`./gradlew clean test`で全テスト成功を確認し、`lib`の生成jarが`cherry-testtool-core-0.0.1-SNAPSHOT.jar`になることも確認済み。

## Gradleマルチプロジェクト化(2026-08-09、レビュー時にユーザー指示)

上記rootProject.name変更の翌日、ユーザーからIntelliJ IDEAで`lib`のみ`build.gradle.kts`・`settings.gradle.kts`にエラーが検知されるとの報告を受けた。原因調査のため、まず「Invalidate Caches / Restart」、次に「Gradleプロジェクトから全登録解除して再登録」を試したが解消せず、さらにユーザーが「`lib`+`demo`以外の3プロジェクトを登録した状態ではエラー無し、`demo`を追加登録すると`lib`のみエラー検知される」という再現性のある切り分けを行った。これにより、`demo`の`includeBuild("../lib")`(複合ビルド)によって`lib`が「単独リンクされたプロジェクト」と「`demo`のincludeBuild先」の両方としてIntelliJに認識され、ビルドスクリプトの解析モデルが競合していることが原因と特定した。この種の競合はキャッシュ再構築では解消しない、IntelliJ Gradleプラグインの構造的な制約である。

Unit 2(demo)完了時点では「複合ビルドのままで問題なく動作する」ことを理由にマルチプロジェクト化を見送っていたが(demo-unit-summary.md「マルチプロジェクト化の検討経緯」参照)、今回は再現性のある具体的なIDE不具合が確認できたため、判断を改めてマルチプロジェクト化を実施することで合意した。

### 実施内容

- リポジトリ直下に`settings.gradle.kts`を新設(`rootProject.name = "cherry-testtool"`、`include(":lib", ":demo", ":client:webconsole", ":client:cli")`)
- `lib/build.gradle.kts`から`group`/`version`を削除(複合ビルド解決のために必要だったが、マルチプロジェクト化により不要に)。`base { archivesName.set("cherry-testtool-core") }`を追加し、成果物名(`cherry-testtool-core.jar`、バージョン無し)を維持
- `demo/build.gradle.kts`の依存を`implementation("cherry.testtool:cherry-testtool-core:0.0.1-SNAPSHOT")`から`implementation(project(":lib"))`へ変更。`base.archivesName`(`cherry-testtool-demo`)を追加
- `client/webconsole`・`client/cli`にも`base.archivesName`を追加(マルチプロジェクトではサブプロジェクトの既定名がディレクトリ名(`webconsole`/`cli`)になり、現行の成果物名`cherry-testtool-*`を失うため)
- 4つの`settings.gradle.kts`と、4モジュールに重複していたGradle Wrapper一式(`gradlew`/`gradlew.bat`/`gradle/wrapper/*`)を削除し、リポジトリ直下の1組へ統合

### 検証

リポジトリ直下から`./gradlew clean test`を実行し、`:lib`(31)・`:demo`(2)・`:client:webconsole`(3)・`:client:cli`(15)の全51テストが成功することを確認。`:lib:jar`・`:demo:bootJar`・`:client:webconsole:bootJar`・`:client:cli:bootJar`で成果物名(`cherry-testtool-core.jar`等)が維持されていることも確認。`demo`+`webconsole`+`cli`を同時起動し、プロキシ経由アクセス・CLI直接呼出しが従来通り動作することも再確認した(いずれもUnit 3/4完了時に実施した手動結合確認の再実施)。
