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
import InvokerPage from './InvokerPage'
import { invoke } from '../../api/invoker'
import { resolveBeanName, resolveMethod } from '../../api/resolve'

vi.mock('../../api/invoker', () => ({ invoke: vi.fn() }))
vi.mock('../../api/resolve', () => ({
  resolveBeanName: vi.fn(),
  resolveMethod: vi.fn(),
}))

const mockedInvoke = vi.mocked(invoke)
const mockedResolveBeanName = vi.mocked(resolveBeanName)
const mockedResolveMethod = vi.mocked(resolveMethod)

beforeEach(() => {
  mockedInvoke.mockReset()
  mockedResolveBeanName.mockReset().mockResolvedValue([])
  mockedResolveMethod.mockReset().mockResolvedValue([])
})

describe('InvokerPage', () => {
  it('renders the page title and initial placeholder values', () => {
    render(<InvokerPage />)
    expect(screen.getByRole('heading', { level: 1, name: '呼出しツール' })).toBeInTheDocument()
    expect(screen.getByTestId('invoker-bean-name-select')).toHaveValue('Bean名称(非必須)')
    expect(screen.getByTestId('invoker-method-index-select')).toHaveValue('0')
  })

  it('resolves the bean name list when the class name field loses focus', async () => {
    mockedResolveBeanName.mockResolvedValue(['sampleService'])
    const user = userEvent.setup()
    render(<InvokerPage />)

    await user.type(
      screen.getByTestId('invoker-class-name-input'),
      'cherry.testtool.demo.SampleService',
    )
    await user.tab()

    expect(mockedResolveBeanName).toHaveBeenCalledWith('cherry.testtool.demo.SampleService')
    expect(await screen.findByTestId('invoker-bean-name-select')).toHaveValue('sampleService')
  })

  it('resolves the method candidate list when the method name field loses focus', async () => {
    mockedResolveMethod.mockResolvedValue(['(long,long)'])
    const user = userEvent.setup()
    render(<InvokerPage />)

    await user.type(screen.getByTestId('invoker-method-name-input'), 'toBeInvoked1')
    await user.tab()

    expect(mockedResolveMethod).toHaveBeenCalledWith('', 'toBeInvoked1')
    const select = await screen.findByTestId('invoker-method-index-select')
    expect(select).toHaveValue('0')
    expect(screen.getByRole('option', { name: '(long,long)' })).toBeInTheDocument()
  })

  it('invokes with the current form values and shows the result', async () => {
    mockedInvoke.mockResolvedValue('--- 30')
    mockedResolveBeanName.mockResolvedValue(['sampleService'])
    mockedResolveMethod.mockResolvedValue(['(long,long)'])
    const user = userEvent.setup()
    render(<InvokerPage />)

    await user.type(
      screen.getByTestId('invoker-class-name-input'),
      'cherry.testtool.demo.SampleService',
    )
    await user.type(screen.getByTestId('invoker-method-name-input'), 'toBeInvoked1')
    // resolveBeanName/resolveMethod resolve asynchronously on blur; wait for
    // their selects to reflect the resolved values before continuing.
    expect(await screen.findByTestId('invoker-bean-name-select')).toHaveValue('sampleService')
    expect(await screen.findByTestId('invoker-method-index-select')).toHaveValue('0')
    fireEvent.change(screen.getByTestId('invoker-script-textarea'), {
      target: { value: '[10, 20]' },
    })
    await user.click(screen.getByTestId('invoker-invoke-button'))

    expect(mockedInvoke).toHaveBeenCalledWith(
      'sampleService',
      'cherry.testtool.demo.SampleService',
      'toBeInvoked1',
      '0',
      '[10, 20]',
      '',
    )
    expect(await screen.findByTestId('invoker-result-textarea')).toHaveValue('--- 30')
  })

  it('shows the rejection value in the result field when invoke fails', async () => {
    mockedInvoke.mockRejectedValue('error: something went wrong')
    const user = userEvent.setup()
    render(<InvokerPage />)

    await user.click(screen.getByTestId('invoker-invoke-button'))

    expect(await screen.findByTestId('invoker-result-textarea')).toHaveValue(
      'error: something went wrong',
    )
  })
})
