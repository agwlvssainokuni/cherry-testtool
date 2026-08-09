# `/testtool/**` APIキー保護 - 確認質問

これまでの相談内容を踏まえた前提(承認済み):
- OAuth2/OIDC等の大掛かりな仕組みは採用しない
- `spring-boot-starter-security`等の重量級・オピニオン付き依存は`lib`へ追加しない(消費側アプリの既存Spring Security構成と衝突しうるため)。追加依存ゼロのカスタム`Filter`/`HandlerInterceptor`で実装する
- 標準の`Authorization`ヘッダは使わず、専用ヘッダ名(例: `X-Cherry-Testtool-Api-Key`)を新設する(消費側の認証方式・手前のリバースプロキシ等との名前空間衝突を避けるため)
- `lib`・`client/webconsole`・`client/cli`いずれも同じ考え方(`application.yml`ベースの構成項目)でAPIキーを扱う。`client/cli`が`--header`の都度指定に頼る現状は解消する
- 未設定時は現状通り検証をスキップする(後方互換。既定で機能を破壊しない)

上記を踏まえ、2点確認させてください。

## Question 1
`client/webconsole`自体のゲーティング範囲をどうしますか？(`client/webconsole`は`backend.api-key`のような構成項目でAPIキーを保持し、backendの`/testtool/**`へプロキシする際に自動付与する想定です)

A) `webconsole`はAPIキーを内部保持するのみとし、SPA利用者(ブラウザ)には別途キー入力を求めない。`webconsole`が「鍵を知っている信頼されたクライアント」として振る舞い、SPA経由のアクセスはそのまま通す(直接`/testtool/**`を叩く経路のみを防ぐ、最小限のスコープ)

B) `webconsole`自身もSPA利用者にAPIキーの入力(例: 初回アクセス時にプロンプト、以降はブラウザに保持)を求め、ブラウザ→webconsole間・webconsole→backend間の両方でキー検証を行う(実装コストは増えるがwebconsoleの入口自体も保護できる)

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 2
設定プロパティの命名は以下の案でよいですか？

- `lib`(サーバ側検証、`TesttoolAutoConfiguration`): `cherry.testtool.web.api-key`(未設定なら検証スキップ)
- `client/webconsole`(backendへの自動付与): `backend.api-key`(既存の`backend.protocol`/`host`/`port`と同じ並び)
- `client/cli`(リクエスト時の既定ヘッダ値、`--header`で個別上書きも引き続き可能): `cherry.testtool.cli.api-key`

A) この命名案のまま進めてよい

B) 変更したい(Other欄に具体的な命名を記載)

C) Other (please describe after [Answer]: tag below)

[Answer]: 
