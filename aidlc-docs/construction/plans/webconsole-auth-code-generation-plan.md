# Code Generation Plan: webconsole Basic認証追加(FR13)

## Unit Context
- **対象Unit**: `client/webconsole`(既存Unit境界内、新規Unitではない)
- **依存**: なし(`lib`・`demo`・`client/cli`への変更は無し)
- **Requirements参照**: `aidlc-docs/inception/requirements/requirements.md` FR13
- **Execution Plan参照**: `aidlc-docs/inception/plans/webconsole-auth-execution-plan.md`

## 事前調査結果(Step 1相当、計画作成時に実施済み)
- 既存の`client/webconsole`backendは`GatewayRouteConfig`(`/testtool/**`プロキシ、APIキー自動付与)・`WebConfig`(静的リソース配信+SPAフォールバック)の2Configurationのみで、Spring Securityは未導入
- `lib`の`ApiKeyFilter`(`FilterRegistrationBean`で`/testtool/*`に限定登録)は依存ゼロの自前実装だが、今回はユーザー合意によりSpring Security依存を追加する方針のため別パターンとなる
- `client/webconsole/build.gradle.kts`は`implementation("org.springframework.boot:spring-boot-starter-web")`等を直接列挙する形式。`spring-boot-starter-security`も同様に追加する
- 既存E2Eの`webconsole-api-key-mismatch.spec.ts`が、global-setup(8080/9090)とは独立した専用ポート(8081/9091)でdemo/webconsoleを`test.afterEach`で都度起動・停止する自己完結パターンを採用しており、今回のBasic認証専用シナリオもこれを踏襲する

## 実装方針
- `WebSecurityConfig`(新規)で`SecurityFilterChain`を1つ定義し、`cherry.testtool.web.auth.username`/`cherry.testtool.web.auth.password`が両方設定されている場合のみBasic認証を有効化する(`httpBasic()`+`authorizeHttpRequests(anyRequest().authenticated())`+セッションSTATELESS+CSRF disable)。片方のみ設定・両方未設定の場合は`anyRequest().permitAll()`(既存動作を維持)。
- パスワード照合は`PasswordEncoderFactories.createDelegatingPasswordEncoder()`をベースに、`setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance())`でプレフィックス無し値を平文照合させる。`{bcrypt}`プレフィックス付き値はBCryptで照合される(Q4回答A、平文既定+BCrypt対応の両立)。
- `InMemoryUserDetailsManager`(単一ユーザー)を`SecurityFilterChain`内で組み立てる(専用の`UserDetailsService` Beanとしては公開しない、他Beanから参照される想定が無いため)。

## Steps

- [ ] **Step 1: `client/webconsole/build.gradle.kts`へ`spring-boot-starter-security`依存を追加**
  - `dependencies`ブロックの`implementation("org.springframework.boot:spring-boot-starter-web")`の次行に`implementation("org.springframework.boot:spring-boot-starter-security")`を追加

- [ ] **Step 2: `WebSecurityConfig.java`新設**
  - パス: `client/webconsole/src/main/java/cherry/testtool/webconsole/WebSecurityConfig.java`
  - `@Configuration`クラス、`@Bean public SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${cherry.testtool.web.auth.username:}") String username, @Value("${cherry.testtool.web.auth.password:}") String password) throws Exception`
  - 上記「実装方針」の通り、username/password両方`StringUtils.hasText`の場合のみBasic認証を有効化する条件分岐を実装
  - ライセンスヘッダ(Apache License 2.0、年表記`2026`)を付与

- [ ] **Step 3: `WebSecurityConfig`の単体テスト追加**
  - パス: `client/webconsole/src/test/java/cherry/testtool/webconsole/WebSecurityConfigTest.java`
  - `@SpringBootTest`+`MockMvc`(または`WebTestClient`)で以下を検証:
    - 認証情報未設定時: 認証ヘッダ無しでも200が返る(既存動作維持)
    - 認証情報設定時: 認証ヘッダ無しは401、正しいBasic認証情報は200、誤ったパスワードは401
  - テストプロファイル(`@TestPropertySource`または`@SpringBootTest(properties = ...)`)で`cherry.testtool.web.auth.username`/`password`を設定したテストクラスと、設定しないテストクラスを分ける(既存の設定を動的に切り替えるより、Spring Bootコンテキストを分離する方が単純なため)

- [ ] **Step 4: `client/webconsole/README.md`へ設定方法を追記**
  - `cherry.testtool.web.auth.username`/`cherry.testtool.web.auth.password`の設定例(既存のAPIキー設定例と同じ節構成に揃える)
  - 未設定時は認証なしで動作する旨、パスワードは平文または`{bcrypt}`プレフィックス付きBCryptハッシュに対応する旨を記載

- [ ] **Step 5: `e2e/support/config.ts`へBasic認証専用のポート・認証情報定数を追加**
  - `AUTH_WEBCONSOLE_PORT`(webconsole専用起動ポート、他の専用テストと重複しない値)
  - `AUTH_WEBCONSOLE_URL`
  - `AUTH_USERNAME`・`AUTH_PASSWORD`(テスト用固定値)

- [ ] **Step 6: `e2e/tests/webconsole-basic-auth.spec.ts`新設**
  - `webconsole-api-key-mismatch.spec.ts`と同様の自己完結パターン(専用ポートでwebconsoleを`test.afterEach`で都度起動・停止)
  - global-setupのdemo(8080)をbackendとして指定して起動(Basic認証はwebconsole自身への認証であり、backend/demoには影響しないため、既存起動中のdemoをそのまま使う)
  - シナリオ: (1) 認証ヘッダ無しでアクセス→401、(2) 正しいBasic認証情報でアクセス→200、(3) 誤ったパスワードでアクセス→401
  - E2E_API_KEYの有無に依存しないためno-keyパスでのみ実行(`test.skip`、既存パターン踏襲)

- [ ] **Step 7: `e2e/README.md`の構成説明・テスト一覧へ追記**

- [ ] **Step 8: ローカル動作確認**
  - `./gradlew build`(全モジュール、リグレッション無し確認)
  - `npx tsc --noEmit`(e2e/)
  - `npm run format:check`(e2e/)
  - `npm run test:e2e`(no-key/with-keyとも成功確認)
  - 実ブラウザ(`npm run dev`不要、ビルド済みjarを直接起動)でBasic認証ダイアログの表示・認証成功/失敗を目視確認

- [ ] **Step 9: サマリー作成**
  - パス: `aidlc-docs/construction/webconsole/code/basic-auth-summary.md`
  - 変更ファイル一覧、実装方針、確認結果を記載
