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
import org.springframework.boot.test.context.SpringBootTest;

/**
 * {@link WebconsoleApplication}のコンテキストロード確認。
 * <p>
 * {@link GatewayRouteConfig}の{@code RouterFunction} Bean定義、{@link WebConfig}の静的リソース設定が
 * 例外なく解決されることを検証する({@code backend.uri}はapplication.ymlの既定値で解決され、
 * backendへの実際の疎通は行わない)。
 */
@SpringBootTest
class WebconsoleApplicationTests {

    @Test
    void contextLoads() {
    }

}
