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

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * {@link ScriptFileScanner}のディレクトリ再帰走査・ソート順(BR1)、
 * className/methodName/methodIndex抽出規則(BR2)を検証する。
 */
class ScriptFileScannerTest {

    private final ScriptFileScanner scanner = new ScriptFileScanner();

    @Test
    void testScan_RecursiveAcrossClassDirs_SortedByPath_ExcludesNonJs() throws IOException {
        var dir = Path.of(new ClassPathResource("scriptfilescanner").getFile().toURI());

        var entries = scanner.scan(dir);

        // notes.txtは対象外(*.jsのみ)。5件の*.jsファイルが、パス文字列の辞書順で返る。
        assertThat(entries, hasSize(5));
        var sortedPaths = entries.stream().map(ScriptFileEntry::filePath).map(Path::toString).sorted(Comparator.naturalOrder()).toList();
        assertThat(entries.stream().map(ScriptFileEntry::filePath).map(Path::toString).toList(), is(sortedPaths));
    }

    @Test
    void testScan_ClassNameFromParentDirectory() throws IOException {
        var dir = Path.of(new ClassPathResource("scriptfilescanner").getFile().toURI());

        var entries = scanner.scan(dir);

        assertThat(entries.stream().map(ScriptFileEntry::className).distinct().sorted().toList(),
                contains("another.example.Foo", "cherry.testtool.demo.SampleService"));
    }

    @Test
    void testScan_NoDotSuffix_MethodIndexIsZero() throws IOException {
        var dir = Path.of(new ClassPathResource("scriptfilescanner/cherry.testtool.demo.SampleService").getFile().toURI());

        var entries = scanner.scan(dir);

        var entry = findByFileName(entries, "toBeInvoked0.js");
        assertThat(entry.methodName(), is("toBeInvoked0"));
        assertThat(entry.methodIndex(), is(0));
    }

    @Test
    void testScan_NumericSuffix_MethodIndexExtracted() throws IOException {
        var dir = Path.of(new ClassPathResource("scriptfilescanner/cherry.testtool.demo.SampleService").getFile().toURI());

        var entries = scanner.scan(dir);

        var entry = findByFileName(entries, "toBeStubbed1.1.js");
        assertThat(entry.methodName(), is("toBeStubbed1"));
        assertThat(entry.methodIndex(), is(1));
    }

    @Test
    void testScan_NonNumericSuffix_MethodIndexIsSentinelMinusOne() throws IOException {
        var dir = Path.of(new ClassPathResource("scriptfilescanner/cherry.testtool.demo.SampleService").getFile().toURI());

        var entries = scanner.scan(dir);

        var entry = findByFileName(entries, "toBeStubbed2.bad.js");
        assertThat(entry.methodName(), is("toBeStubbed2"));
        assertThat(entry.methodIndex(), is(-1));
    }

    private ScriptFileEntry findByFileName(List<ScriptFileEntry> entries, String fileName) {
        return entries.stream()
                .filter(e -> e.filePath().getFileName().toString().equals(fileName))
                .findFirst()
                .orElseThrow();
    }

}
