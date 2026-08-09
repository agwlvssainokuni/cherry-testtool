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
 */

package cherry.testtool.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * {@code /testtool/**}宛リクエストのヘッダを検証する、最小限のAPIキー認証{@link Filter}。
 * <p>
 * OAuth2/OIDCのような大掛かりな仕組みや{@code spring-boot-starter-security}のような
 * 消費側アプリの既存構成と衝突しうる重量級依存を避け、追加依存ゼロで実装している。
 * 標準の{@code Authorization}ヘッダは、消費側アプリ自体の認証方式や手前のリバースプロキシとの
 * 名前空間衝突を避けるため使用しない。
 */
public class ApiKeyFilter implements Filter {

    private final String headerName;

    private final String apiKey;

    public ApiKeyFilter(String headerName, String apiKey) {
        this.headerName = headerName;
        this.apiKey = apiKey;
    }

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;
        var provided = httpRequest.getHeader(headerName);
        if (!matches(provided)) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * タイミング攻撃を避けるため、{@link MessageDigest#isEqual(byte[], byte[])}による
     * 定数時間比較でヘッダ値を照合する。
     */
    private boolean matches(String provided) {
        var providedBytes = (provided != null ? provided : "").getBytes(StandardCharsets.UTF_8);
        var expectedBytes = apiKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }

}
