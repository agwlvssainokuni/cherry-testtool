# Unit 1(lib) - Code Generation Summary

## 対応FR/NFR

FR3(意図的な例外処理へのコメント補足)、FR4(Interface/Impl分離の解消)、FR7(コメント充実、lib分)、FR8(Controller統合)、NFR5(JSpecifyベースのNullability規約統一、lib分)。

## 変更ファイル一覧

### 新規作成
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
- `lib/src/main/java/cherry/testtool/stub/StubInterceptor.java`
- `lib/src/main/java/cherry/testtool/stub/StubConfig.java`
- `lib/src/main/java/cherry/testtool/stub/StubInvocation.java`
- `lib/src/main/java/cherry/testtool/util/ReflectionUtil.java`
- `lib/src/main/java/cherry/testtool/util/ToMapUtil.java`
- `lib/build.gradle`(`org.jspecify:jspecify`依存を追加)

### 削除
- `lib/src/main/java/cherry/testtool/invoker/InvokerServiceImpl.java`
- `lib/src/main/java/cherry/testtool/reflect/ReflectionResolverImpl.java`
- `lib/src/main/java/cherry/testtool/script/ScriptProcessorImpl.java`
- `lib/src/main/java/cherry/testtool/stub/StubRepositoryImpl.java`
- `lib/src/main/java/cherry/testtool/stub/StubResolverImpl.java`
- `lib/src/main/java/cherry/testtool/web/InvokerController.java`
- `lib/src/main/java/cherry/testtool/web/StubConfigController.java`
- `lib/src/test/java/cherry/testtool/TestMain.java`(レビュー時にユーザー指示で追加削除。デモアプリ新設(Unit 2)により手動起動用フィクスチャとしての役目を終えるため、Unit 2への移管ではなく廃止とした)

### 変更なし(確認済み)
- `lib/src/test`配下の既存5テストクラス(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubInterceptorTest`、`StubRepositoryTest`) — いずれも自身に`@ImportResource`でXML設定を読み込んでおり`TestMain`には依存していないため、削除の影響なし
- `lib/src/test`配下のフィクスチャ(`ToolTester`、`ToolTesterImpl`、`StubAspect`)、XML設定ファイル(`appctx-stub.xml`、`appctx-trace.xml`) — Unit 2(demo)での移管対象のため本Unitでは触れていない

## 詳細サマリー

- [business-logic-summary.md](business-logic-summary.md) — Interface統合の詳細
- [api-layer-summary.md](api-layer-summary.md) — Controller統合の詳細

## ビルド検証(早期確認)

正式なBuild and Testフェーズ(全Unit完了後)とは別に、本Unit完了時点で`./gradlew compileJava compileTestJava test`を実行し、コンパイル成功・全31テスト成功(既存24件+新規`TesttoolControllerTest`7件)を確認済み。

この過程で判明した技術的事項に対応するため、計画外の追加修正を行った。

- `lib/build.gradle`: `testRuntimeOnly 'org.springframework.boot:spring-boot-starter-web'`を`testImplementation`へ変更(テストコンパイル時にSpring MVCの型が必要なため)
- `TesttoolControllerTest`: `@WebMvcTest`+`@MockitoBean`から`MockMvcBuilders.standaloneSetup`方式へ変更(詳細は[api-layer-summary.md](api-layer-summary.md)参照)

**Unit 3・Unit 4への申し送り**: Spring Boot 4.1.0では`@WebMvcTest`等のWebスライステストアノテーションのパッケージが`org.springframework.boot.webmvc.test.autoconfigure`へ変更されている(旧`org.springframework.boot.test.autoconfigure.web.servlet`)。Spring Bootのテストコードを新規作成する際は留意すること。
