---
name: layout-css
description: Use when writing layout CSS (flex/grid positioning of multiple elements) for a screen or component in an app that consumes the make-you-chic-ui design system. Covers how to name and scope layout classes instead of reaching for a generic layout component or a Tailwind-style utility-class pile.
---

# 画面レイアウトCSSの書き方

make-you-chic-ui(デザインシステム)を利用するアプリで、画面・コンポーネントのレイアウト(要素の並び、余白)を実装する際の方針。

## 前提となる判断

以下は検討済みで、却下されている。理由も含めて再提案しない。

- **汎用レイアウトコンポーネント(`<Stack>`/`<Grid>`等)をデザインシステム側に追加する**: レイアウトパターンは画面ごとに形が大きく異なり、汎用化しようとすると過剰なprops/バリエーションを抱えることになる
- **Tailwindのような原子的ユーティリティクラス(`flex`, `items-center`, `gap-4`...)を大量に並べる**: スタイル変更のたびにマークアップ側を触ることになり、「スタイルの実体はCSS」という単一の真実の情報源が失われる

## 方針

**その画面・コンポーネント専用の、少数の意味づけされたCSSクラスを、都度その場で定義する。** 事前に汎用部品として用意しない。

1. **配置**: スタイル対象のコンポーネントファイルと同じディレクトリに`.css`を置く(例: `UserDetailPage.tsx`と同じ場所に`UserDetailPage.css`)。make-you-chic-ui本体のコンポーネントごとのCSS配置と同じ考え方
2. **命名**: クラス名はCSSプロパティではなく画面上の役割で付ける。`.flex-row-gap-4`ではなく`.filter-bar`や`.user-detail-header`
3. **値**: 余白・角丸はmake-you-chic-uiが公開するプリミティブトークンを参照する。決め打ちのpx値をハードコードしない
   - スペーシング: `var(--space-1)`(4px) 〜 `var(--space-6)`(24px)
   - 角丸: `var(--radius-sm)`(4px) / `var(--radius-md)`(6px) / `var(--radius-lg)`(10px)
4. **構造**: `display: flex` / `display: grid`とその関連プロパティ(`gap`, `align-items`, `grid-template-columns`等)を、そのクラスの中に直接書く。TailwindのようなユーティリティクラスをHTML側で組み合わせない

```css
/* UserDetailPage.css */
.user-detail-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
}
```

```tsx
<div className="user-detail-header">...</div>
```

## 共通化するかどうかの判断

同じ形のレイアウトが、無関係な画面で3箇所以上、偶然ではなく本質的に同じ理由で繰り返された場合のみ、共通化を検討する。その場合も、まずは共有CSSクラスへの格上げを検討し、Reactコンポーネント化(state管理・a11y対応が要る本格的な部品)は最後の手段とする。
