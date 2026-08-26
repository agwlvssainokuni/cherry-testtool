# Requirements Document

## Intent Analysis Summary
- **User Request**: cherry-testtoolの既存機能の改善・リファクタリング。具体的には (1) `client/gateway`のビルド構成是正、(2) `client/spa`と`client/gateway`の統合(新モジュール`client/webconsole`)、(3) `InvokerServiceImpl`の意図的な例外処理へのコメント補足、(4) `lib`内のInterface/Impl分離の解消、(5) `client/cli`のSpring Bootアプリ化、(6) `lib`を組み込むデモアプリの新設
- **Request Type**: Refactoring + Enhancement + Migration(複合)
- **Scope Estimate**: System-wide(既存4モジュール中3モジュールに変更が及び、新規2モジュールを追加)
- **Complexity Estimate**: Complex

## Functional Requirements

### FR1: client/gatewayのビルド構成是正
`client/gateway`(統合後は`client/webconsole`)に`settings.gradle`を追加し、`lib`と同様に`rootProject.name`を明示する。

**(2026-08-09追記、レビュー時にユーザー指示)**: 本項および後述FR2.6/FR5.4/FR6.4が前提としていた「各モジュールが独立した`rootProject.name`を持つ」構成は、IntelliJ IDEAでのビルドスクリプト解析競合(`demo`の`includeBuild`と`lib`の単独リンクが共存する状態が原因)を受け、単一`settings.gradle.kts`配下のGradleマルチプロジェクトへ変更した。詳細はArchitectural Considerationsの表、および`lib-unit-summary.md`「Gradleマルチプロジェクト化」を参照。

### FR2: client/spaとclient/gatewayの統合(client/webconsole)
`client/spa`と`client/gateway`を新規モジュール`client/webconsole`へ統合する。

- **FR2.1**: `client/webconsole`はSpring MVC(Servlet)ベースのSpring Bootアプリケーションとする(現行`client/gateway`のWebFlux版から変更)。
- **FR2.2**: APIプロキシ機能はSpring Cloud Gatewayのサーブレット版(`spring-cloud-starter-gateway-server-webmvc`)を用いて実現する。プロキシ対象は`/testtool/**`のみに限定する(現行`client/gateway`の`/**`から変更。静的リソース配信・SPAフォールバックとの競合を避けるため)。ルート定義はJava(Functional Route、`GatewayRouterFunctions`+`RequestPredicates`+`HandlerFunctions.http(...)`)で記述する。既存`client/gateway`のセキュリティヘッダ付与・レスポンスヘッダ重複排除の設定内容は踏襲するが、CORS設定はFR2.6により不要となるため踏襲しない。
- **FR2.3**: SPAのビルド成果物(静的ファイル)は、Spring Bootの標準的な静的リソース配信の仕組みで配信する。
- **FR2.4**: `client/webconsole`ディレクトリ配下に、Spring Boot本体のJavaプロジェクトと、フロントエンド(旧`client/spa`相当のReact/TypeScript/Viteプロジェクト)を同居させる。ビルド時にフロントエンドをビルドし、その成果物をJava側の静的リソースとして組み込む(具体的な組込み手順はApplication Design/Functional Designで確定する)。
- **FR2.5**: 既存の`client/gateway`・`client/spa`ディレクトリは、`client/webconsole`へ統合完了後に廃止する。
- **FR2.6**: `client/webconsole`の`settings.gradle`で`rootProject.name`を`cherry-testtool-webconsole`に設定する。**(2026-08-09追記)** マルチプロジェクト化により、Gradleパス`:client:webconsole`・成果物名(`base.archivesName`)`cherry-testtool-webconsole`へ読み替え(FR1追記参照)。
- **FR2.7**: `client/webconsole`の待受ポートは`9090`とする(現行`client/gateway`の`8070`から変更)。
- **FR2.8**: 静的リソース配信において、リクエストパスが既存の静的ファイルにもAPIプロキシ(`/testtool/**`)にも一致しない場合は`index.html`を返すSPAフォールバックルーティングを実装する(React Routerのクライアントサイドルーティング、`/invoker`・`/stubconfig`への直接アクセス・リロードに対応するため)。
- **FR2.9**: フロントエンド(開発時、`npm run dev`)は、Viteの開発サーバープロキシ機能(`server.proxy`)を用いて`/testtool`宛リクエストを`client/webconsole`(既定`:9090`)へサーバー間プロキシする。これによりブラウザからは常に同一オリジン通信となりCORS設定が不要になる(本番はwebconsoleがSPA・APIを同一オリジンで配信するため、同様にCORS不要)。`common.ts`の絶対URL解決(`VITE_TESTTOOL_ROOT`)は相対パスベースに簡素化する。

### FR3: 意図的な例外処理へのコメント補足
`InvokerServiceImpl`(統合後は具象クラス`InvokerService`、FR4参照)の`invoke(beanName, className, methodName, methodIndex, script, engine)`における`catch (Exception ex)`による例外の一括捕捉は、テストツールとして想定外の例外も含めて実行結果として表示するための意図的な仕様である。その意図が分かるコメントを追加する。**挙動は変更しない。**

### FR4: lib内のInterface/Impl分離の解消
`lib`モジュール内の以下5組について、インタフェースを削除し、実装クラスをインタフェース名(`Impl`サフィックスなし)の具象クラスへリネームする。

| 現状(インタフェース/実装) | 変更後(具象クラス) |
|---|---|
| `InvokerService` / `InvokerServiceImpl` | `InvokerService` |
| `ReflectionResolver` / `ReflectionResolverImpl` | `ReflectionResolver` |
| `ScriptProcessor` / `ScriptProcessorImpl` | `ScriptProcessor` |
| `StubRepository` / `StubRepositoryImpl` | `StubRepository` |
| `StubResolver` / `StubResolverImpl` | `StubResolver` |

- `StubResolver`・`ReflectionResolver`インタフェースが持つdefaultメソッド(オーバーロード委譲)は、具象クラスの通常メソッドとして維持する。
- `TesttoolConfiguration`のBean定義、および呼出し側(Controller等)の型参照は全て具象クラス名に更新する。

### FR5: client/cliのSpring Bootアプリ化
`client/cli`のシェルスクリプト(`invoker.sh`/`stubconfig.sh`)をSpring Bootアプリケーション(CLI)へ置き換える。

- **FR5.1**: 既存シェルスクリプトと同等以上の機能(指定ディレクトリ配下のスクリプトファイルを走査してのメソッド呼出し一括実行、スタブの登録/参照/解除の一括実行、BASIC認証・追加HTTPヘッダの指定)を提供する。
- **FR5.2**: コマンドラインのオプション体系は既存シェルスクリプトを踏襲する必要はなく、Spring Bootアプリケーションとして適切な形式に刷新してよい。
- **FR5.3**: 実行可能jar(`java -jar`)として配布・実行できる形式にする。
- **FR5.4**: `settings.gradle`で`rootProject.name`を`cherry-testtool-cli`に設定する。**(2026-08-09追記)** マルチプロジェクト化により、Gradleパス`:client:cli`・成果物名(`base.archivesName`)`cherry-testtool-cli`へ読み替え(FR1追記参照)。
- **FR5.5**: Picocli(`picocli-spring-boot-starter`)を用いて実装する。`invoker.sh`/`stubconfig.sh`相当の機能をそれぞれ`invoke`サブコマンド・`stubconfig`サブコマンド(`register`/`clear`/`show`モード)としてマッピングし、`CommandLineRunner`からPicocliの`CommandLine`を実行する構成とする。
- **FR5.6**: 異常終了時に終了コードで判別できるよう、`CommandLineRunner`を実装するクラスに`ExitCodeGenerator`も併せて実装する(`run()`実行時にPicocliの実行結果の終了コードをフィールドへ保持し、`getExitCode()`で返す)。`main()`側で`SpringApplication.exit(context)`により当該終了コードを取得し、`System.exit(...)`に反映する。

### FR6: libを組み込むデモアプリの新設
`lib`を組み込むデモアプリケーションを新規モジュールとして追加する。

- **FR6.1**: `lib`を依存追加した最小構成のSpring Bootアプリケーション(既定ポート8080)とする。配置場所はリポジトリ直下の`demo`ディレクトリとし、`lib`と同じ階層に置く(`client/`配下ではない)。
- **FR6.2**: `lib/src/test`に現在配置されている検証用フィクスチャ(`ToolTester`/`ToolTesterImpl`/`StubAspect`/`TestMain`等)をデモアプリへ移管してよい。全面移管・部分移管(libの単体テストに必要な最小限を残す等)は実装時の判断に委ねる。
- **FR6.3**: デモアプリは、`client/webconsole`のプロキシ先、および`client/cli`の呼出し先として、一連の動作確認に用いる。
- **FR6.4**: `settings.gradle`で`rootProject.name`を`cherry-testtool-demo`に設定する。**(2026-08-09追記)** マルチプロジェクト化により、Gradleパス`:demo`・成果物名(`base.archivesName`)`cherry-testtool-demo`へ読み替え、`lib`への依存はGradle複合ビルド(`includeBuild`)からプロジェクト依存(`project(":lib")`)へ変更(FR1追記参照)。

### FR7: コードコメントの充実
既存コード全般でコメント(特にJavadoc等のドキュメンテーションコメント)が不足しているため、本サイクルで変更・新規作成するコードを中心に、積極的にコメントを追記する。対象はFR1-FR6で変更が及ぶ`lib`・`client/webconsole`・`client/cli`・デモアプリの全コードとし、既存の`lib`コードのうち今回の変更対象に含まれるクラス群についても、コメント不足の解消を合わせて行う。

- クラス/メソッドレベルではJavadocを付与し、責務・引数・戻り値・例外の意味を説明する
- 実装の意図が非自明な箇所には、その理由(WHY)を説明するコメントを付与する

### FR8: lib Controllerの統合
`InvokerController`と`StubConfigController`は、Bean名解決(`bean`)・メソッド解決(`method`)のエンドポイント実装が実質的に重複しているため、1つのController(`TesttoolController`)へ統合する。

- **FR8.1**: `invoke`(`/testtool/invoker/invoke`)、`put`/`get`/`list`(`/testtool/stubconfig/put`,`get`,`list`)は現行のURLパスのまま`TesttoolController`内のメソッドとして維持する。
- **FR8.2**: `bean`/`method`解決エンドポイントは、新設の共通パス(`/testtool/resolve/bean`、`/testtool/resolve/method`)へ一本化する。現行の`/testtool/invoker/bean`,`method`、`/testtool/stubconfig/bean`,`method`は廃止する。
- **FR8.3**: 現行`cherry.testtool.web.invoker`/`cherry.testtool.web.stubconfig`という2つの独立した`@ConditionalOnProperty`トグルを、単一のトグル(例: `cherry.testtool.web.enabled`)へ統合する。既定値は現行同様に有効(`matchIfMissing=true`相当)とする。
- **FR8.4**: 本統合に伴い、`client/webconsole`のフロントエンド(`invoker/api.ts`、`stubconfig/api.ts`)の`resolveBeanName`/`resolveMethod`呼出し先を新パス(`/testtool/resolve/bean`,`method`)へ追随修正する(NFR1)。
- **FR8.5**(Unit 3着手時の追加、実装上の制約): `TesttoolController`は`cherry.testtool.web`パッケージに属し、利用側アプリのコンポーネントスキャン範囲外となるため、`@RestController`の付与だけでは登録されない。`TesttoolConfiguration`(他5Beanと同様の明示的`@Bean`登録方式)に`testtoolController`用の`@Bean`メソッドを追加し、`@ConditionalOnWebApplication`/`@ConditionalOnProperty`もそのメソッド側で評価する構成とする。

### FR9: スタブ実行時のトレースログ出力(2026-08-09追記、Post-Construction Maintenance時の新規改修依頼)
`StubResolver.getStubInvocation(Method)`が返す`StubInvocation`の実行(スクリプト評価)時に、TRACEレベルでスタブの内容をログ出力する。デバッグ目的(どのメソッドに、どのスタブ設定が、どの引数で適用され、何を返した/どんな例外を投げたか)であり、TRACEレベル(既定では出力されず能動的に有効化する想定)のため情報を出し惜しみしない方針とする(確認質問Q1回答: D)。

- **FR9.1**: ログ出力箇所は`StubResolver.getStubInvocation(Method)`(スタブ設定を保持するラムダ、`StubInvocation.invoke`実装)とする。SLF4J(`StubConfigLoader`と同様の`private final Logger logger = LoggerFactory.getLogger(getClass());`パターン)を用いる。
- **FR9.2**: ログレベルはTRACE。
- **FR9.3**: ログに含める情報は、対象メソッド(クラス名・メソッド名)・スタブ設定(script・engine)・呼出し引数(args)・スクリプト評価結果(戻り値、または例外発生時はその内容)とする。
- **FR9.4**: ログ出力タイミングはスクリプト評価後にまとめて1回とする(評価前の情報+評価結果を1行にまとめる。前後2回に分けると並行実行時に他のログと混ざり対応が取りにくくなるため。確認質問Q2回答: C)。例外発生時も同一箇所で結果に代えて例外情報を含めて出力してから、既存の例外変換(`ScriptException`のcause再throw)処理へ進む。

### FR10: `/testtool/**` APIキーによる保護(2026-08-09追記、Post-Construction Maintenance時の新規改修依頼)
`lib`が提供する`/testtool/**`(`TesttoolController`)は現状、認証・認可の仕組みが一切無く素通しである。ユーザーとの相談を通じて、OAuth2/OIDCのような大掛かりな仕組みは採用しない一方、最低限のアクセス防止策として専用ヘッダによるAPIキー方式を導入する方針に収束した。

**設計方針(相談時に確定)**:
- 重量級の認証機構(`spring-boot-starter-security`等)は`lib`へ追加しない。`lib`は消費側アプリへ組み込む前提のライブラリであり、消費側アプリが既に持つSpring Security構成等と衝突するリスクがあるため。追加依存ゼロの自前`jakarta.servlet.Filter`で実装する。
- 標準の`Authorization`ヘッダは使わない。消費側アプリ自体の認証(Basic/Bearer等)や手前のリバースプロキシ/API Gatewayが同じヘッダ名を別の用途で使っている場合の名前空間衝突を避けるため、専用ヘッダ名を新設する。
- 未設定(APIキー用プロパティが空/未指定)の場合は、現状通り検証をスキップする(既定動作を破壊しない後方互換)。

- **FR10.1**: `lib`(`TesttoolAutoConfiguration`)に、`/testtool/**`宛リクエストのヘッダ検証を行う`jakarta.servlet.Filter`実装(例: `ApiKeyFilter`)を追加する。`cherry.testtool.web.api-key`プロパティが設定されている場合のみ、Beanとして登録し検証を有効化する(`@ConditionalOnProperty`、未設定時はFilter自体を登録しない)。ヘッダ値が一致しない、またはヘッダが無い場合は`401 Unauthorized`を返す。
  - **登録方式**: `FilterRegistrationBean<ApiKeyFilter>`を`@Bean`として返し、`.addUrlPatterns("/testtool/*")`(Servlet API本来のワイルドカード構文。単純な`Filter`型Beanのまま返すと既定URL patternが`/*`となり、消費側アプリの全リクエストが対象になってしまうため明示指定する)を設定する。`FilterRegistrationBean`もSpring Bootの`ServletContextInitializerBeans`により自動検出・登録されるため、`TesttoolController`の`@Bean`登録と同様、消費側アプリでの追加のFilter登録設定(`web.xml`相当の記述等)は一切不要。
- **FR10.2**: 検証対象ヘッダ名は`cherry.testtool.web.api-key-header`プロパティで変更可能とし、既定値は`X-Cherry-Testtool-Api-Key`とする。
- **FR10.3**: プロパティ名(`cherry.testtool.web.api-key`・`cherry.testtool.web.api-key-header`)は、`lib`・`client/webconsole`・`client/cli`の3コンポーネント全てで統一する(確認質問Q2回答: C)。同じキー・同じヘッダ名を各コンポーネントの`application.yml`で共有し、把握・設定すべき項目を1系統に揃える。
- **FR10.4**: `client/webconsole`(`GatewayRouteConfig`)は、`cherry.testtool.web.api-key`が設定されている場合、backendへプロキシするリクエストへ自動的に該当ヘッダを付与する。SPA利用者(ブラウザ)には別途キー入力を求めない。`webconsole`は鍵を内部保持する「信頼されたクライアント」として振る舞い、直接`/testtool/**`を叩く経路のみを保護する最小スコープとする(確認質問Q1回答: A)。
- **FR10.5**: `client/cli`(`RootCommand`・`RequestHeaderBuilder`)は、`cherry.testtool.web.api-key`が設定されている場合、リクエストへ既定で該当ヘッダを付与する。既存の`--header`オプションによる個別指定は引き続き利用可能とする(都度指定にも対応しつつ、設定ファイルによる既定値付与を主とする)。
- **FR10.6**: `demo`アプリでの動作確認用に、`demo/application.yml`へ`cherry.testtool.web.api-key`の設定例(コメントアウト等、既定は無効)を用意する。

### FR11: webconsole frontendのUIライブラリ移行(make-you-chic-uiへの切替)(2026-08-14追記、Post-Construction Change時の新規改修依頼)
`client/webconsole/frontend`が現在使用しているUIライブラリ`MUI`(`@mui/material`・`@emotion/styled`)を、自作のデザインシステム`make-you-chic-ui`(git submodule: `client/webconsole/frontend/vendor/make-you-chic-ui`、npm依存として`file:vendor/make-you-chic-ui/packages/make-you-chic-ui`参照)へ全面的に切り替える。対象は既存の3画面(`Home.tsx`・`invoker/App.tsx`・`stubconfig/App.tsx`)。

**設計方針(確認質問回答により確定、全問A回答)**:
- MUI依存(`@mui/material`・`@emotion/styled`)は完全に置き換え、package.jsonから削除する(確認質問Q1回答: A)。
- 現状、`Home`/`Invoker`/`Stubconfig`の3画面間はナビゲーションリンクが無くURL直接指定でのみ遷移可能という弱点があるため、make-you-chic-uiの`AppShell`(Sidebar付き全画面レイアウト)を導入し、3画面を行き来できるナビゲーションを新設する(確認質問Q2回答: A)。
- make-you-chic-uiの推奨セットアップである`ThemeProvider`・`ToastProvider`・`ModalStackProvider`をアプリケーションルートに配置する(確認質問Q3回答: A)。
- Webフォント(`@fontsource/noto-sans-jp`・`@fontsource/noto-serif-jp`、`japanese`サブセットのみ)を利用側の依存として追加する(確認質問Q4回答: A)。
- Invoker/Stubconfig画面の「実行結果」欄は表示方法を変えず、部品のみ`TextField`(multiline)→`Textarea`へ置換する。Alert/Toastによるエラー通知への刷新は今回のスコープ外とする(確認質問Q5回答: A)。
- 画面固有のレイアウトCSS(Grid相当の配置等)は、make-you-chic-uiが提供する`.claude/skills/layout-css/SKILL.md`(`client/webconsole/frontend/.claude/skills/layout-css/`へコピー済み)の方針に従う。汎用レイアウトコンポーネントやユーティリティクラスの乱用ではなく、コンポーネントごとに意味づけされた少数のCSSクラスを都度定義し、余白・角丸はmake-you-chic-uiのトークン(`var(--space-*)`・`var(--radius-*)`)を参照する。
- `src`配下のディレクトリ構成は、画面(ページ)単位でコンポーネント・CSSをまとめる「コロケーション方式」を基本としつつ、API呼び出しモジュールは`src/api/`へ集約する(2026-08-14追記、レビュー時の追加依頼。ディレクトリ構成確認質問Q1回答: A、ただしCode Generationレビュー時に「APIは`api/`ディレクトリに集約」との追加依頼を受けAPI部分のみ修正)。目標構成は以下の通り(FR11.10参照)。

```
src/
  main.tsx
  App.tsx                   (ルーティング定義のみ)
  vite-env.d.ts
  lib/
    common.ts                (旧common.ts)
  layouts/
    AppShellLayout.tsx        (FR11.5のレイアウトルート)
  api/
    resolve.ts                 (resolveBeanName/resolveMethod、invoker・stubconfig共通)
    invoker.ts                (旧invoker/api.ts)
    stubconfig.ts              (旧stubconfig/api.ts)
  pages/
    Home/
      HomePage.tsx
      HomePage.css
    Invoker/
      InvokerPage.tsx
      InvokerPage.css
    Stubconfig/
      StubconfigPage.tsx
      StubconfigPage.css
public/
  favicon.ico
  favicon.xcf
  logo.svg
  logo.xcf
  logo192.png
  logo512.png
  manifest.json
```

- **FR11.1**: git submodule(`client/webconsole/frontend/vendor/make-you-chic-ui`)の追加、および`package.json`への依存追加(`"make-you-chic-ui": "file:vendor/make-you-chic-ui/packages/make-you-chic-ui"`)。(Requirements Analysis開始前に準備済み)
- **FR11.2**: `package.json`から`@mui/material`・`@emotion/styled`を削除する。
- **FR11.3**: アプリケーションルート(`main.tsx`)に`ThemeProvider`・`ToastProvider`・`ModalStackProvider`を配置し、make-you-chic-uiのグローバルスタイル(`theme/tokens.css`・`theme/semantic.css`、コンポーネント毎のCSS)が反映されることを確認する。ビルド成果物(`dist/index.js`)はCSSをJSから分離した別ファイル(`dist/index.css`)として出力し、ソース上の`import './theme/tokens.css'`等の副作用importはビルド後のJSに残らないため、`main.tsx`で`import 'make-you-chic-ui/style.css'`(`package.json`の`exports`で公開されたサブパス)を明示的にimportする(2026-08-14追記、Build and Test時に発見・修正。FR11.12参照)。
- **FR11.4**: `@fontsource/noto-sans-jp`・`@fontsource/noto-serif-jp`を依存に追加し、`main.tsx`で`japanese-400/500/600/700`のCSSをimportする。
- **FR11.5**: `AppShell`を導入し、`Home`・`Invoker`(`/invoker`)・`Stubconfig`(`/stubconfig`)を切り替えられるSidebarナビゲーションを新設する。react-routerの「レイアウトルート」パターンで`src/layouts/AppShellLayout.tsx`を新設して`App.tsx`に追加し、既存3ルートをその子ルートとする。
- **FR11.5.1**: AppShellのTopbarへテーマ選択UIを配置する(2026-08-14追記、レビュー時の追加依頼)。make-you-chic-ui本体に、Topbarへ任意コンテンツを差し込める拡張ポイント`topbarStart`/`topbarEnd`(`AppShellProps`)が新設された(ユーザーによりsubmodule側で直接実装・コミット済み、FR11.9参照)ため、`topbarEnd`(右寄せ、ユーザーメニュー手前の領域。本アプリに`user`は無いため実質Topbar右端)へ配置する。当初はテーマ「まとめボタン+Dropdown」方式(1個の`Button`+`Dropdown`で4軸を1メニューにまとめる)で実装したが、レビュー時の追加依頼(2026-08-14)により以下の方式へ変更した: 4軸を個別の項目として左から「ダーク/ライト」「文字サイズ」「フォントファミリ」「ブランド」の順に並べる。
  - **ダーク/ライト**: `Switch`(既定`light`、オンで`dark`)。ラベル「ダーク」
  - **文字サイズ**: `RadioGroup`(小/中/大、`value`は`sm`/`md`/`lg`)。横並びにするため`layout-css` Skill方針に従い画面固有CSSクラス(`.theme-controls-radio-row`)で`flex-direction: row`を上書き
  - **フォントファミリ**: `Select`(ゴシック/明朝、`value`は`sans`/`serif`)
  - **ブランド**: `Select`(青/緑/紫/橙の日本語ラベル、`value`は`blue`/`green`/`purple`/`orange`)
  - 各項目の間に区切りの縦線を配置する(2026-08-14追記、レビュー時の追加依頼)。`.theme-controls-item`(先頭以外)へ`border-left`+左右`padding`(`var(--color-border)`・`var(--space-4)`)を付与し、外枠`.theme-controls`側の`gap`は廃止(paddingと二重にならないよう)。
  - 区切り線の位置ずれ・高さ不揃いを修正する(2026-08-14追記、レビュー時の追加指摘)。当初`padding-left`のみだったため線が項目間中央からずれていた点は、全項目に左右均等の`padding: 0 var(--space-4)`を付与して解消。線の高さがTopbar全体でなく各項目の内容物の高さになっていた点は、`.theme-controls`へ`align-self: stretch`(Topbarの高さいっぱいに広げる)・`align-items: stretch`(子の`.theme-controls-item`もその高さに広げる)を指定し、各項目は内部で`align-items: center`により中身を垂直中央寄せする形で解消。
- **FR11.9**: make-you-chic-ui submoduleを、FR11.5.1で必要な`topbarStart`/`topbarEnd`拡張を含むコミット(`origin/main`、ユーザーが直接実装・push済み)までfast-forward更新し、`npm run build`でdistを再ビルドする。(Requirements Analysis中に実施済み)
- **FR11.6**: `Home.tsx`(移行後: `src/pages/Home/HomePage.tsx`)の`Container`・`Typography`をmake-you-chic-ui側の同等表現へ置換する(`Container`相当のレイアウトはlayout-css Skill方針に従い画面固有のCSSクラス`HomePage.css`で実装、見出しは`Typography`が無いため素のHTML要素+CSSクラスで表現)。あわせて、各機能(Invoker「呼出しツール」・Stubconfig「スタブ設定ツール」)ごとに説明文を記述した`Card`を配置し、各Cardをその機能画面(`/invoker`・`/stubconfig`)への遷移リンクとしても機能させる(react-router-domの`Link`で`Card`をラップし、Card全体をクリック可能にする。2026-08-14追記、レビュー時の追加依頼)。
- **FR11.6.1**: Cardのタイトルは各画面のタイトル(`呼出しツール`・`スタブ設定ツール`)、説明文は各画面の機能概要(1〜2文程度)とする。CardのレイアウトはFR11.7と同様、layout-css Skill方針に従った画面固有CSSクラス(flex/grid直書き)で複数Cardを並べる。
- **FR11.7**: `invoker/App.tsx`(移行後: `src/pages/Invoker/InvokerPage.tsx`)・`stubconfig/App.tsx`(移行後: `src/pages/Stubconfig/StubconfigPage.tsx`)の`Grid`ベースのフォームレイアウトを、layout-css Skillの方針に従った画面固有CSSクラス(`InvokerPage.css`・`StubconfigPage.css`にflex/grid直書き)へ置換し、単一行の`TextField`→`TextInput`、複数行(`multiline`)の`TextField`(スクリプト入力欄・実行結果欄の2箇所×2画面)→`Textarea`、`Select`+`MenuItem`→`Select`+`SelectOption`、`InputLabel`→`FormField`の`label`、`Button`→`Button`へ置換する。各画面の`api.ts`は`src/api/invoker.ts`・`src/api/stubconfig.ts`へ移動する(2026-08-14追記、Code Generationレビュー時の追加依頼によりAPI集約方針へ変更。FR11.10参照)。両ファイルに重複していた`resolveBeanName`/`resolveMethod`は`src/api/resolve.ts`へ切り出す(2026-08-14追記、レビュー時の追加指摘)。`invoker.ts`/`stubconfig.ts`は再exportせず、`InvokerPage.tsx`/`StubconfigPage.tsx`が`resolve.ts`から直接importする(2026-08-14追記、レビュー時の追加指摘。不要な中継exportを避ける)。
- **FR11.7.1**: スクリプト入力欄・実行結果欄の`Textarea`(計4箇所)には、コード/レスポンス表示に適した等幅フォントを適用する(2026-08-14追記、レビュー時の追加依頼)。make-you-chic-uiのテーマ軸には等幅フォントの選択肢が無いため、新規Web等幅フォントの追加(依存追加)はせず、OS標準の等幅フォントスタック(`ui-monospace, SFMono-Regular, 'SF Mono', Consolas, 'Liberation Mono', Menlo, monospace`)をlayout-css Skill方針に従った画面固有CSSクラスで指定する(`InvokerPage.css`・`StubconfigPage.css`)。**この「Web等幅フォントは追加しない」判断は、後日FR11.20により見直された(現有のNoto Sans/Serif JP Webフォント資産を踏まえ、追加コストが軽微なNoto Sans Monoのlatinサブセットのみを追加する方針へ変更)。**
- **FR11.8**: `client/webconsole/frontend/vendor/make-you-chic-ui/.claude/skills/layout-css/`を`client/webconsole/frontend/.claude/skills/layout-css/`へコピーする。(Requirements Analysis中に準備済み)
- **FR11.10**: `src`配下をコロケーション方式のディレクトリ構成へ再編する(2026-08-14追記、レビュー時の追加依頼。ディレクトリ構成確認質問Q1回答: A)。`common.ts`は`src/lib/common.ts`へ移動。`App.tsx`はルーティング定義のみを残す(FR11.5のレイアウトルート含む)。各画面の`api.ts`は`src/api/`へ集約する(2026-08-14追記、Code Generationレビュー時の追加依頼)。この再編はFR11.5〜FR11.7の実装と一体で行う(先に置換してから再配置、または再配置してから置換のどちらでもよいが、最終形は上記ディレクトリツリーに一致させる)。
- **FR11.11**: `src/assets/`配下の静的ファイル(`favicon.ico`・`logo.svg`・`logo.xcf`・`logo192.png`・`logo512.png`・`manifest.json`)を全て`public/`へ移動する(ディレクトリ構成確認質問Q2回答: A、未参照ファイルも含めて移動しVite標準の静的アセット配置に揃える)。`index.html`の参照パスを`/src/assets/...`から`/...`(public直下の絶対パス)へ更新し、あわせて`<link rel="manifest" href="/manifest.json"/>`を追加して`manifest.json`を実際に参照される状態にする(ユーザーからの追加指定)。
- **FR11.12**: Build and Test時に実ブラウザで発見した3件の不具合を修正する(2026-08-14追記)。
  - **CSS未適用**: FR11.3参照。`main.tsx`に`import 'make-you-chic-ui/style.css'`を追加。
  - **Reactフックエラー(白画面)**: `vendor/make-you-chic-ui`が自身のビルド・テスト用に`node_modules/react`を保持しており、submoduleをsymlink経由でfile:参照する本アプリのVite解決が、frontend自身の`node_modules/react`ではなく`vendor/make-you-chic-ui/node_modules/react`を拾ってしまい、Reactが二重にロードされて`useState`が`null`を参照する`TypeError`が発生し画面が真っ白になっていた(`integration-guide.md`が事前に警告していた既知のリスクが顕在化)。`vite.config.ts`の`resolve.dedupe: ["react", "react-dom"]`で、常にfrontend直下の単一のReactインスタンスへ解決するよう修正。ユーザーがmake-you-chic-ui本体(submodule)の`docs/integration-guide.md`へこの回避策と、CSS importパスの誤り(`/dist/index.css`ではなく`package.json`の`exports`で公開された`/style.css`が正しい)を追記・push済み(commit `2e5da1f`まで取り込み済み、コードへの影響なし)。
  - **`npm run dev`が起動時にクラッシュ(react/jsx-runtime起因)**: 上記2件を修正しビルド(`npm run build`)・実ブラウザ確認が完了した後、`npm run dev`(開発サーバ)を試したところ`Error: Calling require for "react" in an environment that doesn't expose the require function`で画面が真っ白になる別の不具合が判明した。原因はmake-you-chic-ui本体の`vite.config.ts`(`rollupOptions.external`)が`react`・`react-dom`のみを外部化しており、JSX自動変換が使う`react/jsx-runtime`(CJS専用)が外部化されずビルドに巻き込まれ、Rolldownのrequire相互運用シムが埋め込まれていたため(開発サーバはsymlink経由のパッケージを生のまま配信するため直撃する。本番ビルドでは消費側アプリの再バンドル過程で問題が吸収され顕在化しなかった)。消費側の`optimizeDeps.include`による回避を試みたが、Viteのプリバンドラ自体も同じRolldownで同じ壊れ方を再現し不成立。ユーザーがmake-you-chic-ui本体側の`vite.config.ts`へ`external: ['react', 'react-dom', 'react/jsx-runtime', 'react/jsx-dev-runtime']`を追加・push済み(commit `93fd631`まで取り込み・dist再ビルド済み)。修正後`npm run dev`・`npm run build`・`npm run lint`いずれも成功、実ブラウザでのエラー無し表示を再確認した。
- **FR11.13**: `client/webconsole/frontend`へmake-you-chic-ui本体と同様のPrettier設定を導入する(2026-08-14追記、レビュー時の追加依頼)。`devDependencies`に`prettier`(`^3.9.6`)を追加し、`.prettierrc.json`(`semi: false`・`singleQuote: true`・`trailingComma: "all"`・`printWidth: 100`・`tabWidth: 2`、make-you-chic-uiの`.prettierrc.json`と同一設定)・`.prettierignore`(`dist/`・`node_modules/`・`vendor/`(submoduleは別リポジトリのため対象外))を新設する。`package.json`へ`format`(`prettier --write .`)・`format:check`(`prettier --check .`)スクリプトを追加する。導入時点で`npm run format`を1回実行し、既存ソース(改行コードのCRLF→LF正規化、クォート統一等)を新設定に揃える(ロジック変更は伴わない)。
- **FR11.14**: Code Generationで新規作成した4つのCSSファイル(`AppShellLayout.css`・`HomePage.css`・`InvokerPage.css`・`StubconfigPage.css`)にライセンスヘッダ(Apache License 2.0、プロジェクト全体の`.ts`/`.tsx`ファイルと同一書式)が漏れていたため追加する(2026-08-14追記、レビュー時の追加指摘)。年表記は対応する画面コンポーネント(`.tsx`)のヘッダと揃える(`AppShellLayout.css`は`2026`、`HomePage.css`は`2023,2026`、`InvokerPage.css`・`StubconfigPage.css`は`2021,2026`)。既存の`vite-env.d.ts`(Vite標準の型参照ファイル、本FR11以前から存在)はプロジェクト内の既存慣習通りヘッダ対象外のまま維持する。
- **FR11.15**: `@vitejs/plugin-react-swc`から`@vitejs/plugin-react`(`^6.0.5`)へ切り替える(2026-08-14追記、レビュー時の追加依頼)。本プロジェクトのVite(`^8.1.3`)はrolldown-vite(内部的にRolldownを使用)であり、`npm run dev`実行時に`[vite:react-swc] We recommend switching to @vitejs/plugin-react for improved performance as no swc plugins are used.`という警告が出ていた。Vite公式のRolldown移行ガイド(WebSearchで確認)によれば、rolldown-vite環境ではOxcベースの高速な変換を活用できる`@vitejs/plugin-react`(v5.0.0以降)への統一が推奨されており、SWC固有プラグイン・カスタムSWCオプションを使用していない場合はそのまま切り替え可能。過去に推奨されていた`@vitejs/plugin-react-oxc`はこの機能が`@vitejs/plugin-react`本体へ統合されたことに伴い廃止予定のため対象外とする。`vite.config.ts`のimportを差し替えるのみ(プラグイン呼び出し自体は`react()`のまま変更なし)。
- **FR11.16**: `client/webconsole/frontend`の依存ライブラリを最新化する(2026-08-14追記、レビュー時の追加依頼)。`make-you-chic-ui`(submoduleへのfile:参照、バージョン管理対象外)を除く全依存(`dependencies`5件・`devDependencies`13件)を`npm uninstall`→`npm install`(バージョン指定無し)で入れ替える方式で最新化した。結果、`typescript`のみ`typescript-eslint`のpeer dependency制約(TS 7系に未対応)により`6.0.3`のまま据え置かれ、それ以外は全て最新化された(`react`/`react-dom` `19.2.8`、`react-router-dom` `7.18.2`、`vite` `8.2.1`、`eslint` `10.8.1`、`typescript-eslint` `8.67.0`、`globals` `17.11.0`、`eslint-plugin-react-refresh` `0.5.4`等)。`npm audit`の指摘も0件になった。
- **FR11.17**: Invoker/Stubconfig画面フッターのコピーライト表記のカンマ位置を修正する(2026-08-14追記、レビュー時の追加指摘)。`Copyright &copy;, 2015,2026, agwlvssainokuni`(`©`直後と名前の前に不要なカンマ)から、プロジェクト全体のソースファイルのライセンスヘッダコメント(`Copyright 2021,2026 agwlvssainokuni`形式、年の間のみカンマ)に合わせて`Copyright &copy; 2015,2026 agwlvssainokuni`へ修正する。
- **FR11.18**: `client/webconsole/frontend`のlintをmake-you-chic-ui本体と同様にoxlintへ切り替える(2026-08-14追記、レビュー時の追加依頼)。make-you-chic-uiの`.oxlintrc.json`(`plugins: ["react", "jsx-a11y", "unicorn", "typescript", "oxc"]`、`categories.correctness: "error"`、jsx-a11y/react/typescript各ルールを個別`error`指定)を同一内容でコピー(`ignorePatterns`のみ本プロジェクトの構成に合わせ`["dist", "node_modules", "vendor"]`とし、存在しない`html-demo`は除外)。ESLintは`eslint-plugin-react-hooks`専用に縮小する(oxlintがreact-hooksの一部ルール(`rules-of-hooks`・`exhaustive-deps`)しか再実装していないため、残りのルールをESLint側に残す。make-you-chic-uiの`eslint.config.js`と同一方針)。あわせて`eslint-plugin-react-refresh`はmake-you-chic-uiに合わせ削除する(不要と判断された既存の意思決定を踏襲)。`package.json`の`devDependencies`から`@eslint/js`・`globals`・`typescript-eslint`・`eslint-plugin-react-refresh`を削除し、`oxlint`(`^1.78.0`)・`@typescript-eslint/parser`(`^8.67.0`、react-hooksルールの構文解析用)を追加。`lint`スクリプトを`oxlint . && eslint .`へ変更する。
- **FR11.19**: `client/webconsole/frontend`に自動化されたテストコードを追加する(2026-08-14追記、レビュー時の追加依頼。「現在の実装を正として」との指定により、既存実装の挙動をそのまま検証するテストとする。バグ修正は伴わない)。テスト基盤はmake-you-chic-ui本体と同様の構成(`vitest`・`@testing-library/react`・`@testing-library/jest-dom`・`@testing-library/user-event`・`jsdom`)を採用する。
  - `vitest.config.ts`(`environment: "jsdom"`、`globals: true`、`setupFiles: ["./vitest.setup.ts"]`、`resolve.dedupe`はFR11.12と同じ理由で設定。`test.exclude`に`vendor/**`を追加し、submodule自身のテスト(別のreactを参照し同じフックエラーになる)を除外)・`vitest.setup.ts`(`@testing-library/jest-dom/vitest`のimportと`afterEach`の`cleanup()`)を新設
  - `tsconfig.app.json`の`include`へ`vitest.setup.ts`、`tsconfig.node.json`の`include`へ`vitest.config.ts`を追加(ambient型拡張(`toHaveValue`等のjest-dom matcher)を`src`配下のテストファイルの型チェックで有効にするため。make-you-chic-uiの`tsconfig.json`と同一方針)
  - `package.json`へ`test`(`NODE_OPTIONS=--no-experimental-webstorage vitest run`)・`test:watch`スクリプトを追加(Node組込みの実験的`localStorage`グローバルがjsdomの`window.localStorage`と衝突するため無効化する。make-you-chic-uiの`package.json`と同一の対処)
  - テスト対象・観点: `src/lib/common.ts`(`uri()`のパス生成)、`src/api/{resolve,invoker,stubconfig}.ts`(`fetch`をモックしリクエストURL・bodyパラメータ・レスポンス解析を検証)、`src/pages/Home/HomePage.tsx`(タイトル・Card2件・各Cardのリンク先)、`src/pages/Invoker/InvokerPage.tsx`(クラス名/メソッド名blurでの自動解決、実行ボタンでの`invoke`呼出しと結果表示、エラー時の表示)、`src/pages/Stubconfig/StubconfigPage.tsx`(登録・現在値取得・クリア・一覧の4操作)、`src/layouts/AppShellLayout.tsx`(Sidebarナビゲーション、テーマ4軸の初期値、各コントロール操作による`<html>`の`data-*`属性反映)、`src/App.tsx`(3ルートでの各ページ描画、AppShellの共通表示)
  - `npm run test`(全32テスト)・`npm run lint`・`npm run build`(`tsc -b`によるテストファイルの型チェック含む)・`./gradlew :client:webconsole:build`(Gradle経由のnpmビルド)いずれも成功を確認。Gradleの`test`タスク(Java側)へのフロントエンドテストの組み込みは今回のスコープ外とする(npm scriptsとして独立して実行する運用のまま)。
- **FR11.20**: スクリプト入力欄・実行結果欄の`Textarea`(FR11.7.1参照)の等幅フォントへ、Web等幅フォント`Noto Sans Mono`(`@fontsource/noto-sans-mono`)を追加する(2026-08-15追記、レビュー時の追加依頼によりFR11.7.1の判断を見直し)。判断の経緯: 本アプリは`@fontsource/noto-sans-jp`・`@fontsource/noto-serif-jp`(japaneseサブセット×4ウェイト×2書体、woff2実配信で計約4.5MB)を既にWebフォントとして読み込んでおり(FR11.4参照)、「Webフォント資産ゼロ」を前提としたFR11.7.1の判断は現状と整合しなくなっていた。`@fontsource/noto-sans-mono`にはjapaneseサブセットが存在しない(cyrillic/greek/latin/latin-ext/vietnameseのみ)ため、日本語表示は既存のNoto Sans/Serif JPに委ね、コード/数値表示部分にのみNoto Sans Monoをフォールバック先頭として追加する構成とする。
  - `package.json`へ`@fontsource/noto-sans-mono`を依存追加。
  - `main.tsx`へ`import '@fontsource/noto-sans-mono/latin-400.css'`・`import '@fontsource/noto-sans-mono/latin-ext-400.css'`を追加(Textareaは通常字重(400)のみ使用のため400のみ導入。追加コストはwoff2で約61KB(latin 10.87KB + latin-ext 50.50KB)、既存Noto JP資産(9MB超)比で誤差レベル)。
  - `InvokerPage.css`・`StubconfigPage.css`の等幅フォントスタック(FR11.7.1)の先頭に`'Noto Sans Mono'`を追加する(`'Noto Sans Mono', ui-monospace, SFMono-Regular, 'SF Mono', Consolas, 'Liberation Mono', Menlo, monospace`)。
  - `npm run lint`・`npm run build`・`npm run test`(全32テスト)いずれも成功を確認。実ブラウザ(`npm run dev`)でInvokerPageの引数欄に`0O1lI`等の紛らわしい文字列を入力し、Noto Sans Mono特有のグリフ(0のスラッシュ/ドット、lの終端カール、Iのセリフ)で判別しやすく表示されることを目視確認した。
- **FR11.21**: `client/webconsole/frontend`へstylelintを導入する(2026-08-15追記、レビュー時の追加依頼。make-you-chic-ui本体には`stylelint`+`stylelint-config-standard`による`lint:css`スクリプトが存在するが、webconsole/frontend自体には未導入だった)。make-you-chic-uiと同一バージョン(`stylelint ^17.14.1`・`stylelint-config-standard ^40.0.0`)を`devDependencies`へ追加し、`.stylelintrc.json`(`extends: ["stylelint-config-standard"]`、`custom-property-pattern`・`selector-class-pattern`を`null`で無効化、make-you-chic-uiの基本設定と同一)を新設する。make-you-chic-ui側の`overrides`(コンポーネントCSSでのプリミティブトークン直接参照禁止等)はデザインシステム固有のルールでありwebconsole/frontendのページ固有CSSには該当しないため移植しない。`package.json`へ`lint:css`(`stylelint "src/**/*.css"`)スクリプトを追加(既存の`lint`スクリプトには統合せず、make-you-chic-uiと同様に独立させる)。既存4CSSファイル(`AppShellLayout.css`・`HomePage.css`・`InvokerPage.css`・`StubconfigPage.css`)は全てエラー無く通過することを確認。意図的に重複プロパティを混入させ`declaration-block-no-duplicate-properties`ルールが実際に検知することを確認した上で復元。`npm run lint:css`・`npm run lint`・`npm run build`・`npm run test`(全32テスト)いずれも成功。

### FR12: demo+クライアント(cli/webconsole)のE2Eテスト追加(2026-08-15追記、Post-Construction Change時の新規改修依頼)

**動機**: MVP段階を過ぎ、今後依存ライブラリのバージョンアップにより挙動が変わりうることへの備えとして、demo・cli・webconsoleをまたぐ一気通貫(E2E)の自動テストを設け、依存バージョンアップに伴う回帰を検知できるようにする。従来NFR2は「結合テストは手動確認手順で代替する」方針だった(cli/webconsole README「手動結合確認手順」)が、本FR12によりこの一部を自動化する(手動確認手順自体は残置し、E2Eテストと併存させる)。実際、FR11の作業中に発生した3件の不具合(Vite `resolve.dedupe`未設定による二重React読込み、CSS未適用、`react/jsx-runtime`起因のクラッシュ)はいずれも依存ライブラリ(Vite/rolldown/make-you-chic-ui)側の変更が引き金であり、かつ実ブラウザでしか検知できなかった実例であるため、実ブラウザ操作を伴うE2Eの価値は高いと判断した(確認質問Q2の議論より)。

**確認質問への回答**(`aidlc-docs/inception/requirements/e2e-test-verification-questions.md`・`e2e-test-clarification-questions.md`参照):

- **対象経路**: cli・webconsoleの両方を対象にする。cliはdemoへ直接(`http://localhost:8080`)、webconsoleは実ブラウザ操作でSPA→webconsoleのAPIプロキシ→demoの経路を、それぞれ検証する(FR9/FR10 Build and Testで実施した「demo単体・webconsole経由・cli直接」の3系統確認パターンを踏襲)。
- **webconsole側の検証手法**: Playwright(`@playwright/test`)による実ブラウザ自動操作を採用する。HTTPレベルの直接検証が必要な箇所(webconsoleのAPIプロキシ層など)についても、Java(`RestTemplate`/`WebTestClient`)ではなくPlaywrightの`request`機能を用い、ツールをPlaywright(Node.js)へ一本化する。
- **スタブ効果の検証**: 含める。`stubconfig register`でスタブを登録→対象メソッドを`invoke`実行してスタブ値が返ることを確認→`stubconfig clear`で解除→元の計算結果に戻ることまで確認する。
- **配置場所**: リポジトリ直下に新規`e2e/`ディレクトリを新設する。Gradleマルチプロジェクト(`settings.gradle.kts`)には含めない独立したnpmプロジェクトとし、`@playwright/test`を使用する。demo・webconsole・cliのいずれもこのディレクトリから横断的に扱う。
- **プロセスの起動・停止**: Playwright側(`globalSetup`/`globalTeardown`または`webServer`設定)で、テスト実行時にdemo・webconsoleを自動的に起動し、終了時に停止する。ローカル実行・CI実行とも同じ手順で完結させる。
- **cliのビルドタイミング**: 毎回`./gradlew :client:cli:bootJar`でビルドし直してから、ビルド済みjarを子プロセス(`java -jar ...`)として実行し、標準出力・終了コードを検証する(依存ライブラリの最新化が確実にテストへ反映されるようにする)。
- **通常のbuild/checkへの組込み**: 含めない。`e2e/`配下の独立したnpm script(例: `npm run test:e2e`)として実行し、Gradleの`build`/`check`タスクには含めない(複数プロセスの起動を伴い時間がかかる・環境依存で不安定になりうるため)。
- **実行トリガー**: ローカル手動実行に加え、GitHub Actionsワークフローを新設し、`main`ブランチへのpush時・Pull Request作成/更新時の自動実行、および`workflow_dispatch`による手動実行の両方に対応する。
- **APIキー(FR9)との組合せ**: 含める。APIキー未設定時・設定時(ヘッダ一致)の両方をE2Eシナリオに含める。**(2026-08-15追記、レビュー時の追加依頼により拡張)** さらに、cli経由では「サーバー側キー設定時にクライアントがヘッダ無しだと拒否される」「サーバー側キー未設定時にクライアントがヘッダ付きでも成功する」という2つの不一致パターンを簡易的に確認する。値の不一致(ヘッダはあるが値が違う)等の細かい異常系はFR9/FR10 Build and Testで既に手動確認済みのため対象外とする。
- **webconsole側のAPIキー不一致(2026-08-15追記、レビュー時の追加依頼)**: webconsoleはブラウザからの受信リクエストに対してAPIキーを検証しない(`GatewayRouteConfig`はdemoへの転送時にヘッダを自動付与するのみ、FR10.4)ため、cliと同じ「クライアント(ブラウザ)のヘッダ有無」という軸での不一致確認は意味を持たない。代わりに、demo・webconsoleそれぞれの`cherry.testtool.web.api-key`設定が食い違うケースを確認する: (1) demoのみキー設定時、webconsole経由のリクエストがdemoからの401をそのまま伝播すること、(2) webconsoleのみキー設定時、demoはキー未要求のため成功すること。これらは既存のglobal-setup(8080/9090)とは独立した専用ポート(8081/9091)でdemo/webconsoleを都度起動・停止する自己完結したテストとして実装する。

**実装方針**:

- `e2e/package.json`を新設し、`@playwright/test`を依存追加する。`playwright.config.ts`で`globalSetup`/`globalTeardown`によりdemo(`java -jar demo/build/libs/*.jar`)・webconsole(`java -jar client/webconsole/build/libs/*.jar`)を起動・停止する(起動確認はヘルスチェック的なポーリングで行う)。
- cliシナリオは、Playwrightのテストコードから`child_process`(Node.js標準)で`java -jar client/cli/build/libs/cherry-testtool-cli.jar ...`を実行し、標準出力・終了コードをアサーションする。
- テストシナリオ(最低限): (1) cli `invoke`(`toBeInvoked*`系メソッド呼出しの成功確認)、(2) cli `stubconfig register/show/clear`とスタブ効果の反映確認、(3) webconsole実ブラウザ操作でHome→Invoker→Stubconfig遷移・呼出し・スタブ登録操作、(4) APIキー設定時の上記シナリオ(未設定時との差分確認)。詳細なテストケース一覧はCode Generation Planningで確定する。
- GitHub Actionsワークフロー(`.github/workflows/e2e.yml`)を新設する。トリガーは`push`(`main`)・`pull_request`・`workflow_dispatch`。ステップ: JDK・Node.jsのセットアップ→`./gradlew build`(demo・webconsole・cli一式をビルド)→`e2e/`で`npm ci`・`npx playwright install --with-deps`・`npm run test:e2e`。
- 既存の手動結合確認手順(cli/webconsole README)は残置する(E2Eで自動化された範囲と重複するが、開発時の即時確認用途として維持する)。

**FR12.1: GitHub Actions初回実行時の不具合修正(2026-08-15追記、Build and Test時に発見)**:

- **submodule未取得によるビルド失敗**: `.github/workflows/e2e.yml`の`actions/checkout@v4`が既定で`submodules`を取得しないため、`client/webconsole/frontend`が`file:`参照する`vendor/make-you-chic-ui`が空のままとなり、`./gradlew build`の`:client:webconsole:npmBuild`が`Cannot find module 'make-you-chic-ui'`で失敗した。`checkout`ステップへ`with: submodules: true`を追加して解消。
- **vendorのdist未ビルドによるビルド失敗(真にクリーンな環境で潜在していた問題)**: 上記を修正してsubmoduleを取得しても、`vendor/make-you-chic-ui`自体の`dist/`(`.gitignore`対象のビルド成果物、`main`/`types`が指す実体)が無いままではフロントエンドの型解決に失敗する(`TS7006`等)ことが判明。この問題はFR11時点から存在していたが、ローカル環境では過去の対話的作業で`dist/`が既にビルド済みだったため顕在化していなかった(本FR12でCIを初めて導入し、真にクリーンな環境(fresh clone + submodule init直後)でのビルドを初めて実行したことで発覚)。`client/webconsole/build.gradle.kts`へ`vendorInstall`/`vendorBuild`タスク(`vendor/make-you-chic-ui`直下でnpm workspaceの`install`→`build`を実行)を新設し、`npmInstall`タスクの前提とすることで、クリーンな環境からの`./gradlew build`が常に成功するようにした(ローカル開発時にも同様の恩恵がある)。
- 修正後、`git clone`+`git submodule update --init --recursive`した完全にクリーンな一時ディレクトリで`./gradlew build`(全モジュール)が成功することを確認した。
- あわせて、`.github/workflows/e2e.yml`が参照する各GitHub Actions(`actions/checkout`・`actions/setup-java`・`actions/setup-node`・`actions/upload-artifact`)をリリース時点の最新メジャーバージョン(`v4`→`checkout v7`・`setup-java v5`・`setup-node v7`・`upload-artifact v7`)へ更新した(ユーザー指摘)。

**FR12.2: `e2e/`へのPrettier導入(2026-08-15追記、レビュー時の追加依頼)**: `client/webconsole/frontend`と同一設定(`semi: false`・`singleQuote: true`・`trailingComma: "all"`・`printWidth: 100`・`tabWidth: 2`)で`.prettierrc.json`・`.prettierignore`(`node_modules/`・`test-results/`・`playwright-report/`・`playwright/.cache/`)を新設。`devDependencies`に`prettier`(`^3.9.6`)を追加し、`format`(`prettier --write .`)・`format:check`(`prettier --check .`)スクリプトを追加。導入時点で`npm run format`を1回実行し、既存ソース(長い行の折返し、READMEのテーブル整形)を新設定に揃えた(ロジック変更は伴わない)。

**FR12.3: `demo.stub-loader`(起動時スタブ自動ロード)のE2Eカバレッジ追加(2026-08-15追記、レビュー時の指摘)**: ユーザー指摘により、demoの`demo.stub-loader`機能(`StubAutoLoadRunner`、既定は無効、`demo.stub-loader.enabled=true`で起動時に指定ディレクトリ配下のスタブ設定を一括読込みする)がE2Eの対象から漏れていたことが判明。既存のE2Eシナリオ(cli/webconsole双方)は、いずれも`stub-loader.enabled`を指定しない既定(無効)状態のdemoしか起動していなかったため。`demo-stub-auto-load.spec.ts`を新設し、`webconsole-api-key-mismatch.spec.ts`と同様の自己完結パターン(global-setupとは独立した専用ポート、demo`8082`/webconsole`9092`でdemo・webconsoleを`test.afterEach`で都度起動・停止)で、`--demo.stub-loader.enabled=true`(directory/extは既定値のまま、`demo/`を作業ディレクトリとするため`demo/stub-samples`が対象になる)で起動したdemoが、`stubconfig register`を一度も呼ばずに起動直後からスタブ値(`toBeStubbed1.1.js`の`9999`)を返すことを確認する。当初はdemo直接(`/api/sample/**`)のみの確認だったが、「webconsoleからも実行して欲しい」とのユーザー追加依頼を受け、webconsoleも同じdemoを指して起動し、`/testtool/stubconfig/list`(webconsole経由)で自動ロード済みスタブが観測できることも確認する構成へ拡張した(`/api/sample/**`はwebconsoleのプロキシ対象`/testtool/**`に含まれないため、webconsole経由での確認は`/testtool/stubconfig/list`で行う)。E2E_API_KEYに依存しないためno-keyパスでのみ実行(`test.skip`)。

### FR13: webconsoleへのBasic認証追加(2026-08-17追記、Post-Construction Change時の新規改修依頼)

**動機**: ユーザーからの相談「webconsoleに認証追加するとしたらどんな方式が良いか」を起点に検討。現状webconsole自体には認証がなく、`ApiKeyFilter`(FR10)はwebconsole↔demo間のヘッダー保護に留まる(ブラウザ→webconsoleは無防備)。Spring Security依存を追加し、Basic認証を導入することで、webconsoleへのアクセス自体を保護する。「Actuatorらしい操作モデルへ変換した上で同等機能を提供する」案(APIキー認証なしでの利用)も検討したが、既存の`GatewayRouteConfig`プロキシ層・cliのレスポンス解析部分との互換性が崩れる規模になるため保留とした。

**確認質問への回答**(`aidlc-docs/inception/requirements/webconsole-auth-verification-questions.md`・`webconsole-auth-clarification-questions.md`参照):

- **認証情報のプロパティ設計**: 専用プロパティ(`cherry.testtool.web.auth.username`/`cherry.testtool.web.auth.password`)を新設する。既存のAPIキー保護(`cherry.testtool.web.api-key`)と同じ名前空間・設計パターンを踏襲し一貫性を持たせる。**(明確化質問により確定)** 当初はSpring Bootの標準プロパティ(`spring.security.user.name`/`spring.security.user.password`)をそのまま使う案(確認質問Q1回答A)だったが、これは「認証情報未設定時は認証なしで動作する」(Q3回答A)と技術的に矛盾する(Spring Security依存追加時点で`UserDetailsServiceAutoConfiguration`により自動的にBasic認証が有効化され、パスワード未設定時は認証無効ではなくランダムパスワード生成・ログ出力という既定動作になるため)。この矛盾を解消するため、標準プロパティに頼らない専用プロパティ方式へ変更した。
- **適用範囲**: webconsole全体(SPA配信・静的アセット含む全パス)に認証をかける。画面は見えるが操作は全て失敗するという中途半端な状態を避ける。
- **未設定時の既定動作**: 認証情報(ユーザー名・パスワードの専用プロパティ)が未設定の場合は認証なしで動作する(既存のAPIキー保護(FR10)と同じ後方互換方針)。専用プロパティを新設したことで、Spring Bootの自動生成パスワード機構に頼らず、プロパティの有無による`SecurityFilterChain`の条件登録(または同等の仕組み)で実現する。
- **パスワードの保存方式**: 平文で設定ファイル等に記述することを既定とする(ローカル開発ツールとしてのシンプルさを優先)。実装はSpring Securityの`DelegatingPasswordEncoder`(`PasswordEncoderFactories.createDelegatingPasswordEncoder()`)を用いることで、`{bcrypt}`プレフィックスを付けた値であればBCryptハッシュにも自然に対応できるようにする(平文運用を既定としつつ、必要に応じてハッシュ化する逃げ道を実装コストほぼゼロで残す)。
- **既存E2Eテストへの対応**: Basic認証を有効化した専用のE2Eシナリオを追加する。既存のE2Eシナリオ(`webconsole-ui.spec.ts`・`webconsole-api.spec.ts`等)は認証無効のまま維持し並存させる。専用シナリオは`webconsole-api-key-mismatch.spec.ts`と同様、global-setupとは独立した専用ポートでwebconsoleを自己完結的に起動・停止するパターンとする。

**実装方針**(詳細はWorkflow Planning/Code Generationで確定):

- `client/webconsole/build.gradle.kts`(または`build.gradle`)へ`spring-boot-starter-security`への依存を追加する(`lib`本体には追加しない、既存のFR10設計方針「消費側embed前提のlibには重量級依存を追加しない」はwebconsole自体には適用されないため問題なし)。
- `cherry.testtool.web.auth.users`(リスト)が1件以上設定されている場合のみBasic認証を有効化する`SecurityFilterChain`を登録する。未設定(空リスト)の場合は認証を無効化する(既存のAPIキー保護の設計と対称的な扱いとする)。
- パスワード比較は`DelegatingPasswordEncoder`を用い、`{bcrypt}`プレフィックス付きの値・プレフィックス無しの平文値のいずれも扱えるようにする。

**FR13.1: 複数ユーザー対応(2026-08-17追記、Code Generationレビュー時の追加依頼)**: 当初単一の`cherry.testtool.web.auth.username`/`cherry.testtool.web.auth.password`プロパティとして実装したが、ユーザーからの相談「Basic認証を複数ユーザ対応させるのは難しいか」を受け、「リスト形式に統一する」との指示により、単一プロパティ形式との後方互換は持たせずリスト形式(`cherry.testtool.web.auth.users[].username`/`cherry.testtool.web.auth.users[].password`)へ設計変更した。`@ConfigurationProperties(prefix = "cherry.testtool.web.auth")`を持つ`WebAuthProperties`(record、`users`フィールド)を新設し、`WebSecurityConfig`は`InMemoryUserDetailsManager`へ複数`UserDetails`を登録する形に変更。`users`が空の場合は従来通り認証を無効化する(後方互換の考え方自体は維持)。

### NFR1: 互換性
外部インタフェース(REST APIのパス・パラメータ等)の変更は許容する。ただし変更する場合は、影響するSPA(webconsoleに統合されたフロントエンド)・CLI・デモアプリ側の追随修正を同一サイクル内で行う。

### NFR2: テスト
変更箇所に対応する単体テストを追加・更新する。加えて、可能な範囲で結合テスト(例: webconsole経由のプロキシ動作確認、CLIからのAPI呼出し確認)および手動確認手順を整備する。**(2026-08-15追記: FR12により、demo+cli/webconsoleをまたぐ一気通貫のE2E自動テストをPlaywrightで整備する。手動確認手順はE2Eと併存させ、開発時の即時確認用途として残置する。)**

### NFR3: 適用しない拡張機能
Security Baseline、Resiliency Baseline、Property-Based Testingの各拡張は、本サイクルでは適用しない(理由: ローカル開発用のテストツールであり、本番運用・高可用性・複雑なビジネスロジックを前提としないため)。

### NFR4: 作業分解
変更規模が大きいため、Application Design(コンポーネント設計)およびUnits Generation(複数Unitへの分解)を実行し、Unit単位で段階的に設計・実装を進める。

### NFR5: Nullability規約の統一(JSpecify採用)
コード全体(FR7と同じ対象範囲)で、JSpecify(`org.jspecify.annotations`)ベースの「原則として非null」を既定とするNullability規約に統一する。

- 各パッケージの`package-info.java`に`@NullMarked`を付与し、そのパッケージ配下の型・フィールド・メソッド引数・戻り値を既定で非null(non-null)として扱う
- nullを許容する箇所にのみ`org.jspecify.annotations.Nullable`を付与する
- 現状`lib`コードで使われている`jakarta.annotation.Nonnull`/`jakarta.annotation.Nullable`は、本サイクルで変更対象となるクラスについては`org.jspecify.annotations`ベースへ置き換える(`@Nonnull`相当は`@NullMarked`によるパッケージ既定へ委譲し個別注釈は廃止、`@Nullable`のみJSpecify版へ切替)
- `lib/build.gradle`に`org.jspecify:jspecify`への依存を追加する(Spring Framework自体がJSpecifyへ移行済みのため、推移的依存で解決される場合は明示追加が不要か確認する)

## Architectural Considerations
- **新モジュール名**: `client/webconsole`(旧`client/gateway`・`client/spa`を統合)。
- **各モジュールのGradleプロジェクトパス・成果物名・待受ポート**(2026-08-09、レビュー時にユーザー指示でGradleマルチプロジェクト化。詳細はlib-unit-summary.md「Gradleマルチプロジェクト化」参照):
  | モジュール | ディレクトリ | Gradleパス | 成果物名(`base.archivesName`) | 待受ポート |
  |---|---|---|---|---|
  | lib | `lib` | `:lib` | `cherry-testtool-core` | -(ライブラリのため無し) |
  | webconsole | `client/webconsole` | `:client:webconsole` | `cherry-testtool-webconsole` | `9090`(現行8070から変更) |
  | cli | `client/cli` | `:client:cli` | `cherry-testtool-cli` | -(CLIのため無し) |
  | demo | `demo`(リポジトリ直下、`lib`と同じ階層) | `:demo` | `cherry-testtool-demo` | `8080`(既定) |

  リポジトリ全体は単一の`settings.gradle.kts`(`rootProject.name = "cherry-testtool"`)配下のマルチプロジェクトであり、`rootProject.name`は各モジュール毎ではなくリポジトリ全体で1つ。`demo`は`:lib`へGradleプロジェクト依存(`project(":lib")`)する。
- **パッケージ命名の重複回避**: `lib`内で既に`cherry.testtool.web`パッケージ(Controller群)を使用しているため、`client/webconsole`のJavaパッケージ名は別名とする(具体名はApplication Design/Functional Designで確定)。
- **設定の引継ぎ**: `client/gateway`が持つCORS・ルーティング・レスポンスヘッダ重複排除の設定は、`client/webconsole`への統合時に引き継ぐ。
- **デモアプリとの関係**: `client/webconsole`のプロキシ先(現行`backend.uri`に相当)は、新設するデモアプリを既定値として想定する。
- **スタブ介入方式の見直し(Unit 1実装時に確定)**: `StubInterceptor`(AOP Alliance `MethodInterceptor`、利用側でXML等によるpointcut配線が必要)は`@Deprecated`とし後方互換のため残置する。代わりに、アノテーションベースの`@Aspect`+`@Around`パターン(`StubAspect`)を正規の推奨方式とする。`StubAspect`自体は`lib/src/main`へは昇格させず、組み込み方の手引書と共にデモアプリ(Unit 2)側のリファレンス実装として提供する。これに伴い、XML設定`appctx-stub.xml`と対応するテスト(`StubInterceptorTest`)は`lib`から削除する。
- **トレースアスペクトのアノテーション化(Unit 1実装時に確定)**: メソッド呼出しトレースを行う`appctx-trace.xml`(XML設定の`CustomizableTraceInterceptor`)を廃止し、`@Aspect`+`@Around`によるアノテーションベースの`TraceAspect`(`lib/src/test`)へ置換する。`@Value`の各設定項目には旧XML設定と同値をデフォルト値として埋め込む。
- **ToolTesterのInterface統合(Unit 1実装時に確定)**: テストフィクスチャ`ToolTester`(interface)/`ToolTesterImpl`も、`lib`本体の5組(FR4)と同じ方針でImpl無しの具象クラス`ToolTester`へ統合する。
- **Aspectのパッケージ整理(Unit 1実装時に確定)**: `TraceAspect`・`StubAspect`は`cherry.testtool.aspect`パッケージへ配置する。`TraceAspect`のpointcutは`execution(* cherry.testtool..*.*(..)) && !within(cherry.testtool.aspect..*)`とし、対象を`cherry.testtool`配下に絞り込みつつ、`aspect`パッケージ自身(Aspect自体の呼出し)はトレース対象から除外する。
- **設定ファイルのYAML化(Unit 1実装時に確定)**: `lib/src/test/resources/application.properties`は`application.yml`へ変換する。
- **demoモジュールのビルド方式(Unit 2実装時に確定、2026-08-09にマルチプロジェクト化により変更)**: 当初`demo`は独自の`settings.gradle`(`rootProject.name = cherry-testtool-demo`、FR6.4)を維持する独立したGradleプロジェクトとし、`lib`への依存はGradle複合ビルド(`includeBuild('../lib')`)で解決していた。マルチプロジェクト化(単一`settings.gradle`配下への統合)も検討したが、当時判明していた2件の不具合修正後は複合ビルドのままで問題なく動作することを確認できたため一旦見送っていた。その後、IntelliJ IDEAで`lib`が「単独リンクされたプロジェクト」と「`demo`のincludeBuild先」の両方として扱われることによるビルドスクリプト解析競合(`lib/build.gradle.kts`・`settings.gradle.kts`にのみ偽陽性のエラーが表示される)が判明し、キャッシュ再構築やGradleプロジェクトの再登録でも解消しないことを確認したため、判断を改めてマルチプロジェクト化を実施した(単一`settings.gradle.kts`、`demo`は`project(":lib")`でlibを直接参照)。詳細は`aidlc-docs/construction/lib/code/lib-unit-summary.md`「Gradleマルチプロジェクト化」参照。
- **lib側の追加修正(Unit 2着手時に発覚、Unit 1へ遡及適用)**: (1) `io.spring.dependency-management`のBOM/バージョン管理は複合ビルドを跨いで伝播しないため、`lib/build.gradle`の該当依存(`commons-collections4`、GraalVM JS関連、`jspecify`)にバージョンを直接明記する形へ変更。(2) `lib`の自動構成登録がSpring Boot 4.1.0では機能しない旧形式`META-INF/spring.factories`のままだったため、新形式`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`へ置き換え。詳細は`aidlc-docs/construction/lib/code/lib-unit-summary.md`参照。

## Summary
既存4モジュール構成(lib, gateway, spa, cli)を、(1) libのInterface/Impl分離解消による簡素化、(2) gatewayとspaを統合した`client/webconsole`(プロジェクト名`cherry-testtool-webconsole`)への再編(Spring MVC + Spring Cloud Gateway Servlet版)、(3) cliのSpring Bootアプリ化、(4) libを組み込むデモアプリの新設、(5) libの`InvokerController`/`StubConfigController`を`TesttoolController`へ統合しbean/method解決APIを共通パスへ一本化(FR8)、を伴う形に再構成する、システム全体に及ぶリファクタリングである。加えて、変更対象コード全般でコメントの充実(FR7)とNullability規約の統一(NFR5: 原則非null、null許容箇所のみ`@Nullable`)を行う。外部インタフェースの変更は許容し、拡張機能(Security/Resiliency/PBT)は適用しない。規模が大きいため、Application Design/Units Generationステージを経て複数Unitに分解し、段階的に構築を進める。
