# Component Inventory

## Application Packages
- `client/gateway` - SPAとテスト対象アプリを中継するSpring Cloud Gatewayアプリケーション
- `client/spa` - メソッド呼出し・スタブ設定用のReact SPAアプリケーション

## Infrastructure Packages
- なし(CDK/Terraform/CloudFormation等のIaC定義は未検出)

## Shared Packages
- `lib` - Models/Utilities/Clientsを兼ねる、テスト対象アプリに組み込むSpring Boot自動構成ライブラリ(InvokerService, StubResolver, ScriptProcessor, ReflectionResolver, REST Controllerを提供)

## Test Packages
- `lib/src/test` - Unit(`InvokerServiceTest`、`ReflectionResolverTest`、`ScriptProcessorTest`、`StubInterceptorTest`、`StubRepositoryTest`)、およびAOPスタブ検証用フィクスチャ(`ToolTester`、`ToolTesterImpl`、`StubAspect`、`TestMain`)
- `client/cli` - Integration/Manual(REST APIを実際に呼び出す手動実行用シェルスクリプト。自動テストではない)

## Total Count
- **Total Packages**: 4(lib, client/gateway, client/spa, client/cli)
- **Application**: 2(client/gateway, client/spa)
- **Infrastructure**: 0
- **Shared**: 1(lib)
- **Test**: 1(client/cli。lib内のテストソースは独立パッケージとしてはカウントせず、libの一部として扱う)
