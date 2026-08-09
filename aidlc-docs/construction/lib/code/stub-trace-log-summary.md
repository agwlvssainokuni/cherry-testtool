# スタブ実行時のトレースログ出力 - Code Generation Summary

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR9)、`aidlc-docs/construction/plans/lib-stub-trace-log-code-generation-plan.md`

## 変更ファイル一覧

### 修正
- `lib/src/main/java/cherry/testtool/stub/StubResolver.java`
  - SLF4J `Logger`フィールドを追加(`private final Logger logger = LoggerFactory.getLogger(getClass());`、`StubConfigLoader`と同一パターン)
  - `getStubInvocation(Method)`のラムダ(`StubInvocation`実装本体)で、`scriptProcessor.eval(...)`呼出しの成功時・`ScriptException`捕捉時それぞれの分岐で、評価後にまとめて1回`logger.trace(...)`を出力するよう変更。既存の`cause`再throwロジックは変更していない

## ログ仕様

- **ログレベル**: TRACE
- **出力箇所**: `StubResolver.getStubInvocation(Method)`のラムダ内(`StubInvocation.invoke`実装)、スクリプト評価の直後
- **成功時のログ内容**: 対象メソッド(`Method#toString()`)・スタブのscript・engine・呼出し引数(`Arrays.toString(args)`)・評価結果(戻り値)
- **例外時のログ内容**: 対象メソッド・script・engine・引数に加え、SLF4Jの標準的な「可変長引数の最後がThrowableの場合はプレースホルダー置換に使わずスタックトレース出力用に特別扱いする」挙動を利用し、例外(`ScriptException`のcauseがあればcause、無ければ`ScriptException`自体)のフルスタックトレースをログの末尾に付与する

## 実装上の注意点(ハマりどころ)

当初、例外時のログを`"...exception={}"`という5個目のプレースホルダーに`Throwable`を渡す形で実装したが、SLF4Jは**可変長引数の最後の要素が`Throwable`だと、プレースホルダー数と一致していてもメッセージ置換に使わずスタックトレース出力専用の引数として抜き出す**という仕様があり、`exception={}`が未置換のまま出力される不具合が生じた。実機検証(demoアプリを`--logging.level.cherry.testtool.stub=TRACE`で起動し、意図的に例外を投げるスタブを登録して確認)で発見し、フォーマット文字列を4プレースホルダー("...exception thrown"という固定テキストで終える)に修正し、`Throwable`を素直にSLF4Jのスタックトレース出力機能に委ねる形へ変更した。これによりTRACEレベルの目的(詳細を出し惜しみしない)により適した、完全なスタックトレース付きのログとなった。

## 動作確認

`./gradlew :lib:build`で既存31テストが全て回帰無く成功することを確認した上で、実機(demoアプリ)による手動確認を実施した(NFR2)。

1. `./gradlew :demo:bootRun --args='--logging.level.cherry.testtool.stub=TRACE'`でTRACEログを有効化して起動
2. `POST /testtool/stubconfig/put`で`SampleService.toBeStubbed1(Integer,Integer)`版へ固定値`9999`を返すスタブを登録し、`GET /api/sample/stubbed1/int?p1=1030&p2=204`を呼び出し、以下のログが出力されることを確認:
   ```
   TRACE ... cherry.testtool.stub.StubResolver - stub invoked: method=public java.lang.Integer cherry.testtool.demo.SampleService.toBeStubbed1(java.lang.Integer,java.lang.Integer), script=9999, engine=, args=[1030, 204], result=9999
   ```
3. 同メソッドへ例外を投げるスタブを登録し、同エンドポイントを呼び出し(HTTP 500)、以下のログ(末尾に完全なスタックトレース付き)が出力されることを確認:
   ```
   TRACE ... cherry.testtool.stub.StubResolver - stub invoked: method=public java.lang.Integer cherry.testtool.demo.SampleService.toBeStubbed1(java.lang.Integer,java.lang.Integer), script=throw Java.type('java.lang.RuntimeException').new('stub error test'), engine=, args=[1030, 204], exception thrown
   TypeError: TypeError: invokeMember (new) on java.lang.RuntimeException failed due to: Unknown identifier: new
   	at ...(スタックトレース)
   ```
4. いずれもスタブ解除後、`./gradlew :lib:test`で既存テストへの回帰が無いことを再確認した

いずれも想定通り(FR9.1〜FR9.4)動作することを確認済み。
