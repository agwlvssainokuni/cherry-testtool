# Code Generation Plan: webconsole frontendのUIライブラリ移行(make-you-chic-uiへの切替)

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR11.1〜FR11.11)、`aidlc-docs/inception/plans/ui-library-migration-execution-plan.md`

## 事前調査結果(実装方針の確定)

対象は`client/webconsole/frontend`のみ。Requirements Analysis中に以下は完了済みで、本計画のStepには含めない。

- FR11.1: git submodule追加・`package.json`への`make-you-chic-ui`依存追加
- FR11.8: `layout-css` Skillの`client/webconsole/frontend/.claude/skills/`へのコピー
- FR11.9: make-you-chic-ui submoduleのfast-forward更新(`topbarStart`/`topbarEnd`拡張含む)・dist再ビルド

**コンポーネント対応表**(MUI → make-you-chic-ui):

| MUI | make-you-chic-ui | 備考 |
|---|---|---|
| `Container` | 素の`<div>`+画面固有CSSクラス | layout-css Skill方針 |
| `Typography` | 素のHTML要素(`h1`/`p`等)+画面固有CSSクラス | 同上 |
| `Button` | `Button` | `variant`値が異なる(`contained`→`primary`等) |
| `Grid` | 画面固有CSSクラス(flex/grid直書き) | layout-css Skill方針 |
| `TextField`(単一行) | `FormField`+`TextInput` | `onBlur`等の標準input属性はそのまま透過 |
| `TextField`(`multiline`) | `FormField`+`Textarea` | 対象4箇所(invoker/stubconfig×スクリプト入力欄・実行結果欄)にFR11.7.1の等幅フォントCSSクラスを付与 |
| `Select`+`MenuItem` | `Select`(`options: {label,value}[]`)+`SelectOption`型 | `onChange`はイベントでなく`value: string`を直接渡す設計のため呼出し側を書き換える |
| `InputLabel` | `FormField`の`label`プロップ | 単体コンポーネントではないため`FormField`が入力を包む形にする |

`FormField`は`children`の入力コンポーネント(`TextInput`/`Select`/`Textarea`)へContext経由でid/aria属性を自動付与するため、各入力は必ず`FormField`の直接の子として配置する。

## Steps

### 依存関係・エントリポイント

- [x] **Step 1**: `package.json`を修正する
  - `dependencies`から`@mui/material`・`@emotion/styled`を削除
  - `@fontsource/noto-sans-jp`・`@fontsource/noto-serif-jp`を`dependencies`へ追加
  - `npm install`を実行し`package-lock.json`を更新する

- [x] **Step 2**: 静的アセットを`public/`へ移動する
  - `src/assets/`配下の6ファイル(`favicon.ico`・`logo.svg`・`logo.xcf`・`logo192.png`・`logo512.png`・`manifest.json`)を`public/`直下へ移動
  - `index.html`の`<link rel="icon">`・`<link rel="apple-touch-icon">`の参照パスを`/favicon.ico`・`/logo192.png`(public直下の絶対パス)へ更新し、`<link rel="manifest" href="/manifest.json"/>`を追加
  - `src/assets/`ディレクトリを削除する

- [x] **Step 3**: `src/lib/common.ts`を新設する
  - `src/common.ts`の内容をそのまま`src/lib/common.ts`へ移動する
  - 旧`src/common.ts`を削除する(参照元の更新は後続Stepで実施)

- [x] **Step 4**: `src/main.tsx`を修正する
  - `make-you-chic-ui`から`ThemeProvider`・`ToastProvider`・`ModalStackProvider`をimportし、`ThemeProvider > ToastProvider > ModalStackProvider > BrowserRouter > App`の順で入れ子にする(integration-guideのセットアップ例に準拠)
  - `@fontsource/noto-sans-jp/japanese-{400,500,600,700}.css`・`@fontsource/noto-serif-jp/japanese-{400,500,600,700}.css`(計8つ)をimportする

### レイアウト・ルーティング

- [x] **Step 5**: `src/layouts/AppShellLayout.tsx`を新設する
  - `make-you-chic-ui`の`AppShell`・`useTheme`・`Button`・`Dropdown`をimport、`react-router-dom`の`Outlet`・`useLocation`(または`NavLink`相当の選択状態管理、`AppShell`の`navItems`側で現在地判定が必要な場合)をimport
  - `navItems`に3画面(ラベル「ホーム」`href="/"`、「呼出しツール」`href="/invoker"`、「スタブ設定ツール」`href="/stubconfig"`)を設定する
  - `topbarEnd`に、`useTheme()`から取得した現在のテーマ状態を表示する`Button`(`variant="ghost"`、ラベル「テーマ」)をtriggerとする`Dropdown`を実装する。`items`は4つの`MenuItem`とし、それぞれクリックのたびに次の値へ切り替える(モード: light⇄dark、ブランド: blue→green→purple→orange→blue…循環、フォント: sans⇄serif、文字サイズ: sm→md→lg→sm…循環)
  - `children`に`<Outlet/>`を配置する

- [x] **Step 6**: `src/App.tsx`を書き換える
  - ルーティング定義のみに整理する(state・ロジックは持たない)
  - `<Routes>`内に`path`無しの親`<Route element={<AppShellLayout/>}>`を配置し、既存3ルート(`/`→`HomePage`、`/invoker`→`InvokerPage`、`/stubconfig`→`StubconfigPage`)をその子ルートとしてネストする(react-routerの「レイアウトルート」パターン)
  - importパスを新配置(`./pages/Home/HomePage`・`./pages/Invoker/InvokerPage`・`./pages/Stubconfig/StubconfigPage`・`./layouts/AppShellLayout`)に更新する

### 画面コンポーネント

- [x] **Step 7**: `src/pages/Home/HomePage.tsx`・`src/pages/Home/HomePage.css`を新設する
  - 見出しを素のHTML要素(`<h1>`)+`HomePage.css`のクラスで表現する(`Typography`の代替)
  - `make-you-chic-ui`の`Card`を2つ配置する: 「呼出しツール」(説明文: 指定したBeanのメソッドを任意の引数で直接呼び出せる旨)、「スタブ設定ツール」(説明文: メソッドの戻り値をスクリプトで差し替えるスタブを設定できる旨)
  - 各`Card`は`react-router-dom`の`Link`(`to="/invoker"`・`to="/stubconfig"`)でラップし、Card全体をクリック可能にする
  - `HomePage.css`はlayout-css Skill方針(画面固有クラス、`var(--space-*)`等のトークン参照)に従いCardの並びをflex/gridで組む
  - 旧`src/Home.tsx`を削除する

- [x] **Step 8**: `src/pages/Invoker/`(`InvokerPage.tsx`・`InvokerPage.css`・`api.ts`)を新設する
  - 旧`src/invoker/App.tsx`のstate・ハンドラ(`handleClassName`・`handleMethodName`・`handleInvoke`)をそのまま移植する
  - クラス名・メソッド名の入力は`FormField`(`label`に元の`InputLabel`テキスト)+`TextInput`(`value`/`onChange`/`onBlur`は元のTextFieldから引き継ぐ)に置換する
  - Bean名称・メソッド候補の`Select`は、`options={list.map((label, i) => ({ label, value: String(i) }))}`のように変換し、`onChange={(value) => ...}`で直接値を受け取る形に書き換える(元は`e.target.value`イベント経由)
  - 引数生成スクリプト欄・実行結果欄は`FormField`+`Textarea`に置換し、`InvokerPage.css`で定義する等幅フォントクラス(FR11.7.1: `ui-monospace, SFMono-Regular, 'SF Mono', Consolas, 'Liberation Mono', Menlo, monospace`)を付与する
  - 実行ボタンは`Button`(`variant="primary"`)に置換する
  - フォーム全体のレイアウトはlayout-css Skill方針に従い`InvokerPage.css`のflex/gridクラスで組む(元の`Grid`のラベル+入力の2カラム構成を踏襲)
  - 旧`src/invoker/api.ts`を`src/api/invoker.ts`へ移動し、`common.ts`の参照パスを`../lib/common`へ更新する(2026-08-14追記: レビュー時の追加依頼「APIはapi/ディレクトリに集約」を受け、当初計画の`src/pages/Invoker/api.ts`から変更)
  - 旧`src/invoker/`ディレクトリを削除する

- [x] **Step 9**: `src/pages/Stubconfig/`(`StubconfigPage.tsx`・`StubconfigPage.css`・`api.ts`)を新設する
  - Step 8と同様の方針で、旧`src/stubconfig/App.tsx`のstate・ハンドラを移植し、コンポーネントを置換する
  - 旧`src/stubconfig/api.ts`を`src/api/stubconfig.ts`へ移動し、`common.ts`の参照パスを`../lib/common`へ更新する(2026-08-14追記: レビュー時の追加依頼「APIはapi/ディレクトリに集約」を受け、当初計画の`src/pages/Stubconfig/api.ts`から変更)
  - 旧`src/stubconfig/`ディレクトリを削除する

### 検証・後片付け

- [x] **Step 10**: 重複・未参照ファイルの確認
  - 旧ファイル(`src/App.tsx`は内容置換のため残置、`src/Home.tsx`・`src/common.ts`・`src/invoker/`・`src/stubconfig/`・`src/assets/`)が削除されていることを確認する
  - `git status`で意図した差分(新設・移動・削除)のみになっていることを確認する

- [x] **Step 11**: ビルド確認
  - `npm run lint`(oxlint/eslint)を実行しエラー無く成功することを確認する
  - `npm run build`(`tsc -b && vite build`)を実行し、型エラー無く成功することを確認する

- [x] **Step 12**: サマリー文書を作成する
  - `aidlc-docs/construction/webconsole/code/ui-library-migration-summary.md`を新規作成し、変更ファイル一覧(新設/移動/削除)・主要な設計判断(コンポーネント対応表、ディレクトリ再編、Topbarテーマ選択の実装)を記録する
