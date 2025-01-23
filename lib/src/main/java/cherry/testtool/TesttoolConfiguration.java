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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
public class TesttoolConfiguration {

    // invoker

    @Bean
    public InvokerService invokerService(
            ReflectionResolver reflectionResolver,
            ScriptProcessor scriptProcessor,
            ConversionService conversionService,
            ApplicationContext applicationContext) {
        return new InvokerServiceImpl(reflectionResolver, scriptProcessor, conversionService, applicationContext);
    }

    // reflect

    @Bean
    public ReflectionResolver reflectionResolver(
            ApplicationContext applicationContext) {
        return new ReflectionResolverImpl(applicationContext);
    }

    // script

    @Bean
    public ScriptProcessor scriptProcessor(
            ApplicationContext applicationContext) {
        return new ScriptProcessorImpl(applicationContext);
    }

    // stub

    @Bean
    public StubRepository stubRepository() {
        return new StubRepositoryImpl();
    }

    @Bean
    public StubConfigLoader stubConfigLoader(
            StubRepository stubRepository,
            ReflectionResolver reflectionResolver) {
        return new StubConfigLoader(stubRepository, reflectionResolver);
    }

    @Bean
    public StubResolver stubResolver(
            StubRepository stubRepository,
            ScriptProcessor scriptProcessor) {
        return new StubResolverImpl(stubRepository, scriptProcessor);
    }

}
