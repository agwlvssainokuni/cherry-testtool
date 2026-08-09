# スタブ実行時のトレースログ出力 - 確認質問

対象箇所は`lib/src/main/java/cherry/testtool/stub/StubResolver.java`の`getStubInvocation(Method)`(スタブ設定が登録されている場合に返す`StubInvocation`の実行本体)と特定しました。ログレベルは`TRACE`、SLF4J(`StubConfigLoader.java`等で使用中の`private final Logger logger = LoggerFactory.getLogger(getClass());`パターン)を踏襲する前提です。

「スタブの内容」として、具体的にどこまでをログへ含めるかを確認させてください。

## Question 1
ログに含める情報の範囲はどこまでとしますか？

A) スタブ設定(script・engine)のみ

B) スタブ設定に加え、対象メソッド(クラス名・メソッド名)

C) スタブ設定・対象メソッドに加え、呼出し引数(args)も

D) 上記(スタブ設定・対象メソッド・引数)に加え、スクリプト評価結果(戻り値、または例外発生時はその内容)も

E) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 2
ログ出力のタイミングはどうしますか？(Question 1でC以上を選んだ場合、評価結果を含めるには評価後のログも必要になります)

A) スクリプト評価の直前に1回だけ出力する(評価結果は含めない、または含められない)

B) スクリプト評価の直前と直後の2回に分けて出力する(直前:スタブ設定・メソッド・引数、直後:評価結果)

C) スクリプト評価の直後にまとめて1回出力する(直前情報も含めて1行にまとめる)

D) Other (please describe after [Answer]: tag below)

[Answer]: C
