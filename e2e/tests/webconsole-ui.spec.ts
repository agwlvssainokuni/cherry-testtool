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

import { expect, test } from '@playwright/test'
import { DEMO_URL } from '../support/config'

test('Home→Invoker: クラス/メソッド解決から実行結果表示まで', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { level: 1, name: 'テストツール' })).toBeVisible()

  await page.getByTestId('home-card-invoker').click()
  await expect(page.getByRole('heading', { level: 1, name: '呼出しツール' })).toBeVisible()

  await page.getByTestId('invoker-class-name-input').fill('cherry.testtool.demo.SampleService')
  await page.getByTestId('invoker-class-name-input').blur()
  await expect(page.getByTestId('invoker-bean-name-select')).not.toHaveValue('Bean名称(非必須)')

  await page.getByTestId('invoker-method-name-input').fill('toBeInvoked1')
  await page.getByTestId('invoker-method-name-input').blur()
  await page.getByTestId('invoker-method-index-select').selectOption({ label: '(long,long)' })

  await page.getByTestId('invoker-script-textarea').fill('[3, 4]')
  await page.getByTestId('invoker-invoke-button').click()

  await expect(page.getByTestId('invoker-result-textarea')).toContainText('7')
})

test('Home→Stubconfig: 登録・スタブ効果・クリア', async ({ page, request }) => {
  await page.goto('/')
  await page.getByTestId('home-card-stubconfig').click()
  await expect(page.getByRole('heading', { level: 1, name: 'スタブ設定ツール' })).toBeVisible()

  await page.getByTestId('stubconfig-class-name-input').fill('cherry.testtool.demo.SampleService')
  await page.getByTestId('stubconfig-class-name-input').blur()
  await expect(page.getByTestId('stubconfig-bean-name-select')).not.toHaveValue('(参考)')

  await page.getByTestId('stubconfig-method-name-input').fill('toBeStubbed1')
  await page.getByTestId('stubconfig-method-name-input').blur()
  await page.getByTestId('stubconfig-method-index-select').selectOption({ index: 1 })

  await page.getByTestId('stubconfig-script-textarea').fill('9999')
  await page.getByTestId('stubconfig-register-button').click()
  await expect(page.getByTestId('stubconfig-result-textarea')).toContainText('true')

  const stubbedResponse = await request.get(`${DEMO_URL}/api/sample/stubbed1/int?p1=1030&p2=204`)
  expect(await stubbedResponse.text()).toBe('9999')

  await page.getByTestId('stubconfig-clear-button').click()
  await page.getByTestId('stubconfig-register-button').click()
  await expect(page.getByTestId('stubconfig-result-textarea')).toContainText('true')

  const restoredResponse = await request.get(`${DEMO_URL}/api/sample/stubbed1/int?p1=1030&p2=204`)
  expect(await restoredResponse.text()).toBe('1234')
})
