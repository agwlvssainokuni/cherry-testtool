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

package cherry.testtool.cli.service;

import cherry.testtool.cli.client.ApiClientConfig;
import cherry.testtool.cli.client.ApiClientFactory;
import cherry.testtool.cli.client.TesttoolApiClient;
import cherry.testtool.cli.scan.ScriptFileScanner;
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

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * {@link InvokeService#invokeAll}の、1件失敗しても処理を継続し{@link BatchResult}へ集計する挙動(BR4)を検証する。
 * <p>
 * {@link TesttoolApiClient}は{@link ApiClientConfig}と同じ組立て({@code RestClient} → {@code RestClientAdapter} →
 * {@code HttpServiceProxyFactory})で実体を生成し、{@code RestClient}を{@link MockRestServiceServer}へ結び付ける
 * (Mockitoで{@code TesttoolApiClient}自体をモックすると、{@code @RequestParam}/{@code @RequestHeader}の
 * マッピングを検証できないため)。
 */
@ExtendWith(MockitoExtension.class)
class InvokeServiceTest {

    @Mock
    private ApiClientFactory apiClientFactory;

    private final ScriptFileScanner scanner = new ScriptFileScanner();

    private MockRestServiceServer server;

    private InvokeService invokeService;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("http://localhost:8080");
        server = MockRestServiceServer.bindTo(builder).build();
        var adapter = RestClientAdapter.create(builder.build());
        var client = HttpServiceProxyFactory.builder().exchangeAdapter(adapter).build().createClient(TesttoolApiClient.class);
        when(apiClientFactory.create(any())).thenReturn(client);
        invokeService = new InvokeService(scanner, apiClientFactory);
    }

    @Test
    void testInvokeAll_ContinuesAfterFailure_AggregatesBatchResult() throws Exception {
        server.expect(requestTo(startsWith("http://localhost:8080/testtool/invoker/invoke")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));
        server.expect(requestTo(startsWith("http://localhost:8080/testtool/invoker/invoke")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        var dir = Path.of(new ClassPathResource("invokeservice/cherry.testtool.demo.SampleService").getFile().toURI());
        var result = invokeService.invokeAll(URI.create("http://localhost:8080"), List.of(dir.getParent()), null, List.of());

        assertThat(result.results(), is(org.hamcrest.Matchers.hasSize(2)));
        assertThat(result.failureCount(), is(1));
        server.verify();
    }

}
