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
  AUTH_PASSWORD,
  AUTH_USERNAME,
  AUTH_WEBCONSOLE_PORT,
  AUTH_WEBCONSOLE_URL,
  getApiKey,
  WEBCONSOLE_DIR,
  WEBCONSOLE_JAR,
} from '../support/config'
import { startJavaProcess, stopProcess, waitForHttp } from '../support/processes'

// global-setup(9090)とは独立に、cherry.testtool.web.auth.*(Basic認証)を有効化したwebconsoleを
// このファイル内だけで起動・停止する(別ポート9093を使用)。backendはglobal-setupが起動済みのdemo(8080)を
// そのまま指す(Basic認証はwebconsole自身への認証であり、demoの状態には無関係なため)。
// E2E_API_KEYの有無に依存しない検証のため、no-keyパスでのみ実行する(重複実行を避ける)。
test.describe('webconsole: Basic認証(cherry.testtool.web.auth.*)', () => {
  test.skip(!!getApiKey(), 'no-keyパスでのみ実行(E2E_API_KEYの有無に依存しない検証のため)')

  let webconsole: ChildProcess | undefined

  test.beforeAll(async () => {
    webconsole = startJavaProcess(
      WEBCONSOLE_JAR,
      [
        `--server.port=${AUTH_WEBCONSOLE_PORT}`,
        `--cherry.testtool.web.auth.username=${AUTH_USERNAME}`,
        `--cherry.testtool.web.auth.password=${AUTH_PASSWORD}`,
      ],
      WEBCONSOLE_DIR,
    )
    await waitForHttp(`${AUTH_WEBCONSOLE_URL}/`, 60_000)
  })

  test.afterAll(async () => {
    if (webconsole?.pid) await stopProcess(webconsole.pid)
    webconsole = undefined
  })

  test('認証ヘッダ無しでアクセスすると401になる', async ({ request }) => {
    const response = await request.get(`${AUTH_WEBCONSOLE_URL}/`)
    expect(response.status()).toBe(401)
  })

  test('正しいBasic認証情報でアクセスすると成功する', async ({ request }) => {
    const response = await request.get(`${AUTH_WEBCONSOLE_URL}/`, {
      headers: { Authorization: basicAuthHeader(AUTH_USERNAME, AUTH_PASSWORD) },
    })
    expect(response.ok()).toBeTruthy()
  })

  test('誤ったパスワードでアクセスすると401になる', async ({ request }) => {
    const response = await request.get(`${AUTH_WEBCONSOLE_URL}/`, {
      headers: { Authorization: basicAuthHeader(AUTH_USERNAME, 'wrong-password') },
    })
    expect(response.status()).toBe(401)
  })
})

function basicAuthHeader(username: string, password: string): string {
  return `Basic ${Buffer.from(`${username}:${password}`).toString('base64')}`
}
