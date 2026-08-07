# Unit of Work

Application Designで識別したモジュール構成をそのままUnit分解の基礎とする(モジュール = Unit)。着手順序は Unit 1 → Unit 2 → Unit 3 → Unit 4。

## Unit 1: lib

- **対象モジュール**: `lib`(既存、パッケージ`cherry.testtool.*`)
- **責務**: Interface/Impl分離の解消(具象クラス化)、`InvokerController`/`StubConfigController`の`TesttoolController`への統合、意図的な例外処理へのコメント補足、コメント充実、JSpecifyベースのNullability規約統一
- **主要コンポーネント**: `TesttoolConfiguration`、`InvokerService`、`ReflectionResolver`、`ScriptProcessor`、`StubRepository`、`StubResolver`、`StubConfigLoader`、`StubInterceptor`、`TesttoolController`
- **対応FR**: FR3, FR4, FR7(lib分), FR8, NFR5(lib分)
- **依存関係**: 他Unitに依存しない(最初に着手する理由: Unit 2がコンパイル依存するため)

## Unit 2: demo

- **対象モジュール**: `demo`(新設、リポジトリ直下、`lib`と同階層、パッケージ`cherry.testtool.demo`)
- **責務**: `lib`を組み込む最小Spring Bootアプリの新設、`ToolTester`/`ToolTesterImpl`と`StubAspect`(アノテーションベース、XML不使用)の`lib/src/test`からの移管
- **主要コンポーネント**: `DemoApplication`、`ToolTester`、`ToolTesterImpl`、`StubAspect`
- **対応FR**: FR6, FR7(demo分), NFR5(demo分)
- **依存関係**: Unit 1(lib)にコンパイル依存。Unit 1完了後に着手する

## Unit 3: webconsole

- **対象モジュール**: `client/webconsole`(新設、旧`client/gateway`+`client/spa`統合、パッケージ`cherry.testtool.webconsole`)
- **責務**: Spring MVC + Spring Cloud Gateway Servlet版による`/testtool/**`限定プロキシ(Java Functional Route)、SPAフォールバック付き静的リソース配信、フロントエンド(旧spa)の同居、Vite dev server proxyによるCORS不要化
- **主要コンポーネント**: `WebconsoleApplication`、`GatewayRouteConfig`、`SpaFallbackResourceResolver`、フロントエンド(旧`client/spa`相当)
- **対応FR**: FR1, FR2, FR7(webconsole分), NFR5(webconsole分)
- **依存関係**: ビルド時はUnit 1/2に非依存。結合確認(手動確認)にはUnit 2(demo)の起動が必要

## Unit 4: cli

- **対象モジュール**: `client/cli`(新設、旧シェルスクリプトから全面書換え、パッケージ`cherry.testtool.cli`)
- **責務**: Picocliベースのサブコマンド構成(`invoke`/`stubconfig`)、`HttpServiceProxyFactory`+`@HttpExchange`による宣言的HTTPクライアント(`TesttoolApiClient`、Spring管理prototype Bean)、`ExitCodeGenerator`による終了コード制御
- **主要コンポーネント**: `CliApplication`、`RootCommand`、`InvokeCommand`、`StubConfigCommand`、`InvokeService`、`StubConfigService`、`ScriptFileScanner`、`TesttoolApiClient`、`ApiClientConfig`、`ApiClientFactory`
- **対応FR**: FR5, FR7(cli分), NFR5(cli分)
- **依存関係**: ビルド時はUnit 1/2に非依存。結合確認(手動確認)にはUnit 2(demo)の起動が必要

## 着手順序

```
Unit 1(lib) → Unit 2(demo) → Unit 3(webconsole) → Unit 4(cli)
```

Unit 1→Unit 2は依存関係上必須の順序。Unit 3・Unit 4は互いに依存が無いが、AI-DLCのPer-Unit Loopに従い明示的な順序(webconsole→cli)で1つずつ完了させる。
