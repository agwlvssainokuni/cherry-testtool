# UIライブラリ移行(make-you-chic-ui) 確認事項

`client/webconsole/frontend`のUIライブラリをMUI(`@mui/material`)から自作の`make-you-chic-ui`(submodule: `vendor/make-you-chic-ui`)へ切り替えるにあたっての確認事項です。各質問の選択肢から選び、[Answer]:タグの後ろに記入してください。

## Question 1
移行の範囲について。MUI依存(`@mui/material`・`@emotion/styled`)の扱いはどうしますか。

A) make-you-chic-uiへ完全移行し、`@mui/material`・`@emotion/styled`をpackage.jsonから削除する

B) まずは既存3画面(Home/Invoker/Stubconfig)をmake-you-chic-uiへ置き換えるが、MUI依存自体は当面残す

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 2
レイアウト・ナビゲーション方針について。現状Home/Invoker/StubconfigはURL直接指定でのみ遷移可能で、画面間のナビゲーションリンクは存在しません。make-you-chic-uiには`AppShell`(Sidebar付きの全画面レイアウト)コンポーネントがあります。

A) `AppShell`を導入し、3画面を行き来できるSidebarナビゲーションを新設する

B) `AppShell`は導入せず、各画面は現状同様の単純なレイアウトのまま、個別部品(Button/TextInput/Select等)だけを置き換える

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 3
`ThemeProvider`/`ToastProvider`/`ModalStackProvider`(いずれもmake-you-chic-uiの推奨セットアップ)の導入範囲について。

A) 3つとも導入する(テーマ切替・トースト通知・モーダルスタック管理を有効化する)

B) `ThemeProvider`のみ導入する(トースト通知・モーダルは現状使用箇所がないため不要)

C) Providerは一切導入せず、個別コンポーネントの置き換えのみに留める(いずれも省略可能なfail-soft設計のため動作はする)

D) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 4
Webフォント(`@fontsource/noto-sans-jp`・`@fontsource/noto-serif-jp`)の追加について。make-you-chic-ui本体はフォント本体を同梱しないため、利用側で追加する方針が推奨されています。

A) integration-guideの推奨通り追加する(自己ホスティングの日本語Webフォントを使用する)

B) 追加せず、システムフォントへのフォールバックのまま使用する

C) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 5
Invoker/Stubconfig画面の「実行結果」欄の表示方法について。現状はTextField(multiline)にAPIレスポンス文字列・エラー文字列をそのまま表示しています。

A) 現状踏襲で、`Textarea`にそのまま表示する(表示方法は変えず部品だけ`TextField`→`Textarea`へ置換する)

B) エラー時は`Alert`または`Toast`コンポーネントで通知し、実行結果欄は正常時のレスポンス表示専用に用途を絞る

C) Other (please describe after [Answer]: tag below)

[Answer]: 
