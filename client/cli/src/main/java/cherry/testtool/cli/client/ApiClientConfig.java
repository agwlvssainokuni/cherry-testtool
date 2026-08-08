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

package cherry.testtool.cli.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.URI;

/**
 * {@link TesttoolApiClient}のBean定義。接続先URL(実行時のCLIオプション由来)を
 * Beanファクトリメソッドの引数として受け取るprototype-scoped Beanとし、
 * {@link ApiClientFactory}が{@code ApplicationContext.getBean(TesttoolApiClient.class, baseUri)}で
 * 明示的に取得する(Application Design参照)。
 */
@Configuration
public class ApiClientConfig {

    @Bean
    @Scope("prototype")
    public TesttoolApiClient testtoolApiClient(URI baseUri) {
        var restClient = RestClient.builder().baseUrl(baseUri).build();
        var adapter = RestClientAdapter.create(restClient);
        var factory = HttpServiceProxyFactory.builder().exchangeAdapter(adapter).build();
        return factory.createClient(TesttoolApiClient.class);
    }

}
