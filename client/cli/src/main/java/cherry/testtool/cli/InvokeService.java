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
 * 旧{@code invoker.sh}相当、メソッド呼出し一括実行のオーケストレーション(Functional Design参照)。
 */
@Service
public class InvokeService {

    private final ScriptFileScanner scanner;

    private final ApiClientFactory apiClientFactory;

    public InvokeService(ScriptFileScanner scanner, ApiClientFactory apiClientFactory) {
        this.scanner = scanner;
        this.apiClientFactory = apiClientFactory;
    }

    /**
     * 指定ディレクトリ群配下の{@code *.js}ファイルを走査し、各スクリプトに対応する呼出しAPIを実行する。
     * ファイル単位の失敗(接続エラー・HTTPエラー応答・methodIndex変換エラー等)は処理を中断せず、
     * 結果を記録した上で次のファイルへ進む(BR4)。
     *
     * @param baseUrl     接続先ベースURL
     * @param directories 走査対象ディレクトリ群
     * @param basicAuth   BASIC認証情報({@code user:pass}形式)。不要なら{@code null}
     * @param headers     追加リクエストヘッダ({@code Name: Value}形式)
     * @return 全ファイルの処理結果
     */
    public BatchResult invokeAll(URI baseUrl, List<Path> directories, @Nullable String basicAuth, List<String> headers) {
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
                results.add(processOne(client, requestHeaders, entry));
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

    private FileProcessingResult processOne(TesttoolApiClient client,
                                             MultiValueMap<String, String> headers,
                                             ScriptFileEntry entry) {
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
            var output = client.invoke(entry.className(), entry.methodName(), entry.methodIndex(), script, "", headers);
            System.out.println(output);
            return new FileProcessingResult(entry, true, output);
        } catch (IOException | RestClientException ex) {
            var message = ex.getMessage();
            System.out.println(message);
            return new FileProcessingResult(entry, false, String.valueOf(message));
        }
    }

}
