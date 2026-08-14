# Integration Test Instructions

## Purpose

`lib`を組み込んだ`demo`を実際に起動し、`client/webconsole`(プロキシ経由)・`client/cli`(直接HTTP)の両クライアントから正しくアクセスできることを確認する。単体テスト(モック/`MockRestServiceServer`等)では検証できない、実際のプロセス間HTTP通信・Spring Boot自動構成の解決・AOPによるスタブ介入の実動作を対象とする。

自動化されたE2Eテストスイートは用意していない(構成の複雑さに対して得られる保証が薄いため、Unit 3/4のCode Generation時点で見送りを判断済み)。代わりに、以下の手動確認手順を整備し、開発時に実際に実施・成功を確認済みである。

## Test Scenarios

### Scenario 1: demo単体の動作確認
- **Description**: `lib`が提供する自動構成(`TesttoolAutoConfiguration`)が`demo`から正しく解決され、`TesttoolController`のREST APIが機能することを確認する
- **Setup**: `./gradlew :demo:bootRun`(ポート8080)
- **Test Steps**:
  ```bash
  curl -s -X POST http://localhost:8080/testtool/resolve/bean --data-urlencode "className=cherry.testtool.demo.SampleService"
  curl -s "http://localhost:8080/api/sample/stubbed1/int?p1=1030&p2=204"
  ```
- **Expected Results**: 1つ目は`["sampleService"]`、2つ目は`1234`(スタブ未登録時の実計算結果)
- **Cleanup**: `demo`プロセスを停止する

### Scenario 2: webconsole → demo プロキシ
- **Description**: `client/webconsole`の`/testtool/**`が`demo`へ正しくプロキシされ、SPA配信・セキュリティヘッダも機能することを確認する(Unit 3 Code Generationで実施・確認済み)
- **Setup**: Scenario 1に加え、`./gradlew :client:webconsole:bootRun`(ポート9090)
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
- **Setup**: Scenario 1のみ(webconsoleは不要)。`./gradlew :client:cli:bootJar`
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

### Scenario 5: `/testtool/**` APIキー保護(FR10)
- **Description**: `cherry.testtool.web.api-key`の未設定時(後方互換)・設定時(ヘッダ無し/不一致/一致)、および`client/webconsole`・`client/cli`からの自動付与が正しく機能することを確認する
- **Setup**: `./gradlew :demo:bootRun --args='--cherry.testtool.web.api-key=test-secret-key'`(ポート8080)
- **Test Steps**:
  ```bash
  # (a) 未設定時の後方互換(別途 ./gradlew :demo:bootRun のみで起動して確認)
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/testtool/resolve/bean?className=cherry.testtool.demo.SampleService"

  # (b)-(d) 上記Setup(APIキー設定済み)の状態で
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/testtool/resolve/bean?className=cherry.testtool.demo.SampleService"                                        # ヘッダ無し
  curl -s -o /dev/null -w "%{http_code}\n" -H "X-Cherry-Testtool-Api-Key: wrong-key" "http://localhost:8080/testtool/resolve/bean?className=cherry.testtool.demo.SampleService" # 不一致
  curl -s -o /dev/null -w "%{http_code}\n" -H "X-Cherry-Testtool-Api-Key: test-secret-key" "http://localhost:8080/testtool/resolve/bean?className=cherry.testtool.demo.SampleService" # 一致
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/api/sample/stubbed1/int?p1=1&p2=2"                                                                          # 対象外パス(/testtool/**以外)

  # (e) webconsole自動付与(./gradlew :client:webconsole:bootRun --args='--cherry.testtool.web.api-key=test-secret-key' も起動して)
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:9090/testtool/resolve/bean?className=cherry.testtool.demo.SampleService" # ブラウザ利用者はキー入力不要

  # (f) cli自動付与
  java -jar client/cli/build/libs/cherry-testtool-cli.jar --url http://localhost:8080 stubconfig show demo/stub-samples                              # 未設定時は401
  java -Dcherry.testtool.web.api-key=test-secret-key -jar client/cli/build/libs/cherry-testtool-cli.jar --url http://localhost:8080 stubconfig show demo/stub-samples # 設定時は成功
  ```
- **Expected Results**: (a)`200`、(b)`401`、(c)`401`、(d)`200`(`["sampleService"]`)、`/api/sample/**`は`200`(キーの有無に関わらず対象外)、(e)`200`(webconsoleが自動付与、ブラウザ利用者は未入力のままアクセス可能)、(f)未設定時は`401 Unauthorized`エラー表示、設定時は正常にスタブ登録内容を表示
- **Cleanup**: 全プロセスを停止する

### Scenario 6: webconsole frontendのUIライブラリ移行(FR11)確認
- **Description**: MUIからmake-you-chic-uiへ全面移行した`client/webconsole/frontend`が、ビルド・配信・API連携の観点で従来通り機能することを確認する。UIライブラリ・ディレクトリ構成の変更が中心のため、画面表示・クリック操作等の視覚的な確認はこのシナリオでは自動化せず、ビルド成果物の配信内容とAPI連携(フロントエンドが呼び出す全エンドポイント)をcurlで確認する
- **Setup**: `./gradlew --stop`(daemonのPATHキャッシュ問題を避けるため)、`./gradlew :demo:bootRun`(ポート8080)、`./gradlew :client:webconsole:bootRun`(ポート9090)
- **Test Steps**:
  ```bash
  # ビルド成果物の配信確認
  curl -s http://localhost:9090/ | grep -E "favicon|manifest|logo192|script|stylesheet"
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/            # Home
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/invoker     # Invoker(SPAフォールバック)
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/stubconfig  # Stubconfig(SPAフォールバック)
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/favicon.ico
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/manifest.json

  # Invoker画面が呼ぶAPI一式(resolve/bean・resolve/method・invoker/invoke)
  curl -s -X POST http://localhost:9090/testtool/resolve/bean --data-urlencode "className=cherry.testtool.demo.SampleService"
  curl -s -X POST http://localhost:9090/testtool/resolve/method --data-urlencode "className=cherry.testtool.demo.SampleService" --data-urlencode "methodName=toBeInvoked1"
  curl -s -X POST http://localhost:9090/testtool/invoker/invoke --data-urlencode "beanName=sampleService" --data-urlencode "className=cherry.testtool.demo.SampleService" --data-urlencode "methodName=toBeInvoked1" --data-urlencode "methodIndex=0" --data-urlencode "script=[10, 20]" --data-urlencode "engine="

  # Stubconfig画面が呼ぶAPI一式(stubconfig/put・get・list)、登録後にinvokeでスタブ適用を確認
  curl -s -X POST http://localhost:9090/testtool/stubconfig/put --data-urlencode "className=cherry.testtool.demo.SampleService" --data-urlencode "methodName=toBeInvoked1" --data-urlencode "methodIndex=0" --data-urlencode "script=999" --data-urlencode "engine="
  curl -s -X POST http://localhost:9090/testtool/stubconfig/list --data-urlencode "className=cherry.testtool.demo.SampleService"
  curl -s -X POST http://localhost:9090/testtool/stubconfig/get --data-urlencode "className=cherry.testtool.demo.SampleService" --data-urlencode "methodName=toBeInvoked1" --data-urlencode "methodIndex=0"
  curl -s -X POST http://localhost:9090/testtool/invoker/invoke --data-urlencode "beanName=sampleService" --data-urlencode "className=cherry.testtool.demo.SampleService" --data-urlencode "methodName=toBeInvoked1" --data-urlencode "methodIndex=0" --data-urlencode "script=[10, 20]" --data-urlencode "engine="
  ```
- **Expected Results**: HTMLに`/favicon.ico`・`/manifest.json`・`/logo192.png`・新JS/CSSバンドルへの参照が含まれる。Home/Invoker/Stubconfigとも`200`。favicon.ico/manifest.jsonとも`200`。`resolve/bean`は`["sampleService"]`、`resolve/method`は`["(long,long)"]`、`invoker/invoke`(スタブ登録前)は`--- 30`(10+20の計算結果)。`stubconfig/put`は`true`、`stubconfig/list`は登録メソッドを含む配列、`stubconfig/get`はscript「999」を含む3要素配列。スタブ登録後の`invoker/invoke`は`--- 999`(スタブ値が適用される)
- **Cleanup**: `demo`・`webconsole`プロセスを停止する
- **補足**: 当初はブラウザ拡張(Claude in Chrome)が未接続だったためHTTPレベルの確認に留めていたが、ユーザーが拡張機能をインストール後、Scenario 6.1として実ブラウザでの視覚的確認も実施した

### Scenario 6.1: webconsole frontendの実ブラウザ確認(FR11、Claude in Chrome使用)
- **Description**: Scenario 6のHTTPレベル確認に加え、実際のブラウザでレンダリング・クリック操作を確認する。**この確認で2件の重大な不具合(画面が真っ白/CSS未適用)を発見・修正した**(詳細はrequirements.md FR11.12、`ui-library-migration-summary.md`)。`npm run build`(型チェック・バンドル)ではいずれも検出できず、実ブラウザでの確認が不可欠だった
- **Setup**: Scenario 6と同じ(demo:8080、webconsole:9090)
- **Test Steps**: Claude in Chromeで`http://localhost:9090/`を開き、(1)Home画面のSidebar/Topbar/Card表示、(2)呼出しツールCardのクリックによる`/invoker`遷移、(3)Invoker画面でクラス名`cherry.testtool.demo.SampleService`・メソッド名`toBeInvoked1`を入力しBean名称・メソッド候補が自動解決されること、(4)引数`[10, 20]`を入力し実行ボタンを押して結果が表示されること、(5)Sidebarから「スタブ設定ツール」へ遷移、(6)同様にクラス名・メソッド名を入力し返却値`888`を登録ボタンで登録すること、(7)Topbarの「テーマ」ボタンからDropdownを開き「モード: ダークへ切替」をクリックして画面全体がダークテーマになること、を順に確認
- **Expected Results**: (1)Sidebarに3項目のリンク、Topbar右上に「テーマ」ボタン、Home画面に「呼出しツール」「スタブ設定ツール」の2つのCard(タイトル+説明文)が表示される。(2)`/invoker`へ遷移しInvokerPage表示。(3)Bean名称欄に`sampleService`、メソッド候補欄に`(long,long)`が自動反映される。(4)実行結果欄に`--- 30`が表示される。(5)`/stubconfig`へ遷移。(6)登録結果欄に`true`が表示される。(7)背景・パネルが暗色に切り替わる
- **発見した不具合と修正**(いずれも初回確認時に発生、修正後は解消を確認):
  1. **画面が真っ白**: `vendor/make-you-chic-ui`が自身のビルド用に保持する`node_modules/react`を、symlink経由でfile:参照するVite側が誤って解決し、Reactが二重ロードされて`TypeError: Cannot read properties of null (reading 'useState')`が発生していた。`client/webconsole/frontend/vite.config.ts`に`resolve: { dedupe: ["react", "react-dom"] }`を追加して解消
  2. **CSSが一切適用されない**(無地の文字だけのページ): make-you-chic-uiのビルド成果物はCSSをJSから分離した別ファイル(`dist/index.css`)として出力するため、`main.tsx`に`import 'make-you-chic-ui/style.css'`を明示的に追加する必要があった。追加前は`:root`のトークン定義・`mycui-*`のコンポーネントCSSが一切バンドルに含まれていなかった(`grep`で確認)
- **Cleanup**: `demo`・`webconsole`プロセス、およびブラウザタブを閉じる

## 実施結果

上記5シナリオを実際に実行し、いずれも期待結果通りであることを確認済み(Scenario 4は当初のBuild and Testステージで実施)。Gradleマルチプロジェクト化(2026-08-09)後、Scenario 1-3を再度実施し、いずれも同じ結果が得られることを再確認済み。Scenario 5(APIキー保護、FR10)は2026-08-09に追加・実施し、後方互換(未設定時)・保護動作(設定時のヘッダ無し/不一致/一致)・webconsole/cliからの自動付与、いずれも想定通りであることを確認済み。Scenario 6(UIライブラリ移行、FR11)は2026-08-14に追加・実施し、ビルド成果物の配信・API連携面はいずれも想定通りであることを確認した。続けてScenario 6.1として実ブラウザ確認を実施し、Reactフックエラーによる白画面・CSS未適用という2件の重大な不具合を発見、修正後に全て想定通りの表示・動作となることを確認した。
