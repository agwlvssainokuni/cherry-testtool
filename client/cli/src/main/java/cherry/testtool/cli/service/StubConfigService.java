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

package cherry.testtool.cli.service;

import cherry.testtool.cli.client.ApiClientFactory;
import cherry.testtool.cli.client.RequestHeaderBuilder;
import cherry.testtool.cli.client.TesttoolApiClient;
import cherry.testtool.cli.scan.ScriptFileEntry;
import cherry.testtool.cli.scan.ScriptFileScanner;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 旧{@code stubconfig.sh}相当、スタブ登録/参照/解除一括実行のオーケストレーション(Functional Design参照)。
 * {@code register}/{@code clear}/{@code show}いずれも走査・繰返し構造は共通で(BR7)、
 * 呼出しAPIとscriptパラメータのみが異なる。
 */
@Service
public class StubConfigService {

    private final ScriptFileScanner scanner;

    private final ApiClientFactory apiClientFactory;

    public StubConfigService(ScriptFileScanner scanner, ApiClientFactory apiClientFactory) {
        this.scanner = scanner;
        this.apiClientFactory = apiClientFactory;
    }

    /**
     * 指定ディレクトリ群配下の各スクリプトファイルの内容をスタブとして登録する({@code /testtool/stubconfig/put}、BR7)。
     */
    public BatchResult registerAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers) {
        return runBatch(baseUrl, directories, basicAuth, headers, false,
                (client, requestHeaders, entry, script) ->
                        client.putStub(entry.className(), entry.methodName(), entry.methodIndex(), script, "", requestHeaders));
    }

    /**
     * 指定ディレクトリ群配下の各スクリプトファイルに対応するスタブ登録を解除する
     * ({@code /testtool/stubconfig/put}、{@code script}は空文字列、BR7)。
     */
    public BatchResult clearAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers) {
        return runBatch(baseUrl, directories, basicAuth, headers, false,
                (client, requestHeaders, entry, script) ->
                        client.putStub(entry.className(), entry.methodName(), entry.methodIndex(), "", "", requestHeaders));
    }

    /**
     * 指定ディレクトリ群配下の各スクリプトファイルに対応する、現在のスタブ登録内容を表示する
     * ({@code /testtool/stubconfig/get}、BR7)。応答(script/engine/評価結果の3要素)は1要素1行で表示する。
     */
    public BatchResult showAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers) {
        return runBatch(baseUrl, directories, basicAuth, headers, true,
                (client, requestHeaders, entry, script) -> {
                    var list = client.getStub(entry.className(), entry.methodName(), entry.methodIndex(), requestHeaders);
                    return String.join("\n", list);
                });
    }

    @FunctionalInterface
    private interface EntryCall {
        String call(TesttoolApiClient client, MultiValueMap<String, String> headers, ScriptFileEntry entry, String script)
                throws IOException;
    }

    private BatchResult runBatch(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers,
                                  boolean blankLineAfter, EntryCall call) {
        for (var dir : directories) {
            if (!Files.isDirectory(dir)) {
                throw new IllegalArgumentException("directory not found: " + dir);
            }
        }

        var client = apiClientFactory.create(baseUrl);
        var requestHeaders = RequestHeaderBuilder.build(basicAuth, headers);

        var results = new ArrayList<FileProcessingResult>();
        for (var dir : directories) {
            System.out.println("PROCESSING " + dir);
            for (var entry : scan(dir)) {
                results.add(processOne(client, requestHeaders, entry, blankLineAfter, call));
            }
        }
        return new BatchResult(results);
    }

    private List<ScriptFileEntry> scan(Path dir) {
        try {
            return scanner.scan(dir);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private FileProcessingResult processOne(TesttoolApiClient client, MultiValueMap<String, String> headers,
                                             ScriptFileEntry entry, boolean blankLineAfter, EntryCall call) {
        System.out.println(entry.filePath());
        System.out.println("  " + entry.className());
        System.out.println("  " + entry.methodName() + " " + entry.methodIndex());

        if (entry.methodIndex() < 0) {
            var message = "invalid methodIndex in file name: " + entry.filePath();
            System.out.println(message);
            return new FileProcessingResult(entry, false, message);
        }

        try {
            var script = Files.readString(entry.filePath());
            var output = call.call(client, headers, entry, script);
            System.out.println(output);
            if (blankLineAfter) {
                System.out.println();
            }
            return new FileProcessingResult(entry, true, output);
        } catch (IOException | RestClientException ex) {
            var message = ex.getMessage();
            System.out.println(message);
            return new FileProcessingResult(entry, false, String.valueOf(message));
        }
    }

}
