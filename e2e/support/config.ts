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

import * as path from 'path'

export const REPO_ROOT = path.resolve(__dirname, '..', '..')

export const DEMO_DIR = path.join(REPO_ROOT, 'demo')
export const WEBCONSOLE_DIR = path.join(REPO_ROOT, 'client', 'webconsole')
export const CLI_DIR = path.join(REPO_ROOT, 'client', 'cli')

export const DEMO_JAR = path.join(DEMO_DIR, 'build', 'libs', 'cherry-testtool-demo.jar')
export const WEBCONSOLE_JAR = path.join(
  WEBCONSOLE_DIR,
  'build',
  'libs',
  'cherry-testtool-webconsole.jar',
)
export const CLI_JAR = path.join(CLI_DIR, 'build', 'libs', 'cherry-testtool-cli.jar')

export const DEMO_URL = 'http://localhost:8080'
export const WEBCONSOLE_URL = 'http://localhost:9090'

// demo/webconsoleのAPIキー設定を意図的に食い違わせて検証する専用ポート(global-setupの8080/9090とは別)
export const MISMATCH_DEMO_PORT = 8081
export const MISMATCH_WEBCONSOLE_PORT = 9091
export const MISMATCH_DEMO_URL = `http://localhost:${MISMATCH_DEMO_PORT}`
export const MISMATCH_WEBCONSOLE_URL = `http://localhost:${MISMATCH_WEBCONSOLE_PORT}`
export const MISMATCH_API_KEY = 'e2e-mismatch-api-key'

// demo.stub-loader(起動時スタブ自動ロード)を検証する専用ポート(global-setupの8080/9090とは別)
export const AUTO_LOAD_DEMO_PORT = 8082
export const AUTO_LOAD_WEBCONSOLE_PORT = 9092
export const AUTO_LOAD_DEMO_URL = `http://localhost:${AUTO_LOAD_DEMO_PORT}`
export const AUTO_LOAD_WEBCONSOLE_URL = `http://localhost:${AUTO_LOAD_WEBCONSOLE_PORT}`

// webconsoleのBasic認証(cherry.testtool.web.auth.*)を検証する専用ポート(global-setupの9090とは別)。
// backendはglobal-setupが起動済みのdemo(8080)をそのまま指す(Basic認証はwebconsole自身への認証のため、demoの状態は無関係)。
export const AUTH_WEBCONSOLE_PORT = 9093
export const AUTH_WEBCONSOLE_URL = `http://localhost:${AUTH_WEBCONSOLE_PORT}`
export const AUTH_USERNAME = 'e2e-testuser'
export const AUTH_PASSWORD = 'e2e-testpass'

export const INVOKE_SAMPLES_DIR = path.join(
  DEMO_DIR,
  'invoke-samples',
  'cherry.testtool.demo.SampleService',
)
export const STUB_SAMPLES_DIR = path.join(
  DEMO_DIR,
  'stub-samples',
  'cherry.testtool.demo.SampleService',
)

export const PIDS_FILE = path.join(__dirname, '..', '.e2e-pids.json')

export const API_KEY_HEADER = 'X-Cherry-Testtool-Api-Key'

export function getApiKey(): string | undefined {
  const value = process.env.E2E_API_KEY
  return value && value.length > 0 ? value : undefined
}
