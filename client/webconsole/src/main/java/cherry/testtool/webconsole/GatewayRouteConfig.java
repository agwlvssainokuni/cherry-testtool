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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions.DedupeStrategy;
import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

/**
 * {@code /testtool/**}宛のリクエストのみをbackend({@code backend.uri}、既定はUnit2デモアプリの{@code 8080})へ
 * プロキシするルート定義(Spring Cloud Gateway Server MVC、Java Functional Route)。
 * <p>
 * 旧{@code client/gateway}(WebFlux版、{@code /**}全体をプロキシ)からの変更点は以下の通り。
 * <ul>
 *     <li>プロキシ対象を{@code /testtool/**}のみへ限定(SPA配信は{@link WebConfig}が別途担当)</li>
 *     <li>CORS設定は廃止(開発時はVite dev serverの{@code server.proxy}で同一オリジン化するため不要)</li>
 * </ul>
 * 旧{@code SecureHeaders}・{@code DedupeResponseHeader}フィルタ相当の処理は、
 * このバージョンのSpring Cloud Gateway Server MVCには{@code SecureHeaders}フィルタ関数が存在しないため、
 * セキュリティヘッダ付与は自前の{@link HandlerFilterFunction}として実装し、レスポンスヘッダ重複排除のみ
 * {@link FilterFunctions#dedupeResponseHeader(String, DedupeStrategy)}をそのまま用いる。
 */
@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouterFunction<ServerResponse> testtoolRoute(
            @Value("${backend.uri}") URI backendUri,
            @Value("${cherry.testtool.web.api-key:}") String apiKey,
            @Value("${cherry.testtool.web.api-key-header:X-Cherry-Testtool-Api-Key}") String apiKeyHeader
    ) {
        var route = GatewayRouterFunctions.route("testtool_backend")
                .route(RequestPredicates.path("/testtool/**"), HandlerFunctions.http())
                .filter(FilterFunctions.uri(backendUri))
                .filter(secureHeaders())
                .filter(FilterFunctions.dedupeResponseHeader("Vary", DedupeStrategy.RETAIN_UNIQUE));
        if (StringUtils.hasText(apiKey)) {
            // backendのApiKeyFilter(lib)向けにAPIキーを自動付与する。SPA利用者(ブラウザ)には別途要求しない
            // (webconsoleが鍵を内部保持する最小スコープ、FR10.4)。
            route = route.filter(FilterFunctions.setRequestHeader(apiKeyHeader, apiKey));
        }
        return route.build();
    }

    /**
     * 旧gatewayの{@code SecureHeaders}フィルタ相当のセキュリティヘッダを応答へ付与する。
     */
    private HandlerFilterFunction<ServerResponse, ServerResponse> secureHeaders() {
        return HandlerFilterFunction.ofResponseProcessor((request, response) -> {
            var headers = response.headers();
            headers.add("X-Xss-Protection", "1; mode=block");
            headers.add("Strict-Transport-Security", "max-age=631138519");
            headers.add("X-Frame-Options", "DENY");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.add("Referrer-Policy", "no-referrer");
            headers.add("X-Download-Options", "noopen");
            headers.add("X-Permitted-Cross-Domain-Policies", "none");
            return response;
        });
    }

}
