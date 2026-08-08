# Business Rules - cli(Unit 4)

Functional Design Plan(`cli-functional-design-plan.md`)のQuestion回答(いずれも推奨案Aを採用)に基づく確定ルール。

## BR1: ディレクトリ走査規則

- 位置引数で与えられた各ディレクトリ配下を再帰的に走査し、拡張子`.js`のファイルを全て抽出する(`Files.walk`相当、旧`find ... -name '*.js'`と同等)
- 抽出したファイルは、パス文字列の辞書順(lexicographic)でソートする(旧`sort`と同等)
- 複数ディレクトリが指定された場合、指定順に1ディレクトリずつ処理する

## BR2: className/methodName/methodIndex抽出規則(旧スクリプトを踏襲)

- `className` = 対象ファイルの**直接の親ディレクトリ名**(パッケージ階層としてのフルパスではなく、単純なディレクトリ名文字列)
- ファイル名(`.js`拡張子を除く)を最初の`.`で分割し、前半を`methodName`、後半があれば`methodIndex`(整数)、無ければ`methodIndex=0`とする
- `methodIndex`が数値でない場合の変換エラーは、BR4(失敗時継続)の対象とする(その1件を失敗として記録し処理を継続する)

## BR3: 終了コード算出規則(Q1=A採用)

- `ExitCodeGenerator.getExitCode()`は、`BatchResult.failureCount() == 0`ならば`0`、1件でも失敗があれば`1`を返す
- 全ディレクトリ・全ファイルの処理結果を横断して判定する(ディレクトリ単位ではなく実行全体で1つの終了コード)

## BR4: バッチ処理中の失敗時継続規則(Q4=A採用)

- 個々のファイルの処理(HTTP呼出し)が失敗(接続エラー、タイムアウト、4xx/5xx応答、`methodIndex`変換エラー等)しても、処理を中断せず次のファイルへ進む
- 失敗した個々の結果は`FileProcessingResult(success=false, output=エラーメッセージ)`として記録し、標準出力/標準エラーへその旨を表示した上で処理を継続する
- 全ファイル処理後、`BatchResult`を基に最終的な終了コード(BR3)を決定する

## BR5: 出力形式規則(Q5=A採用、旧スクリプトの表示形式を踏襲)

各ディレクトリの処理開始時に`PROCESSING {dir}`を1行出力する。各ファイルについて、以下を標準出力へ順に表示する。

```
{ファイルパス}
  {className}
  {methodName} {methodIndex}
{API応答本文 または エラーメッセージ}
```

`stubconfig show`のみ、旧スクリプトが応答表示後に空行を1行追加していたため、その形式を踏襲する。

## BR6: BASIC認証・追加ヘッダ規則(Q6=A採用)

- `--basic-auth user:pass`が指定された場合、`user:pass`をBase64エンコードし、`Authorization: Basic {encoded}`ヘッダを全リクエストへ付加する
- `--header "Name: Value"`(複数指定可)は、最初の`:`で分割して`Name`/`Value`(前後の空白はtrim)を取り出し、`MultiValueMap<String,String>`へ追加した上で全リクエストへ付加する
- BASIC認証ヘッダと`--header`由来のヘッダは併用可能(両方あれば両方付加する)

## BR7: 呼出し/スタブ設定APIへのマッピング規則

| コマンド | HTTPエンドポイント | scriptパラメータ |
|---|---|---|
| `invoke {dirs}...` | `POST /testtool/invoker/invoke` | ファイル内容 |
| `stubconfig register {dirs}...` | `POST /testtool/stubconfig/put` | ファイル内容 |
| `stubconfig clear {dirs}...` | `POST /testtool/stubconfig/put` | 空文字列(スタブ解除) |
| `stubconfig show {dirs}...` | `POST /testtool/stubconfig/get` | (パラメータなし) |

いずれも`engine`パラメータは空文字列で固定する(旧スクリプトの挙動を踏襲。スクリプトエンジンの自動判定に委ねる)。

## BR8: 共通オプションのスコープ規則(Q2=A採用)

`--url`(既定`http://localhost:8080`)、`--basic-auth`、`--header`は`RootCommand`で定義し、Picocliの`scope = ScopeType.INHERIT`により`InvokeCommand`・`StubConfigCommand`配下の全サブコマンドから共有する。サブコマンド固有のオプションは無い(位置引数のディレクトリ群のみ)。

## BR9: stubconfigサブコマンド構成規則(Q3=A採用)

`StubConfigCommand`はディスパッチ専用(自身は実行ロジックを持たない)とし、`register`/`clear`/`show`の3つのサブサブコマンドを持つ。各サブサブコマンドは`StubConfigService`の対応するメソッド(`registerAll`/`clearAll`/`showAll`)へ処理を委譲する。
