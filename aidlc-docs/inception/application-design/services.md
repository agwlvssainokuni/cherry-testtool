# Services

本プロジェクトは小規模なテストツールであり、独立したサービス層(Service Facade)を新設するのは`client/cli`のみである。他モジュールは既存のコンポーネント構成がそのままオーケストレーションの役割を担う。

## lib: InvokerService(既存のオーケストレーション)
- **Responsibilities**: Bean解決(`ReflectionResolver`)・スクリプト実行(`ScriptProcessor`)・メソッド呼出し・結果整形を1メソッド内でオーケストレーションする、既存構成を踏襲(具象クラス化以外の変更なし)
- **Interactions**: `ReflectionResolver`、`ScriptProcessor`、`ConversionService`(Spring標準)、`ApplicationContext`を呼び出す

## lib: StubResolver(既存のオーケストレーション)
- **Responsibilities**: `StubRepository`からの設定取得と`ScriptProcessor`によるスクリプト評価を仲介し、`StubInvocation`として`StubInterceptor`/デモの`StubAspect`へ提供する
- **Interactions**: `StubRepository`、`ScriptProcessor`

## client/cli: InvokeService(新設)
- **Responsibilities**: `invoke`サブコマンドの一括実行オーケストレーション。`ScriptFileScanner`でファイル一覧を取得し、各エントリについて`TesttoolApiClient.invoke(...)`を呼び出し、結果を集約して終了コードを決定する
- **Interactions**: `ScriptFileScanner`(ディレクトリ走査)、`ApiClientFactory`→`TesttoolApiClient`(REST呼出し)
- **Orchestration Pattern**: 逐次実行(旧`invoker.sh`のforループと同等の順序保証)。1件でも失敗した場合は非ゼロ終了コードを返す

## client/cli: StubConfigService(新設)
- **Responsibilities**: `stubconfig`サブコマンドの`register`/`clear`/`show`各モードの一括実行オーケストレーション
- **Interactions**: `ScriptFileScanner`、`ApiClientFactory`→`TesttoolApiClient`
- **Orchestration Pattern**: `InvokeService`と同様の逐次実行・終了コード決定方針

## client/webconsole: GatewayRouteConfig(設定によるオーケストレーション)
- **Responsibilities**: リクエストの`/**`パターンに対するルーティング、フィルタチェーン(セキュリティヘッダ付与、レスポンスヘッダ重複排除)の適用。宣言的ルート定義であり、独立したServiceクラスは持たない
- **Interactions**: Spring Cloud Gateway(Servlet/MVC版)のルーティング機構、`backend.uri`設定
