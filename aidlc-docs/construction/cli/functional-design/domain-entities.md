# Domain Entities - cli(Unit 4)

## ScriptFileEntry(record)

スタブ設定/呼出し用ディレクトリ配下の1スクリプトファイルを表すエンティティ。

| フィールド | 型 | 説明 |
|---|---|---|
| `filePath` | `Path` | スクリプトファイルの絶対/相対パス |
| `className` | `String` | 呼出し/スタブ対象のFQCN。ファイルの**直接の親ディレクトリ名**から導出 |
| `methodName` | `String` | 対象メソッド名。ファイル名(拡張子`.js`除く)を最初の`.`で分割した前半部分 |
| `methodIndex` | `int` | オーバーロード解決用のインデックス。ファイル名に`.`区切りの後半部分があればその値、無ければ`0` |

**導出規則の例**: `{scanDir}/cherry.testtool.demo.SampleService/toBeStubbed1.js` → `className="cherry.testtool.demo.SampleService"`, `methodName="toBeStubbed1"`, `methodIndex=0`。`toBeStubbed1.1.js`のように`.`が2つある場合は`methodName="toBeStubbed1"`, `methodIndex=1`。

## ConnectionOptions(record)

`RootCommand`で解析される、全サブコマンド共通の接続情報。

| フィールド | 型 | 説明 |
|---|---|---|
| `baseUrl` | `URI` | 接続先ベースURL(既定`http://localhost:8080`) |
| `basicAuth` | `@Nullable String` | `user:pass`形式。指定時はBase64エンコードした`Authorization: Basic`ヘッダを全リクエストへ付加する |
| `headers` | `List<String>` | `Name: Value`形式の生ヘッダ文字列(複数指定可)。全リクエストへ付加する |

## FileProcessingResult(record)

1ファイル分の処理結果。バッチ処理の集計・出力・終了コード算出の基礎となる。

| フィールド | 型 | 説明 |
|---|---|---|
| `entry` | `ScriptFileEntry` | 処理対象ファイル |
| `success` | `boolean` | 呼出し/登録/取得が成功したか(HTTP呼出しが例外を投げなかったか) |
| `output` | `String` | 成功時はAPI応答本文、失敗時はエラーメッセージ |

## BatchResult(record)

複数ディレクトリ・複数ファイルにわたる処理全体の集計結果。`ExitCodeGenerator`の終了コード算出に用いる。

| フィールド | 型 | 説明 |
|---|---|---|
| `results` | `List<FileProcessingResult>` | 全ファイルの処理結果 |
| `failureCount()` | メソッド | `results`中`success=false`の件数を返す派生値 |
