# Application Design Plan

`aidlc-docs/inception/requirements/requirements.md`(FR1-FR7, NFR1-NFR5)を踏まえ、`lib`・`client/webconsole`(新設)・`client/cli`(新設)・`demo`(新設)の4モジュールについて、コンポーネント識別・メソッドシグネチャ・サービス層・依存関係を設計します。

## 設計方針(あらかじめ決定済みの事項)

以下は要件定義で既に確定しているため、設計上の前提として扱います。

- `lib`: `InvokerService`/`ReflectionResolver`/`ScriptProcessor`/`StubRepository`/`StubResolver`はInterface削除、`Impl`無しの具象クラスへ統合(FR4)
- `client/webconsole`: Spring MVC + Spring Cloud Gateway Servlet版、SPA静的配信、待受ポート9090(FR1,FR2,FR2.7)
- `client/cli`: Picocli(`invoke`/`stubconfig`サブコマンド)+ `CommandLineRunner`兼`ExitCodeGenerator`(FR5.5,FR5.6)
- `demo`: リポジトリ直下`demo/`ディレクトリ、libへの依存、既定ポート8080(FR6.1)
- 全モジュール共通: JSpecify(`@NullMarked`+`org.jspecify.annotations.Nullable`)、Javadoc/コメント充実(FR7,NFR5)

## 生成する成果物(Mandatory Design Artifacts)

- [ ] `aidlc-docs/inception/application-design/components.md` — コンポーネント定義・責務
- [ ] `aidlc-docs/inception/application-design/component-methods.md` — メソッドシグネチャ(詳細な業務ルールはFunctional Designで扱う)
- [ ] `aidlc-docs/inception/application-design/services.md` — サービス定義・オーケストレーション
- [ ] `aidlc-docs/inception/application-design/component-dependency.md` — 依存関係マトリクス・通信パターン
- [ ] `aidlc-docs/inception/application-design/application-design.md` — 上記を統合したサマリー文書
- [ ] 設計の完全性・整合性の検証

## 設計対象モジュールと主なコンポーネント(たたき台)

### lib(Interface統合後)
- `TesttoolConfiguration`(Bean定義。具象クラスを直接返すよう更新)
- `InvokerService`(具象クラス、旧`InvokerServiceImpl`)
- `ReflectionResolver`(具象クラス)
- `ScriptProcessor`(具象クラス)
- `StubRepository`(具象クラス)
- `StubResolver`(具象クラス)
- `StubConfigLoader`、`StubInterceptor`、`StubConfig`、`StubInvocation`(変更なし、コメント充実・JSpecify化のみ)
- `InvokerController`、`StubConfigController`(呼出し先を具象クラス型に更新)

### client/webconsole(新設)
- メインクラス(`@SpringBootApplication`、Spring MVC)
- ルーティング設定コンポーネント(Spring Cloud Gateway Servlet版によるプロキシルート定義。現行`client/gateway`のCORS・レスポンスヘッダ重複排除設定を踏襲)
- 静的リソース配信設定(SPAビルド成果物の配信)
- フロントエンド(旧`client/spa`相当、React/TypeScript/Vite。`client/webconsole`配下のサブディレクトリとして同居)

### client/cli(新設)
- メインクラス(`CommandLineRunner`兼`ExitCodeGenerator`実装、Picocliの`CommandLine`を実行)
- Picocliルートコマンド
- `invoke`サブコマンド(旧`invoker.sh`相当)
- `stubconfig`サブコマンド(`register`/`clear`/`show`モード、旧`stubconfig.sh`相当)
- ディレクトリ走査ロジック(クラス名/メソッド名/メソッドインデックスの抽出、旧シェルスクリプトの処理を移植)
- REST API呼出しクライアント(BASIC認証・追加HTTPヘッダ対応)

### demo(新設)
- メインクラス(`@SpringBootApplication`、`lib`へのコンパイル依存)
- サンプル業務コンポーネント(`ToolTester`相当、`lib/src/test`から移管)
- スタブ介入の適用設定(下記Question 1で方式を確定)

## Application Design Questions

以下の[Answer]:タグに回答してください。選択肢が無い場合はX(Other)を選び、具体的に記述してください。

### Question 1: デモアプリでのスタブ介入の実現方式
現行`lib/src/test`には2つのスタブ介入方式が存在します。(a) `lib`本体が提供する`StubInterceptor`(AOP Alliance `MethodInterceptor`、本番相当の利用方法)、(b) テストコードのみにあった`StubAspect`(AspectJ `@Around`、宣言的アノテーションベース)。デモアプリではどちらを採用しますか。

A) `lib`本体が提供する`StubInterceptor`を、Spring AOPの`Advisor`/`ProxyFactory`等を用いて明示的に適用する構成にする(利用者が`lib`を実際に組み込む際の標準的な使い方を示すデモとして適切)

B) 現行の`StubAspect`(AspectJ `@Around`)パターンをデモアプリへそのまま移植する

C) 両方式を並行してデモに含め、利用者が選べるようにする

X) Other(please describe after [Answer]: tag below)

[Answer]:

### Question 2: 新設3モジュールのJavaパッケージ名
`lib`は`cherry.testtool.*`を使用しており、`client/webconsole`は`cherry.testtool.web`(lib内のController群と重複)を避ける必要があります(requirements.md Architectural Considerations参照)。以下のパッケージ名案でよいか確認してください。

- `client/webconsole`: `cherry.testtool.webconsole`
- `client/cli`: `cherry.testtool.cli`
- `demo`: `cherry.testtool.demo`

A) 上記の案でよい

X) Other(please describe after [Answer]: tag below、変更したいパッケージ名を記述)

[Answer]:

### Question 3: client/cliのHTTPクライアント選定
REST API呼出しに使うHTTPクライアントを選んでください。

A) `RestClient`(Spring Framework 6.1以降の同期クライアント、Spring Boot 4.1世代における推奨的な選択肢)を採用する(推奨)

B) `RestTemplate`(従来からの同期クライアント)を採用する

X) Other(please describe after [Answer]: tag below)

[Answer]:

### Question 4: client/cliの内部レイヤリング
Picocliの`@Command`クラス(CLI引数解析)と、ディレクトリ走査・REST呼出しロジックの関係をどう設計しますか。

A) Picocliコマンドクラスは引数解析・出力表示に専念する薄い層とし、ディレクトリ走査・REST呼出しの実処理は別途Serviceクラス(例: `InvokeService`、`StubConfigService`)に委譲する(推奨。`lib`の`InvokerService`的な構成に揃えたテスト容易性の高い設計)

B) Picocliコマンドクラス自体に処理ロジックを直接実装する(シンプルだがテスト容易性はやや劣る)

X) Other(please describe after [Answer]: tag below)

[Answer]:
