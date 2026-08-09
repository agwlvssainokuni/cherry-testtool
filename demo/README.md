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

## 動作確認手順(手動)

1. デモアプリを起動する(上記)
2. スタブ未登録の状態で呼び出す:
   ```bash
   curl "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"
   # => 1234 (実際の計算結果)
   ```
3. `client/cli`または`client/webconsole`から、`cherry.testtool.demo.SampleService`の`toBeStubbed1`にスタブを登録する
4. 同じエンドポイントを再度呼び出す:
   ```bash
   curl "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"
   # => スタブで指定した値が返る
   ```
5. スタブを解除すると、元の計算結果に戻ることを確認する

この手順を自動化したものが`SampleControllerTest`である。
