# Business Logic Model - cli(Unit 4)

## 全体フロー

```
CliApplication(CommandLineRunner)
  └─ CommandLine(RootCommand).execute(args)
       ├─ RootCommand: --url/--basic-auth/--header(INHERIT)を解析、サブコマンドへディスパッチ
       │
       ├─ invoke {dirs}...
       │    └─ InvokeCommand.call()
       │         └─ InvokeService.invokeAll(baseUrl, dirs, basicAuth, headers)
       │              ├─ for each dir: ScriptFileScanner.scan(dir) → List<ScriptFileEntry>
       │              ├─ for each entry: ApiClientFactory.create(baseUrl).invoke(className, methodName, methodIndex, scriptContent, "", headers)
       │              │     (BR4: 例外はcatchしFileProcessingResult(success=false)として記録、継続)
       │              ├─ 各結果をBR5の形式で標準出力へ表示
       │              └─ BatchResultを返す
       │         └─ 終了コード(BR3)をCliApplicationへ返却
       │
       └─ stubconfig {register|clear|show} {dirs}...
            └─ StubConfigCommand(ディスパッチのみ) → Register/Clear/ShowCommand.call()
                 └─ StubConfigService.{registerAll|clearAll|showAll}(baseUrl, dirs, basicAuth, headers)
                      (invokeAllと同様の走査・呼出し・集計フロー。呼出し先APIとscriptパラメータのみBR7に従い異なる)
```

## InvokeService.invokeAll の詳細アルゴリズム

1. `BatchResult`用の結果リストを初期化する
2. 引数`directories`の各`dir`について、順に以下を実行する
   1. `PROCESSING {dir}`を出力する(BR5)
   2. `ScriptFileScanner.scan(dir)`で`List<ScriptFileEntry>`を取得する(BR1・BR2)
   3. 各`entry`について、順に以下を実行する
      1. `entry.filePath`のファイル内容を読み込む(`Files.readString`)
      2. `ApiClientFactory.create(baseUrl)`で`TesttoolApiClient`を取得する(prototype Bean)
      3. ヘッダ(`--header`由来 + `--basic-auth`由来のBasic認証ヘッダ、BR6)を`MultiValueMap`に組み立てる
      4. `client.invoke(entry.className, entry.methodName, entry.methodIndex, fileContent, "", headers)`を呼び出す
      5. 成功時: `FileProcessingResult(entry, true, 応答本文)`を作り、BR5の形式で標準出力へ表示する
      6. 失敗時(`RestClientException`等): `FileProcessingResult(entry, false, エラーメッセージ)`を作り、同様に表示した上で処理を継続する(BR4)
      7. 結果を結果リストへ追加する
3. `BatchResult(結果リスト)`を返す

## StubConfigService.{registerAll,clearAll,showAll} の詳細アルゴリズム

`invokeAll`と同一の走査・繰返し構造(上記2-3)を共有し、以下の点のみ異なる。

- 呼び出すAPIエンドポイントとscriptパラメータはBR7の表に従う(`registerAll`→`put`(ファイル内容)、`clearAll`→`put`(空文字列)、`showAll`→`get`(パラメータなし))
- `showAll`は成功時の表示末尾に空行を1行追加する(BR5)

## ExitCodeGenerator算出ロジック(CliApplication)

1. `CommandLineRunner.run(String... args)`内で`CommandLine(rootCommand).execute(args)`を実行する
2. 各`*Command.call()`は、対応する`*Service`の戻り値(`BatchResult`)から`BatchResult.failureCount()`を取り出し、`Integer`(Picocliの`call()`戻り値、`0`または`1`)として返す(BR3)。Picocli自体の使用方法誤り(未知オプション等)はPicocliが自動的に非0の終了コードを生成する
3. `CliApplication`は`CommandLine.execute(args)`の戻り値をそのままフィールドへ保持し、`getExitCode()`で返す

## エラーハンドリング方針

- **ファイル単位の処理エラー**(HTTP呼出し失敗、`methodIndex`変換エラー等): BR4により捕捉・記録し、処理は継続する。CLIプロセス自体は異常終了しない(最終的な終了コードはBatchResultの集計結果による)
- **致命的エラー**(指定ディレクトリが存在しない、`baseUrl`が不正なURI等): Picocliの型変換/バリデーション機構、または`InvokeService`/`StubConfigService`冒頭での事前チェックにより早期に例外を投げ、CLIプロセスを異常終了させる(この場合の終了コードはPicocliの規約に従う)
