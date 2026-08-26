# Unit Test Execution

## Run Unit Tests

### 1. 全モジュールの単体テストを実行する

リポジトリ直下から一括で実行する(単一Gradleマルチプロジェクトビルド)。

```bash
./gradlew test
```

個別のサブプロジェクトのみ実行する場合はGradleパスを指定する。

```bash
./gradlew :lib:test
./gradlew :demo:test
./gradlew :client:webconsole:test
./gradlew :client:cli:test
```

### 2. テスト結果の確認

| モジュール | テスト数 | 内容 |
|---|---|---|
| `lib` | 34 | `InvokerService`/`ReflectionResolver`/`ScriptProcessor`/`StubRepository`/`StubAspect`/`TesttoolController`/`ApiKeyFilter`等 |
| `demo` | 3 | `DemoApplicationTests`(コンテキストロード)、`SampleControllerTest`(スタブ介入前後の挙動差)、`StubAutoLoadRunnerTest`(起動時のスタブ自動読込み) |
| `client/webconsole` | 8 | `WebconsoleApplicationTests`(コンテキストロード)、`SpaFallbackResourceResolverTest`(2件)、`WebSecurityConfigAuthDisabledTest`(1件)、`WebSecurityConfigAuthEnabledTest`(4件) |
| `client/cli` | 19 | `ScriptFileScannerTest`(5)、`RequestHeaderBuilderTest`(6)、`InvokeServiceTest`(1)、`StubConfigServiceTest`(3)、`RootCommandTest`(4) |
| **合計** | **64** | |

- **Expected**: 64件全て成功、失敗0件
- **Test Report Location**: 各モジュールの`build/reports/tests/test/index.html`(HTML)、`build/test-results/test/*.xml`(JUnit XML)

### 3. テスト失敗時の対応

1. 該当モジュールの`build/reports/tests/test/index.html`でスタックトレースを確認する
2. 原因を特定し、コードまたはテストを修正する
3. `./gradlew test`を再実行し、全件成功するまで繰り返す

## 最終確認結果

Gradleマルチプロジェクト化(2026-08-09)後、リポジトリ直下から`./gradlew clean test`を実行し、51件全てが成功することを確認済み。その後`demo`へ`StubAutoLoadRunner`を追加した際に`StubAutoLoadRunnerTest`が加わり、52件全てが成功することを再確認した(2026-08-09)。さらにFR9(スタブ実行時のトレースログ出力)・FR10(`/testtool/**` APIキー保護、`ApiKeyFilterTest`3件・`RootCommandTest`4件が追加)の対応後、`./gradlew clean build`で59件全てが成功することを確認した(2026-08-09)。

### FR11(webconsole frontendのUIライブラリ移行)再実行

FR11はフロントエンド(`client/webconsole/frontend`)のみの変更で、Java側のテストコード自体には変更が無いため、テスト件数は変わらず59件。`./gradlew clean build`で全59件が引き続き成功することを確認した(2026-08-14)。`client/webconsole/frontend`自体は自動化されたユニットテストスイート(vitest等)を持たないため(`package.json`に`test`スクリプト無し)、フロントエンドの検証は`npm run lint`(oxlint/eslint)・`npm run build`(`tsc -b`による型チェック含む)を自動チェックとして用い、実際の画面表示・操作はIntegration Test Instructions側の手動確認手順に委ねる。

### FR13(webconsole Basic認証追加)再実行

`client/webconsole`へ`WebSecurityConfigAuthDisabledTest`(1件)・`WebSecurityConfigAuthEnabledTest`(4件、複数ユーザー対応の確認含む)を追加し、`client/webconsole`のテスト数が3件→8件、全体が59件→64件になった。`./gradlew clean build`で64件全てが成功することを確認した(2026-08-17)。
