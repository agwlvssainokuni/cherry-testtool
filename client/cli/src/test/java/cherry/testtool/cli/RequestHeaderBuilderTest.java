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

import java.util.Base64;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * {@link RequestHeaderBuilder}のBASIC認証エンコード・追加ヘッダ解析(BR6)を検証する。
 */
class RequestHeaderBuilderTest {

    @Test
    void testBuild_BasicAuthEncoded() {
        var headers = RequestHeaderBuilder.build("user:pass", List.of());

        var expected = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
        assertThat(headers.getFirst("Authorization"), is(expected));
    }

    @Test
    void testBuild_HeaderParsed_TrimmedNameAndValue() {
        var headers = RequestHeaderBuilder.build(null, List.of("X-Custom:  value  "));

        assertThat(headers.getFirst("X-Custom"), is("value"));
    }

    @Test
    void testBuild_MultipleHeaders() {
        var headers = RequestHeaderBuilder.build(null, List.of("X-One: 1", "X-Two: 2"));

        assertThat(headers.getFirst("X-One"), is("1"));
        assertThat(headers.getFirst("X-Two"), is("2"));
    }

    @Test
    void testBuild_BasicAuthAndHeadersCombined() {
        var headers = RequestHeaderBuilder.build("user:pass", List.of("X-One: 1"));

        assertThat(headers.getFirst("Authorization"), is("Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes())));
        assertThat(headers.getFirst("X-One"), is("1"));
    }

    @Test
    void testBuild_NullBasicAuth_NoAuthorizationHeader() {
        var headers = RequestHeaderBuilder.build(null, List.of());

        assertThat(headers.getFirst("Authorization"), nullValue());
    }

    @Test
    void testBuild_HeaderWithoutColon_TreatedAsNameWithEmptyValue() {
        var headers = RequestHeaderBuilder.build(null, List.of("foo"));

        assertThat(headers.getFirst("foo"), is(""));
    }

}
