# Performance Test Instructions

## 適用判定: N/A

本プロジェクトはローカル開発者向けのテストツール(単一利用者、ローカル環境での対話的な利用が前提)であり、Requirements Analysis時点でPerformance/Resiliency系のNFR拡張(Resiliency Baseline)は明示的に見送りと判断している(`aidlc-docs/aidlc-state.md`のExtension Configuration参照)。要件定義書(`requirements.md`)にもスループット・応答時間・同時接続数等の性能要件は存在しない。

そのため、本Build and Testステージでは性能テスト(負荷試験・ストレス試験)を実施しない。

将来的に多人数・CI組込み等での利用を想定する場合は、`webconsole`のプロキシ経由アクセスに対する簡易な負荷試験(例: `k6`/`hey`等での同時リクエスト試験)の追加を検討すること。
