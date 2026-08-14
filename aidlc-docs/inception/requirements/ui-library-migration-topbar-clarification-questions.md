# FR11 追加確認: Topbarへのテーマ選択配置

「AppShellのsidebarに各画面へのリンク、topbarにテーマ選択を配置」との依頼を受けての確認です。Sidebar側(各画面へのリンク)はAppShellの`navItems`propでそのまま実現できますが、Topbar側は現状のmake-you-chic-ui(`AppShell`/`Topbar`)に任意コンテンツを差し込むprop(スロット)が無く、`user`アバター+`userMenuItems`(Dropdownメニュー)しか拡張点がありません。この状態でテーマ選択をTopbarへ配置する方法を選んでください。

## Question 1
Topbarへのテーマ選択配置方法について。

A) make-you-chic-ui本体(vendor submodule、`client/webconsole/frontend/vendor/make-you-chic-ui`)の`AppShell`/`Topbar`を拡張し、Topbarへ任意コンテンツ(テーマ選択UI)を差し込めるprop(例: `topbarActions`)を新設する。デザインシステム自体への機能追加となるため、submodule側リポジトリでの変更も今回のFR11のスコープに含める

B) `AppShell`は変更せず、既存の`user`アバター+`userMenuItems`(クリックで開くDropdownメニュー)の仕組みをテーマ選択メニューとして転用する(実際のユーザーアカウント機能ではないが、Topbarで唯一カスタマイズ可能な差し込み口のため)

C) Other (please describe after [Answer]: tag below)

[Answer]: A(ただしuser側の作業により、実装済みのprop名は`topbarActions`ではなく`topbarStart`/`topbarEnd`の2スロット)

## Question 2
(Q1でAを選んだ場合のみ回答) submodule側での変更を、cherry-testtool側のこのAI-DLCワークフロー(FR11)の中で一緒に進めてよいか、それとも別リポジトリでの別作業として切り離すか。

A) 一緒に進めてよい(このセッション内でsubmodule側リポジトリも編集し、変更後にdist再ビルド・submoduleポインタ更新・cherry-testtool側の`npm install`までを本FR11のCode Generationで行う)

B) submodule側の変更は別リポジトリでの別作業として切り離し、今回のFR11では最新のdistを前提にmake-you-chic-ui側の対応を待つ(先にProviderの導入・部品置換のみ進め、Topbarテーマ選択は別のPost-Construction Changeとして後日対応)

C) Other (please describe after [Answer]: tag below)

[Answer]: (該当なし。ユーザーがmake-you-chic-ui本体を直接修正・コミット・pushし、cherry-testtool側でorigin/mainをfast-forward取り込み・dist再ビルド済みのため、Code Generationでのsubmodule側変更作業は不要)
