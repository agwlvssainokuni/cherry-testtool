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
import { afterEach, describe, expect, it, vi } from 'vitest'
import { invoke } from './invoker'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('invoke', () => {
  it('posts all arguments to /testtool/invoker/invoke and returns the response text', async () => {
    const text = vi.fn().mockResolvedValue('--- 30')
    const fetchMock = vi.fn().mockResolvedValue({ text })
    vi.stubGlobal('fetch', fetchMock)

    const result = await invoke(
      'sampleService',
      'cherry.testtool.demo.SampleService',
      'toBeInvoked1',
      '0',
      '[10, 20]',
      '',
    )

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/testtool/invoker/invoke')
    expect(init.method).toBe('POST')
    const body = new URLSearchParams(init.body)
    expect(body.get('beanName')).toBe('sampleService')
    expect(body.get('className')).toBe('cherry.testtool.demo.SampleService')
    expect(body.get('methodName')).toBe('toBeInvoked1')
    expect(body.get('methodIndex')).toBe('0')
    expect(body.get('script')).toBe('[10, 20]')
    expect(body.get('engine')).toBe('')
    expect(result).toBe('--- 30')
  })
})
