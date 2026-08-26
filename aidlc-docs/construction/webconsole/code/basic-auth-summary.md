# webconsole Basic認証追加(FR13) サマリー

## FR13.1: 複数ユーザー対応への設計変更(2026-08-17、Code Generationレビュー時の追加依頼)

初版(単一の`cherry.testtool.web.auth.username`/`password`)実装後、ユーザーからの相談・指示により複数ユーザー対応(リスト形式への統一、単一プロパティとの後方互換は持たせない)へ設計変更した。

- **新規**: `client/webconsole/src/main/java/cherry/testtool/webconsole/WebAuthProperties.java` — `@ConfigurationProperties(prefix = "cherry.testtool.web.auth")`のrecord。`List<UserEntry> users`(`users`未設定時は空リストへ正規化)
- **変更**: `WebSecurityConfig.java` — `@Value`による単一username/passwordの受け取りをやめ、`WebAuthProperties`を`@EnableConfigurationProperties`で有効化・注入。`properties.users()`が空でない場合、各エントリを`UserDetails`へ変換し`InMemoryUserDetailsManager`へ複数登録する
- **変更**: `WebSecurityConfigAuthEnabledTest.java` — プロパティを`cherry.testtool.web.auth.users[0].username`等のインデックス付き形式へ変更、2人目ユーザーの認証成功テストを追加(計4テスト)
- **変更**: `WebSecurityConfigAuthDisabledTest.java` — Javadocの記述をリスト形式表記へ更新(テスト内容自体は変更なし)
- **変更**: `application.yml`・`client/webconsole/README.md` — 設定例をリスト形式(`users:`配下に複数エントリ)へ更新
- **変更**: `e2e/support/config.ts` — `AUTH_SECOND_USERNAME`/`AUTH_SECOND_PASSWORD`定数を追加
- **変更**: `e2e/tests/webconsole-basic-auth.spec.ts` — 起動時の引数をインデックス付きプロパティ(`users[0].*`/`users[1].*`)へ変更、2人目ユーザーの認証成功テストを追加(計4テスト)
- **変更**: `e2e/README.md`・`requirements.md`(FR13.1新設) — リスト形式への変更を反映

**確認結果**: `./gradlew build`(全モジュール、単体テスト計5件成功)・`npx tsc --noEmit`・`npm run format:check`・`npm run test:e2e:no-key`(15成功・1スキップ)・`npm run test:e2e:with-key`(8成功・8スキップ)いずれも成功。


## 変更ファイル

### Application Code
- **変更**: `client/webconsole/build.gradle.kts` — `spring-boot-starter-security`依存を追加
- **新規**: `client/webconsole/src/main/java/cherry/testtool/webconsole/WebSecurityConfig.java` — `cherry.testtool.web.auth.username`/`password`が両方設定されている場合のみBasic認証を有効化する`SecurityFilterChain`
- **変更**: `client/webconsole/src/main/resources/application.yml` — 設定例(コメントアウト)を追加
- **新規**: `client/webconsole/src/test/java/cherry/testtool/webconsole/WebSecurityConfigAuthDisabledTest.java` — 未設定時の動作確認
- **新規**: `client/webconsole/src/test/java/cherry/testtool/webconsole/WebSecurityConfigAuthEnabledTest.java` — 設定時の動作確認(認証ヘッダ無し/正しい認証情報/誤ったパスワードの3パターン)
- **変更**: `client/webconsole/README.md` — 「構成」節への`WebSecurityConfig`追記、「Basic認証」節新設
- **変更**: `e2e/support/config.ts` — `AUTH_WEBCONSOLE_PORT`(9093)・`AUTH_WEBCONSOLE_URL`・`AUTH_USERNAME`・`AUTH_PASSWORD`定数を追加
- **新規**: `e2e/tests/webconsole-basic-auth.spec.ts` — Basic認証専用E2Eシナリオ(3テスト)
- **変更**: `e2e/README.md` — 「構成」節・「テスト一覧」・カバー範囲への追記

### Documentation
- 本ファイル

## 実装方針の要点

- **有効化条件**: `cherry.testtool.web.auth.username`/`cherry.testtool.web.auth.password`が両方設定されている場合のみBasic認証を有効化する。片方のみ・両方未設定の場合は`permitAll()`(既存動作維持、後方互換)。
- **Spring Bootの標準プロパティを避けた理由**: `spring.security.user.*`は`spring-boot-starter-security`依存追加時点で自動的にBasic認証を有効化し、パスワード未設定時は「認証なし」ではなく「起動時にランダムパスワード生成」という既定動作になる。これは「未設定時は認証なしで動作する」という要件(Requirements Analysis時の明確化質問で確定)と矛盾するため、専用プロパティによる明示制御へ変更した。
- **パスワード照合**: `PasswordEncoderFactories.createDelegatingPasswordEncoder()`をベースに、`setDefaultPasswordEncoderForMatches`へ自前の`PlainTextPasswordEncoder`(`MessageDigest.isEqual`による定数時間比較、`lib`の`ApiKeyFilter`と同じ哲学)を設定。プレフィックス無しの設定値は平文比較、`{bcrypt}`プレフィックス付きの値はBCryptで照合される。非推奨の`NoOpPasswordEncoder`は使用していない。
- **`DaoAuthenticationProvider`のAPI差異**: 当初`DaoAuthenticationProvider(PasswordEncoder)`コンストラクタを想定していたが、このバージョンのSpring Securityには存在せず`DaoAuthenticationProvider(UserDetailsService)`+`setPasswordEncoder(...)`のセッターパターンへ修正した(コンパイルエラーにより発覚)。
- **認証適用範囲**: `authorizeHttpRequests(auth -> auth.anyRequest().authenticated())`によりwebconsole全体(SPA配信・`/testtool/**`含む全パス)に適用。セッションはSTATELESS、CSRFはdisable(Basic認証+GET主体のAPIのため)。

## 計画からの逸脱

- **単体テストのファイル分割**: 計画では`WebSecurityConfigTest.java`単一ファイルを想定していたが、「認証情報設定あり/なし」で別々の`@SpringBootTest`コンテキストが必要なため、`WebSecurityConfigAuthDisabledTest.java`/`WebSecurityConfigAuthEnabledTest.java`の2ファイルに分割した(1ファイルに複数のトップレベルクラスは書けないため)。
- **E2Eシナリオの起動パターン**: 計画では`webconsole-api-key-mismatch.spec.ts`と同様の`test.afterEach`(テストごとに都度起動・停止)パターンを想定していたが、3テストとも同一の起動パラメータ(同じusername/password)で足りるため、`test.beforeAll`/`test.afterAll`(スイート全体で1回だけ起動・停止)へ簡略化した。

## 確認結果

- `./gradlew build`(全モジュール): 成功。`WebSecurityConfigAuthDisabledTest`(1テスト)・`WebSecurityConfigAuthEnabledTest`(3テスト)含め全テスト成功
- `npx tsc --noEmit`(e2e/): 成功
- `npm run format:check`(e2e/): 成功(README.mdは`npm run format`で1回整形)
- `npm run test:e2e:no-key`: 14成功・1スキップ(新規`webconsole-basic-auth.spec.ts`3件含む)
- `npm run test:e2e:with-key`: 8成功・7スキップ(新規3件はno-keyパスのみのためスキップ、想定通り)
- curlによる手動確認: 認証ヘッダ無し→401、正しい認証情報(`-u`)→200
- 実ブラウザ(Claude in Chrome)でのBasic認証ダイアログ表示確認: ブラウザネイティブの認証ダイアログを自動化ツールが操作できず(スクリーンショット取得・URL埋め込み認証情報でのアクセスいずれも失敗)、目視確認は実施できなかった。curl・単体テスト・E2Eテストで機能面は十分に検証済みのため、これ以上のリトライは行わずここで打ち切った。
