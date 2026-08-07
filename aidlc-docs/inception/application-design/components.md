# Components

## lib(Interface統合後)

### TesttoolConfiguration
- **Purpose**: `lib`が提供する全Beanを定義するSpring自動構成クラス
- **Responsibilities**: 具象クラス(`InvokerService`、`ReflectionResolver`、`ScriptProcessor`、`StubRepository`、`StubConfigLoader`、`StubResolver`)をBean定義する
- **Interfaces**: Spring `@Configuration`(公開APIとしての型は無し)

### InvokerService(具象クラス、旧`InvokerServiceImpl`)
- **Purpose**: リフレクションによる動的メソッド呼出しサービス
- **Responsibilities**: Bean解決、スクリプトによる引数生成、型変換、メソッド呼出し、結果/例外のYAMLシリアライズ
- **Interfaces**: `InvokerController`から利用される公開クラス

### ReflectionResolver(具象クラス)
- **Purpose**: Bean名・メソッドの解決ユーティリティ
- **Responsibilities**: `ApplicationContext`からのBean名解決、宣言メソッドの名前一致検索
- **Interfaces**: `InvokerService`・各Controller・`StubConfigLoader`から利用

### ScriptProcessor(具象クラス)
- **Purpose**: GraalVM JSスクリプト実行サービス
- **Responsibilities**: `ScriptEngineManager`経由でのスクリプト評価、`appctx`/`args`バインディング設定
- **Interfaces**: `InvokerService`・`StubResolver`・`StubConfigController`から利用

### StubRepository(具象クラス)
- **Purpose**: スタブ設定のインメモリ格納
- **Responsibilities**: `Method`をキーとした`StubConfig`のCRUD操作
- **Interfaces**: `StubConfigLoader`・`StubResolver`・`StubConfigController`から利用

### StubResolver(具象クラス)
- **Purpose**: メソッド呼出しをスタブ実行(`StubInvocation`)へ解決する
- **Responsibilities**: `StubRepository`からの設定取得、`ScriptProcessor`によるスタブスクリプト評価への変換
- **Interfaces**: `StubInterceptor`から利用。`Method`/`MethodInvocation`/`ProceedingJoinPoint`いずれの呼出し元にも対応するオーバーロードを提供

### StubConfigLoader(変更なし)
- **Purpose**: ディレクトリ配下のスクリプトファイルを一括読込みしてスタブ登録する
- **Responsibilities**: ファイル走査、ファイル名からのメソッド解決、`StubRepository`への登録

### StubInterceptor(変更なし)
- **Purpose**: AOP Alliance `MethodInterceptor`によるスタブ介入の実装
- **Responsibilities**: `StubResolver`への委譲、スタブ有無に応じた`proceed()`分岐

### StubConfig(record、変更なし) / StubInvocation(関数型インタフェース、変更なし)
- **Purpose**: スタブ設定の値オブジェクト、スタブ実行を表す関数型インタフェース

### ReflectionUtil / ToMapUtil(静的ユーティリティ、変更なし)
- **Purpose**: クラス/メソッドの文字列表現生成、`Throwable`の`Map`変換

### TesttoolController(新設、`InvokerController`+`StubConfigController`を統合、FR8)
- **Purpose**: `lib`が提供する全REST APIを1つのControllerへ集約する
- **Responsibilities**: `invoke`(`/testtool/invoker/invoke`)、`put`/`get`/`list`(`/testtool/stubconfig/**`)は現行URLのまま維持。`bean`/`method`解決は新設の共通パス(`/testtool/resolve/bean`,`/testtool/resolve/method`)へ一本化し、重複していた実装を1箇所に集約する
- **Interfaces**: 単一の`@ConditionalOnProperty`(例: `cherry.testtool.web.enabled`、既定有効)でController全体の有効/無効を切り替える(現行の`invoker`/`stubconfig`独立トグルは廃止)。呼出し先は具象クラス(`InvokerService`、`StubRepository`、`ScriptProcessor`、`ReflectionResolver`)に更新

## client/webconsole(新設、パッケージ`cherry.testtool.webconsole`)

### WebconsoleApplication
- **Purpose**: Spring MVC(Servlet)ベースのエントリポイント
- **Responsibilities**: Spring Bootアプリケーションの起動
- **Interfaces**: `@SpringBootApplication`

### GatewayRouteConfig
- **Purpose**: Spring Cloud Gateway(Servlet/MVC版)によるAPIプロキシのルート定義
- **Responsibilities**: `/testtool/**` → backend(既定はデモアプリ`:8080`)へのルーティングのみに限定(静的リソース配信・SPAフォールバックとの競合を避けるため)。セキュリティヘッダ付与、レスポンスヘッダ重複排除は現行`client/gateway`の設定を踏襲。CORSは不要(FR2.9によりVite dev server proxyで同一オリジン化するため設定しない)
- **Interfaces**: Java Functional Route(`RouterFunction<ServerResponse>` Bean、`GatewayRouterFunctions`+`RequestPredicates`+`HandlerFunctions.http(...)`、`spring-cloud-starter-gateway-server-webmvc`)

### 静的リソース配信 / SpaFallbackResourceResolver
- **Purpose**: SPAビルド成果物(旧`client/spa`)の配信、およびクライアントサイドルーティング(React Router)のフォールバック対応
- **Responsibilities**: Spring Bootの標準的な静的リソース配信機構を利用しつつ、リクエストパスが既存の静的ファイルにも`/testtool/**`にも一致しない場合は`index.html`を返す`PathResourceResolver`拡張コンポーネント(`SpaFallbackResourceResolver`)を実装する。`GatewayRouteConfig`(`/testtool/**`)とのハンドラ優先順位を明確にする(ビルド時の静的リソース組込み手順の詳細はFunctional Design/Code Generationで確定)
- **Interfaces**: `WebMvcConfigurer.addResourceHandlers`に登録する`ResourceResolver`

### フロントエンド(旧`client/spa`相当)
- **Purpose**: メソッド呼出し・スタブ設定のWeb UI
- **Responsibilities**: `client/webconsole`のサブディレクトリとして同居する独立したReact/TypeScript/Viteプロジェクト。既存の`Home`/`invoker/App`/`stubconfig/App`をそのまま移設。開発時はVite dev serverの`server.proxy`機能で`/testtool`宛リクエストを`client/webconsole`(既定`:9090`)へサーバー間プロキシし、CORSを不要にする(`common.ts`は絶対URL解決から相対パスベースへ簡素化)
- **Interfaces**: `client/webconsole`が提供する`/testtool/**`(本番・開発とも同一オリジン、CORS設定なし)

## client/cli(新設、パッケージ`cherry.testtool.cli`)

### CliApplication
- **Purpose**: CLIのエントリポイント
- **Responsibilities**: `CommandLineRunner`としてPicocliの`CommandLine`を実行し、`ExitCodeGenerator`として実行結果の終了コードを保持・返却する
- **Interfaces**: `CommandLineRunner`, `ExitCodeGenerator`

### RootCommand
- **Purpose**: Picocliのルートコマンド
- **Responsibilities**: `invoke`・`stubconfig`サブコマンドへのディスパッチ、共通オプション(接続先URL等)の定義
- **Interfaces**: Picocli `@Command(subcommands = {InvokeCommand.class, StubConfigCommand.class})`

### InvokeCommand
- **Purpose**: 旧`invoker.sh`相当のサブコマンド(引数解析・出力表示のみ)
- **Responsibilities**: コマンドラインオプションの受付、`InvokeService`への処理委譲、結果の標準出力表示
- **Interfaces**: Picocli `@Command("invoke")`

### StubConfigCommand
- **Purpose**: 旧`stubconfig.sh`相当のサブコマンド(引数解析・出力表示のみ)
- **Responsibilities**: `register`/`clear`/`show`モードの受付、`StubConfigService`への処理委譲、結果の標準出力表示
- **Interfaces**: Picocli `@Command("stubconfig")`

### InvokeService
- **Purpose**: メソッド呼出し一括実行のオーケストレーション
- **Responsibilities**: `ScriptFileScanner`によるファイル走査、`TesttoolApiClient`経由での呼出しAPI実行、結果集約
- **Interfaces**: `InvokeCommand`から利用

### StubConfigService
- **Purpose**: スタブ登録/参照/解除一括実行のオーケストレーション
- **Responsibilities**: `ScriptFileScanner`によるファイル走査、`TesttoolApiClient`経由でのスタブAPI実行、結果集約
- **Interfaces**: `StubConfigCommand`から利用

### ScriptFileScanner
- **Purpose**: スクリプト設定ディレクトリの走査ユーティリティ
- **Responsibilities**: 指定ディレクトリ配下の`*.js`ファイル走査、ファイルパスからのクラス名・メソッド名・メソッドインデックス抽出(旧シェルスクリプトのロジックを移植)
- **Interfaces**: `InvokeService`・`StubConfigService`から利用される共有コンポーネント

### TesttoolApiClient
- **Purpose**: `lib`のREST API(`/testtool/invoker/**`、`/testtool/stubconfig/**`)を呼び出す宣言的HTTPクライアント
- **Responsibilities**: `@HttpExchange`メソッド定義によるAPI呼出し。動的ヘッダ(BASIC認証・追加ヘッダ)を`@RequestHeader`引数で受け付ける
- **Interfaces**: Spring管理の**prototype-scoped Bean**(`ApiClientConfig`が定義)。接続先URL(実行時のCLIオプション由来)はBeanファクトリメソッドの引数として受け取り、`ApplicationContext.getBean(TesttoolApiClient.class, baseUri)`による明示的な引数付き取得で生成される。`InvokeService`・`StubConfigService`から`ApiClientFactory`経由で利用

### ApiClientConfig(新設)
- **Purpose**: `TesttoolApiClient`のBean定義を提供する設定クラス
- **Responsibilities**: `@Bean @Scope("prototype") TesttoolApiClient testtoolApiClient(URI baseUri)`を定義し、内部で`RestClient`(`baseUri`をベースURLとする)→`HttpServiceProxyFactory`(`RestClientAdapter`経由)→`TesttoolApiClient`プロキシを組み立てる
- **Interfaces**: `@Configuration`

### ApiClientFactory
- **Purpose**: 実行時のCLIオプション(接続先URL)を基に、Spring管理Beanとしての`TesttoolApiClient`を取得する薄いファサード
- **Responsibilities**: `ApplicationContext.getBean(TesttoolApiClient.class, baseUri)`の呼出しのみ(CLIの引数構成・Picocliによる解析フローは変更しない)
- **Interfaces**: `RootCommand`/各サブコマンド(の`InvokeService`・`StubConfigService`)から利用。コンストラクタで`ApplicationContext`を注入

## demo(新設、パッケージ`cherry.testtool.demo`、ディレクトリ`demo/`(リポジトリ直下、`lib`と同階層))

### DemoApplication
- **Purpose**: `lib`を組み込む最小構成のSpring Bootアプリケーション
- **Responsibilities**: アプリケーション起動、`@EnableAspectJAutoProxy`によるAOPアノテーション認識の有効化
- **Interfaces**: `@SpringBootApplication`

### ToolTester(インタフェース) / ToolTesterImpl(実装)
- **Purpose**: 呼出し・スタブ動作確認用のサンプル業務コンポーネント(`lib/src/test`から移管)
- **Responsibilities**: 各種引数パターン(プリミティブ、日時型、DTO、オーバーロード)を持つサンプルメソッド群の提供
- **Interfaces**: `lib`のInterface/Impl統合(FR4)の対象外のため、Interface+Impl構成のまま移管する

### StubAspect(アノテーションベース、`lib/src/test`から移管、XML設定は持ち込まない)
- **Purpose**: `ToolTester`のメソッド呼出しへスタブ介入を適用するAspect
- **Responsibilities**: `@Around`アドバイスによる`StubResolver`への委譲(既存`StubAspect`と同一ロジック)
- **Interfaces**: `@Aspect`+`@Component`。`@EnableAspectJAutoProxy`(`DemoApplication`)により有効化。`appctx-stub.xml`は移植しない
