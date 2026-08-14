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
import { describe, expect, it } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import HomePage from './HomePage'

function renderHomePage() {
  return render(
    <MemoryRouter>
      <HomePage />
    </MemoryRouter>,
  )
}

describe('HomePage', () => {
  it('renders the page title', () => {
    renderHomePage()
    expect(screen.getByRole('heading', { level: 1, name: 'テストツール' })).toBeInTheDocument()
  })

  it('renders a card linking to the invoker page', () => {
    renderHomePage()
    expect(screen.getByText('呼出しツール')).toBeInTheDocument()
    expect(
      screen.getByText('指定したBeanのメソッドを、任意の引数を指定して直接呼び出します。'),
    ).toBeInTheDocument()
    expect(screen.getByTestId('home-card-invoker')).toHaveAttribute('href', '/invoker')
  })

  it('renders a card linking to the stubconfig page', () => {
    renderHomePage()
    expect(screen.getByText('スタブ設定ツール')).toBeInTheDocument()
    expect(
      screen.getByText('メソッドの戻り値をスクリプトで差し替えるスタブを設定します。'),
    ).toBeInTheDocument()
    expect(screen.getByTestId('home-card-stubconfig')).toHaveAttribute('href', '/stubconfig')
  })
})
