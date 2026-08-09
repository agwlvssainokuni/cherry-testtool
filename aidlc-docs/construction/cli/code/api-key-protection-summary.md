# `/testtool/**` APIキー保護(cli部分)- Code Generation Summary

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR10)、`aidlc-docs/construction/plans/api-key-protection-code-generation-plan.md`(Step8-11)

## 変更ファイル一覧

### 新規作成
- `client/cli/src/test/java/cherry/testtool/cli/command/RootCommandTest.java` — `RootCommand.effectiveHeaders()`の単体テスト(4ケース: 未設定→`headers`をそのまま返す、空文字→同様、設定済み→ヘッダ合成、明示ヘッダ無し+設定済み→APIキーヘッダのみ)

### 修正
- `client/cli/src/main/java/cherry/testtool/cli/command/RootCommand.java`
  - `@Value("${cherry.testtool.web.api-key:}")`(`apiKey`)・`@Value("${cherry.testtool.web.api-key-header:X-Cherry-Testtool-Api-Key}")`(`apiKeyHeader`)フィールドを追加。Picocliの`@Option`ではなくSpring設定由来の値(FR10.5)
  - `List<String> effectiveHeaders()`を新設。`apiKey`が設定されていれば`headers`のコピーへ`"{apiKeyHeader}: {apiKey}"`を追加、未設定なら`headers`をそのまま返す
- `client/cli/src/main/java/cherry/testtool/cli/command/InvokeCommand.java`、`StubConfigRegisterCommand.java`、`StubConfigClearCommand.java`、`StubConfigShowCommand.java` — `rootCommand.headers`/`rc.headers`の参照4箇所を`rootCommand.effectiveHeaders()`/`rc.effectiveHeaders()`へ変更

## 設計判断

- 当初検討した「`RequestHeaderBuilder.build(...)`・`InvokeService`・`StubConfigService`へパラメータを追加する」案は、既存テスト(`RequestHeaderBuilderTest`・`InvokeServiceTest`・`StubConfigServiceTest`)の呼出し箇所も含め影響範囲が広くなるため採用しなかった。代わりに`RootCommand`へ`effectiveHeaders()`を新設し、呼出し元4箇所の参照を差し替えるだけに留めることで、これら既存クラス・テストを一切変更せずに実現した
- `--header`オプションによる個別指定は引き続き利用可能(`effectiveHeaders()`は`headers`フィールドを起点に合成するため、`--header`で明示指定された内容は保持される)

## 動作確認

`./gradlew :client:cli:build`で新規`RootCommandTest`4件を含む全テストが成功することを確認済み(既存`RequestHeaderBuilderTest`・`InvokeServiceTest`・`StubConfigServiceTest`は無変更のため回帰リスクなし)。実際にCLIからAPIキー付きリクエストが送出されることの結合確認は、`lib`・`webconsole`の実装と合わせてBuild and Testフェーズで実施する。
