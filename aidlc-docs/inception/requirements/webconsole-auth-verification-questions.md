# webconsole Basic認証 確認質問

以下の質問に回答してください。各質問の [Answer]: タグの後ろに、選択したアルファベットを記入してください。

## Question 1
認証情報(ユーザー名・パスワード)を扱うプロパティ設計はどちらが良いですか?

A) Spring Bootの標準プロパティ(`spring.security.user.name` / `spring.security.user.password`)をそのまま使う(Spring Security標準機構に乗る、実装量が最小)

B) 既存のAPIキー(`cherry.testtool.web.api-key`)と同じ名前空間の専用プロパティ(例: `cherry.testtool.web.auth.username` / `cherry.testtool.web.auth.password`)を新設する(webconsole固有の設定として一貫性を持たせる)

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 2
認証を適用する範囲はどこまでにしますか?

A) webconsole全体(SPA配信・静的アセット含む全パス)に認証をかける(未認証ではUIの表示すら不可)

B) APIエンドポイント(`/testtool/**`)のみに認証をかける(SPA自体の見た目は誰でも見えるが、操作時にAPIが401を返す)

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 3
認証情報(ユーザー名・パスワード)が未設定の場合の既定動作はどうしますか?

A) 認証なしで動作する(既存のAPIキー保護(`cherry.testtool.web.api-key`)と同じ、未設定時は現状通りの後方互換方針)

B) 認証情報を必須とし、未設定の場合はアプリ起動時にエラーとする

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 4
パスワードの保存方式はどうしますか?

A) 平文で設定ファイル等に記述する(ローカル開発ツールとしてのシンプルさを優先)

B) BCryptハッシュ化した値を設定ファイルに記述する(Spring Security標準の`{bcrypt}`プレフィックス対応、セキュリティを優先するが設定はやや煩雑になる)

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 5
既存のE2Eテスト(`e2e/tests/webconsole-ui.spec.ts`・`webconsole-api.spec.ts`等、実ブラウザ/HTTPでwebconsoleを操作するテスト)への対応はどうしますか?

A) Basic認証を有効化した専用のE2Eシナリオを追加する(既存シナリオは認証無効のまま維持し、並存させる)

B) 既存の全webconsole関連E2Eシナリオに認証情報を組み込む(Basic認証が既定で有効な運用を前提にする)

C) 今回のE2E対応は見送り、別途検討する

D) Other (please describe after [Answer]: tag below)

[Answer]: 
