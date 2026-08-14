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
import { getStub, getStubbedMethod, putStub } from './stubconfig'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('putStub', () => {
  it('posts all arguments to /testtool/stubconfig/put and returns the response text', async () => {
    const text = vi.fn().mockResolvedValue('true')
    const fetchMock = vi.fn().mockResolvedValue({ text })
    vi.stubGlobal('fetch', fetchMock)

    const result = await putStub(
      'cherry.testtool.demo.SampleService',
      'toBeInvoked1',
      '0',
      '999',
      '',
    )

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/testtool/stubconfig/put')
    const body = new URLSearchParams(init.body)
    expect(body.get('className')).toBe('cherry.testtool.demo.SampleService')
    expect(body.get('methodName')).toBe('toBeInvoked1')
    expect(body.get('methodIndex')).toBe('0')
    expect(body.get('script')).toBe('999')
    expect(body.get('engine')).toBe('')
    expect(result).toBe('true')
  })
})

describe('getStub', () => {
  it('posts className/methodName/methodIndex to /testtool/stubconfig/get and returns the JSON body', async () => {
    const json = vi.fn().mockResolvedValue(['999', '', '--- 999\n'])
    const fetchMock = vi.fn().mockResolvedValue({ json })
    vi.stubGlobal('fetch', fetchMock)

    const result = await getStub('cherry.testtool.demo.SampleService', 'toBeInvoked1', '0')

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/testtool/stubconfig/get')
    const body = new URLSearchParams(init.body)
    expect(body.get('className')).toBe('cherry.testtool.demo.SampleService')
    expect(body.get('methodName')).toBe('toBeInvoked1')
    expect(body.get('methodIndex')).toBe('0')
    expect(result).toEqual(['999', '', '--- 999\n'])
  })
})

describe('getStubbedMethod', () => {
  it('posts className to /testtool/stubconfig/list and returns the JSON body', async () => {
    const json = vi
      .fn()
      .mockResolvedValue(['cherry.testtool.demo.SampleService.toBeInvoked1(long,long)'])
    const fetchMock = vi.fn().mockResolvedValue({ json })
    vi.stubGlobal('fetch', fetchMock)

    const result = await getStubbedMethod('cherry.testtool.demo.SampleService')

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/testtool/stubconfig/list')
    expect(new URLSearchParams(init.body).get('className')).toBe(
      'cherry.testtool.demo.SampleService',
    )
    expect(result).toEqual(['cherry.testtool.demo.SampleService.toBeInvoked1(long,long)'])
  })
})
