/*
 * Copyright 2019,2026 agwlvssainokuni
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

package cherry.testtool;

import cherry.testtool.invoker.InvokerService;
import cherry.testtool.reflect.ReflectionResolver;
import cherry.testtool.script.ScriptProcessor;
import cherry.testtool.stub.*;
import cherry.testtool.web.ApiKeyFilter;
import cherry.testtool.web.TesttoolController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;

/**
 * {@code lib}が提供する全Beanを定義する自動構成クラス。
 * <p>
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}経由で
 * Spring Bootの自動構成として登録される。
 */
@AutoConfiguration
public class TesttoolAutoConfiguration {

    // invoker

    @Bean
    public InvokerService invokerService(
            ReflectionResolver reflectionResolver,
            ScriptProcessor scriptProcessor,
            ConversionService conversionService,
            ApplicationContext applicationContext
    ) {
        return new InvokerService(reflectionResolver, scriptProcessor, conversionService, applicationContext);
    }

    // reflect

    @Bean
    public ReflectionResolver reflectionResolver(
            ApplicationContext applicationContext
    ) {
        return new ReflectionResolver(applicationContext);
    }

    // script

    @Bean
    public ScriptProcessor scriptProcessor(
            ApplicationContext applicationContext
    ) {
        return new ScriptProcessor(applicationContext);
    }

    // stub

    @Bean
    public StubRepository stubRepository() {
        return new StubRepository();
    }

    @Bean
    public StubConfigLoader stubConfigLoader(
            StubRepository stubRepository,
            ReflectionResolver reflectionResolver
    ) {
        return new StubConfigLoader(stubRepository, reflectionResolver);
    }

    @Bean
    public StubResolver stubResolver(
            StubRepository stubRepository,
            ScriptProcessor scriptProcessor
    ) {
        return new StubResolver(stubRepository, scriptProcessor);
    }

    // web

    /**
     * {@code TesttoolController}は{@code cherry.testtool.web}パッケージにあり、利用側アプリの
     * コンポーネントスキャン対象外となる(スキャン範囲は通常{@code @SpringBootApplication}のパッケージ配下に限られる)ため、
     * 他のBeanと同様にここで明示的に登録する。有効/無効の判定(単一トグル、既定有効)もこのBeanメソッドで行う。
     */
    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnProperty(prefix = "cherry.testtool.web", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TesttoolController testtoolController(
            InvokerService invokerService,
            ReflectionResolver reflectionResolver,
            StubRepository stubRepository,
            ScriptProcessor scriptProcessor
    ) {
        return new TesttoolController(invokerService, reflectionResolver, stubRepository, scriptProcessor);
    }

    /**
     * {@code cherry.testtool.web.api-key}が設定されている場合のみ、{@code /testtool/**}宛リクエストへの
     * APIキー検証({@link ApiKeyFilter})を有効化する。OAuth2/OIDCのような大掛かりな仕組みや
     * {@code spring-boot-starter-security}のような消費側アプリの既存構成と衝突しうる重量級依存を避けた、
     * 最低限のアクセス防止策(未設定時は現状通り検証をスキップする後方互換動作)。
     * <p>
     * 単純に{@link ApiKeyFilter}型のBeanを返すと既定のURL patternが{@code /*}(消費側アプリの全リクエスト)に
     * なってしまうため、{@link FilterRegistrationBean}で{@code /testtool/*}に明示的に限定する。
     */
    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnProperty(prefix = "cherry.testtool.web", name = "api-key")
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilter(
            @Value("${cherry.testtool.web.api-key}") String apiKey,
            @Value("${cherry.testtool.web.api-key-header:X-Cherry-Testtool-Api-Key}") String apiKeyHeader
    ) {
        var registration = new FilterRegistrationBean<>(new ApiKeyFilter(apiKeyHeader, apiKey));
        registration.addUrlPatterns("/testtool/*");
        return registration;
    }

}
