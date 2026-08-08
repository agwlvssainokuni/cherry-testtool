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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * BASIC認証・追加ヘッダをリクエストヘッダへ組み立てる共通処理(Functional Design BR6)。
 */
public final class RequestHeaderBuilder {

    private RequestHeaderBuilder() {
    }

    /**
     * {@code basicAuth}(指定時は{@code user:pass}をBase64エンコードした{@code Authorization: Basic}ヘッダ)と、
     * {@code headers}(各要素を最初の{@code :}で分割した{@code Name: Value}形式、前後の空白はtrim)を
     * 1つの{@link MultiValueMap}へ組み立てる。{@code :}を含まない要素は、値を空文字列としたヘッダ名として扱う。
     *
     * @param basicAuth {@code user:pass}形式の認証情報。{@code null}または空ならBASIC認証ヘッダは付加しない
     * @param headers   {@code Name: Value}形式の生ヘッダ文字列のリスト
     * @return 組み立てたリクエストヘッダ
     */
    public static MultiValueMap<String, String> build(@Nullable String basicAuth, List<String> headers) {
        var result = new LinkedMultiValueMap<String, String>();
        if (StringUtils.hasText(basicAuth)) {
            var encoded = Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8));
            result.add("Authorization", "Basic " + encoded);
        }
        for (var header : headers) {
            var idx = header.indexOf(':');
            if (idx < 0) {
                result.add(header.trim(), "");
            } else {
                result.add(header.substring(0, idx).trim(), header.substring(idx + 1).trim());
            }
        }
        return result;
    }

}
