# webconsole frontendのUIライブラリ移行(make-you-chic-uiへの切替) - Code Generation Summary

**参照**: `aidlc-docs/inception/requirements/requirements.md`(FR11.1〜FR11.11)、`aidlc-docs/construction/plans/webconsole-ui-library-migration-code-generation-plan.md`(全12Step)

## 変更ファイル一覧

### 依存関係
- `client/webconsole/frontend/package.json` — `@mui/material`・`@emotion/styled`を削除、`@fontsource/noto-sans-jp`・`@fontsource/noto-serif-jp`(いずれも`^5.3.0`)を追加。`npm install`実行済み(`package-lock.json`更新)

### 静的アセット(移動)
- `src/assets/favicon.ico` → `public/favicon.ico`
- `src/assets/favicon.xcf` → `public/favicon.xcf`(計画作成時に見落としていたファイル。「未参照ファイルも含め全て移動」という確認済み方針(ディレクトリ構成確認質問Q2回答: A)に沿って移動)
- `src/assets/logo.svg` → `public/logo.svg`
- `src/assets/logo.xcf` → `public/logo.xcf`
- `src/assets/logo192.png` → `public/logo192.png`
- `src/assets/logo512.png` → `public/logo512.png`
- `src/assets/manifest.json` → `public/manifest.json`
- `index.html` — アイコン参照パスを`/favicon.ico`・`/logo192.png`(public直下の絶対パス)へ更新、`<link rel="manifest" href="/manifest.json"/>`を追加

### エントリポイント・レイアウト
- `src/main.tsx` — `ThemeProvider`/`ToastProvider`/`ModalStackProvider`を`BrowserRouter`の外側にネスト、`@fontsource/noto-sans-jp`・`@fontsource/noto-serif-jp`の日本語ウェイト(400/500/600/700)をimport
- `src/layouts/AppShellLayout.tsx`(新規) — `AppShell`を導入。`navItems`は`useNavigate()`を使ったSPAナビゲーション(`onClick`で`preventDefault`+`navigate`)。`topbarEnd`に`useTheme()`の4軸(mode/brand/fontFamily/fontSize)を順送りで切り替える`Dropdown`+`Button`(ラベル「テーマ」)を配置
- `src/App.tsx`(修正) — ルーティング定義のみに整理。`AppShellLayout`を親とするレイアウトルートへ既存3ルートをネスト

### ディレクトリ再編・画面コンポーネント
- `src/common.ts` → `src/lib/common.ts`(移動のみ、内容変更なし)
- `src/Home.tsx` → `src/pages/Home/HomePage.tsx` + `HomePage.css`(新規) — `Container`/`Typography`を素のHTML要素+CSSクラスへ、Invoker/Stubconfigへの説明文付き`Card`(`react-router-dom`の`Link`でラップ)を新設
- `src/invoker/App.tsx` → `src/pages/Invoker/InvokerPage.tsx` + `InvokerPage.css`(新規)
- `src/stubconfig/App.tsx` → `src/pages/Stubconfig/StubconfigPage.tsx` + `StubconfigPage.css`(新規)
- `src/invoker/api.ts` → `src/api/invoker.ts`、`src/stubconfig/api.ts` → `src/api/stubconfig.ts`(import元を`../lib/common`へ更新。当初計画では`src/pages/<Page>/api.ts`とコロケーションする予定だったが、Code Generationレビュー時の「APIは`api/`ディレクトリに集約」との追加依頼を受けて`src/api/`へ集約する形へ修正、`InvokerPage.tsx`・`StubconfigPage.tsx`のimport元も追随修正)
- `src/api/resolve.ts`(新規) — `invoker.ts`・`stubconfig.ts`の両方に一字一句同じ実装で重複していた`resolveBeanName`/`resolveMethod`を切り出し(レビュー時の追加指摘)。`invoker.ts`/`stubconfig.ts`が再exportする中継は行わず、`InvokerPage.tsx`/`StubconfigPage.tsx`が`resolve.ts`から直接importする形とした(レビュー時の追加指摘)

## 設計判断

- **コンポーネント対応**: `TextField`(単一行)→`FormField`+`TextInput`、`TextField`(`multiline`)→`FormField`+`Textarea`、`Select`+`MenuItem`→`Select`(`options: {label,value}[]`)、`InputLabel`→`FormField`の`label`プロップ。`Select`/`Textarea`の`onChange`はイベントでなく値を直接返す設計のため、呼出し側ハンドラを`(v) => setState(v)`の形へ書き換えた
- **等幅フォント(FR11.7.1)**: Invoker/Stubconfigのスクリプト入力欄・実行結果欄(計4箇所)へ`invoker-code-textarea`/`stubconfig-code-textarea`クラスでOS標準等幅フォントスタックを適用
- **Stubconfigの「返却値」欄**: 元のMUI版は縦積みの「クリア」「現在値」ボタン+テキストエリアという2カラム構成だったため、`stubconfig-return-row`(flex)で同様の配置を再現
- **レイアウトCSS**: 全画面固有CSSはlayout-css Skill方針に従い、コンポーネントと同一ディレクトリに配置し、`var(--space-*)`トークンを参照する意味づけされたクラス名(例: `.invoker-form-row`)とした
- **data-testid**: 新規追加したインタラクティブ要素(入力・ボタン・Home Card)へ`{component}-{role}`形式の`data-testid`を付与(自動化フレンドリー)

## Build and Testで発見・修正した不具合

Code Generation時点の`npm run lint`・`npm run build`はいずれも成功していたが、Build and Testで実際にブラウザ確認したところ、以下2件の不具合が判明し修正した(詳細はrequirements.md FR11.12)。

- **CSS未適用**(画面は表示されるがスタイルが一切当たらない): make-you-chic-uiの**ビルド成果物**はCSSをJSから分離した別ファイル(`dist/index.css`)として出力する。ソース上の`import './theme/tokens.css'`等はビルド後のJSに残らないため、`main.tsx`へ`import 'make-you-chic-ui/style.css'`を追加。当初のFR11.3の記述(「バレルエクスポート経由で自動import」)はソースコードの読み方に基づく誤った想定だった
- **Reactフックエラーで画面が真っ白**: `vendor/make-you-chic-ui`(submodule)が自身のビルド・テスト用に`node_modules/react`を保持しており、symlink経由でfile:参照するVite側の解決がfrontend自身のReactではなくvendor側のReactを拾ってしまい、Reactが二重ロードされて`useState`が`null`を参照する例外が発生していた。`vite.config.ts`へ`resolve.dedupe: ["react", "react-dom"]`を追加して解決

いずれも`npm run build`(型チェック・バンドル)では検出できず、実際にブラウザでレンダリングして初めて顕在化した不具合である。

## 動作確認

`npm run lint`(oxlint/eslint)・`npm run build`(`tsc -b && vite build`)、`./gradlew clean build`(リポジトリ全体、59テスト)がいずれもエラー無く成功することを確認済み。Claude in Chromeによる実ブラウザ確認で、Home画面(Sidebar/Topbar/Card表示)・Card クリックによる`/invoker`遷移・Invoker画面でのBean/メソッド自動解決・実行(結果`--- 30`)・Stubconfig画面での登録(結果`true`)・Topbarのテーマ切替(ダークモードへの反映)を確認済み。
