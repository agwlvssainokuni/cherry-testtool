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
import { configDefaults, defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  resolve: {
    // FR11.12参照: symlink経由のmake-you-chic-uiがvendor側自身のreactを
    // 誤って解決しないよう、テスト実行時もdedupeで一本化する。
    dedupe: ['react', 'react-dom'],
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./vitest.setup.ts'],
    css: false,
    // vendor/はmake-you-chic-ui自身の別リポジトリ(submodule)であり、
    // 既定のincludeパターンだとその配下のテスト(自身のreactを使うため
    // dedupeでも解決しないフックエラーになる)まで拾ってしまうため除外する。
    exclude: [...configDefaults.exclude, 'vendor/**'],
  },
})
