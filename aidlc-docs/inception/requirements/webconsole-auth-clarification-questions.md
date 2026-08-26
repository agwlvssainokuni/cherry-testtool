# webconsole Basic認証 明確化質問

`webconsole-auth-verification-questions.md`の回答を分析した結果、Q1とQ3の組み合わせに技術的な矛盾が見つかりました。

## 矛盾: 標準プロパティ利用(Q1:A) vs 未設定時は認証なし(Q3:A)
Q1で「Spring Bootの標準プロパティ(`spring.security.user.name`/`spring.security.user.password`)をそのまま使う」(A)を選びましたが、Q3では「認証情報が未設定の場合は認証なしで動作する(後方互換)」(A)も選んでいます。

Spring Bootは`spring-boot-starter-security`への依存を追加した時点で、`UserDetailsServiceAutoConfiguration`により自動的にBasic認証が有効化されます。`spring.security.user.password`が未設定の場合でも認証が無効になるわけではなく、起動時にランダムパスワードが生成されログへ出力される、という既定動作になります。つまり「標準プロパティをそのまま使う」だけでは「未設定時は認証なしで動作する」を実現できません。

### 明確化質問 1
未設定時に認証を無効化する(Q3:Aを実現する)ために、どちらの対応にしますか?

A) 標準プロパティ(`spring.security.user.*`)の値の有無を見て、`SecurityFilterChain`の登録自体を条件分岐させるカスタムロジックを実装する(Spring Bootの自動生成パスワード機構には頼らない)

B) 代わりにQ1の回答をBに変更し、専用プロパティ(`cherry.testtool.web.auth.username`/`password`)を新設する。その有無で認証の有効/無効を明示的に制御する(既存のAPIキー保護(`cherry.testtool.web.api-key`)と同じ設計パターン)

C) Q3の回答をBに変更する(認証情報を必須とし、未設定時はアプリ起動時にエラーとする。Spring Bootの標準動作にそのまま委ねる)

D) Other (please describe after [Answer]: tag below)

[Answer]: B
