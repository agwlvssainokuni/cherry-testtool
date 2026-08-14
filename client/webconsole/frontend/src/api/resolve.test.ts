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
import { resolveBeanName, resolveMethod } from './resolve'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('resolveBeanName', () => {
  it('posts className to /testtool/resolve/bean and returns the JSON body', async () => {
    const json = vi.fn().mockResolvedValue(['sampleService'])
    const fetchMock = vi.fn().mockResolvedValue({ json })
    vi.stubGlobal('fetch', fetchMock)

    const result = await resolveBeanName('cherry.testtool.demo.SampleService')

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/testtool/resolve/bean')
    expect(init.method).toBe('POST')
    expect(new URLSearchParams(init.body).get('className')).toBe(
      'cherry.testtool.demo.SampleService',
    )
    expect(result).toEqual(['sampleService'])
  })
})

describe('resolveMethod', () => {
  it('posts className and methodName to /testtool/resolve/method and returns the JSON body', async () => {
    const json = vi.fn().mockResolvedValue(['(long,long)'])
    const fetchMock = vi.fn().mockResolvedValue({ json })
    vi.stubGlobal('fetch', fetchMock)

    const result = await resolveMethod('cherry.testtool.demo.SampleService', 'toBeInvoked1')

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/testtool/resolve/method')
    const body = new URLSearchParams(init.body)
    expect(body.get('className')).toBe('cherry.testtool.demo.SampleService')
    expect(body.get('methodName')).toBe('toBeInvoked1')
    expect(result).toEqual(['(long,long)'])
  })
})
