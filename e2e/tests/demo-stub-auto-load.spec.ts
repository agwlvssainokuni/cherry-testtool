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
  AUTO_LOAD_DEMO_PORT,
  AUTO_LOAD_DEMO_URL,
  AUTO_LOAD_WEBCONSOLE_PORT,
  AUTO_LOAD_WEBCONSOLE_URL,
  DEMO_DIR,
  DEMO_JAR,
  getApiKey,
  WEBCONSOLE_DIR,
  WEBCONSOLE_JAR,
} from '../support/config'
import { startJavaProcess, stopProcess, waitForHttp } from '../support/processes'

// global-setup(8080/9090)とは独立に、demo.stub-loader(起動時スタブ自動ロード、既定は無効)を
// 有効化したdemo・webconsoleをこのファイル内だけで起動・停止する(別ポート8082/9092を使用)。
// E2E_API_KEYの有無に依存しない検証のため、no-keyパスでのみ実行する(重複実行を避ける)。
test.describe('demo: 起動時スタブ自動ロード(demo.stub-loader)', () => {
  test.skip(!!getApiKey(), 'no-keyパスでのみ実行(E2E_API_KEYの有無に依存しない検証のため)')

  let demo: ChildProcess | undefined
  let webconsole: ChildProcess | undefined

  test.afterEach(async () => {
    if (webconsole?.pid) await stopProcess(webconsole.pid)
    if (demo?.pid) await stopProcess(demo.pid)
    demo = undefined
    webconsole = undefined
  })

  test('demo.stub-loader.enabled=true時、起動直後からスタブが適用済みである(demo直接+webconsole経由)', async ({
    request,
  }) => {
    // directory/extは既定値(stub-samples/.js)のまま、demo/を作業ディレクトリとして起動する
    // (demo/stub-samples/cherry.testtool.demo.SampleService/toBeStubbed1.1.jsが自動登録される)。
    demo = startJavaProcess(
      DEMO_JAR,
      [`--server.port=${AUTO_LOAD_DEMO_PORT}`, '--demo.stub-loader.enabled=true'],
      DEMO_DIR,
    )
    await waitForHttp(`${AUTO_LOAD_DEMO_URL}/api/sample/stubbed1/int?p1=0&p2=0`, 60_000)

    // stubconfig registerを一度も呼んでいない状態で、既にスタブ値が返ることを確認する(demo直接)。
    const directResponse = await request.get(
      `${AUTO_LOAD_DEMO_URL}/api/sample/stubbed1/int?p1=1030&p2=204`,
    )
    expect(await directResponse.text()).toBe('9999')

    // webconsole経由でも、自動ロード済みのスタブ登録状態を観測できることを確認する。
    webconsole = startJavaProcess(
      WEBCONSOLE_JAR,
      [`--server.port=${AUTO_LOAD_WEBCONSOLE_PORT}`, `--backend.port=${AUTO_LOAD_DEMO_PORT}`],
      WEBCONSOLE_DIR,
    )
    await waitForHttp(`${AUTO_LOAD_WEBCONSOLE_URL}/`, 60_000)

    const listResponse = await request.get(`${AUTO_LOAD_WEBCONSOLE_URL}/testtool/stubconfig/list`, {
      params: { className: 'cherry.testtool.demo.SampleService' },
    })
    expect(listResponse.ok()).toBeTruthy()
    const stubbedMethods = (await listResponse.json()) as string[]
    expect(stubbedMethods.some((m) => m.includes('toBeStubbed1'))).toBeTruthy()
  })
})
