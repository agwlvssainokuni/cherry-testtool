# demo+クライアント(cli/webconsole)のE2Eテスト追加 - 確認質問

依存ライブラリのバージョンアップによる挙動変化を摘出できる、demo(バックエンド)+client/cli+client/webconsoleをまたぐ一気通貫(E2E)の自動テストを追加するにあたり、以下を確認させてください。

## Question 1
E2Eテストの対象経路はどこまでにしますか？

A) cli経由のみ(`invoke`・`stubconfig register/show/clear`)を対象にする

B) webconsole経由のみ(SPA/プロキシ→demoへのHTTP呼出し)を対象にする

C) 両方(cli・webconsoleいずれも)を対象にする

D) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 2
webconsole側をE2Eテストに含める場合、どのレベルで検証しますか？

A) 実ブラウザ自動操作(Playwright等)でSPAのクリック・入力操作まで含めて検証する

B) HTTPレベルのみ(SPAの画面操作は介さず、webconsoleのAPIプロキシ層をRestTemplate/WebTestClient等で直接検証する)

C) 両方を段階的に整備する(まずHTTPレベルを整備し、将来的にブラウザE2Eを追加する)

D) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 3
スタブ設定の「効果」まで検証しますか(`stubconfig register`でスタブ登録した内容が、実際に`invoke`の結果へ反映されることまで確認する)？

A) 含める(register→対象メソッドの`invoke`実行→スタブ値が返ることを確認、さらに`clear`後は元の計算結果に戻ることも確認する)

B) 含めない(各APIの呼出し・レスポンス形式が正しいことのみ確認し、スタブの実効果は対象外とする)

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 4
このE2Eテストは、通常のビルド(`./gradlew build`または`./gradlew check`)に組み込みますか？

A) 別Gradleタスク(例: `e2eTest`)として分離し、通常のbuild/checkには含めない(複数プロセスの起動を伴い時間がかかる・環境依存で不安定になりうるため)

B) 通常のbuild/checkに含め、常に実行されるようにする

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 5
実行環境・トリガーの想定はどちらですか？

A) ローカル開発者が手動実行する想定(現行の「手動結合確認手順」をコード化・自動実行可能にする位置づけ。CI組み込みは今回のスコープ外)

B) CI(GitHub Actions等)での自動実行も見据えて設計する(今回のスコープに含める)

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 6
API保護機能(FR9のAPIキー、FR10のトレースログ)との組み合わせも検証対象に含めますか？

A) 含めない(APIキー未設定の既定状態のみを対象にする。認証絡みの検証は今回のスコープ外)

B) 含める(APIキー設定時・未設定時の両方をE2Eシナリオに含める)

C) Other (please describe after [Answer]: tag below)

[Answer]: 
