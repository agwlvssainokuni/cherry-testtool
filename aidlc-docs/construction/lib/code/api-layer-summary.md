# API Layer Summary - lib(Unit 1, Step 5-7)

## 変更内容(FR8)

`InvokerController`と`StubConfigController`を`TesttoolController`へ統合した。

| エンドポイント | 変更内容 |
|---|---|
| `/testtool/invoker/invoke` | 現行のまま維持(`TesttoolController`内のメソッドとして実装) |
| `/testtool/stubconfig/put` | 現行のまま維持 |
| `/testtool/stubconfig/get` | 現行のまま維持 |
| `/testtool/stubconfig/list` | 現行のまま維持 |
| `/testtool/invoker/bean`、`/testtool/stubconfig/bean` | 廃止し`/testtool/resolve/bean`へ一本化(重複実装を解消) |
| `/testtool/invoker/method`、`/testtool/stubconfig/method` | 廃止し`/testtool/resolve/method`へ一本化(重複実装を解消) |

- `@ConditionalOnProperty`は、現行の2つの独立トグル(`cherry.testtool.web.invoker`/`cherry.testtool.web.stubconfig`)から単一トグル(`cherry.testtool.web.enabled`、既定有効)へ統合した。
- 呼出し先の型は全て具象クラス(`InvokerService`、`ReflectionResolver`、`StubRepository`、`ScriptProcessor`)。

## テスト

`TesttoolControllerTest`(新規)を`@WebMvcTest(TesttoolController.class)`+`@MockitoBean`で作成し、以下を検証した。

- `invoke`・`put`(登録/解除)・`get`・`list`の各エンドポイントが対応するサービスメソッドへ正しく委譲すること
- 統合後の`/testtool/resolve/bean`・`/testtool/resolve/method`が機能すること

## 変更ファイル一覧

- 新規作成: `web/TesttoolController.java`、`lib/src/test/java/cherry/testtool/web/TesttoolControllerTest.java`
- 削除: `web/InvokerController.java`、`web/StubConfigController.java`

## SPA/CLIへの影響(NFR1)

`/testtool/resolve/bean`・`/testtool/resolve/method`への統合に伴い、Unit 3(webconsole)のフロントエンド(`invoker/api.ts`、`stubconfig/api.ts`)側で呼出し先パスの追随修正が必要(FR8.4)。本Unitの範囲外であり、Unit 3で対応する。
