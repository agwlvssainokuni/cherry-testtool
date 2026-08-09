# Unit 4(cli) - Code Generation Summary

## 対応FR/NFR

FR5(`client/cli`のSpring Bootアプリ化)、FR7(コメント充実、cli分)、NFR5(JSpecify、cli分)。

## 新規作成モジュール: client/cli(`cherry-testtool-cli`)

旧`invoker.sh`/`stubconfig.sh`(bashスクリプト)を全面的に置き換える、Picocli(`picocli-spring-boot-starter:4.7.7`)ベースのSpring Boot CLIアプリケーション。`lib`への依存は無く(HTTP経由のみ)、`webconsole`同様の独立したGradleプロジェクト(Kotlin DSL)。

### 変更ファイル一覧(新規作成)

- `client/cli/settings.gradle.kts`・`build.gradle.kts`(`spring-boot-starter`、`spring-web`、`spring-boot-starter-json`、`picocli-spring-boot-starter:4.7.7`、Gradle Wrapper一式)
- ドメインエンティティ: `ScriptFileEntry`(scanパッケージ)・`FileProcessingResult`・`BatchResult`(serviceパッケージ)
- 共通コンポーネント: `ScriptFileScanner`(scanパッケージ、BR1・BR2)、`RequestHeaderBuilder`(clientパッケージ、BR6)、`TesttoolApiClient`(`@HttpExchange`)、`ApiClientConfig`、`ApiClientFactory`(いずれもclientパッケージ)
- サービス層: `InvokeService`(`invokeAll`)、`StubConfigService`(`registerAll`/`clearAll`/`showAll`)(serviceパッケージ)
- Picocliコマンド層: `RootCommand`、`InvokeCommand`、`StubConfigCommand`(ディスパッチ専用)、`StubConfigRegisterCommand`/`StubConfigClearCommand`/`StubConfigShowCommand`(commandパッケージ)
- エントリポイント: `CliApplication`(`CommandLineRunner`+`ExitCodeGenerator`、ルートパッケージ)
- 各パッケージの`package-info.java`(`@NullMarked`)、`src/main/resources/application.yml`(`spring.main.web-application-type: none`)
- テスト: `ScriptFileScannerTest`(5件)、`RequestHeaderBuilderTest`(6件)、`InvokeServiceTest`(1件)、`StubConfigServiceTest`(3件)、計15件
- `client/cli/README.md`(ビルド・実行方法、コマンド一覧、旧シェルスクリプトからの移行ガイド、手動結合確認手順)

### 削除

- `client/cli/invoker.sh`・`client/cli/stubconfig.sh`(FR5.1)

## パッケージ構成の見直し(レビュー時にユーザー指示)

当初は`cherry.testtool.cli`パッケージ直下に18ファイルを平置きしていたが、レビューで「もうちょいパッケージ分けした方が良いかも」との指摘を受け、役割別に5パッケージへ再編した(`lib`の既存パッケージ構成(invoker/reflect/script/stub/util/web)と粒度を揃える方針)。

| パッケージ | 内容 |
|---|---|
| `cherry.testtool.cli` | `CliApplication`(エントリポイントのみ) |
| `cherry.testtool.cli.command` | `RootCommand`、`InvokeCommand`、`StubConfigCommand`+3葉コマンド |
| `cherry.testtool.cli.service` | `InvokeService`、`StubConfigService`、`BatchResult`、`FileProcessingResult` |
| `cherry.testtool.cli.scan` | `ScriptFileScanner`、`ScriptFileEntry` |
| `cherry.testtool.cli.client` | `TesttoolApiClient`、`ApiClientConfig`、`ApiClientFactory`、`RequestHeaderBuilder` |

各パッケージへ`package-info.java`(`@NullMarked`)を新設した(5ファイル)。テストクラスも実装と同じパッケージ構成へ移動した。

この見直しの過程で、`domain-entities.md`で定義したものの実装(`RootCommand`)からは一度も参照されていなかった`ConnectionOptions`(record)が未使用のデッドコードであることが判明したため削除した。`RootCommand`は`baseUrl`/`basicAuth`/`headers`を個別フィールドとして保持し、`*Command`側もそれらを個別に参照する設計のまま(component-methods.mdが定めた各Service/Commandのメソッドシグネチャと一貫させるため)で機能上の問題は無い。

再編後、`./gradlew clean test`で全15テストが成功することを再確認し、`--help`・実行可能jarの起動も問題ないことを確認済み。

## Functional Designとの対応

Functional Design(`aidlc-docs/construction/cli/functional-design/`)で確定したBR1-BR9を全て実装した。

| BR | 内容 | 実装 |
|---|---|---|
| BR1 | ディレクトリ再帰走査・パス文字列順ソート | `ScriptFileScanner.scan` |
| BR2 | className/methodName/methodIndex抽出 | `ScriptFileScanner.scan`(数値変換失敗時は`-1`のsentinel値、AskUserQuestionで確認済み) |
| BR3 | 終了コード算出(0=全成功/1=失敗あり) | `BatchResult.failureCount()` → 各`*Command.call()` |
| BR4 | 失敗時継続 | `InvokeService`/`StubConfigService`の`processOne` |
| BR5 | 標準出力形式 | 同上(`show`はAskUserQuestionの結果、3行+空行形式へ変更、旧スクリプトの生JSON表示とは異なる) |
| BR6 | BASIC認証・追加ヘッダ | `RequestHeaderBuilder`(コロン無しヘッダはAskUserQuestionの結果、空値ヘッダ名として受理) |
| BR7 | コマンド→APIマッピング | `TesttoolApiClient`呼出し(`InvokeService`/`StubConfigService`) |
| BR8 | 共通オプションのスコープ | `RootCommand`の`scope = ScopeType.INHERIT` |
| BR9 | stubconfigサブコマンド構成 | `StubConfigCommand`+3葉コマンド |

## 実装前のAPI実物確認

Unit 1-3の前例(Spring Boot 4.x系APIの破壊的変更)に倣い、本Unitで新規に採用した依存の実APIを`javap`で確認した。

- `picocli-spring-boot-starter:4.7.7`: `PicocliAutoConfiguration`が`@Primary @Bean CommandLine.IFactory picocliSpringFactory(ApplicationContext)`を提供することを確認。`AutoConfiguration.imports`も新形式であり、Unit1/3のような破壊的変更は無かった
- `picocli:4.7.7`(core): `CommandLine.ScopeType`・`CommandLine.ParentCommand`の存在を確認
- `spring-test:7.0.8`: `MockRestServiceServer.bindTo(RestClient.Builder)`の存在を確認

## 実機確認で発見・修正した不具合

`demo`(8080)を実際に起動し、ビルド済みjarで`invoke`・`stubconfig register/show/clear`を実行する手動結合確認を行ったところ、`stubconfig show`が以下のエラーで失敗することを発見した。

```
Could not extract response: no suitable HttpMessageConverter found for response type List<String> and content type application/json
```

原因は、本モジュールが`spring-boot-starter-web`に依存しないため(NFR: 組込みTomcat不要)、`RestClient`のデフォルトJSON用`HttpMessageConverter`(Jacksonベース)がクラスパス上に存在せず自動登録されなかったこと。`build.gradle.kts`へ`spring-boot-starter-json`を追加して解決した。

修正後、`demo`に対する実際のHTTP往復で以下を確認した。

- `stubconfig register`でスタブを登録すると、`demo`の`/api/sample/stubbed1/int`が登録値を返すようになる
- `stubconfig show`でscript/engine/評価結果が3行で表示される
- `stubconfig clear`で解除すると、元の計算結果に戻る
- `invoke`で、正常呼出し(応答本文表示)・例外発生(YAML形式のエラー情報表示、いずれも終了コード0、BR3の通りHTTPレベルの成功/失敗のみが終了コードに反映される)の両方が想定通り動作する

## ビルド検証

`./gradlew clean test`を実行し、全15テストが成功することを確認済み。

## Gradleマルチプロジェクト化(2026-08-09、レビュー時にユーザー指示)

`lib`の`includeBuild`起因のIntelliJ IDE不具合を受け、リポジトリ全体がGradleマルチプロジェクト化された。本モジュールはGradleパス`:client:cli`(独自の`settings.gradle.kts`・Gradle Wrapperは削除、リポジトリ直下の1組へ統合)となり、`build.gradle.kts`へ`base { archivesName.set("cherry-testtool-cli") }`を追加して成果物名を維持した(依存関係・Picocliコマンド構成自体に変更は無い)。詳細は[lib-unit-summary.md](../../lib/code/lib-unit-summary.md)「Gradleマルチプロジェクト化」を参照。
