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

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * 実行時のCLIオプション(接続先URL)を基に、Spring管理Beanとしての{@link TesttoolApiClient}を取得する
 * 薄いファサード(Application Design参照)。
 */
@Component
public class ApiClientFactory {

    private final ApplicationContext applicationContext;

    public ApiClientFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @param baseUri 接続先ベースURL
     * @return {@code baseUri}をベースURLとする{@link TesttoolApiClient}
     */
    public TesttoolApiClient create(URI baseUri) {
        return applicationContext.getBean(TesttoolApiClient.class, baseUri);
    }

}
