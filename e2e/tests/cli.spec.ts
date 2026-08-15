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

import { APIRequestContext, expect, test } from '@playwright/test'
import { runCli } from '../support/cli'
import { DEMO_URL, getApiKey, INVOKE_SAMPLES_DIR, STUB_SAMPLES_DIR } from '../support/config'

const apiKey = getApiKey()

async function stubbedValue(request: APIRequestContext) {
  const response = await request.get(`${DEMO_URL}/api/sample/stubbed1/int?p1=1030&p2=204`)
  expect(response.ok()).toBeTruthy()
  return await response.text()
}

test.describe('cli経由(demoへ直接)', () => {
  test('invoke: toBeInvoked系メソッドの一括呼出しが成功する', async () => {
    const result = await runCli(['--url', DEMO_URL, 'invoke', INVOKE_SAMPLES_DIR], apiKey)
    expect(result.exitCode).toBe(0)
    expect(result.stdout).toContain('toBeInvoked1')
  })

  test('stubconfig: register→スタブ効果反映→show→clear→復元', async ({ request }) => {
    const before = await stubbedValue(request)
    expect(before).toBe('1234')

    const registerResult = await runCli(
      ['--url', DEMO_URL, 'stubconfig', 'register', STUB_SAMPLES_DIR],
      apiKey,
    )
    expect(registerResult.exitCode).toBe(0)

    const stubbed = await stubbedValue(request)
    expect(stubbed).toBe('9999')

    const showResult = await runCli(
      ['--url', DEMO_URL, 'stubconfig', 'show', STUB_SAMPLES_DIR],
      apiKey,
    )
    expect(showResult.exitCode).toBe(0)
    expect(showResult.stdout).toContain('9999')

    const clearResult = await runCli(
      ['--url', DEMO_URL, 'stubconfig', 'clear', STUB_SAMPLES_DIR],
      apiKey,
    )
    expect(clearResult.exitCode).toBe(0)

    const after = await stubbedValue(request)
    expect(after).toBe('1234')
  })
})
