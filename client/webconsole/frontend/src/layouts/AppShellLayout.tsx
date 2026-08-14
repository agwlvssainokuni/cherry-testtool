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

import {AppShell, Button, Dropdown, useTheme} from "make-you-chic-ui"
import type {AppShellNavItem, MenuItem} from "make-you-chic-ui"
import {Outlet, useNavigate} from "react-router-dom"

const BRAND_ORDER = ["blue", "green", "purple", "orange"] as const
const FONT_SIZE_ORDER = ["sm", "md", "lg"] as const

const AppShellLayout = () => {

    const navigate = useNavigate()
    const {theme, brand, fontFamily, fontSize, setTheme, setBrand, setFontFamily, setFontSize} = useTheme()

    const navItems: AppShellNavItem[] = [
        {label: "ホーム", href: "/"},
        {label: "呼出しツール", href: "/invoker"},
        {label: "スタブ設定ツール", href: "/stubconfig"},
    ].map((item) => ({
        ...item,
        onClick: (e: React.MouseEvent) => {
            e.preventDefault()
            navigate(item.href)
        },
    }))

    const nextBrand = BRAND_ORDER[(BRAND_ORDER.indexOf(brand) + 1) % BRAND_ORDER.length]
    const nextFontSize = FONT_SIZE_ORDER[(FONT_SIZE_ORDER.indexOf(fontSize) + 1) % FONT_SIZE_ORDER.length]

    const themeMenuItems: MenuItem[] = [
        {
            label: theme === "light" ? "モード: ダークへ切替" : "モード: ライトへ切替",
            onClick: () => setTheme(theme === "light" ? "dark" : "light"),
        },
        {
            label: `ブランド: ${nextBrand}へ切替`,
            onClick: () => setBrand(nextBrand),
        },
        {
            label: fontFamily === "sans" ? "フォント: 明朝へ切替" : "フォント: ゴシックへ切替",
            onClick: () => setFontFamily(fontFamily === "sans" ? "serif" : "sans"),
        },
        {
            label: `文字サイズ: ${nextFontSize}へ切替`,
            onClick: () => setFontSize(nextFontSize),
        },
    ]

    return (
        <AppShell
            navItems={navItems}
            topbarEnd={
                <Dropdown
                    trigger={<Button variant="ghost" size="sm">テーマ</Button>}
                    items={themeMenuItems}
                    placement="bottom-end"
                />
            }>
            <Outlet/>
        </AppShell>
    )
}

export default AppShellLayout
