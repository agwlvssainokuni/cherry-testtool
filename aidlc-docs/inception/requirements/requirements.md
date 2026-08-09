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

## Non-Functional Requirements

### NFR1: 互換性
外部インタフェース(REST APIのパス・パラメータ等)の変更は許容する。ただし変更する場合は、影響するSPA(webconsoleに統合されたフロントエンド)・CLI・デモアプリ側の追随修正を同一サイクル内で行う。

### NFR2: テスト
変更箇所に対応する単体テストを追加・更新する。加えて、可能な範囲で結合テスト(例: webconsole経由のプロキシ動作確認、CLIからのAPI呼出し確認)および手動確認手順を整備する。

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
