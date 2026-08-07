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

### 変更なし(確認済み)
- `lib/src/test`配下の既存5テストクラス(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubInterceptorTest`、`StubRepositoryTest`)
- `lib/src/test`配下のフィクスチャ(`ToolTester`、`ToolTesterImpl`、`StubAspect`、`TestMain`)、XML設定ファイル — Unit 2(demo)での移管対象のため本Unitでは触れていない

## 詳細サマリー

- [business-logic-summary.md](business-logic-summary.md) — Interface統合の詳細
- [api-layer-summary.md](api-layer-summary.md) — Controller統合の詳細

## 未実施事項(Build and Testフェーズで対応)

`./gradlew build`によるコンパイル・単体テスト実行での最終検証は、全Unit完了後のBuild and Testフェーズで行う。
