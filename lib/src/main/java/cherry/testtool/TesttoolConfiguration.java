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
import cherry.testtool.web.TesttoolController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

/**
 * {@code lib}が提供する全Beanを定義する自動構成クラス。
 * <p>
 * {@code META-INF/spring.factories}経由でSpring Bootの自動構成として登録される。
 */
@Configuration
public class TesttoolConfiguration {

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

}
