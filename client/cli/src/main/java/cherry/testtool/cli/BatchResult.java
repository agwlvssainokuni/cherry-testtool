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

package cherry.testtool.cli;

import java.util.List;

/**
 * 複数ディレクトリ・複数ファイルにわたる処理全体の集計結果。
 * {@code failureCount()}が{@link CliApplication}の終了コード算出(BR3)の基礎となる。
 *
 * @param results 全ファイルの処理結果
 */
public record BatchResult(
        List<FileProcessingResult> results
) {

    /**
     * @return {@code results}中、失敗({@code success=false}) の件数
     */
    public int failureCount() {
        return (int) results.stream().filter(r -> !r.success()).count();
    }

}
