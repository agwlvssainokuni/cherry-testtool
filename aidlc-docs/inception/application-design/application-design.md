# Application Design(統合サマリー)

`aidlc-docs/inception/requirements/requirements.md`(FR1-FR8, NFR1-NFR5)を踏まえた、4モジュール(`lib`、`client/webconsole`、`client/cli`、`demo`)のコンポーネント設計。詳細は各分割文書を参照。

- コンポーネント定義: [components.md](components.md)
- メソッドシグネチャ: [component-methods.md](component-methods.md)
- サービス層: [services.md](services.md)
- 依存関係: [component-dependency.md](component-dependency.md)

## 設計決定の要点

1. **lib**: `InvokerService`/`ReflectionResolver`/`ScriptProcessor`/`StubRepository`/`StubResolver`をInterface削除・具象クラス化(FR4)。加えて`InvokerController`/`StubConfigController`を`TesttoolController`へ統合し、`bean`/`method`解決APIを新設共通パス(`/testtool/resolve/bean`,`/testtool/resolve/method`)へ一本化、`@ConditionalOnProperty`も単一トグルへ統合(FR8)。

2. **client/webconsole**(新設、パッケージ`cherry.testtool.webconsole`): Spring MVC + Spring Cloud Gateway Servlet版(`GatewayRouteConfig`)によるAPIプロキシと、Spring Boot標準の静的リソース配信によるSPAホスティングを1モジュールに統合。フロントエンド(旧`client/spa`)はサブディレクトリとして同居。待受ポート9090。

3. **client/cli**(新設、パッケージ`cherry.testtool.cli`): Picocliの`RootCommand`(→`InvokeCommand`/`StubConfigCommand`)を薄い層とし、`InvokeService`/`StubConfigService`が`ScriptFileScanner`(ディレクトリ走査)と`TesttoolApiClient`(`HttpServiceProxyFactory`+`@HttpExchange`による宣言的HTTPクライアント、内部トランスポートは`RestClient`)を用いて処理を実行する構成。`CliApplication`が`CommandLineRunner`と`ExitCodeGenerator`を兼ねる。

4. **demo**(新設、リポジトリ直下`demo/`、パッケージ`cherry.testtool.demo`): `lib`へのコンパイル依存を持つ最小Spring Bootアプリ。`lib/src/test`の`ToolTester`/`ToolTesterImpl`(Interface+Impl構成のまま、FR4の対象外)と`StubAspect`(アノテーションベース、`@EnableAspectJAutoProxy`、XML設定`appctx-stub.xml`は移植しない)を移管。

## モジュール間の関係

`client/webconsole`・`client/cli`は`lib`・`demo`に対してビルド時の依存を持たず、HTTP通信のみで疎結合を維持する(既存構成の設計思想を踏襲)。ビルド順序上の制約は「`lib`→`demo`」のみ(`demo`が`lib`にコンパイル依存するため)。

## 設計の完全性・整合性検証

- [x] Requirements Analysis(FR1-FR8, NFR1-NFR5)の全項目が、いずれかのコンポーネントの責務としてマッピングされていることを確認
- [x] パッケージ命名の重複(`lib`の`cherry.testtool.web`と新設モジュール)が解消されていることを確認(`cherry.testtool.webconsole`/`cherry.testtool.cli`/`cherry.testtool.demo`)
- [x] `client/webconsole`・`client/cli`のビルド時非依存という既存設計思想が維持されていることを確認
- [x] Application Design Questionsで生じた曖昧点(デモのAOP方式、CLIのHTTPクライアント)がいずれも技術調査により解消され、コンポーネント設計に反映されていることを確認
