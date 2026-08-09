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

package cherry.testtool.cli.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

/**
 * {@link RootCommand#effectiveHeaders()}(FR10.5)を検証する。
 */
class RootCommandTest {

    @Test
    void testEffectiveHeaders_ApiKeyNotConfigured_ReturnsHeadersAsIs() {
        var rootCommand = new RootCommand();
        rootCommand.headers = List.of("X-Foo: bar");
        rootCommand.apiKey = null;

        assertThat(rootCommand.effectiveHeaders(), sameInstance(rootCommand.headers));
    }

    @Test
    void testEffectiveHeaders_ApiKeyBlank_ReturnsHeadersAsIs() {
        var rootCommand = new RootCommand();
        rootCommand.headers = List.of("X-Foo: bar");
        rootCommand.apiKey = "";

        assertThat(rootCommand.effectiveHeaders(), sameInstance(rootCommand.headers));
    }

    @Test
    void testEffectiveHeaders_ApiKeyConfigured_AppendsApiKeyHeader() {
        var rootCommand = new RootCommand();
        rootCommand.headers = List.of("X-Foo: bar");
        rootCommand.apiKey = "secret-key";
        rootCommand.apiKeyHeader = "X-Cherry-Testtool-Api-Key";

        assertThat(rootCommand.effectiveHeaders(), contains("X-Foo: bar", "X-Cherry-Testtool-Api-Key: secret-key"));
    }

    @Test
    void testEffectiveHeaders_ApiKeyConfiguredWithNoExplicitHeaders_ReturnsApiKeyHeaderOnly() {
        var rootCommand = new RootCommand();
        rootCommand.headers = List.of();
        rootCommand.apiKey = "secret-key";
        rootCommand.apiKeyHeader = "X-Cherry-Testtool-Api-Key";

        assertThat(rootCommand.effectiveHeaders(), contains("X-Cherry-Testtool-Api-Key: secret-key"));
        assertThat(rootCommand.headers, is(List.of()));
    }

}
