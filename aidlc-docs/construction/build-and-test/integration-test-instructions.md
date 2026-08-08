# Integration Test Instructions

## Purpose

`lib`を組み込んだ`demo`を実際に起動し、`client/webconsole`(プロキシ経由)・`client/cli`(直接HTTP)の両クライアントから正しくアクセスできることを確認する。単体テスト(モック/`MockRestServiceServer`等)では検証できない、実際のプロセス間HTTP通信・Spring Boot自動構成の解決・AOPによるスタブ介入の実動作を対象とする。

自動化されたE2Eテストスイートは用意していない(構成の複雑さに対して得られる保証が薄いため、Unit 3/4のCode Generation時点で見送りを判断済み)。代わりに、以下の手動確認手順を整備し、開発時に実際に実施・成功を確認済みである。

## Test Scenarios

### Scenario 1: demo単体の動作確認
- **Description**: `lib`が提供する自動構成(`TesttoolConfiguration`)が`demo`から正しく解決され、`TesttoolController`のREST APIが機能することを確認する
- **Setup**: `cd demo && ./gradlew bootRun`(ポート8080)
- **Test Steps**:
  ```bash
  curl -s -X POST http://localhost:8080/testtool/resolve/bean --data-urlencode "className=cherry.testtool.demo.SampleService"
  curl -s "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"
  ```
- **Expected Results**: 1つ目は`["sampleService"]`、2つ目は`1234`(スタブ未登録時の実計算結果)
- **Cleanup**: `demo`プロセスを停止する

### Scenario 2: webconsole → demo プロキシ
- **Description**: `client/webconsole`の`/testtool/**`が`demo`へ正しくプロキシされ、SPA配信・セキュリティヘッダも機能することを確認する(Unit 3 Code Generationで実施・確認済み)
- **Setup**: Scenario 1に加え、`cd client/webconsole && ./gradlew bootRun`(ポート9090)
- **Test Steps**:
  ```bash
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/                      # SPA配信
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/invoker/some/deep/path # SPAフォールバック
  curl -s -X POST http://localhost:9090/testtool/resolve/bean --data-urlencode "className=cherry.testtool.demo.SampleService"
  curl -s -D - -o /dev/null -X POST http://localhost:9090/testtool/resolve/bean --data-urlencode "className=cherry.testtool.demo.SampleService" | grep -i x-frame-options
  ```
- **Expected Results**: 1つ目・2つ目とも`200`(存在しないパスもSPAの`index.html`が返る)、3つ目は`["sampleService"]`(demoへプロキシされた結果)、4つ目に`X-Frame-Options: DENY`が含まれる
- **Cleanup**: `webconsole`プロセスを停止する

### Scenario 3: cli → demo 直接呼出し
- **Description**: `client/cli`から`invoke`・`stubconfig register/show/clear`を実行し、`demo`のスタブ介入(`StubAspect`)が実際に機能することを確認する(Unit 4 Code Generationで実施・確認済み)
- **Setup**: Scenario 1のみ(webconsoleは不要)。`cd client/cli && ./gradlew bootJar`
- **Test Steps**:
  ```bash
  # {className}/{methodName}[.{index}].jsの構造でディレクトリを用意し、以下を順に実行
  java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig register {DIR}
  curl -s "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"   # 登録値が返る
  java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig show {DIR}
  java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig clear {DIR}
  curl -s "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"   # 元の計算結果(1234)に戻る
  java -jar client/cli/build/libs/cherry-testtool-cli.jar invoke {DIR}
  ```
- **Expected Results**: register後は登録値が返る、show はscript/engine/評価結果を3行表示、clear後は元の計算結果に戻る、invokeは応答本文(または例外のYAML)を表示し終了コード0
- **Cleanup**: 特になし(demoプロセスは他シナリオと共用のため停止不要)

### Scenario 4: demo + webconsole + cli 同時実行
- **Description**: 3プロセス(demo/webconsole/cli)が同時に稼働している状態で、webconsole経由のアクセスとcliからの直接アクセスが互いに干渉しないことを確認する
- **Setup**: Scenario 1・2のプロセスを起動したまま、Scenario 3のcliコマンドを実行する
- **Test Steps**: Scenario 2の`curl`とScenario 3の`java -jar ... invoke`を同一のdemo稼働中に実行
- **Expected Results**: いずれも単独実行時と同じ結果が得られる(相互干渉なし)
- **Cleanup**: 全プロセスを停止する

## 実施結果(2026-08-08時点)

上記4シナリオを実際に実行し、いずれも期待結果通りであることを確認済み(Scenario 4は本Build and Testステージで実施)。
