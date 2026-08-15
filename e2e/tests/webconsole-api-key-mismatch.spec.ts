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

import { ChildProcess } from 'child_process'
import { expect, test } from '@playwright/test'
import {
  DEMO_DIR,
  DEMO_JAR,
  getApiKey,
  MISMATCH_API_KEY,
  MISMATCH_DEMO_PORT,
  MISMATCH_DEMO_URL,
  MISMATCH_WEBCONSOLE_PORT,
  MISMATCH_WEBCONSOLE_URL,
  WEBCONSOLE_DIR,
  WEBCONSOLE_JAR,
} from '../support/config'
import { startJavaProcess, stopProcess, waitForHttp } from '../support/processes'

// global-setup(8080/9090)とは独立に、demo/webconsoleのAPIキー設定を意図的に
// 食い違わせた組合せだけをこのファイル内で起動・停止する(別ポート8081/9091を使用)。
// E2E_API_KEYの有無に依存しない検証のため、no-keyパスでのみ実行する(重複実行を避ける)。
test.describe('webconsole: demo/webconsoleのAPIキー設定不一致', () => {
  test.skip(!!getApiKey(), 'no-keyパスでのみ実行(E2E_API_KEYの有無に依存しない検証のため)')

  let demo: ChildProcess | undefined
  let webconsole: ChildProcess | undefined

  test.afterEach(async () => {
    if (webconsole?.pid) await stopProcess(webconsole.pid)
    if (demo?.pid) await stopProcess(demo.pid)
    demo = undefined
    webconsole = undefined
  })

  test('demoのみキー設定: webconsole経由のリクエストは401が伝播する', async ({ request }) => {
    demo = startJavaProcess(
      DEMO_JAR,
      [`--server.port=${MISMATCH_DEMO_PORT}`, `--cherry.testtool.web.api-key=${MISMATCH_API_KEY}`],
      DEMO_DIR,
    )
    await waitForHttp(`${MISMATCH_DEMO_URL}/api/sample/stubbed1/int?p1=0&p2=0`, 60_000)

    webconsole = startJavaProcess(
      WEBCONSOLE_JAR,
      [`--server.port=${MISMATCH_WEBCONSOLE_PORT}`, `--backend.port=${MISMATCH_DEMO_PORT}`],
      WEBCONSOLE_DIR,
    )
    await waitForHttp(`${MISMATCH_WEBCONSOLE_URL}/`, 60_000)

    const response = await request.get(`${MISMATCH_WEBCONSOLE_URL}/testtool/resolve/bean`, {
      params: { className: 'cherry.testtool.demo.SampleService' },
    })
    expect(response.status()).toBe(401)
  })

  test('webconsoleのみキー設定: demoはキー未要求のため成功する', async ({ request }) => {
    demo = startJavaProcess(DEMO_JAR, [`--server.port=${MISMATCH_DEMO_PORT}`], DEMO_DIR)
    await waitForHttp(`${MISMATCH_DEMO_URL}/api/sample/stubbed1/int?p1=0&p2=0`, 60_000)

    webconsole = startJavaProcess(
      WEBCONSOLE_JAR,
      [
        `--server.port=${MISMATCH_WEBCONSOLE_PORT}`,
        `--backend.port=${MISMATCH_DEMO_PORT}`,
        `--cherry.testtool.web.api-key=${MISMATCH_API_KEY}`,
      ],
      WEBCONSOLE_DIR,
    )
    await waitForHttp(`${MISMATCH_WEBCONSOLE_URL}/`, 60_000)

    const response = await request.get(`${MISMATCH_WEBCONSOLE_URL}/testtool/resolve/bean`, {
      params: { className: 'cherry.testtool.demo.SampleService' },
    })
    expect(response.ok()).toBeTruthy()
    expect(await response.json()).toEqual(['sampleService'])
  })
})
