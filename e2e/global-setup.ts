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

import * as fs from 'fs'
import {
  DEMO_DIR,
  DEMO_JAR,
  DEMO_URL,
  getApiKey,
  PIDS_FILE,
  WEBCONSOLE_DIR,
  WEBCONSOLE_JAR,
  WEBCONSOLE_URL,
} from './support/config'
import { startJavaProcess, waitForHttp } from './support/processes'

export default async function globalSetup(): Promise<void> {
  const apiKey = getApiKey()
  const apiKeyArgs = apiKey ? [`--cherry.testtool.web.api-key=${apiKey}`] : []

  const demo = startJavaProcess(DEMO_JAR, apiKeyArgs, DEMO_DIR)
  await waitForHttp(`${DEMO_URL}/api/sample/stubbed1/int?p1=0&p2=0`, 60_000)

  const webconsole = startJavaProcess(WEBCONSOLE_JAR, apiKeyArgs, WEBCONSOLE_DIR)
  await waitForHttp(`${WEBCONSOLE_URL}/`, 60_000)

  fs.writeFileSync(
    PIDS_FILE,
    JSON.stringify({ demoPid: demo.pid, webconsolePid: webconsole.pid }),
    'utf-8',
  )
}
