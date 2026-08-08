# Functional Design Plan - cli(Unit 4)

## Unit Context

- **対応FR**: FR5(`client/cli`のSpring Bootアプリ化)、FR7(コメント充実、cli分)、NFR5(JSpecify、cli分)
- **現行実装**: `client/cli/invoker.sh`、`client/cli/stubconfig.sh`(bash製CLI)。これらのシェルスクリプトの挙動を仕様のリファレンスとする(FR5.4によりオプション体系自体を維持する必要はない)
- **Application Designでの決定事項**(再掲):
  - Picocli(`picocli-spring-boot-starter`)採用、`RootCommand`配下に`InvokeCommand`・`StubConfigCommand`をサブコマンドとして配置
  - `CliApplication`が`CommandLineRunner`+`ExitCodeGenerator`を実装し、`run()`内で処理結果から終了コードを算出・保持する
  - `TesttoolApiClient`(`@HttpExchange`)はSpring管理のprototype-scoped Bean。`ApiClientFactory.create(URI baseUri)`が`applicationContext.getBean(TesttoolApiClient.class, baseUri)`で取得する
  - `ScriptFileScanner`が`*.js`ファイル走査を担う共有コンポーネント

## 現行シェルスクリプトの挙動分析(仕様のリファレンス)

### invoker.sh
- オプション: `-l {BASE URL}`(既定`http://localhost:8080`)、`-u {BASIC AUTH}`(`user:pass`形式、curlの`-u`相当)、`-H {HEADER}`(`Name: Value`形式、複数指定可、curlの`-H`相当)
- 位置引数: 1つ以上のディレクトリ(スタブ設定ディレクトリと同型のディレクトリ構造を、呼出し対象の指定に流用)
- 各ディレクトリ配下を`find ... -name '*.js' | sort`で走査。各ファイルについて:
  - `className` = ファイルの親ディレクトリ名
  - `methodName`/`methodIndex` = ファイル名(拡張子`.js`除く)を`.`で分割(`methodName.methodIndex.js`形式、`methodIndex`省略時は`0`)
  - 進捗を標準出力へ表示(ファイルパス、className、methodName+methodIndex)
  - `POST {url}/testtool/invoker/invoke`(`className`,`methodName`,`methodIndex`,`script`=ファイル内容,`engine`=空)を呼び出し、応答本文をそのまま標準出力へ表示
- `bash -e`により、curl自体が失敗(接続エラー等)した場合はスクリプト全体が即座に終了する。HTTPエラーステータス(4xx/5xx)は`curl`が非0終了しない(`--fail`未指定)ため、後続処理は継続される

### stubconfig.sh
- オプション: `-l`,`-u`,`-H`は同上。`-r`(register)、`-c`(clear)、いずれも未指定なら`show`(既定)
- ディレクトリ走査・className/methodName/methodIndex抽出はinvoker.shと同一
- `register`: `POST {url}/testtool/stubconfig/put`(`script`=ファイル内容)
- `clear`: `POST {url}/testtool/stubconfig/put`(`script`=空、スタブ解除)
- `show`: `POST {url}/testtool/stubconfig/get`(`script`パラメータなし)、応答をそのまま表示

## Steps

- [x] Step 1: Unit定義・Application Design成果物の再確認(完了、上記「Application Designでの決定事項」参照)
- [x] Step 2: 現行シェルスクリプトの挙動分析(完了、上記参照)
- [x] Step 3: 下記の質問に対する回答を収集(いずれも根拠を明記した推奨案Aを採用)
- [x] Step 4: 回答を踏まえ、`aidlc-docs/construction/cli/functional-design/`配下に成果物(business-logic-model.md、business-rules.md、domain-entities.md)を生成

## Questions

質問は`[Answer]:`タグに選択肢の記号を記入して回答してください。

### Question 1
一括処理(ファイル群への呼出し/スタブ登録等)の失敗時、終了コード(`ExitCodeGenerator`)はどう算出すべきですか?

A) 0=全件成功、1=1件でも失敗あり(単純な成功/失敗の二値。**推奨**: シンプルで判定しやすい)

B) 0=全件成功、それ以外は失敗件数をそのまま終了コードとする(255件超は255にキャップ)

C) 0=全件成功、失敗要因のカテゴリ別に固定コードを割り当てる(例: 2=接続エラー、3=HTTPエラー応答)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2
接続先URL・BASIC認証・追加ヘッダといった共通オプションは、Picocliでどう構成すべきですか?

A) `RootCommand`に定義し、`scope = ScopeType.INHERIT`で全サブコマンドから共有する(**推奨**: 重複が無くシンプル)

B) `InvokeCommand`・`StubConfigCommand`それぞれに個別定義する(サブコマンド単位でオプションを変えたい場合に有利)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3
`stubconfig`の`register`/`clear`/`show`3モードは、Picocliでどう表現すべきですか?

A) サブサブコマンド化する(`cli stubconfig register <dirs>...`、`cli stubconfig clear <dirs>...`、`cli stubconfig show <dirs>...`。**推奨**: Picocliのヘルプ表示が自然になり、モードの排他性も型で保証される)

B) 単一の`stubconfig`コマンドに`--mode=register|clear|show`(既定`show`)オプションを持たせる(現行シェルスクリプトのオプション体系に近い)

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 4
バッチ処理中に1ファイルの呼出しが失敗(接続エラー、HTTPエラー応答等)した場合、残りのファイルの処理はどうすべきですか?

A) 継続する(1件の失敗で全体を止めず、最後に失敗件数をまとめて報告・終了コードへ反映する。**推奨**: CI等での一括投入に向く。現行シェルスクリプトの「HTTPエラー応答では継続、接続エラーでは中断」という不統一な挙動より一貫性がある)

B) 現行シェルスクリプト同様、接続エラー等の異常時点で即座に処理全体を中断する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 5
実行結果の標準出力の形式はどうすべきですか?

A) 現行シェルスクリプトの表示形式を踏襲する(処理中ファイルパス・className・methodName+methodIndexの進捗行 → API応答本文をそのまま表示。**推奨**: 既存ユーザーの見慣れた形式を維持できる)

B) より構造化した出力(例: 1ファイル1行のサマリー形式)へ刷新する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 6
BASIC認証(`-u user:pass`相当)・追加ヘッダ(`-H "Name: Value"`相当)のオプション形式はどうすべきですか?

A) `--basic-auth user:pass`(1つ)と`--header "Name: Value"`(複数回指定可、curlの`-H`と同じ生ヘッダ文字列形式)を踏襲する(**推奨**: 現行ユーザーが移行しやすい)

B) `--header NAME=VALUE`のようなPicocliの`Map`オプション形式へ刷新する

X) Other (please describe after [Answer]: tag below)

[Answer]: A
