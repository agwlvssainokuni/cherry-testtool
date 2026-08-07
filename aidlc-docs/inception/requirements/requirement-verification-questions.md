# Requirements Clarification Questions

「既存機能を改善・リファクタリングしたい」という方向性を伺いました。具体的な対象範囲や制約を確認させてください。各質問の[Answer]:タグに選択肢の記号(複数可の場合はカンマ区切り)を記入してください。

## Question 1
今回のAI-DLCプロセスで対応したい対象を選んでください(複数選択可、カンマ区切り)。Reverse Engineeringで検出した技術的負債項目を選択肢にしています。

A) `client/gateway`に`settings.gradle`が無く、Gradleの既定ルートプロジェクト名に依存している点の是正

B) `lib`と`client/gateway`でJava/Spring Bootバージョンを別々の`build.gradle`で管理しており、将来の更新時に不整合が生じるリスクがある点の是正(バージョン一元管理の仕組み導入など)

C) `client/cli/invoker/`と`client/cli/invoker2/`に同名サンプルスクリプトが重複配置され、`invoker2`の用途が不明な点の整理

D) `StubRepositoryImpl`がインメモリのみで非永続、かつ`StubConfigLoader`によるファイル一括読込み機能がController層から呼び出されておらず経路が不明瞭な点の整理(永続化またはロード経路の明確化)

E) `InvokerServiceImpl`が`catch (Exception ex)`で全例外を握りつぶしレスポンス化している点の見直し(意図的仕様であれば維持しつつコメントを補うなど)

X) Other(上記に無い対象、または複数の組合せの場合は[Answer]:タグの後に具体的に記述してください)

[Answer]:

## Question 2
Question 1で複数選択した場合、今回のAI-DLCの1サイクルで全て対応しますか、それとも段階的に進めますか。

A) 選択した対象すべてを今回のサイクルで一括対応する

B) 優先度の高いものから1件ずつ、今回はまず1件のみ対応する(次回以降のサイクルで残りに対応)

C) Question 1で1件のみ選択したので該当しない

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Question 3
リファクタリング・改善にあたり、既存の外部インタフェース(REST APIのパス・パラメータ、SPA/CLIからの呼出し方法)への影響について、方針を教えてください。

A) 既存の外部インタフェースは一切変更しない(内部実装のみの改善に限定)

B) 必要であれば外部インタフェースの変更も許容する(SPA/CLI側の追随修正も合わせて実施)

C) まだ判断できない、提案してほしい

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Question 4
テストに関する期待値を教えてください。

A) 変更箇所に対応する単体テストの追加・更新のみで十分

B) 単体テストに加え、可能な範囲で結合テスト・手動確認手順も整備してほしい

C) 特にこだわりはない、AIの判断に任せる

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Question 5: Security Extensions
Security Baseline拡張を今回のプロセスに適用しますか。

A) Yes — SECURITYルールをブロッキング制約として全て適用する(本番相当のアプリケーションに推奨)

B) No — SECURITYルールは適用しない(PoC・試験的なプロジェクトに適する)

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Question 6: Resiliency Extensions
Resiliency Baseline拡張(AWS Well-Architected Frameworkの信頼性の柱に基づく方向性のベストプラクティス)を適用しますか。

A) Yes — 方向性のベストプラクティス・設計時ガイダンスとして適用する(ビジネスクリティカルなワークロードに推奨)

B) No — 適用しない(PoC・試作・実験的プロジェクトに適する)

X) Other(please describe after [Answer]: tag below)

[Answer]:

## Question 7: Property-Based Testing Extension
Property-Based Testing(PBT)ルールを今回のプロセスに適用しますか。

A) Yes — PBTルールをブロッキング制約として全て適用する(ビジネスロジック・データ変換・シリアライズ・状態を持つ処理を含む場合に推奨)

B) Partial — 純粋関数・シリアライズの往復変換のみPBTルールを適用する

C) No — PBTルールは適用しない(単純なCRUDやUIのみ、有意なビジネスロジックを含まない薄い連携層の場合に適する)

X) Other(please describe after [Answer]: tag below)

[Answer]:
