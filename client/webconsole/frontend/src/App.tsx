/*
 * Copyright 2023,2026 agwlvssainokuni
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

import { Route, Routes } from 'react-router-dom'
import AppShellLayout from './layouts/AppShellLayout'
import HomePage from './pages/Home/HomePage'
import InvokerPage from './pages/Invoker/InvokerPage'
import StubconfigPage from './pages/Stubconfig/StubconfigPage'

const App = () => {
  return (
    <Routes>
      <Route element={<AppShellLayout />}>
        <Route path="/" Component={HomePage} />
        <Route path="/invoker" Component={InvokerPage} />
        <Route path="/stubconfig" Component={StubconfigPage} />
      </Route>
    </Routes>
  )
}

export default App
