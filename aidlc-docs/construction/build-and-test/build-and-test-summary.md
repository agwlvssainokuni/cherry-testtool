# Build and Test Summary

## Build Status
- **Build Tool**: Gradle(リポジトリ直下1つのWrapper、単一マルチプロジェクトビルド) + npm(`client/webconsole/frontend`のみ)
- **Build Status**: Success(全4サブプロジェクトで`BUILD SUCCESSFUL`)
- **Build Artifacts**:
  - `lib/build/libs/cherry-testtool-core.jar`(ライブラリ)
  - `demo/build/libs/cherry-testtool-demo.jar`(実行可能jar)
  - `client/webconsole/build/libs/cherry-testtool-webconsole.jar`(実行可能jar、SPA静的リソース同梱)
  - `client/cli/build/libs/cherry-testtool-cli.jar`(実行可能jar)
- **Build Time**: 数秒〜十数秒(初回のみnpm install等で追加時間)

## Test Execution Summary

### Unit Tests
- **Total Tests**: 52(lib 31、demo 3、client/webconsole 3、client/cli 15)
- **Passed**: 52
- **Failed**: 0
- **Coverage**: 未計測(カバレッジ計測ツールは本プロジェクトのスコープ外、NFR2「テスト」参照)
- **Status**: Pass

### Integration Tests
- **Test Scenarios**: 4(demo単体、webconsole→demoプロキシ、cli→demo直接呼出し、demo+webconsole+cli同時実行)
- **Passed**: 4
- **Failed**: 0
- **Status**: Pass(手動確認、詳細は`integration-test-instructions.md`)

### Performance Tests
- **Status**: N/A(要件定義時点でスコープ外と判断済み。詳細は`performance-test-instructions.md`)

### Additional Tests
- **Contract Tests**: N/A — マイクロサービス間のAPI契約を管理する構成ではなく(`webconsole`/`cli`は`lib`のREST APIを直接消費するのみ)、契約テストの追加価値が薄いため見送り
- **Security Tests**: N/A — Requirements Analysis時点でSecurity Baseline拡張は不採用(`aidlc-state.md`のExtension Configuration参照)
- **E2E Tests**: N/A(integration-test-instructionsに包含) — SPA/CLIからdemoまでの一連の操作は上記Integration Testsのシナリオ2-4で実質的にE2Eの役割を兼ねるため、別文書化は行わずintegration-test-instructions.mdへ統合した

## Overall Status
- **Build**: Success
- **All Tests**: Pass(単体52件、結合4シナリオ)
- **Ready for Operations**: Yes

## AI-DLCプロセスを通じて判明した主な技術的知見

- Spring Boot 4.1.0での破壊的変更: `@WebMvcTest`等のパッケージ移動(`spring-boot-webmvc-test`分離)、`spring.factories`の完全廃止(`AutoConfiguration.imports`形式のみ有効)
- `io.spring.dependency-management`のバージョン管理はプロジェクト単位の`resolution strategy`であり、Gradle複合ビルド(`includeBuild`)を跨いで伝播しないだけでなく、**真のGradleマルチプロジェクト内のproject依存(`project(":lib")`等)を跨いでも伝播しない**(2026-08-09、マルチプロジェクト化後に再確認して判明)。複数モジュールから参照されうる依存のバージョンは、ルートの`build.gradle.kts`で`subprojects { plugins.withId(...) { configure<DependencyManagementExtension> { dependencies { dependency(...) } } } }`のように一元管理する必要がある
- Spring Cloud Gateway Server MVC(`spring-cloud-gateway-server-webmvc`)には旧WebFlux版の`SecureHeaders`フィルタ関数が存在しない
- `@Bean`メソッドで明示登録するクラスに対する`@ConditionalOnWebApplication`等のクラスレベル`@Conditional`アノテーションは、`@Bean`メソッド側に付与しないと評価されない(`TesttoolController`の登録漏れバグの根本原因)
- Spring `RestClient`はクラスパス上にJacksonが無いとJSON用`HttpMessageConverter`を自動登録しない(`spring-boot-starter-web`に依存しないアプリでは明示的に`spring-boot-starter-json`等を追加する必要がある)
- Gradle複合ビルド(`includeBuild`)は、あるプロジェクトが「単独リンクされたGradleプロジェクト」と「他プロジェクトのincludeBuild先」の両方としてIntelliJ IDEAに認識されると、ビルドスクリプト解析モデルが競合し偽陽性のエラーが表示されることがある(キャッシュ再構築でも解消しない構造的制約)。単一`settings.gradle.kts`配下の真のマルチプロジェクトへ統合することで構造的に解消した(Build and Test完了後、2026-08-09に実施。詳細は`lib-unit-summary.md`参照)
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`経由で読み込む自動構成クラスは、`@Configuration`ではなくSpring Boot 2.7以降の規約である`@AutoConfiguration`を用い、クラス名もSpring Boot標準命名(`XxxAutoConfiguration`)に揃えるべき(`TesttoolConfiguration`→`TesttoolAutoConfiguration`。Build and Test完了後、2026-08-09に対応。詳細は`aidlc-state.md`のPost-Construction Maintenance参照)
- `Class#getDeclaredMethods()`が返すメソッドの順序はJVM仕様上保証されておらず、実行毎に変動しうる。`ReflectionResolver.resolveMethod`はオーバーロード解決を`methodIndex`による位置指定に依存する設計だったため、この非決定性が`StubAutoLoadRunnerTest`の稀な失敗(フレーク)として顕在化した。パラメータ型名によるソートを追加し順序を決定的にすることで解消(Build and Test完了後、2026-08-09に対応。詳細は`aidlc-state.md`のPost-Construction Maintenance参照)

これらはいずれも実装前後の実機検証(jarの`javap`確認、実際のHTTPサーバに対する手動結合確認)によって発見・解決した。「未検証のAPI仮定でコードを書かない」という方針を全Unitで一貫して適用した結果である。

## Build and Test完了後の追加対応

Build and Test完了(2026-08-08T22:34:00Z)後、正規のAI-DLCステージを経由しないアドホックな保守依頼として以下を実施した。技術的な知見を伴うもの(自動構成クラスの規約対応、リフレクション順序のフレーク修正)は上記「AI-DLCプロセスを通じて判明した主な技術的知見」にも記載済み。詳細な経緯は`aidlc-state.md`のPost-Construction Maintenance節および`audit.md`(各エントリに「事後記録」と付記)を参照。

- GraalVM JavaScriptエンジン(`org.graalvm.js:js`/`js-scriptengine`)を`25.1.3`→`25.2.4`へ更新(2026-08-09)
- `TesttoolConfiguration`を`TesttoolAutoConfiguration`へ改名し`@AutoConfiguration`化(2026-08-09)
- `ReflectionResolver.resolveMethod`のオーバーロード解決順序フレークを修正(2026-08-09)
- `CLAUDE.local.md`を削除(記載事項は各README.mdへ反映済みであることを確認の上で実施。2026-08-09)
- 重複していた`.gitignore`(`demo`/`client:cli`/`client:webconsole`)を削除(2026-08-09)

## Next Steps
全4Unit(lib/demo/webconsole/cli)のビルド・単体テスト・結合的な動作確認が完了した。OPERATIONS PHASE(現在プレースホルダー)へ進む準備が整っている。
