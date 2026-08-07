# Requirements Document

## Intent Analysis Summary
- **User Request**: cherry-testtoolの既存機能の改善・リファクタリング。具体的には (1) `client/gateway`のビルド構成是正、(2) `client/spa`と`client/gateway`の統合(新モジュール`client/webconsole`)、(3) `InvokerServiceImpl`の意図的な例外処理へのコメント補足、(4) `lib`内のInterface/Impl分離の解消、(5) `client/cli`のSpring Bootアプリ化、(6) `lib`を組み込むデモアプリの新設
- **Request Type**: Refactoring + Enhancement + Migration(複合)
- **Scope Estimate**: System-wide(既存4モジュール中3モジュールに変更が及び、新規2モジュールを追加)
- **Complexity Estimate**: Complex

## Functional Requirements

### FR1: client/gatewayのビルド構成是正
`client/gateway`(統合後は`client/webconsole`)に`settings.gradle`を追加し、`lib`と同様に`rootProject.name`を明示する。

### FR2: client/spaとclient/gatewayの統合(client/webconsole)
`client/spa`と`client/gateway`を新規モジュール`client/webconsole`へ統合する。

- **FR2.1**: `client/webconsole`はSpring MVC(Servlet)ベースのSpring Bootアプリケーションとする(現行`client/gateway`のWebFlux版から変更)。
- **FR2.2**: APIプロキシ機能はSpring Cloud Gatewayのサーブレット版(`spring-cloud-starter-gateway-server-webmvc`)を用いて実現する。プロキシ対象は`/testtool/**`のみに限定する(現行`client/gateway`の`/**`から変更。静的リソース配信・SPAフォールバックとの競合を避けるため)。ルート定義はJava(Functional Route、`GatewayRouterFunctions`+`RequestPredicates`+`HandlerFunctions.http(...)`)で記述する。既存`client/gateway`のセキュリティヘッダ付与・レスポンスヘッダ重複排除の設定内容は踏襲するが、CORS設定はFR2.6により不要となるため踏襲しない。
- **FR2.3**: SPAのビルド成果物(静的ファイル)は、Spring Bootの標準的な静的リソース配信の仕組みで配信する。
- **FR2.4**: `client/webconsole`ディレクトリ配下に、Spring Boot本体のJavaプロジェクトと、フロントエンド(旧`client/spa`相当のReact/TypeScript/Viteプロジェクト)を同居させる。ビルド時にフロントエンドをビルドし、その成果物をJava側の静的リソースとして組み込む(具体的な組込み手順はApplication Design/Functional Designで確定する)。
- **FR2.5**: 既存の`client/gateway`・`client/spa`ディレクトリは、`client/webconsole`へ統合完了後に廃止する。
- **FR2.6**: `client/webconsole`の`settings.gradle`で`rootProject.name`を`cherry-testtool-webconsole`に設定する。
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
- **FR5.4**: `settings.gradle`で`rootProject.name`を`cherry-testtool-cli`に設定する。
- **FR5.5**: Picocli(`picocli-spring-boot-starter`)を用いて実装する。`invoker.sh`/`stubconfig.sh`相当の機能をそれぞれ`invoke`サブコマンド・`stubconfig`サブコマンド(`register`/`clear`/`show`モード)としてマッピングし、`CommandLineRunner`からPicocliの`CommandLine`を実行する構成とする。
- **FR5.6**: 異常終了時に終了コードで判別できるよう、`CommandLineRunner`を実装するクラスに`ExitCodeGenerator`も併せて実装する(`run()`実行時にPicocliの実行結果の終了コードをフィールドへ保持し、`getExitCode()`で返す)。`main()`側で`SpringApplication.exit(context)`により当該終了コードを取得し、`System.exit(...)`に反映する。

### FR6: libを組み込むデモアプリの新設
`lib`を組み込むデモアプリケーションを新規モジュールとして追加する。

- **FR6.1**: `lib`を依存追加した最小構成のSpring Bootアプリケーション(既定ポート8080)とする。配置場所はリポジトリ直下の`demo`ディレクトリとし、`lib`と同じ階層に置く(`client/`配下ではない)。
- **FR6.2**: `lib/src/test`に現在配置されている検証用フィクスチャ(`ToolTester`/`ToolTesterImpl`/`StubAspect`/`TestMain`等)をデモアプリへ移管してよい。全面移管・部分移管(libの単体テストに必要な最小限を残す等)は実装時の判断に委ねる。
- **FR6.3**: デモアプリは、`client/webconsole`のプロキシ先、および`client/cli`の呼出し先として、一連の動作確認に用いる。
- **FR6.4**: `settings.gradle`で`rootProject.name`を`cherry-testtool-demo`に設定する。

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
- **各モジュールのGradleプロジェクト名(`rootProject.name`)・待受ポート**:
  | モジュール | ディレクトリ | rootProject.name | 待受ポート |
  |---|---|---|---|
  | lib | `lib` | `cherry-testtool`(既存のまま) | -(ライブラリのため無し) |
  | webconsole | `client/webconsole` | `cherry-testtool-webconsole` | `9090`(現行8070から変更) |
  | cli | `client/cli` | `cherry-testtool-cli` | -(CLIのため無し) |
  | demo | `demo`(リポジトリ直下、`lib`と同じ階層) | `cherry-testtool-demo` | `8080`(既定) |
- **パッケージ命名の重複回避**: `lib`内で既に`cherry.testtool.web`パッケージ(Controller群)を使用しているため、`client/webconsole`のJavaパッケージ名は別名とする(具体名はApplication Design/Functional Designで確定)。
- **設定の引継ぎ**: `client/gateway`が持つCORS・ルーティング・レスポンスヘッダ重複排除の設定は、`client/webconsole`への統合時に引き継ぐ。
- **デモアプリとの関係**: `client/webconsole`のプロキシ先(現行`backend.uri`に相当)は、新設するデモアプリを既定値として想定する。
- **スタブ介入方式の見直し(Unit 1実装時に確定)**: `StubInterceptor`(AOP Alliance `MethodInterceptor`、利用側でXML等によるpointcut配線が必要)は`@Deprecated`とし後方互換のため残置する。代わりに、アノテーションベースの`@Aspect`+`@Around`パターン(`StubAspect`)を正規の推奨方式とする。`StubAspect`自体は`lib/src/main`へは昇格させず、組み込み方の手引書と共にデモアプリ(Unit 2)側のリファレンス実装として提供する。これに伴い、XML設定`appctx-stub.xml`と対応するテスト(`StubInterceptorTest`)は`lib`から削除する。
- **トレースアスペクトのアノテーション化(Unit 1実装時に確定)**: メソッド呼出しトレースを行う`appctx-trace.xml`(XML設定の`CustomizableTraceInterceptor`)を廃止し、`@Aspect`+`@Around`によるアノテーションベースの`TraceAspect`(`lib/src/test`)へ置換する。`@Value`の各設定項目には旧XML設定と同値をデフォルト値として埋め込む。
- **ToolTesterのInterface統合(Unit 1実装時に確定)**: テストフィクスチャ`ToolTester`(interface)/`ToolTesterImpl`も、`lib`本体の5組(FR4)と同じ方針でImpl無しの具象クラス`ToolTester`へ統合する。

## Summary
既存4モジュール構成(lib, gateway, spa, cli)を、(1) libのInterface/Impl分離解消による簡素化、(2) gatewayとspaを統合した`client/webconsole`(プロジェクト名`cherry-testtool-webconsole`)への再編(Spring MVC + Spring Cloud Gateway Servlet版)、(3) cliのSpring Bootアプリ化、(4) libを組み込むデモアプリの新設、(5) libの`InvokerController`/`StubConfigController`を`TesttoolController`へ統合しbean/method解決APIを共通パスへ一本化(FR8)、を伴う形に再構成する、システム全体に及ぶリファクタリングである。加えて、変更対象コード全般でコメントの充実(FR7)とNullability規約の統一(NFR5: 原則非null、null許容箇所のみ`@Nullable`)を行う。外部インタフェースの変更は許容し、拡張機能(Security/Resiliency/PBT)は適用しない。規模が大きいため、Application Design/Units Generationステージを経て複数Unitに分解し、段階的に構築を進める。
