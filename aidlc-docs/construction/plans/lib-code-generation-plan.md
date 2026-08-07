# Code Generation Plan - lib(Unit 1)

## Unit Context

- **対応FR**: FR3(例外コメント), FR4(Interface統合), FR7(コメント充実、lib分), FR8(Controller統合), NFR5(JSpecify、lib分)
- **依存Unit**: なし(最初に着手するUnit)
- **依存される側**: Unit 2(demo)が`lib`にコンパイル依存するため、本Unit完了が前提
- **ワークスペースルート**: `~/Documents/project/git/cherry-testtool`(brownfield、既存構造`lib/src/main/java/cherry/testtool/`を使用)

## 事前調査結果(重要)

`lib/src/test`の既存テスト(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubInterceptorTest`、`StubRepositoryTest`)は、いずれも**インタフェース名**(`InvokerService`、`ReflectionResolver`、`ScriptProcessor`、`StubRepository`)で`@Autowired`フィールドを宣言しており、`XxxImpl`という具象クラス名を直接参照していない。Interface削除後は同名の具象クラスがそのままDIされるため、**既存テストコードの変更は不要**。`StubAspect`(テスト)も`StubResolver`型を参照するのみで同様に変更不要。`ToolTester`/`ToolTesterImpl`/`TestMain`/XML設定ファイルは、FR6.2によりUnit 2(demo)への移管対象のため本Unitでは変更しない。

## Steps

### Step 1: Business Logic Generation — Interface統合(5組)
- [ ] Step 1.1: `lib/src/main/java/cherry/testtool/invoker/InvokerService.java`を上書きし、現行`InvokerServiceImpl`の実装内容を持つ具象クラス`InvokerService`とする。Javadoc追加、JSpecify対応(`@Nullable`のみ使用、`@Nonnull`は削除)。FR3: `invoke(beanName, className, methodName, ...)`の`catch (Exception ex)`に、テストツールとして想定外の例外も結果表示する意図的な仕様である旨のコメントを追加(挙動は変更しない)
- [ ] Step 1.2: `lib/src/main/java/cherry/testtool/invoker/InvokerServiceImpl.java`を削除
- [ ] Step 1.3: `lib/src/main/java/cherry/testtool/reflect/ReflectionResolver.java`を上書きし、現行`ReflectionResolverImpl`の実装内容(+現行interfaceのdefaultメソッドだった`resolveBeanName(String)`・`resolveMethod(String,String)`を通常メソッド化)を持つ具象クラスとする。Javadoc追加、JSpecify対応
- [ ] Step 1.4: `lib/src/main/java/cherry/testtool/reflect/ReflectionResolverImpl.java`を削除
- [ ] Step 1.5: `lib/src/main/java/cherry/testtool/script/ScriptProcessor.java`を上書きし、現行`ScriptProcessorImpl`の実装内容を持つ具象クラスとする。Javadoc追加、JSpecify対応
- [ ] Step 1.6: `lib/src/main/java/cherry/testtool/script/ScriptProcessorImpl.java`を削除
- [ ] Step 1.7: `lib/src/main/java/cherry/testtool/stub/StubRepository.java`を上書きし、現行`StubRepositoryImpl`の実装内容を持つ具象クラスとする。Javadoc追加、JSpecify対応
- [ ] Step 1.8: `lib/src/main/java/cherry/testtool/stub/StubRepositoryImpl.java`を削除
- [ ] Step 1.9: `lib/src/main/java/cherry/testtool/stub/StubResolver.java`を上書きし、現行`StubResolverImpl`の実装内容(+現行interfaceのdefaultメソッドだった`getStubInvocation(MethodInvocation)`・`getStubInvocation(ProceedingJoinPoint)`を通常メソッド化)を持つ具象クラスとする。Javadoc追加、JSpecify対応
- [ ] Step 1.10: `lib/src/main/java/cherry/testtool/stub/StubResolverImpl.java`を削除
- [ ] Step 1.11: `lib/src/main/java/cherry/testtool/TesttoolConfiguration.java`を修正し、`new XxxImpl(...)`を`new Xxx(...)`(具象クラス名)へ更新。Javadoc追加、JSpecify対応

### Step 2: Business Logic Generation — その他lib主要クラスへのコメント充実・JSpecify適用
- [ ] Step 2.1: `lib/src/main/java/cherry/testtool/stub/StubConfigLoader.java`にJavadoc追加、JSpecify対応(構造変更なし)
- [ ] Step 2.2: `lib/src/main/java/cherry/testtool/stub/StubInterceptor.java`にJavadoc追加、JSpecify対応(構造変更なし)
- [ ] Step 2.3: `lib/src/main/java/cherry/testtool/stub/StubConfig.java`にJavadoc追加、JSpecify対応(`engine`フィールドを`@Nullable`に)
- [ ] Step 2.4: `lib/src/main/java/cherry/testtool/stub/StubInvocation.java`にJavadoc追加、JSpecify対応
- [ ] Step 2.5: `lib/src/main/java/cherry/testtool/util/ReflectionUtil.java`にJavadoc追加、JSpecify対応(構造変更なし)
- [ ] Step 2.6: `lib/src/main/java/cherry/testtool/util/ToMapUtil.java`のJSpecify対応(`@Nonnull`→JSpecify、既存Javadocは維持)

### Step 3: Business Logic Unit Testing — 既存テストの動作確認
- [ ] Step 3.1: Step 1・Step 2の変更が`lib/src/test`配下の既存5テストクラス(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubInterceptorTest`、`StubRepositoryTest`)を変更せずにコンパイル・実行可能であることを確認する(実際のビルド実行はBuild and Testフェーズで行うが、型・シグネチャの整合性はここで確認する)

### Step 4: Business Logic Summary
- [ ] Step 4.1: `aidlc-docs/construction/lib/code/business-logic-summary.md`を作成し、Interface統合・具象クラス化の内容をサマリーする

### Step 5: API Layer Generation — Controller統合(FR8)
- [ ] Step 5.1: `lib/src/main/java/cherry/testtool/web/TesttoolController.java`を新規作成する。`invoke`(`/testtool/invoker/invoke`)、`put`/`get`/`list`(`/testtool/stubconfig/**`)は現行URL・シグネチャのまま実装。`bean`/`method`解決は新パス(`/testtool/resolve/bean`、`/testtool/resolve/method`)として実装(重複していた実装を1箇所に集約)。`@ConditionalOnProperty(prefix = "cherry.testtool.web", name = "enabled", havingValue = "true", matchIfMissing = true)`による単一トグルとする。呼出し先は具象クラス(`InvokerService`、`StubRepository`、`ScriptProcessor`、`ReflectionResolver`)。Javadoc追加、JSpecify対応
- [ ] Step 5.2: `lib/src/main/java/cherry/testtool/web/InvokerController.java`を削除
- [ ] Step 5.3: `lib/src/main/java/cherry/testtool/web/StubConfigController.java`を削除

### Step 6: API Layer Unit Testing
- [ ] Step 6.1: `lib/src/test/java/cherry/testtool/web/TesttoolControllerTest.java`を新規作成する。`@WebMvcTest(TesttoolController.class)`+`@MockitoBean`(`InvokerService`、`StubRepository`、`ScriptProcessor`、`ReflectionResolver`)を用い、`invoke`・`put`・`get`・`list`・`resolve/bean`・`resolve/method`の各エンドポイントが対応するサービスメソッドへ正しく委譲することを検証する

### Step 7: API Layer Summary
- [ ] Step 7.1: `aidlc-docs/construction/lib/code/api-layer-summary.md`を作成し、Controller統合の内容をサマリーする

### Step 8: Nullability基盤整備(NFR5)
- [ ] Step 8.1: `lib/build.gradle`に`org.jspecify:jspecify`への依存を追加する(バージョンは`dependencyManagement`で`1.0.0`を管理)
- [ ] Step 8.2: 以下7パッケージそれぞれに`package-info.java`を新規作成し、`@NullMarked`(`org.jspecify.annotations.NullMarked`)を付与する: `cherry.testtool`、`cherry.testtool.invoker`、`cherry.testtool.reflect`、`cherry.testtool.script`、`cherry.testtool.stub`、`cherry.testtool.util`、`cherry.testtool.web`

### Step 9: Documentation Generation
- [ ] Step 9.1: `aidlc-docs/construction/lib/code/lib-unit-summary.md`を作成し、Unit 1(lib)全体の変更内容(削除ファイル・新規ファイル・変更ファイル一覧)をまとめる

## Deployment Artifacts
本Unitはライブラリモジュールであり、デプロイ成果物(実行可能jar等)は生成しない。`lib/build.gradle`はビルド設定の一部としてStep 8.1で更新する。
