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

package cherry.testtool.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * {@link StubConfigService}の{@code registerAll}/{@code clearAll}/{@code showAll}が、
 * BR7のAPIマッピング通りに呼び出されることを検証する。{@link InvokeServiceTest}と同様、
 * {@code RestClient}を{@link MockRestServiceServer}へ結び付けた実体の{@link TesttoolApiClient}を用いる。
 */
@ExtendWith(MockitoExtension.class)
class StubConfigServiceTest {

    @Mock
    private ApiClientFactory apiClientFactory;

    private final ScriptFileScanner scanner = new ScriptFileScanner();

    private MockRestServiceServer server;

    private StubConfigService stubConfigService;

    private Path scanRoot;

    @BeforeEach
    void setUp() throws Exception {
        var builder = RestClient.builder().baseUrl("http://localhost:8080");
        server = MockRestServiceServer.bindTo(builder).build();
        var adapter = RestClientAdapter.create(builder.build());
        var client = HttpServiceProxyFactory.builder().exchangeAdapter(adapter).build().createClient(TesttoolApiClient.class);
        when(apiClientFactory.create(any())).thenReturn(client);
        stubConfigService = new StubConfigService(scanner, apiClientFactory);
        scanRoot = Path.of(new ClassPathResource("stubconfigservice").getFile().toURI());
    }

    @Test
    void testRegisterAll_CallsPutWithFileContent_AndPrintsProgress() {
        server.expect(requestTo(startsWith("http://localhost:8080/testtool/stubconfig/put")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("true", MediaType.TEXT_PLAIN));

        var out = new ByteArrayOutputStream();
        var original = System.out;
        System.setOut(new PrintStream(out));
        try {
            var result = stubConfigService.registerAll(URI.create("http://localhost:8080"), List.of(scanRoot), null, List.of());
            assertThat(result.failureCount(), is(0));
        } finally {
            System.setOut(original);
        }

        assertThat(out.toString(), containsString("PROCESSING " + scanRoot));
        assertThat(out.toString(), containsString("cherry.testtool.demo.SampleService"));
        server.verify();
    }

    @Test
    void testClearAll_CallsPutWithEmptyScript() {
        server.expect(requestTo(startsWith("http://localhost:8080/testtool/stubconfig/put")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("true", MediaType.TEXT_PLAIN));

        var result = stubConfigService.clearAll(URI.create("http://localhost:8080"), List.of(scanRoot), null, List.of());

        assertThat(result.failureCount(), is(0));
        server.verify();
    }

    @Test
    void testShowAll_CallsGet_JoinsResponseLines() {
        server.expect(requestTo(startsWith("http://localhost:8080/testtool/stubconfig/get")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("[\"9999\",\"\",\"9999\"]", MediaType.APPLICATION_JSON));

        var result = stubConfigService.showAll(URI.create("http://localhost:8080"), List.of(scanRoot), null, List.of());

        assertThat(result.failureCount(), is(0));
        assertThat(result.results().getFirst().output(), is("9999\n\n9999"));
        server.verify();
    }

}
