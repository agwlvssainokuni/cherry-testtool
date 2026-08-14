/*
 * Copyright 2021,2026 agwlvssainokuni
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

export { uri }

// webconsoleが自身の/testtool/**でAPIを提供するため、絶対URL(VITE_TESTTOOL_ROOT)ではなく
// 常に相対パスで解決する(開発時はvite.config.tsのserver.proxyが/testtool/**を委譲する)。
const uri: (p: string) => string = (path: string) => '/testtool' + path
