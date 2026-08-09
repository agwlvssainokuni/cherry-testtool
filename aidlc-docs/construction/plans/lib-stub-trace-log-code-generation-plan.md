# Code Generation Plan: スタブ実行時のトレースログ出力(lib Unit)

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR9)、`aidlc-docs/inception/plans/stub-trace-log-execution-plan.md`

## Unit Context
- **対象Unit**: `lib`(既存Unit、新規Unit分解なし)
- **対象コンポーネント**: `StubResolver`(`lib/src/main/java/cherry/testtool/stub/StubResolver.java`)
- **依存関係**: `StubInterceptor`(`@Deprecated`)・`demo`の`StubAspect`は`StubResolver.getStubInvocation(...)`の戻り値`StubInvocation`を実行するのみで、シグネチャは変更しないため無影響
- **FR対応**: FR9.1〜FR9.4(全て)

## Steps

- [ ] **Step 1: Business Logic Generation** — `StubResolver.java`を修正する
  - SLF4J `Logger`フィールドを追加(`StubConfigLoader`と同じパターン: `private final Logger logger = LoggerFactory.getLogger(getClass());`)
  - `getStubInvocation(Method)`のラムダ内、`scriptProcessor.eval(...)`の呼出しをtry-catchで囲み、成功時は戻り値を、`ScriptException`捕捉時はその内容を、それぞれ含めて評価後にまとめて1回`logger.trace(...)`で出力する(対象メソッド・script・engine・args・結果を含む。FR9.3/FR9.4)
  - 例外時も既存の`cause`再throwロジックは変更しない(ログ出力はその前に行う)

- [ ] **Step 2: Business Logic Unit Testing** — 既存テストの回帰確認
  - `StubAspectTest`・`StubRepositoryTest`等、`StubResolver`を経由する既存テストが全て成功することを確認する(`./gradlew :lib:test`)
  - ログ出力自体をアサートする専用テストは追加しない(本コードベースにログ出力を検証する既存パターンが無く、ログ文言はデバッグ支援目的でAPI契約ではないため)。代わりに、`./gradlew :lib:test`実行時のログレベルを一時的にTRACEへ引き上げ、標準出力に想定通りの内容(対象メソッド・script・engine・args・結果)が出力されることを目視確認する(NFR2の「手動確認手順」に相当)

- [ ] **Step 3: Business Logic Summary** — `aidlc-docs/construction/lib/code/stub-trace-log-summary.md`を新規作成する
  - 既存の`lib-unit-summary.md`(Unit 1完了時点の記録)は変更せず、本改修専用の新規サマリーとして記録する(reverse-engineering成果物と同様、既存の完了済みサマリーは時点記録として扱う方針)
  - 変更ファイル一覧、ログ出力仕様(FR9.1〜FR9.4)、動作確認結果を記載する

## Out of Scope
- API Layer / Repository Layer / Frontend Components / Database Migration / Deployment Artifacts — 本改修はlib内部のログ追加のみのため該当なし
