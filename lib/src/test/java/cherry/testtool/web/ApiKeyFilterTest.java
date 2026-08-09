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

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ApiKeyFilter}の単体テスト。Spring Contextを起動せず、Servlet APIをMockitoでモック化して検証する。
 */
class ApiKeyFilterTest {

    private static final String HEADER_NAME = "X-Cherry-Testtool-Api-Key";

    private static final String EXPECTED_KEY = "secret-key";

    @Test
    public void testMatchingKey_PassesThrough() throws Exception {
        var filter = new ApiKeyFilter(HEADER_NAME, EXPECTED_KEY);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader(HEADER_NAME)).thenReturn(EXPECTED_KEY);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(any(Integer.class));
    }

    @Test
    public void testMismatchedKey_Returns401() throws Exception {
        var filter = new ApiKeyFilter(HEADER_NAME, EXPECTED_KEY);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader(HEADER_NAME)).thenReturn("wrong-key");

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void testMissingHeader_Returns401() throws Exception {
        var filter = new ApiKeyFilter(HEADER_NAME, EXPECTED_KEY);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader(HEADER_NAME)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

}
