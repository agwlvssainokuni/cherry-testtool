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

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SPA(React Router、ブラウザ履歴ベースのルーティング)向けのフォールバックリソース解決。
 * <p>
 * リクエストされたパスが静的リソース({@code classpath:/static/}配下)として存在しない場合、
 * {@code index.html}を返す(SPA側のルーティングに処理を委ねる)。{@code /testtool/**}は
 * {@link GatewayRouteConfig}が別途処理するため、このクラスの解決対象には現れない。
 */
public class SpaFallbackResourceResolver extends PathResourceResolver {

    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
        var requestedResource = location.createRelative(resourcePath);
        if (requestedResource.exists() && requestedResource.isReadable()) {
            return requestedResource;
        }
        return new ClassPathResource("/static/index.html");
    }

}
