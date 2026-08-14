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
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ThemeProvider } from 'make-you-chic-ui'
import AppShellLayout from './AppShellLayout'

function renderLayout() {
  return render(
    <ThemeProvider>
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<AppShellLayout />}>
            <Route path="/" element={<p>page content</p>} />
          </Route>
        </Routes>
      </MemoryRouter>
    </ThemeProvider>,
  )
}

beforeEach(() => {
  window.localStorage.clear()
})

afterEach(() => {
  window.localStorage.clear()
  document.documentElement.removeAttribute('data-theme')
  document.documentElement.removeAttribute('data-brand')
  document.documentElement.removeAttribute('data-font-family')
  document.documentElement.removeAttribute('data-font-size')
})

describe('AppShellLayout', () => {
  it('renders the sidebar navigation items', () => {
    renderLayout()
    expect(screen.getByRole('link', { name: 'ホーム' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: '呼出しツール' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'スタブ設定ツール' })).toBeInTheDocument()
  })

  it('renders the routed page content via the Outlet', () => {
    renderLayout()
    expect(screen.getByText('page content')).toBeInTheDocument()
  })

  it('defaults the theme controls to light/md/sans/blue', () => {
    renderLayout()
    expect(screen.getByTestId('theme-mode-switch')).not.toBeChecked()
    expect(screen.getByRole('radio', { name: '中' })).toBeChecked()
    expect(screen.getByTestId('theme-font-family-select')).toHaveValue('sans')
    expect(screen.getByTestId('theme-brand-select')).toHaveValue('blue')
  })

  it('toggles data-theme on <html> when the dark switch is clicked', async () => {
    const user = userEvent.setup()
    renderLayout()

    await user.click(screen.getByTestId('theme-mode-switch'))

    expect(document.documentElement.getAttribute('data-theme')).toBe('dark')
  })

  it('updates data-font-size on <html> when a font size radio is selected', async () => {
    const user = userEvent.setup()
    renderLayout()

    await user.click(screen.getByRole('radio', { name: '大' }))

    expect(document.documentElement.getAttribute('data-font-size')).toBe('lg')
  })

  it('updates data-font-family on <html> when the font family select changes', async () => {
    const user = userEvent.setup()
    renderLayout()

    await user.selectOptions(screen.getByTestId('theme-font-family-select'), '明朝')

    expect(document.documentElement.getAttribute('data-font-family')).toBe('serif')
  })

  it('updates data-brand on <html> when the brand select changes', async () => {
    const user = userEvent.setup()
    renderLayout()

    await user.selectOptions(screen.getByTestId('theme-brand-select'), '緑')

    expect(document.documentElement.getAttribute('data-brand')).toBe('green')
  })
})
