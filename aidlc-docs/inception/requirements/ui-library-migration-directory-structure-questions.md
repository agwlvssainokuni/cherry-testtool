# FR11 追加確認: `client/webconsole/frontend/src`配下のディレクトリ構成見直し

「frontend/src配下を典型的なReactのディレクトリ構成に合わせる。必要に応じてファイルの分割・統合も許容する」との依頼を受けての確認です。現状の構成は以下の通りです。

```
src/
  App.tsx          (ルーティング定義)
  Home.tsx
  common.ts        (相対URL解決の共有ユーティリティ)
  main.tsx
  vite-env.d.ts
  assets/          (favicon.ico・logo192.png は index.html から参照。logo.svg/logo.xcf/logo512.png/manifest.json は現状どこからも参照されていない)
  invoker/
    App.tsx
    api.ts
  stubconfig/
    App.tsx
    api.ts
```

「典型的なReact構成」にはいくつか流儀があるため、どちらの方向性で揃えるか確認させてください。

## Question 1
ディレクトリ構成全体の方針について。

A) **画面(ページ)単位のフォルダにまとめる(コロケーション方式)**。現状のinvoker/stubconfigの構成(画面フォルダにコンポーネント・APIをまとめる)をHomeにも適用し、画面ごとに閉じた構成にする。

```
src/
  main.tsx
  App.tsx                 (ルーティング定義のみ)
  vite-env.d.ts
  assets/
  lib/
    common.ts              (旧common.ts)
  layouts/
    AppShellLayout.tsx      (FR11.5のレイアウトルート)
  pages/
    Home/
      HomePage.tsx
      HomePage.css
    Invoker/
      InvokerPage.tsx
      InvokerPage.css
      api.ts
    Stubconfig/
      StubconfigPage.tsx
      StubconfigPage.css
      api.ts
```

B) **種別(役割)単位でトップレベルを分ける(レイヤー方式)**。ページ・API・共通処理をそれぞれ別ディレクトリに集約する、CRA以来の伝統的な構成。

```
src/
  main.tsx
  App.tsx
  vite-env.d.ts
  assets/
  lib/
    common.ts
  layouts/
    AppShellLayout.tsx
  pages/
    HomePage.tsx
    HomePage.css
    InvokerPage.tsx
    InvokerPage.css
    StubconfigPage.tsx
    StubconfigPage.css
  api/
    invoker.ts
    stubconfig.ts
```

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
`src/assets/`配下の静的ファイルの扱いについて。現状`favicon.ico`・`logo192.png`のみがindex.htmlから参照されており、`logo.svg`・`logo.xcf`・`logo512.png`・`manifest.json`はどこからも参照されていません(`manifest.json`はindex.htmlに`<link rel="manifest">`が無く未使用)。

A) Viteの静的アセット規約に合わせて`public/`へ移動する(ビルド時に無加工でコピーされ、`/favicon.ico`等の絶対パスで配信される)。未参照ファイルも含めて全てそのまま移動する。

B) `public/`へ移動するが、未参照ファイル(`logo.svg`・`logo.xcf`・`logo512.png`・`manifest.json`)はこの機会に削除する(実際に使われているのは`favicon.ico`・`logo192.png`のみのため)。

C) `src/assets/`のまま変更しない(ディレクトリ構成の見直しはコンポーネント・画面部分のみとし、静的アセットは対象外とする)。

D) Other (please describe after [Answer]: tag below)

[Answer]: A + index.html に `<link rel="manifest" href="/manifest.json"/>` を追加し、manifest.jsonを実際に使用状態にする