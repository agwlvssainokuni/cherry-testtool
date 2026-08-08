# cherry-testtool-cli

`cherry-testtool`のREST API(`/testtool/invoker/invoke`、`/testtool/stubconfig/put,get`)をコマンドラインから一括実行するSpring Boot(Picocli)アプリケーション。旧`invoker.sh`/`stubconfig.sh`(bashスクリプト)を置き換える。

## ビルド・実行方法

```bash
cd client/cli
./gradlew bootJar
java -jar build/libs/cherry-testtool-cli.jar --help
```

## コマンド一覧

```
cherry-testtool-cli [--url=<baseUrl>] [--basic-auth=<user:pass>] [--header=<Name: Value>]... COMMAND

  invoke DIR...                     指定ディレクトリ群配下のスクリプトを一括呼び出しする
  stubconfig register DIR...        指定ディレクトリ群配下のスクリプトの内容をスタブとして一括登録する
  stubconfig clear DIR...           指定ディレクトリ群配下のスクリプトに対応するスタブ登録を一括解除する
  stubconfig show DIR...            指定ディレクトリ群配下のスクリプトに対応する、現在のスタブ登録内容を一括表示する
```

`--url`/`--basic-auth`/`--header`はルートコマンドで共通定義されており、`invoke`・`stubconfig`のいずれのサブコマンドでも(コマンドラインのどこに書いても)有効になる。

`DIR`配下は`{className}/{methodName}[.{methodIndex}].js`という構造を期待する(旧シェルスクリプトと同じ規約)。例えば`cherry.testtool.demo.SampleService/toBeStubbed1.1.js`は、`cherry.testtool.demo.SampleService`クラスの`toBeStubbed1`メソッド(オーバーロード解決用インデックス`1`)を指す。

終了コードは、指定した全ディレクトリ・全ファイルの処理のうち1件でも失敗があれば`1`、全件成功なら`0`を返す。

## 旧シェルスクリプトからの移行ガイド

| 旧コマンド | 新コマンド |
|---|---|
| `invoker.sh -l {URL} -u {user:pass} -H "{Header}" {DIR}...` | `cherry-testtool-cli --url {URL} --basic-auth {user:pass} --header "{Header}" invoke {DIR}...` |
| `stubconfig.sh -r {DIR}...` | `cherry-testtool-cli stubconfig register {DIR}...` |
| `stubconfig.sh -c {DIR}...` | `cherry-testtool-cli stubconfig clear {DIR}...` |
| `stubconfig.sh {DIR}...`(既定=show) | `cherry-testtool-cli stubconfig show {DIR}...` |

`stubconfig show`の出力形式は、旧スクリプトの生JSON配列表示から、script/engine/評価結果を1行ずつ表示する形式へ変更されている(可読性のため)。

## 手動結合確認手順

自動テストは`InvokeService`/`StubConfigService`を`MockRestServiceServer`で検証しているが、実際のHTTPサーバに対する結合確認として以下の手順を実施済み。

1. [demo](../../demo)を起動する(`cd demo && ./gradlew bootRun`、`http://localhost:8080`)
2. 本モジュールをビルドする(`cd client/cli && ./gradlew bootJar`)
3. `{className}/{methodName}[.{index}].js`構造のディレクトリを用意する(例: `cherry.testtool.demo.SampleService/toBeStubbed1.1.js`、内容は`9999`のようなスタブ返却値スクリプト)
4. `stubconfig register`でスタブを登録し、`curl http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204`で登録値(`9999`)が返ることを確認する
5. `stubconfig show`で登録内容(script/engine/評価結果)が表示されることを確認する
6. `stubconfig clear`で解除し、同じ`curl`で元の計算結果(`1234`)に戻ることを確認する
7. `invoke`で、`toBeInvoked*`系メソッドを呼び出すスクリプトを実行し、応答(または例外情報)が表示されることを確認する

この手順は開発時に実際に実施し、`invoke`・`stubconfig register/show/clear`いずれも想定通り動作することを確認済み。
