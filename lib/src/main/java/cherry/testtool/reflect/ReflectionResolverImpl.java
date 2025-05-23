/*
 * Copyright 2015,2025 agwlvssainokuni
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

package cherry.testtool.reflect;

import jakarta.annotation.Nonnull;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ReflectionResolverImpl implements ReflectionResolver {

    private final ApplicationContext appCtx;

    public ReflectionResolverImpl(
            @Nonnull ApplicationContext applicationContext
    ) {
        appCtx = applicationContext;
    }

    @Nonnull
    @Override
    public List<String> resolveBeanName(
            @Nonnull Class<?> beanClass
    ) {
        return asList(appCtx.getBeanNamesForType(beanClass));
    }

    @Nonnull
    @Override
    public List<Method> resolveMethod(
            @Nonnull Class<?> beanClass,
            @Nonnull String methodName
    ) {
        return Stream.of(beanClass.getDeclaredMethods()).filter(m -> m.getName().equals(methodName))
                .collect(Collectors.toList());
    }

}
