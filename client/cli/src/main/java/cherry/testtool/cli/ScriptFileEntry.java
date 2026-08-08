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

import java.nio.file.Path;

/**
 * スタブ設定/呼出し用ディレクトリ配下の1スクリプトファイルを表す(Functional Design domain-entities.md準拠)。
 * <p>
 * {@code className}はファイルの直接の親ディレクトリ名、{@code methodName}/{@code methodIndex}は
 * ファイル名(拡張子{@code .js}除く)を最初の{@code .}で分割した前後半から導出する
 * ({@link ScriptFileScanner}参照)。ファイル名にmethodIndexの数値変換に失敗した場合は
 * {@code methodIndex}に{@code -1}(sentinel値)を設定する。
 *
 * @param filePath    スクリプトファイルのパス
 * @param className   呼出し/スタブ対象のFQCN
 * @param methodName  対象メソッド名
 * @param methodIndex オーバーロード解決用のインデックス。ファイル名から数値変換できない場合は{@code -1}
 */
public record ScriptFileEntry(
        Path filePath,
        String className,
        String methodName,
        int methodIndex
) {
}
