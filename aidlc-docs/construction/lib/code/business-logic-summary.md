# Business Logic Summary - lib(Unit 1, Step 1-4)

## 変更内容

`lib`のInterface/Impl分離を解消し、以下5組をそれぞれ1つの具象クラスへ統合した(FR4)。

| 統合前 | 統合後(具象クラス) |
|---|---|
| `InvokerService`(interface) / `InvokerServiceImpl` | `InvokerService` |
| `ReflectionResolver`(interface) / `ReflectionResolverImpl` | `ReflectionResolver` |
| `ScriptProcessor`(interface) / `ScriptProcessorImpl` | `ScriptProcessor` |
| `StubRepository`(interface) / `StubRepositoryImpl` | `StubRepository` |
| `StubResolver`(interface) / `StubResolverImpl` | `StubResolver` |

- `ReflectionResolver`・`StubResolver`が持っていたinterfaceのdefaultメソッド(`resolveBeanName(String)`、`resolveMethod(String,String)`、`getStubInvocation(MethodInvocation)`、`getStubInvocation(ProceedingJoinPoint)`)は、具象クラスの通常メソッドとしてそのまま維持した。
- `TesttoolConfiguration`のBean定義メソッドを、`new XxxImpl(...)`から`new Xxx(...)`(具象クラス名)へ更新した。
- `InvokerService.invoke(beanName, className, methodName, ...)`の`catch (Exception ex)`に、テストツールとしての意図的な仕様である旨のコメントを追加した(FR3、挙動は変更していない)。

## コメント充実・JSpecify対応

上記5クラスに加え、`StubConfigLoader`・`StubInterceptor`・`StubConfig`・`StubInvocation`・`ReflectionUtil`・`ToMapUtil`にJavadocを追加し、`jakarta.annotation.Nonnull`/`Nullable`を`org.jspecify.annotations.Nullable`へ置き換えた(非null既定は`package-info.java`の`@NullMarked`で表現する、Step 8で追加)。

## 既存テストへの影響

`lib/src/test`配下の既存5テストクラス(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubInterceptorTest`、`StubRepositoryTest`)は、いずれもインタフェース名(具象クラス化後も同名)で型宣言しており、`XxxImpl`という名前を直接参照していないことを確認した。**変更不要**。

## 変更ファイル一覧

- 上書き: `invoker/InvokerService.java`、`reflect/ReflectionResolver.java`、`script/ScriptProcessor.java`、`stub/StubRepository.java`、`stub/StubResolver.java`
- 削除: `invoker/InvokerServiceImpl.java`、`reflect/ReflectionResolverImpl.java`、`script/ScriptProcessorImpl.java`、`stub/StubRepositoryImpl.java`、`stub/StubResolverImpl.java`
- 修正: `TesttoolConfiguration.java`、`stub/StubConfigLoader.java`、`stub/StubInterceptor.java`、`stub/StubConfig.java`、`stub/StubInvocation.java`、`util/ReflectionUtil.java`、`util/ToMapUtil.java`
