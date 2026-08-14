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
import { afterEach, describe, expect, it } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { ThemeProvider } from 'make-you-chic-ui'
import App from './App'

function renderAt(path: string) {
  return render(
    <ThemeProvider>
      <MemoryRouter initialEntries={[path]}>
        <App />
      </MemoryRouter>
    </ThemeProvider>,
  )
}

afterEach(() => {
  window.localStorage.clear()
})

describe('App routing', () => {
  it('renders HomePage at /', () => {
    renderAt('/')
    expect(screen.getByRole('heading', { level: 1, name: 'テストツール' })).toBeInTheDocument()
  })

  it('renders InvokerPage at /invoker', () => {
    renderAt('/invoker')
    expect(screen.getByRole('heading', { level: 1, name: '呼出しツール' })).toBeInTheDocument()
  })

  it('renders StubconfigPage at /stubconfig', () => {
    renderAt('/stubconfig')
    expect(screen.getByRole('heading', { level: 1, name: 'スタブ設定ツール' })).toBeInTheDocument()
  })

  it('renders the shared AppShell sidebar on every route', () => {
    renderAt('/invoker')
    expect(screen.getByRole('link', { name: 'ホーム' })).toBeInTheDocument()
  })
})
