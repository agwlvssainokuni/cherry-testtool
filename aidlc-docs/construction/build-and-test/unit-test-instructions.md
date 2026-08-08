# Unit Test Execution

## Run Unit Tests

### 1. 全モジュールの単体テストを実行する

```bash
cd lib && ./gradlew test
cd ../demo && ./gradlew test
cd ../client/webconsole && ./gradlew test
cd ../cli && ./gradlew test
```

### 2. テスト結果の確認

| モジュール | テスト数 | 内容 |
|---|---|---|
| `lib` | 31 | `InvokerService`/`ReflectionResolver`/`ScriptProcessor`/`StubRepository`/`StubAspect`/`TesttoolController`等 |
| `demo` | 2 | `DemoApplicationTests`(コンテキストロード)、`SampleControllerTest`(スタブ介入前後の挙動差) |
| `client/webconsole` | 3 | `WebconsoleApplicationTests`(コンテキストロード)、`SpaFallbackResourceResolverTest`(2件) |
| `client/cli` | 15 | `ScriptFileScannerTest`(5)、`RequestHeaderBuilderTest`(6)、`InvokeServiceTest`(1)、`StubConfigServiceTest`(3) |
| **合計** | **51** | |

- **Expected**: 51件全て成功、失敗0件
- **Test Report Location**: 各モジュールの`build/reports/tests/test/index.html`(HTML)、`build/test-results/test/*.xml`(JUnit XML)

### 3. テスト失敗時の対応

1. 該当モジュールの`build/reports/tests/test/index.html`でスタックトレースを確認する
2. 原因を特定し、コードまたはテストを修正する
3. `./gradlew test`を再実行し、全件成功するまで繰り返す

## 最終確認結果(2026-08-08時点)

上記4モジュール全てで`./gradlew clean test`を実行し、51件全てが成功することを確認済み。
