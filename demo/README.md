# cherry-testtool-demo

`lib`(`:lib`、成果物名`cherry-testtool-core`)を組み込んだ最小構成のデモアプリケーション。`client/webconsole`・`client/cli`の動作確認先として使うほか、`lib`を自分のアプリへ組み込む際の**スタブ組み込み方の手引書**を兼ねる。

`cherry-testtool`リポジトリ全体はGradleマルチプロジェクトであり、本モジュールは`:demo`として、リポジトリ直下の`./gradlew`から実行する。

## 起動方法

```bash
./gradlew :demo:bootRun
```

既定で`http://localhost:8080`で起動する。

## 構成

- `SampleService` — 実アプリが持つであろうサンプル業務サービス。`lib`の`InvokerService`によるリフレクション呼出しの対象となる`toBeInvoked*`メソッド群と、スタブ介入の対象となる`toBeStubbed*`メソッド群を持つ
- `SampleController`(`/api/sample/**`) — `SampleService`の`toBeStubbed*`系メソッドを通常のREST APIとして公開する。`cherry-testtool`自身のAPI(`/testtool/**`)とは独立しており、スタブ登録前後で応答が変わる様子を素直なHTTPリクエストで確認できる
- `aspect/StubAspect` — `lib`が提供する`StubResolver`を使い、`SampleService`へスタブ介入を適用するAspect(下記「スタブの組み込み方」参照)
- `aspect/TraceAspect` — メソッド呼出しのトレースログを出力するAspect(`lib`の同名クラスと同じパターン)

## スタブの組み込み方

`lib`は2つのスタブ介入方式を提供している。

| 方式 | 状態 | 特徴 |
|---|---|---|
| `StubInterceptor`(AOP Alliance `MethodInterceptor`) | `@Deprecated` | 利用側で`<aop:config>`等によるpointcut配線が別途必要 |
| `@Aspect`+`@Around`パターン(本アプリの`StubAspect`) | 推奨 | アノテーションのみで完結し、XML設定が不要 |

自分のアプリへ組み込む場合、本アプリの`aspect/StubAspect.java`をコピーし、以下の2点を変更するだけでよい。

1. `@Around`アノテーションのpointcut式(`execution(* cherry.testtool.demo.SampleService.*(..))`)を、実際にスタブ対象としたい自分のクラスへ書き換える
2. パッケージ宣言を自分のアプリの構成に合わせて調整する

`lib`への依存を追加し(`spring-boot-starter-aspectj`も併せて追加)、上記のAspectクラスを配置するだけで、Spring Bootの自動構成によりAspectJ自動プロキシが有効になり(`spring.aop.auto=true`が既定)、スタブ介入が機能する。特別な設定は不要。

## 呼出しのサンプル(invoke-samples/)

`invoke-samples/`配下に、`SampleService`の`toBeInvoked*`メソッド向けの引数生成スクリプトのサンプルを用意している。`stub-samples/`と同様、`client/cli`の走査規約に沿ったディレクトリ構造のため、`client/cli`にそのまま渡せる。`client/webconsole`の`/invoker`画面で使う場合は、対象クラス名・メソッド名を画面上で指定した上で、該当ファイルの中身を引数生成スクリプト欄へ貼り付ければよい。

```
invoke-samples/
  cherry.testtool.demo.SampleService/
    toBeInvoked0.js    # 引数無し
    toBeInvoked1.js    # long, long → 3, 4(戻り値7)
    toBeInvoked2.js    # Long, Long → 3, 4(戻り値7)
    toBeInvoked3.js    # LocalDate, LocalTime(Java.typeで生成)
    toBeInvoked4.js    # Dto1, Dto1(ネストしたrecordをJava.typeで直接生成)
    toBeInvoked5.js    # Dto2, Dto2(Dto1をさらにネスト)
    toBeInvoked6.0.js  # (int, int)オーバーロード版。10, 3(戻り値-7)
    toBeInvoked6.1.js  # (long, long)オーバーロード版。10, 3(戻り値7)
```

`toBeInvoked3`以降は、引数の型がプリミティブ/文字列で表せないため、GraalVM JSの`Java.type(...)`でJavaの型を直接参照し、その場でインスタンスを生成している(`Dto1`/`Dto2`は`SampleService`のネストしたrecordのため、バイナリ名`cherry.testtool.demo.SampleService$Dto1`で参照する)。

### client/cliでの使い方

```bash
java -jar client/cli/build/libs/cherry-testtool-cli.jar invoke demo/invoke-samples
```

### client/webconsoleでの使い方

1. `http://localhost:9090/invoker`を開く
2. 対象クラスに`cherry.testtool.demo.SampleService`、メソッドに呼び出したいメソッド(オーバーロードがある場合は該当するインデックス)を指定する
3. `invoke-samples/cherry.testtool.demo.SampleService/`配下の対応するファイルの中身を引数生成スクリプト欄へ貼り付けて実行する

## スタブのサンプル(stub-samples/)

`stub-samples/`配下に、`SampleService`の`toBeStubbed*`メソッド向けのスタブ設定スクリプトのサンプルを用意している。`client/cli`の走査規約(`{className}/{methodName}[.methodIndex].js`)に沿ったディレクトリ構造のため、`client/cli`にそのまま渡せる。`client/webconsole`の`/stubconfig`画面で使う場合は、対象クラス名・メソッド名を画面上で指定した上で、該当ファイルの中身をスクリプト欄へ貼り付ければよい。

```
stub-samples/
  cherry.testtool.demo.SampleService/
    toBeStubbed1.0.js   # toBeStubbed1(BigDecimal, BigDecimal)版。12345.67を返す
    toBeStubbed1.1.js   # toBeStubbed1(Integer, Integer)版。9999を返す(/api/sample/stubbed1/intが呼ぶのはこちら)
    toBeStubbed2.js      # toBeStubbed2(LocalDate, LocalTime)。2030-01-01T12:00:00を返す
```

`toBeStubbed1`はオーバーロードされており(`Integer`版・`BigDecimal`版)、リフレクションでの解決順は`methodIndex=0`が`BigDecimal`版、`methodIndex=1`が`Integer`版になる。`SampleController`の`/api/sample/stubbed1/int`が呼ぶのは`Integer`版のため、`toBeStubbed1.1.js`が対応する。

### client/cliでの使い方

```bash
./gradlew :client:cli:bootJar
java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig register demo/stub-samples
java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig show demo/stub-samples
java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig clear demo/stub-samples
```

### client/webconsoleでの使い方

1. `./gradlew :demo:bootRun`・`./gradlew :client:webconsole:bootRun`を起動し、`http://localhost:9090/stubconfig`を開く
2. 対象クラスに`cherry.testtool.demo.SampleService`、メソッドに`toBeStubbed1`(オーバーロードは`Integer`版を選択)を指定する
3. `stub-samples/cherry.testtool.demo.SampleService/toBeStubbed1.1.js`の中身(`9999`)をスクリプト欄へ貼り付けて登録する

## 動作確認手順(手動)

1. デモアプリを起動する(上記)
2. スタブ未登録の状態で呼び出す:
   ```bash
   curl "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"
   # => 1234 (実際の計算結果)
   ```
3. `client/cli`または`client/webconsole`から、`cherry.testtool.demo.SampleService`の`toBeStubbed1`にスタブを登録する(上記「スタブのサンプル」参照)
4. 同じエンドポイントを再度呼び出す:
   ```bash
   curl "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"
   # => スタブで指定した値(9999)が返る
   ```
5. スタブを解除すると、元の計算結果に戻ることを確認する

この手順を自動化したものが`SampleControllerTest`である。`stub-samples/`・`invoke-samples/`を使った`client/cli`・`client/webconsole`経由の確認は、開発時に実際に実施し、いずれも想定通り動作することを確認済み。
