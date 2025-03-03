/*
 * Copyright 2019,2025 agwlvssainokuni
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
import cherry.testtool.invoker.InvokerServiceImpl;
import cherry.testtool.reflect.ReflectionResolver;
import cherry.testtool.reflect.ReflectionResolverImpl;
import cherry.testtool.script.ScriptProcessor;
import cherry.testtool.script.ScriptProcessorImpl;
import cherry.testtool.stub.*;
import jakarta.annotation.Nonnull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
public class TesttoolConfiguration {

    // invoker

    @Nonnull
    @Bean
    public InvokerService invokerService(
            @Nonnull ReflectionResolver reflectionResolver,
            @Nonnull ScriptProcessor scriptProcessor,
            @Nonnull ConversionService conversionService,
            @Nonnull ApplicationContext applicationContext
    ) {
        return new InvokerServiceImpl(reflectionResolver, scriptProcessor, conversionService, applicationContext);
    }

    // reflect

    @Nonnull
    @Bean
    public ReflectionResolver reflectionResolver(
            @Nonnull ApplicationContext applicationContext
    ) {
        return new ReflectionResolverImpl(applicationContext);
    }

    // script

    @Nonnull
    @Bean
    public ScriptProcessor scriptProcessor(
            @Nonnull ApplicationContext applicationContext
    ) {
        return new ScriptProcessorImpl(applicationContext);
    }

    // stub

    @Nonnull
    @Bean
    public StubRepository stubRepository() {
        return new StubRepositoryImpl();
    }

    @Nonnull
    @Bean
    public StubConfigLoader stubConfigLoader(
            @Nonnull StubRepository stubRepository,
            @Nonnull ReflectionResolver reflectionResolver
    ) {
        return new StubConfigLoader(stubRepository, reflectionResolver);
    }

    @Nonnull
    @Bean
    public StubResolver stubResolver(
            @Nonnull StubRepository stubRepository,
            @Nonnull ScriptProcessor scriptProcessor
    ) {
        return new StubResolverImpl(stubRepository, scriptProcessor);
    }

}
