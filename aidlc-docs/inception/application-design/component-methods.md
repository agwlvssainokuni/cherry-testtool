# Component Methods

詳細な業務ルールはFunctional Design(Unit毎、CONSTRUCTION phase)で定義する。ここではメソッドシグネチャと概要のみを示す。

## lib

### TesttoolController(新設、`InvokerController`+`StubConfigController`統合、FR8)
- `@RequestMapping("invoker/invoke") String invoke(...)` — 現行`InvokerController.invoke`と同一シグネチャ・同一パス
- `@RequestMapping("stubconfig/put") String putStubConfig(...)` — 現行`StubConfigController.putStubConfig`と同一シグネチャ・同一パス
- `@RequestMapping("stubconfig/get") List<String> getStubConfig(...)` — 現行`StubConfigController.getStubConfig`と同一シグネチャ・同一パス
- `@RequestMapping("stubconfig/list") List<String> getStubbedMethod(...)` — 現行`StubConfigController.getStubbedMethod`と同一シグネチャ・同一パス
- `@RequestMapping("resolve/bean") List<String> resolveBeanName(String className)` — 現行`InvokerController`/`StubConfigController`双方の`bean`エンドポイントを統合した新パス
- `@RequestMapping("resolve/method") List<String> resolveMethod(String className, String methodName)` — 現行`InvokerController`/`StubConfigController`双方の`method`エンドポイントを統合した新パス

### InvokerService(具象クラス)
- `String invoke(@Nullable String beanName, Class<?> beanClass, Method method, String script, @Nullable String engine)` — 解決済みのBean/Methodに対して直接呼出しを実行する
- `String invoke(@Nullable String beanName, String className, String methodName, int methodIndex, String script, @Nullable String engine)` — クラス名・メソッド名からメソッドを解決した上で呼出しを実行する(例外はYAML化して返却)

### ReflectionResolver(具象クラス)
- `List<String> resolveBeanName(Class<?> beanClass)` — 指定クラスに対応するBean名一覧を返す
- `List<String> resolveBeanName(String beanClassName)` — FQCN文字列からクラスをロードした上で上記に委譲する(`ClassNotFoundException`をスロー)
- `List<Method> resolveMethod(Class<?> beanClass, String methodName)` — 指定クラス・メソッド名に一致する宣言メソッド一覧を返す
- `List<Method> resolveMethod(String beanClassName, String methodName)` — FQCN文字列版

### ScriptProcessor(具象クラス)
- `<T> T eval(String script, @Nullable String engine, Object... args)` — スクリプトを評価し結果を返す(`ScriptException`をスロー)

### StubRepository(具象クラス)
- `List<Method> getStubbedMethod()` — スタブ登録済みメソッド一覧
- `boolean contains(Method method)` — 登録有無の判定
- `void clear(Method method)` — 登録解除
- `@Nullable StubConfig get(Method method)` — 登録内容の取得
- `void put(Method method, StubConfig stubConfig)` — 登録/更新

### StubResolver(具象クラス)
- `Optional<StubInvocation> getStubInvocation(Method method)` — メソッドからスタブ実行を解決する
- `Optional<StubInvocation> getStubInvocation(MethodInvocation invocation)` — AOP Alliance呼出しからの解決(デフォルトメソッド相当)
- `Optional<StubInvocation> getStubInvocation(ProceedingJoinPoint pjp)` — AspectJ呼出しからの解決(デフォルトメソッド相当)

## client/webconsole

### GatewayRouteConfig
- `RouterFunction<ServerResponse> testtoolRoute()` — `/testtool/**`のみをbackend URIへプロキシするJava Functional RouteのBean定義(`GatewayRouterFunctions.route(...)`、CORSフィルタは含めず、セキュリティヘッダ付与・レスポンスヘッダ重複排除フィルタのみ現行`client/gateway`を踏襲)

### SpaFallbackResourceResolver
- `Resource getResource(String resourcePath, Resource location)` — `PathResourceResolver`のオーバーライド。リクエストされたリソースが存在すればそのまま返し、存在しなければ`index.html`を返す

## client/cli

### InvokeCommand
- `Integer call()` — Picocli実行エントリ。オプション(接続先URL、BASIC認証、追加ヘッダ、スクリプト設定ディレクトリ)を`InvokeService`へ渡し、終了コードを返す

### StubConfigCommand
- `Integer call()` — Picocli実行エントリ。モード(`register`/`clear`/`show`)とオプションを`StubConfigService`へ渡し、終了コードを返す

### InvokeService
- `int invokeAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers)` — 指定ディレクトリ群を走査し、各スクリプトファイルに対応する呼出しAPIを実行する。失敗件数等から終了コードを算出する

### StubConfigService
- `int registerAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers)` — スタブ登録一括実行
- `int clearAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers)` — スタブ解除一括実行
- `int showAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers)` — 現在のスタブ内容一括表示

### ScriptFileScanner
- `List<ScriptFileEntry> scan(Path directory)` — ディレクトリ配下の`*.js`ファイルを走査し、`ScriptFileEntry`(ファイルパス・クラス名・メソッド名・メソッドインデックス)のリストを返す

### TesttoolApiClient(HTTPインタフェース、`@HttpExchange`)
- `@PostExchange("/invoker/invoke") String invoke(@RequestParam ... , @RequestHeader MultiValueMap<String,String> headers)`
- `@PostExchange("/stubconfig/put") String putStub(@RequestParam ..., @RequestHeader MultiValueMap<String,String> headers)`
- `@PostExchange("/stubconfig/get") List<String> getStub(@RequestParam ..., @RequestHeader MultiValueMap<String,String> headers)`

### ApiClientConfig
- `@Bean @Scope("prototype") TesttoolApiClient testtoolApiClient(URI baseUri)` — `baseUri`を基に`RestClient`→`HttpServiceProxyFactory`→`TesttoolApiClient`プロキシを構築するBean定義(引数付き`getBean`呼出しでのみ生成される)

### ApiClientFactory
- `TesttoolApiClient create(URI baseUri)` — `applicationContext.getBean(TesttoolApiClient.class, baseUri)`を呼び出すのみ

## demo

### ToolTester(インタフェース) / ToolTesterImpl
- 現行`lib/src/test`の`ToolTester`インタフェースのメソッド群(`toBeInvoked0`〜`toBeInvoked6`、`toBeStubbed1`〜`toBeStubbed2`)をそのまま移管する

### StubAspect
- `Object around(ProceedingJoinPoint pjp)` — `@Around("execution(* cherry.testtool.demo.ToolTester.*(..))")`。現行`lib/src/test`の`StubAspect`と同一ロジック
