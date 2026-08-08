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

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * スタブ設定/呼出し用ディレクトリ配下の{@code *.js}ファイルを走査する(旧{@code invoker.sh}/{@code stubconfig.sh}の
 * {@code find ... -name '*.js' | sort}相当、Functional Design BR1・BR2)。
 */
@Component
public class ScriptFileScanner {

    private static final String SUFFIX = ".js";

    /**
     * 指定ディレクトリ配下を再帰的に走査し、{@code *.js}ファイルをパス文字列の辞書順でソートして返す(BR1)。
     * <p>
     * {@code className}はファイルの直接の親ディレクトリ名、{@code methodName}/{@code methodIndex}は
     * ファイル名を最初の{@code .}で分割して導出する(BR2)。{@code methodIndex}部分が数値でない場合は
     * {@code -1}(sentinel値)とし、例外は投げずに1件のエントリとして返す(呼出し側でBR4に従い失敗として扱う)。
     *
     * @param directory 走査対象ディレクトリ
     * @return 発見した{@link ScriptFileEntry}のパス順ソート済みリスト
     * @throws IOException ディレクトリ走査に失敗した場合
     */
    public List<ScriptFileEntry> scan(Path directory) throws IOException {
        List<Path> files;
        try (var stream = Files.walk(directory)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(SUFFIX))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }

        var entries = new ArrayList<ScriptFileEntry>(files.size());
        for (var file : files) {
            entries.add(toEntry(file));
        }
        return entries;
    }

    private ScriptFileEntry toEntry(Path file) {
        var className = file.getParent().getFileName().toString();
        var fileName = file.getFileName().toString();
        var base = fileName.substring(0, fileName.length() - SUFFIX.length());
        var parts = base.split("\\.", 2);
        var methodName = parts[0];
        var methodIndex = 0;
        if (parts.length > 1) {
            try {
                methodIndex = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                methodIndex = -1;
            }
        }
        return new ScriptFileEntry(file, className, methodName, methodIndex);
    }

}
