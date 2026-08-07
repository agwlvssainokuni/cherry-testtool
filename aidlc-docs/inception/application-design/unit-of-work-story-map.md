# Unit of Work - FR Map(Story Mapの代替)

本プロジェクトはUser Storiesステージをスキップしているため、`requirements.md`の機能要件(FR)・非機能要件(NFR)をUnitへマッピングする。全FR/NFRがいずれかのUnitに割り当てられていることを確認する。

## FRマッピング

| FR | 内容 | 割当Unit |
|---|---|---|
| FR1 | `client/gateway`のビルド構成是正(`settings.gradle`追加) | Unit 3: webconsole(統合先に引き継ぎ) |
| FR2(FR2.1-2.9) | `client/spa`と`client/gateway`の統合(`client/webconsole`) | Unit 3: webconsole |
| FR3 | `InvokerServiceImpl`の意図的な例外処理へのコメント補足 | Unit 1: lib |
| FR4 | `lib`内のInterface/Impl分離の解消 | Unit 1: lib |
| FR5(FR5.1-5.6) | `client/cli`のSpring Bootアプリ化(Picocli、ExitCodeGenerator) | Unit 4: cli |
| FR6(FR6.1-6.4) | `lib`を組み込むデモアプリの新設 | Unit 2: demo |
| FR7 | コードコメントの充実(全モジュール横断) | Unit 1〜4(各Unit実施時に該当コードへ適用) |
| FR8(FR8.1-8.4) | lib Controllerの統合(`TesttoolController`) | Unit 1: lib |

## NFRマッピング

| NFR | 内容 | 割当Unit |
|---|---|---|
| NFR1 | 互換性(外部インタフェース変更の許容と追随修正) | Unit 1〜4(該当する変更を含むUnitで対応) |
| NFR2 | テスト方針(単体テスト+可能な範囲で結合テスト・手動確認) | Unit 1〜4、および全Unit完了後のBuild and Test |
| NFR3 | 適用しない拡張機能(Security/Resiliency/PBT) | 対象外(全Unit共通で不適用) |
| NFR4 | 作業分解(Application Design/Units Generation実行) | 本ステージ自体で対応済み |
| NFR5 | Nullability規約統一(JSpecify) | Unit 1〜4(各Unit実施時に該当コードへ適用) |

## 検証

- [x] FR1-FR8の全項目がいずれかのUnitに割り当てられている
- [x] NFR1-NFR5の全項目がいずれかのUnit、または全Unit横断の方針として割り当てられている
- [x] FR7・NFR5は特定の1Unitに閉じない横断的要件のため、各Unit(1〜4)の実施時にそれぞれ適用される旨を明記した
