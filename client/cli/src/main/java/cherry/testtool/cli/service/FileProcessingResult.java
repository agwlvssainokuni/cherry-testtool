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

package cherry.testtool.cli.service;

import cherry.testtool.cli.scan.ScriptFileEntry;

/**
 * 1ファイル分の処理結果(Functional Design BR4: 失敗時も処理を継続し、結果を記録する)。
 *
 * @param entry   処理対象ファイル
 * @param success 呼出し/登録/取得が成功したか
 * @param output  成功時はAPI応答本文、失敗時はエラーメッセージ
 */
public record FileProcessingResult(
        ScriptFileEntry entry,
        boolean success,
        String output
) {
}
