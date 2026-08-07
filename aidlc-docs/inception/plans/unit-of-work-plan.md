# Unit of Work Plan

`aidlc-docs/inception/application-design/`(components.md等)は既にモジュール単位(`lib`、`client/webconsole`、`client/cli`、`demo`)でコンポーネントを整理している。この構成をそのままUnit分解の基礎として用いる。

**補足**: 本プロジェクトはUser Storiesステージをスキップしているため、`unit-of-work-story-map.md`はストーリーではなく、`requirements.md`のFR(機能要件)をUnitへマッピングする文書として作成する。

## 生成する成果物(Mandatory Unit Artifacts)

- [ ] `aidlc-docs/inception/application-design/unit-of-work.md` — Unit定義・責務
- [ ] `aidlc-docs/inception/application-design/unit-of-work-dependency.md` — Unit間依存関係マトリクス
- [ ] `aidlc-docs/inception/application-design/unit-of-work-story-map.md` — FR(機能要件)とUnitのマッピング(Storiesの代替)
- [ ] Unit境界・依存関係の妥当性検証
- [ ] 全FRがいずれかのUnitに割り当てられていることの確認

## 提案するUnit分解(たたき台)

Application Design・execution-plan.mdのModule Update Strategyを踏襲し、モジュール = Unitとして1:1で対応させる。

| Unit | 対象モジュール | 主なFR |
|---|---|---|
| Unit 1: lib | `lib` | FR3, FR4, FR7(lib分), FR8, NFR5(lib分) |
| Unit 2: demo | `demo`(新設) | FR6, FR7(demo分), NFR5(demo分) |
| Unit 3: webconsole | `client/webconsole`(新設、旧gateway+spa) | FR1, FR2, FR7(webconsole分), NFR5(webconsole分) |
| Unit 4: cli | `client/cli`(新設、全面書換え) | FR5, FR7(cli分), NFR5(cli分) |

## 各カテゴリの評価

- **Story Grouping**: 対象外(User Storiesスキップのため、FRベースのマッピングで代替)
- **Dependencies**: `demo`は`lib`にコンパイル依存するため、Unit 1(lib)→Unit 2(demo)の順序が必須。`webconsole`・`cli`はビルド時に`lib`・`demo`いずれにも依存しないため、Unit 1/2完了後であれば任意の順で着手可能(結合確認のみ`demo`起動が必要)
- **Team Alignment**: 本プロジェクトは単一開発者(AI-DLCのPer-Unit Loopに従い1Unitずつ順に完了させる)。Unit間のオーナーシップ分割は不要
- **Technical Considerations**: 各Unit(モジュール)はスケーラビリティ・デプロイ要件に差異が無いローカル開発ツールであり、対象外
- **Business Domain**: 単一の境界づけられたコンテキスト(テストツール)であり、Unit分割の判断材料としては対象外
- **Code Organization**: ディレクトリ構成(`lib/`、`demo/`、`client/webconsole/`、`client/cli/`)はrequirements.mdで既に確定済み

## Unit of Work Questions

### Question 1: Unit分解の妥当性
上記4Unit(lib / demo / webconsole / cli)への分解、およびモジュール=Unitという対応関係でよいか確認してください。

A) この4Unit分解でよい

X) Other(please describe after [Answer]: tag below、分割・統合したい場合は具体的に記述)

[Answer]: A

### Question 2: Unit着手順序
Unit 1(lib)→Unit 2(demo)の順序は依存関係上必須です。Unit 3(webconsole)・Unit 4(cli)は互いに依存が無いため順序は任意ですが、CLAUDE.mdのPer-Unit Loopは1Unitずつ順に完了させる方式のため、明示的な着手順序を決める必要があります。

A) lib → demo → webconsole → cli の順で進める(推奨。ブラウザで確認できるwebconsoleを先に完成させ、CLIは最後に回す)

B) lib → demo → cli → webconsole の順で進める

[Answer]: A

X) Other(please describe after [Answer]: tag below)

[Answer]:
