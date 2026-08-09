# Code Generation Plan: `/testtool/**` APIキー保護(lib / client:webconsole / client:cli / demo)

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR10.1〜FR10.6)、`aidlc-docs/inception/plans/api-key-protection-execution-plan.md`

## 事前調査結果(実装方針の確定)

`client/cli`は`RootCommand`(`baseUrl`/`basicAuth`/`headers`をPicocliオプションとして保持)を、`InvokeCommand`・`StubConfigRegisterCommand`・`StubConfigClearCommand`・`StubConfigShowCommand`の4箇所が参照し、それぞれ`InvokeService`/`StubConfigService`(→`RequestHeaderBuilder.build(basicAuth, headers)`)へ委譲している。`RequestHeaderBuilder.build(...)`・`InvokeService`・`StubConfigService`のシグネチャ変更は影響範囲(既存テスト`InvokeServiceTest`・`StubConfigServiceTest`・`RequestHeaderBuilderTest`)が広くなるため、代わりに`RootCommand`へ`effectiveHeaders()`(設定済みAPIキーを`Name: Value`形式で`headers`へ合成して返す)を新設し、既存4箇所の呼出しを`rootCommand.headers`から`rootCommand.effectiveHeaders()`へ差し替えるだけに留める。これにより`RequestHeaderBuilder`・`InvokeService`・`StubConfigService`とその既存テストは無変更で済む。

## Steps

### lib

- [ ] **Step 1**: `lib/src/main/java/cherry/testtool/web/ApiKeyFilter.java`を新規作成する
  - `jakarta.servlet.Filter`実装。コンストラクタでヘッダ名・期待するAPIキー値を受け取る
  - `doFilter`で対象リクエストのヘッダ値を取得し、`java.security.MessageDigest.isEqual(...)`によるタイミング攻撃耐性のある比較で照合。不一致・未指定なら`HttpServletResponse.SC_UNAUTHORIZED`(401)を返しチェーンを止める。一致すれば`chain.doFilter(...)`

- [ ] **Step 2**: `lib/src/main/java/cherry/testtool/TesttoolAutoConfiguration.java`を修正する
  - `cherry.testtool.web.api-key`プロパティが設定されている場合のみ、`FilterRegistrationBean<ApiKeyFilter>`を`@Bean`として返すメソッドを追加(`@ConditionalOnWebApplication(type = Type.SERVLET)`・`@ConditionalOnProperty(prefix = "cherry.testtool.web", name = "api-key")`)
  - ヘッダ名は`cherry.testtool.web.api-key-header`プロパティ(既定`X-Cherry-Testtool-Api-Key`)から取得
  - `registration.addUrlPatterns("/testtool/*")`でURL patternを`/testtool/**`相当に限定する(FR10.1補強内容)

- [ ] **Step 3**: `lib/src/test/java/cherry/testtool/web/ApiKeyFilterTest.java`を新規作成する
  - `ApiKeyFilter`を直接インスタンス化し、モックの`HttpServletRequest`/`HttpServletResponse`/`FilterChain`で、(a)正しいヘッダ値→`chain.doFilter`が呼ばれる、(b)ヘッダ値不一致→401かつ`chain.doFilter`が呼ばれない、(c)ヘッダ未指定→401、の3ケースを検証する
  - 既存31テストの回帰確認(`./gradlew :lib:test`)

- [ ] **Step 4**: `aidlc-docs/construction/lib/code/api-key-protection-summary.md`を新規作成する(lib部分の変更ファイル一覧・設計判断を記録)

### client/webconsole

- [ ] **Step 5**: `client/webconsole/src/main/java/cherry/testtool/webconsole/GatewayRouteConfig.java`を修正する
  - `cherry.testtool.web.api-key`(未設定なら空文字)・`cherry.testtool.web.api-key-header`(既定`X-Cherry-Testtool-Api-Key`)を`@Value`で受け取る
  - backendへのプロキシリクエストへ、APIキーが設定されている場合のみ該当ヘッダを付与するリクエスト側フィルタを追加する(既存の`secureHeaders()`レスポンス側フィルタと対になる形。Spring Cloud Gateway Server MVCの`FilterFunctions`にリクエストヘッダ付与の組込み関数があれば利用し、無ければ`HandlerFilterFunction`を自前実装する)
  - SPA利用者(ブラウザ)側には別途キー入力を求めない(FR10.4、webconsoleが鍵を内部保持する最小スコープ)

- [ ] **Step 6**: 既存テストの回帰確認(`./gradlew :client:webconsole:test`)。ログ出力等の専用テストは追加しない(Build and Testフェーズでの実機結合確認に委ねる)

- [ ] **Step 7**: `aidlc-docs/construction/webconsole/code/api-key-protection-summary.md`を新規作成する

### client/cli

- [ ] **Step 8**: `client/cli/src/main/java/cherry/testtool/cli/command/RootCommand.java`を修正する
  - `@Value("${cherry.testtool.web.api-key:}")`(`@Nullable String apiKey`)・`@Value("${cherry.testtool.web.api-key-header:X-Cherry-Testtool-Api-Key}")`(`String apiKeyHeader`)フィールドを追加(Picocli `@Option`ではなくSpring設定由来の値)
  - `List<String> effectiveHeaders()`メソッドを追加。`apiKey`が空でなければ`headers`のコピーへ`"{apiKeyHeader}: {apiKey}"`を追加して返す。空なら`headers`をそのまま返す

- [ ] **Step 9**: 呼出し元4箇所を`rootCommand.headers`/`rc.headers`から`rootCommand.effectiveHeaders()`/`rc.effectiveHeaders()`へ変更する
  - `InvokeCommand.java`、`StubConfigRegisterCommand.java`、`StubConfigClearCommand.java`、`StubConfigShowCommand.java`

- [ ] **Step 10**: `RootCommand`の`effectiveHeaders()`単体テストを追加する(`RootCommandTest.java`新規、または既存の関連テストへ追加。APIキー未設定時は`headers`と同一、設定時は合成されることを検証)。既存テスト(`RequestHeaderBuilderTest`含む)の回帰確認(`./gradlew :client:cli:test`)

- [ ] **Step 11**: `aidlc-docs/construction/cli/code/api-key-protection-summary.md`を新規作成する

### demo

- [ ] **Step 12**: `demo/src/main/resources/application.yml`へ、`cherry.testtool.web.api-key`の設定例をコメントアウトした状態(既定は無効)で追記する(FR10.6)

- [ ] **Step 13**: `aidlc-docs/construction/demo/code/api-key-protection-summary.md`を新規作成する(設定例追加のみである旨を記録)

## Out of Scope
- Repository Layer / Frontend Components(SPA側でのAPIキー入力UI等) — FR10.4によりSPA利用者への入力要求は行わないため不要
- Database Migration / Deployment Artifacts — 該当なし
