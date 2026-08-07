/*
 * Copyright 2015,2026 agwlvssainokuni
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

package cherry.testtool.webconsole;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link SpaFallbackResourceResolver}の解決分岐(存在する静的リソース/存在しない場合のindex.htmlフォールバック)を検証する。
 * <p>
 * {@code getResource(String, Resource)}はprotectedのため、同一パッケージから直接呼び出す。
 */
class SpaFallbackResourceResolverTest {

    private final SpaFallbackResourceResolver resolver = new SpaFallbackResourceResolver();

    @Test
    void testExistingResource_ReturnsAsIs() throws IOException {
        var location = new ClassPathResource("spa-fallback-fixture/");
        var result = resolver.getResource("existing.txt", location);
        assertThat(result.exists(), is(true));
        assertThat(result.getFilename(), is("existing.txt"));
    }

    @Test
    void testMissingResource_FallsBackToIndexHtml() throws IOException {
        var location = new ClassPathResource("spa-fallback-fixture/");
        var result = resolver.getResource("no/such/path", location);
        assertThat(result.exists(), is(true));
        assertThat(result.getFilename(), is("index.html"));
    }

}
