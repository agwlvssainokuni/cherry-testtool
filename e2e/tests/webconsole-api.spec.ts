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
import { API_KEY_HEADER, DEMO_URL, getApiKey, WEBCONSOLE_URL } from '../support/config'

const CLASS_NAME = 'cherry.testtool.demo.SampleService'

test.describe('webconsole APIプロキシ層(ブラウザを介さないHTTPレベル検証)', () => {
  test('resolve/bean: webconsole経由とdemo直接で同一結果', async ({ request }) => {
    const apiKey = getApiKey()
    const demoHeaders = apiKey ? { [API_KEY_HEADER]: apiKey } : undefined

    const viaWebconsole = await request.get(`${WEBCONSOLE_URL}/testtool/resolve/bean`, {
      params: { className: CLASS_NAME },
    })
    const viaDemo = await request.get(`${DEMO_URL}/testtool/resolve/bean`, {
      params: { className: CLASS_NAME },
      headers: demoHeaders,
    })
    expect(viaWebconsole.ok()).toBeTruthy()
    expect(await viaWebconsole.json()).toEqual(await viaDemo.json())
  })

  test('invoker/invoke: webconsole経由でdemoのメソッドを呼び出せる', async ({ request }) => {
    const response = await request.get(`${WEBCONSOLE_URL}/testtool/invoker/invoke`, {
      params: {
        className: CLASS_NAME,
        methodName: 'toBeInvoked1',
        methodIndex: '0',
        script: '[3, 4]',
      },
    })
    expect(response.ok()).toBeTruthy()
    expect(await response.text()).toContain('7')
  })

  test('セキュリティヘッダが付与される', async ({ request }) => {
    const response = await request.get(`${WEBCONSOLE_URL}/testtool/resolve/bean`, {
      params: { className: CLASS_NAME },
    })
    expect(response.headers()['x-frame-options']).toBe('DENY')
    expect(response.headers()['x-content-type-options']).toBe('nosniff')
  })
})
