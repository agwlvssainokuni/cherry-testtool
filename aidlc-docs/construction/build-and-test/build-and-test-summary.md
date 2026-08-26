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
- **Total Tests**: 64(lib 34、demo 3、client/webconsole 8、client/cli 19)。2026-08-09、FR9(スタブ実行時のトレースログ出力)・FR10(`/testtool/**` APIキー保護)追加分(lib +3、client/cli +4)、2026-08-17、FR13(webconsole Basic認証)追加分(client/webconsole +5)を含む
- **Passed**: 64
- **Failed**: 0
- **Coverage**: 未計測(カバレッジ計測ツールは本プロジェクトのスコープ外、NFR2「テスト」参照)
- **Status**: Pass

### Integration Tests
- **Test Scenarios**: 8(demo単体、webconsole→demoプロキシ、cli→demo直接呼出し、demo+webconsole+cli同時実行、`/testtool/**` APIキー保護、webconsole frontendのUIライブラリ移行(API連携)、同(実ブラウザ確認)、webconsoleのBasic認証)
- **Passed**: 8(Scenario 6.1の実ブラウザ確認で2件の不具合を発見・修正、詳細下記)
- **Failed**: 0
- **Status**: Pass(手動確認、詳細は`integration-test-instructions.md`)

### Performance Tests
- **Status**: N/A(要件定義時点でスコープ外と判断済み。詳細は`performance-test-instructions.md`)

### Additional Tests
- **Contract Tests**: N/A — マイクロサービス間のAPI契約を管理する構成ではなく(`webconsole`/`cli`は`lib`のREST APIを直接消費するのみ)、契約テストの追加価値が薄いため見送り
- **Security Tests**: N/A — Requirements Analysis時点でSecurity Baseline拡張は不採用(`aidlc-state.md`のExtension Configuration参照)
- **E2E Tests**: Pass — 2026-08-15、FR12により`e2e/`(独立npmプロジェクト、`@playwright/test`)を新設。cli直接呼出し・webconsole実ブラウザ操作・HTTPプロキシ層・スタブ効果・APIキー保護4パターン・起動時スタブ自動ロード・webconsole Basic認証(FR13)を自動検証(`no-key`: 15成功・1スキップ、`with-key`: 8成功・8スキップ)。GitHub Actionsで`push`/`pull_request`/手動実行時に自動実行

## Overall Status
- **Build**: Success
- **All Tests**: Pass(単体64件、結合8シナリオ、E2E自動テスト計16件)
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

## Build and Test再実行(FR9・FR10、正規フロー)

Post-Construction Maintenance(上記、アドホック対応)とは異なり、以下2件はユーザーの明示指定により正規のAI-DLCフロー(Requirements Analysis→Workflow Planning→Code Generation→Build and Test)で対応した。両者をまとめて本Build and Testステージの再実行対象とした。

- **FR9(スタブ実行時のトレースログ出力)**: `StubResolver`のスクリプト評価後、TRACEレベルで対象メソッド・スタブ設定・引数・評価結果(戻り値/例外)をまとめて1回ログ出力。実装過程でSLF4Jの「可変長引数末尾のThrowableはプレースホルダー置換されない」仕様に起因する不具合を発見・修正済み(詳細は`aidlc-state.md`のPost-Construction Change節、`stub-trace-log-summary.md`)
- **FR10(`/testtool/**` APIキー保護)**: `lib`(`ApiKeyFilter`、追加依存ゼロ)・`client/webconsole`(`GatewayRouteConfig`での自動付与)・`client/cli`(`RootCommand.effectiveHeaders()`)・`demo`(設定例)の4コンポーネントに専用ヘッダによるAPIキー検証を追加。`./gradlew clean build`(リポジトリ全体)で59テスト全て成功を確認した上で、以下を実機検証(詳細は`integration-test-instructions.md`のScenario 5):
  - `cherry.testtool.web.api-key`未設定時: `/testtool/**`は現状通りアクセス可能(後方互換)
  - 設定時: ヘッダ無し→401、不一致→401、一致→200。`/testtool/**`以外のパス(`/api/sample/**`)は無関係に200のまま
  - `client/webconsole`: SPA利用者(ブラウザ)はキー入力不要のままアクセス可能(webconsoleが鍵を内部保持し自動付与)
  - `client/cli`: APIキー未設定時は401エラー、`-Dcherry.testtool.web.api-key=...`(`application.yml`相当の設定)で設定時は正常にアクセス可能

いずれも想定通りの結果が得られ、既存機能への回帰も無いことを確認した。

## Build and Test再実行(FR11、正規フロー)

Post-Construction Change(Requirements Analysis→Workflow Planning→Code Generation→Build and Test)として対応した`client/webconsole/frontend`のUIライブラリ移行(MUI→make-you-chic-ui)のBuild and Test。

- `./gradlew --stop`後、`./gradlew clean build`(リポジトリ全体)を実行し、59テスト全てが成功することを確認(FR11はフロントエンドのみの変更のためJava側テスト件数に変化なし)
- `./gradlew :client:webconsole:build`により、npmInstall/npmBuild(`tsc -b && vite build`)を含むGradle経由のビルドが成功することを確認(単体`npm run lint`・`npm run build`のみでなく、実際のCIパイプラインが使うビルド経路での検証)
- `demo`(ポート8080)・`client:webconsole`(ポート9090)を実起動し、Integration Test Instructions Scenario 6を実施: ビルド成果物の配信内容(favicon/manifest/新JS-CSSバンドルの参照)、SPAフォールバック(`/invoker`・`/stubconfig`が200)、静的アセットのpublic配信(`/favicon.ico`・`/manifest.json`が200)、Invoker/Stubconfig画面が呼ぶ全APIエンドポイント(`resolve/bean`・`resolve/method`・`invoker/invoke`・`stubconfig/put`・`stubconfig/list`・`stubconfig/get`)をwebconsole経由で実行し、いずれも想定通りの応答(スタブ登録後の`invoke`がスタブ値を返すことも含む)を確認
- ユーザーがブラウザ拡張(Claude in Chrome)をインストール後、Integration Test Instructions Scenario 6.1として実ブラウザでの視覚的確認を実施。**この確認で2件の重大な不具合を発見・修正した**(詳細はrequirements.md FR11.12、`ui-library-migration-summary.md`):
  1. **画面が真っ白**: `vendor/make-you-chic-ui`(submodule)が自身のビルド用に保持する`node_modules/react`をVite側が誤って解決し、Reactが二重ロードされて`useState`が`null`を参照する`TypeError`が発生していた。`vite.config.ts`へ`resolve.dedupe: ["react", "react-dom"]`を追加して解消
  2. **CSSが一切適用されない**: make-you-chic-uiのビルド成果物はCSSをJSから分離した別ファイル(`dist/index.css`)として出力するため、`main.tsx`への`import 'make-you-chic-ui/style.css'`の明示的な追加が必要だった(当初のFR11.3の想定が誤りだった)
- 修正後、`./gradlew clean build`(59テスト)・`npm run build`の再実行成功に加え、実ブラウザでHome画面(Sidebar/Topbar/Card表示)・Cardクリックによる画面遷移・Invoker画面でのBean/メソッド自動解決と実行(結果`--- 30`)・Stubconfig画面での登録(結果`true`)・Topbarのテーマ切替(ダークモード反映)を全て確認した

`npm run build`(型チェック・バンドル)ではこれらの不具合を検出できず、実ブラウザでのレンダリング確認によって初めて発見できた。UIライブラリ移行のような変更では、ビルド成功だけでなく実機でのレンダリング確認が不可欠であることを示す事例となった。

## Build and Test再実行(FR12・FR13、正規フロー)

FR12(demo+クライアントのE2Eテスト追加)は、Code Generation完了時点(2026-08-15〜16)でのローカル動作確認(`./gradlew build`・`npm run test:e2e`)をもって実質的な検証は完了していたが、正式なBuild and Testステージの記録が漏れていたため、今回FR13と合わせてまとめて記録する。

- **FR12(demo+クライアントのE2Eテスト追加)**: `e2e/`(独立npmプロジェクト、`@playwright/test`)を新設。`cli.spec.ts`・`webconsole-ui.spec.ts`・`webconsole-api.spec.ts`・`webconsole-api-key-mismatch.spec.ts`・`demo-stub-auto-load.spec.ts`の5ファイルで、cli直接呼出し・webconsole実ブラウザ操作・HTTPプロキシ層・スタブ効果・APIキー保護4パターン・起動時スタブ自動ロードを検証。GitHub Actions(`.github/workflows/e2e.yml`)でCI化し、初回実行の失敗(submodule未取得・vendor dist未ビルド)を修正(FR12.1)。Prettier(FR12.2)導入。既に`358c633`までコミット済み・GitHub Actions上でも成功を確認済み
- **FR13(webconsoleへのBasic認証追加)**: `spring-boot-starter-security`を追加し、`WebSecurityConfig`/`WebAuthProperties`により`cherry.testtool.web.auth.users`(リスト、複数ユーザー対応)が1件以上設定されている場合のみwebconsole全体にBasic認証を有効化。未設定時は既存動作(認証なし)を維持。専用E2Eシナリオ`webconsole-basic-auth.spec.ts`(起動引数経由、2ユーザー登録)で自動検証。環境変数経由の設定(Spring Bootのrelaxed binding)はIntegration Test Instructions Scenario 7として手動確認

**確認結果**:
- `./gradlew clean build`(リポジトリ全体): 成功。単体テスト64件(lib 34・demo 3・cli 19・webconsole 8、`WebSecurityConfigAuthDisabledTest`/`WebSecurityConfigAuthEnabledTest`計5件を含む)全て成功
- `npx tsc --noEmit`(e2e/)・`npm run format:check`(e2e/): 成功
- `npm run test:e2e:no-key`: 15成功・1スキップ
- `npm run test:e2e:with-key`: 8成功・8スキップ(APIキー固有・Basic認証専用テストはno-keyパスのみのためスキップ、想定通り)
- Integration Test Instructions Scenario 7(webconsole Basic認証、環境変数経由): 認証ヘッダ無し→401、正しい認証情報→200、誤ったパスワード→401、いずれも想定通り
- 実ブラウザでのBasic認証ダイアログ目視確認は、Claude in Chromeがブラウザネイティブの認証ダイアログを操作できず未実施(curl・単体テスト・E2Eテストの3系統で機能面は検証済みと判断し打ち切り、詳細は`basic-auth-summary.md`)

いずれも想定通りの結果が得られ、既存機能への回帰も無いことを確認した。

## Next Steps
全4Unit(lib/demo/webconsole/cli)のビルド・単体テスト・結合的な動作確認、FR11(webconsole frontendのUIライブラリ移行)の実ブラウザでの視覚的確認、FR12(E2Eテスト基盤)・FR13(webconsole Basic認証)のBuild and Testが完了した。OPERATIONS PHASE(現在プレースホルダー)へ進む準備が整っている。
