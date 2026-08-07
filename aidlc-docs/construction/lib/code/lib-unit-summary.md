# Unit 1(lib) - Code Generation Summary

## 対応FR/NFR

FR3(意図的な例外処理へのコメント補足)、FR4(Interface/Impl分離の解消)、FR7(コメント充実、lib分)、FR8(Controller統合)、NFR5(JSpecifyベースのNullability規約統一、lib分)。

## 変更ファイル一覧

### 新規作成
- `lib/src/test/java/cherry/testtool/aspect/TraceAspect.java`(レビュー時にユーザー指示で追加。`reference/TraceAspect.java`を基に、パッケージ・pointcut・`@Value`のデフォルト値(`reference/application.properties`の値を`${prop:default}`形式で埋め込み)をこのプロジェクトへ適合させたもの。旧`appctx-trace.xml`のアノテーションベース版。当初`cherry.testtool`パッケージに作成後、`cherry.testtool.aspect`へ移動しpointcutも`execution(* cherry.testtool..*.*(..)) && !within(cherry.testtool.aspect..*)`へ絞り込み(自パッケージを対象外に))
- `lib/src/test/java/cherry/testtool/aspect/StubAspect.java`(レビュー時にユーザー指示で移動。旧`lib/src/test/java/cherry/testtool/StubAspect.java`を`aspect`パッケージへ移動、パッケージ宣言のみ変更)
- `lib/src/test/resources/application.yml`(レビュー時にユーザー指示で追加。旧`application.properties`のYAML変換版)
- `lib/src/main/java/cherry/testtool/web/TesttoolController.java`
- `lib/src/test/java/cherry/testtool/web/TesttoolControllerTest.java`
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

正式なBuild and Testフェーズ(全Unit完了後)とは別に、本Unit完了時点で`./gradlew compileJava compileTestJava test`を実行し、コンパイル成功・全テスト成功を都度確認済み。最終状態(`StubInterceptor`の`@Deprecated`化、XML設定ファイル群の削除、`TraceAspect`/`StubAspect`の`aspect`パッケージ移動、`application.yml`化後)では29テスト成功(既存4クラス22件+新規`TesttoolControllerTest`7件)。`--tests`で個別実行しログ出力を確認し、`TraceAspect`が引き続きENTER/EXITトレースログを出力すること、および`aspect`パッケージ自身(`TraceAspect`/`StubAspect`)がトレース対象から除外されていることも確認済み。

この過程で判明した技術的事項に対応するため、計画外の追加修正を行った。

- `lib/build.gradle`: `testRuntimeOnly 'org.springframework.boot:spring-boot-starter-web'`を`testImplementation`へ変更(テストコンパイル時にSpring MVCの型が必要なため)
- `TesttoolControllerTest`: `@WebMvcTest`+`@MockitoBean`から`MockMvcBuilders.standaloneSetup`方式へ変更(詳細は[api-layer-summary.md](api-layer-summary.md)参照)

**Unit 3・Unit 4への申し送り**: Spring Boot 4.1.0では`@WebMvcTest`等のWebスライステストアノテーションのパッケージが`org.springframework.boot.webmvc.test.autoconfigure`へ変更されている(旧`org.springframework.boot.test.autoconfigure.web.servlet`)。Spring Bootのテストコードを新規作成する際は留意すること。
