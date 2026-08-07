# Requirements Clarification Questions

Question 1への回答が当初の技術的負債の是正(A, E)に加えて、アーキテクチャ変更(SPA+Gateway統合、Interface廃止、CLIのSpring Boot化、デモアプリ新設)を含む広範な内容でした。要件定義書を作成する前に、具体的な実現方式を確認させてください。

## Clarification 1: SPAとGatewayの統合方式
`client/spa`と`client/gateway`を統合し、「Spring BootアプリからSPAを配信しつつAPIをproxyする」とのことですが、具体的な実現方式を選んでください。

A) `client/gateway`のビルド時に`client/spa`を`npm run build`し、生成された静的ファイル(dist/)をgatewayのクラスパス上の静的リソースとして同梱して配信する。Spring Cloud Gateway(WebFlux)はAPIプロキシ用途のまま維持し、静的リソース配信機能を追加する

B) `client/gateway`を素のSpring MVC(Servlet)ベースのアプリに作り替え、静的リソース配信は標準のSpring Boot機能に任せ、`/testtool/**`のみリバースプロキシ(RestTemplate/WebClient、またはSpring Cloud Gateway MVC版)を追加する

C) `client/spa`モジュールは開発用(`npm run dev`)として現状のまま残しつつ、本番ビルド成果物のみを`client/gateway`に同梱する二重構成にする(開発時はSPA単体+CORS、本番はGateway同梱配信)

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Clarification 2: `client/spa`モジュール自体の扱い
統合後、`client/spa`ディレクトリ・Gradle/npmビルド構成はどうなりますか。

A) `client/spa`は開発用ビルド(Vite dev server)としてこれまで通り独立して存在し続け、`client/gateway`はその成果物(dist/)を取り込むだけ

B) `client/spa`は`client/gateway`のサブディレクトリ/リソースとして統合し、独立したnpmプロジェクトとしては廃止する

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Clarification 3: Interface統合後のクラス構成
`InvokerService`/`InvokerServiceImpl`等、インタフェースと実装に分かれている5組(Invoker/Reflect/Script/StubRepository/StubResolver)を実装のみへ統合する際のクラス名方針を選んでください。

A) 実装クラス名をインタフェース名(`Impl`無し)にリネームする(例: `InvokerServiceImpl` → 具象クラス`InvokerService`)。呼出し側の型参照も全て具象クラス名に変更

B) `Impl`サフィックス付きのクラス名をそのまま残し、インタフェースのみ削除する(例: `InvokerServiceImpl`という名前の具象クラスとして残す)

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Clarification 4: CLIのSpring Boot化の仕様
`client/cli`を`invoker.sh`/`stubconfig.sh`からSpring Bootアプリ(CLI)に置き換える際の仕様を選んでください。

A) 既存シェルスクリプトと同じオプション体系(`-l URL`、`-u BASIC認証`、`-H ヘッダ`、`stubconfig`の`-r`/`-c`モード等)と引数(スタブ設定ディレクトリのパス)を踏襲した`CommandLineRunner`ベースのSpring Bootアプリ(実行可能jar、`java -jar`で起動)にする

B) オプション体系を刷新してよい(刷新したい内容があれば[Answer]:タグの後に具体的に記述してください)

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Clarification 5: デモアプリの位置づけ
libを組み込むデモアプリについて、配置場所と内容を選んでください。

A) 新規モジュール(例: `demo/`または`client/demo`)としてリポジトリ直下に配置し、`lib`を依存追加した最小Spring Bootアプリ(既定ポート8080)とする。`lib/src/test`にある`ToolTester`/`ToolTesterImpl`相当のサンプルBeanを含め、呼出し・スタブの動作を手動確認できるようにする

B) 既存の`lib/src/test`配下のフィクスチャ(`ToolTester`等)を拡張し、`lib`とは別モジュール化した上でデモアプリとしても使えるようにする

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Clarification 6: 作業の分解方針
Question 1の回答は、(1)gateway `settings.gradle`追加、(2)SPA+Gateway統合、(3)例外コメント補足、(4)Interface廃止、(5)CLIのSpring Boot化、(6)デモアプリ新設、という6項目に及ぶ広範な内容です。規模が大きいため、Application Design(コンポーネント設計)およびUnits Generation(複数の作業単位への分解)ステージを実行し、Unit単位で段階的に設計・実装を進めることを推奨します。この進め方でよいですか。

A) はい、Application Design/Units Generationを実行し、複数Unitに分解して段階的に進めてよい(推奨)

B) いいえ、分解せず一つの塊として一気に進めてほしい

X) Other(please describe after [Answer]: tag below)

[Answer]:
