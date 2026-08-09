# `/testtool/**` APIキー保護(demo部分)- Code Generation Summary

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR10.6)、`aidlc-docs/construction/plans/api-key-protection-code-generation-plan.md`(Step12-13)

## 変更ファイル一覧

### 修正
- `demo/src/main/resources/application.yml` — `cherry.testtool.web.api-key`/`api-key-header`の設定例をコメントアウトした状態(既定は無効)で追記。`client/webconsole`(backendへのプロキシ時に自動付与)・`client/cli`(既定ヘッダ付与)も同じ構成項目を使う旨のコメントを併記

コード変更は無し(設定例の追加のみ)。

## 動作確認

`./gradlew :demo:build`で既存テストの回帰が無いことを確認済み。`cherry.testtool.web.api-key`を実際に設定した上での結合確認(demo+webconsole+cliを実起動し、キー未設定/一致/不一致の3パターンを検証)はBuild and Testフェーズで実施する。
