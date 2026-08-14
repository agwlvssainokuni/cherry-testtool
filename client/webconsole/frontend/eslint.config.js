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
 *
 * As of the oxlint migration, ESLint is scoped to eslint-plugin-react-hooks
 * ONLY: oxlint (see .oxlintrc.json) now covers everything else (core JS
 * correctness, @typescript-eslint, eslint-plugin-react, eslint-plugin-jsx-a11y
 * equivalents). Of react-hooks/recommended's rules, oxlint only reimplements
 * a subset (rules-of-hooks, exhaustive-deps) — the rest have no oxlint
 * equivalent yet, so the full recommended set stays here rather than
 * splitting it and risking drift if oxlint's coverage changes (mirrors
 * make-you-chic-ui's own eslint.config.js).
 */
import tsParser from '@typescript-eslint/parser'
import reactHooks from 'eslint-plugin-react-hooks'

export default [
  { ignores: ['dist', 'node_modules', 'vendor'] },
  {
    files: ['**/*.{ts,tsx}'],
    plugins: { 'react-hooks': reactHooks },
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        ecmaFeatures: { jsx: true },
      },
    },
    rules: reactHooks.configs.recommended.rules,
  },
]
