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
import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import StubconfigPage from './StubconfigPage'
import { getStub, getStubbedMethod, putStub } from '../../api/stubconfig'
import { resolveBeanName, resolveMethod } from '../../api/resolve'

vi.mock('../../api/stubconfig', () => ({
  getStub: vi.fn(),
  getStubbedMethod: vi.fn(),
  putStub: vi.fn(),
}))
vi.mock('../../api/resolve', () => ({
  resolveBeanName: vi.fn(),
  resolveMethod: vi.fn(),
}))

const mockedGetStub = vi.mocked(getStub)
const mockedGetStubbedMethod = vi.mocked(getStubbedMethod)
const mockedPutStub = vi.mocked(putStub)
const mockedResolveBeanName = vi.mocked(resolveBeanName)
const mockedResolveMethod = vi.mocked(resolveMethod)

beforeEach(() => {
  mockedGetStub.mockReset()
  mockedGetStubbedMethod.mockReset()
  mockedPutStub.mockReset()
  mockedResolveBeanName.mockReset().mockResolvedValue([])
  mockedResolveMethod.mockReset().mockResolvedValue([])
})

describe('StubconfigPage', () => {
  it('renders the page title and initial placeholder values', () => {
    render(<StubconfigPage />)
    expect(screen.getByRole('heading', { level: 1, name: 'スタブ設定ツール' })).toBeInTheDocument()
    expect(screen.getByTestId('stubconfig-bean-name-select')).toHaveValue('(参考)')
  })

  it('registers the current form values and shows the result', async () => {
    mockedPutStub.mockResolvedValue('true')
    const user = userEvent.setup()
    render(<StubconfigPage />)

    await user.type(
      screen.getByTestId('stubconfig-class-name-input'),
      'cherry.testtool.demo.SampleService',
    )
    await user.type(screen.getByTestId('stubconfig-method-name-input'), 'toBeInvoked1')
    fireEvent.change(screen.getByTestId('stubconfig-script-textarea'), {
      target: { value: '999' },
    })
    await user.click(screen.getByTestId('stubconfig-register-button'))

    expect(mockedPutStub).toHaveBeenCalledWith(
      'cherry.testtool.demo.SampleService',
      'toBeInvoked1',
      '0',
      '999',
      '',
    )
    expect(await screen.findByTestId('stubconfig-result-textarea')).toHaveValue('true')
  })

  it('peeks the current stub and fills the script/engine/result fields', async () => {
    mockedGetStub.mockResolvedValue(['999', 'javascript', '--- 999\n'])
    const user = userEvent.setup()
    render(<StubconfigPage />)

    await user.click(screen.getByTestId('stubconfig-peek-button'))

    expect(mockedGetStub).toHaveBeenCalledWith('', '', '0')
    expect(await screen.findByTestId('stubconfig-script-textarea')).toHaveValue('999')
    expect(screen.getByTestId('stubconfig-result-textarea')).toHaveValue('--- 999\n')
  })

  it('clears the script field without calling the API', async () => {
    const user = userEvent.setup()
    render(<StubconfigPage />)

    fireEvent.change(screen.getByTestId('stubconfig-script-textarea'), {
      target: { value: '999' },
    })
    await user.click(screen.getByTestId('stubconfig-clear-button'))

    expect(screen.getByTestId('stubconfig-script-textarea')).toHaveValue('')
    expect(mockedPutStub).not.toHaveBeenCalled()
    expect(mockedGetStub).not.toHaveBeenCalled()
  })

  it('lists the stubbed methods joined by newlines', async () => {
    mockedGetStubbedMethod.mockResolvedValue([
      'cherry.testtool.demo.SampleService.toBeInvoked1(long,long)',
      'cherry.testtool.demo.SampleService.toBeInvoked2(Long,Long)',
    ])
    const user = userEvent.setup()
    render(<StubconfigPage />)

    await user.click(screen.getByTestId('stubconfig-list-button'))

    expect(await screen.findByTestId('stubconfig-result-textarea')).toHaveValue(
      'cherry.testtool.demo.SampleService.toBeInvoked1(long,long)\ncherry.testtool.demo.SampleService.toBeInvoked2(Long,Long)',
    )
  })
})
