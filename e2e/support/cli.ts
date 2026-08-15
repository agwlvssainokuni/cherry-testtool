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

import { execFile } from 'child_process'
import { promisify } from 'util'
import { CLI_JAR } from './config'

const execFileAsync = promisify(execFile)

export interface CliResult {
  stdout: string
  stderr: string
  exitCode: number
}

export async function runCli(args: string[], apiKey?: string): Promise<CliResult> {
  const env = apiKey ? { ...process.env, CHERRY_TESTTOOL_WEB_APIKEY: apiKey } : process.env
  try {
    const { stdout, stderr } = await execFileAsync('java', ['-jar', CLI_JAR, ...args], { env })
    return { stdout, stderr, exitCode: 0 }
  } catch (error) {
    const execError = error as { stdout?: string; stderr?: string; code?: number }
    return {
      stdout: execError.stdout ?? '',
      stderr: execError.stderr ?? '',
      exitCode: execError.code ?? 1,
    }
  }
}
