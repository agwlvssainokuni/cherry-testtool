# `/testtool/**` APIキー保護(lib部分)- Code Generation Summary

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR10)、`aidlc-docs/construction/plans/api-key-protection-code-generation-plan.md`(Step1-4)

## 変更ファイル一覧

### 新規作成
- `lib/src/main/java/cherry/testtool/web/ApiKeyFilter.java` — `jakarta.servlet.Filter`実装。指定ヘッダの値を`MessageDigest.isEqual(...)`による定数時間比較で照合し、不一致・未指定なら401を返す
- `lib/src/test/java/cherry/testtool/web/ApiKeyFilterTest.java` — `ApiKeyFilter`の単体テスト(3ケース: 一致→通過、不一致→401、未指定→401)。Spring Contextを起動せずMockitoでServlet APIをモック化

### 修正
- `lib/src/main/java/cherry/testtool/TesttoolAutoConfiguration.java` — `cherry.testtool.web.api-key`プロパティが設定されている場合のみ、`FilterRegistrationBean<ApiKeyFilter>`を返す`@Bean`メソッド(`apiKeyFilter`)を追加。`@ConditionalOnWebApplication(type = Type.SERVLET)`・`@ConditionalOnProperty(prefix = "cherry.testtool.web", name = "api-key")`で未設定時は不登録(後方互換)。`registration.addUrlPatterns("/testtool/*")`で対象を`/testtool/**`相当に限定

## 設計判断

- **登録方式**: 単純に`ApiKeyFilter`型のBeanを返すと既定URL patternが`/*`(消費側アプリの全リクエスト)になってしまうため、`FilterRegistrationBean`で明示的に`/testtool/*`へ限定した(FR10.1補強)。`FilterRegistrationBean`もSpring Bootの`ServletContextInitializerBeans`により自動検出・登録されるため、`TesttoolController`と同様、消費側アプリでの追加設定は不要
- **比較方式**: 単純な文字列比較(`equals`)ではなく`MessageDigest.isEqual(byte[], byte[])`による定数時間比較を採用し、タイミング攻撃への耐性を持たせた
- **ヘッダ名**: 標準の`Authorization`ヘッダは使わず、`cherry.testtool.web.api-key-header`プロパティ(既定`X-Cherry-Testtool-Api-Key`)で指定する専用ヘッダを用いる(消費側アプリ自体の認証方式やリバースプロキシとの名前空間衝突を避けるため)

## 動作確認

`./gradlew :lib:build`で新規`ApiKeyFilterTest`3件を含む全テストが成功することを確認済み。実機(demo)による結合確認は、webconsole・cliの実装完了後にBuild and Testフェーズでまとめて実施する。
