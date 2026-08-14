/*
 * Copyright 2026 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { AppShell, RadioGroup, Select, Switch, useTheme } from 'make-you-chic-ui'
import type { AppShellNavItem, ThemeBrand, ThemeFontFamily, ThemeFontSize } from 'make-you-chic-ui'
import { Outlet, useNavigate } from 'react-router-dom'
import './AppShellLayout.css'

const FONT_SIZE_OPTIONS = [
  { label: '小', value: 'sm' },
  { label: '中', value: 'md' },
  { label: '大', value: 'lg' },
]

const FONT_FAMILY_OPTIONS = [
  { label: 'ゴシック', value: 'sans' },
  { label: '明朝', value: 'serif' },
]

const BRAND_OPTIONS = [
  { label: '青', value: 'blue' },
  { label: '緑', value: 'green' },
  { label: '紫', value: 'purple' },
  { label: '橙', value: 'orange' },
]

const AppShellLayout = () => {
  const navigate = useNavigate()
  const { theme, brand, fontFamily, fontSize, setTheme, setBrand, setFontFamily, setFontSize } =
    useTheme()

  const navItems: AppShellNavItem[] = [
    { label: 'ホーム', href: '/' },
    { label: '呼出しツール', href: '/invoker' },
    { label: 'スタブ設定ツール', href: '/stubconfig' },
  ].map((item) => ({
    ...item,
    onClick: (e: React.MouseEvent) => {
      e.preventDefault()
      navigate(item.href)
    },
  }))

  return (
    <AppShell
      navItems={navItems}
      topbarEnd={
        <div className="theme-controls">
          <div className="theme-controls-item">
            <Switch
              label="ダーク"
              checked={theme === 'dark'}
              onChange={(checked) => setTheme(checked ? 'dark' : 'light')}
              data-testid="theme-mode-switch"
            />
          </div>

          <div className="theme-controls-item">
            <span className="theme-controls-caption">文字サイズ</span>
            <RadioGroup
              name="theme-font-size"
              options={FONT_SIZE_OPTIONS}
              value={fontSize}
              onChange={(v) => setFontSize(v as ThemeFontSize)}
              className="theme-controls-radio-row"
            />
          </div>

          <div className="theme-controls-item">
            <span className="theme-controls-caption">フォント</span>
            <Select
              aria-label="フォント"
              options={FONT_FAMILY_OPTIONS}
              value={fontFamily}
              onChange={(v) => setFontFamily(v as ThemeFontFamily)}
              data-testid="theme-font-family-select"
            />
          </div>

          <div className="theme-controls-item">
            <span className="theme-controls-caption">ブランド</span>
            <Select
              aria-label="ブランド"
              options={BRAND_OPTIONS}
              value={brand}
              onChange={(v) => setBrand(v as ThemeBrand)}
              data-testid="theme-brand-select"
            />
          </div>
        </div>
      }
    >
      <Outlet />
    </AppShell>
  )
}

export default AppShellLayout
