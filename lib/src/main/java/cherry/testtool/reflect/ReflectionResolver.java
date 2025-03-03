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

import java.lang.reflect.Method;
import java.util.List;

public interface ReflectionResolver {

    @Nonnull
    default List<String> resolveBeanName(
            @Nonnull String beanClassName
    ) throws ClassNotFoundException {
        var beanClass = getClass().getClassLoader().loadClass(beanClassName);
        return resolveBeanName(beanClass);
    }

    @Nonnull
    List<String> resolveBeanName(
            @Nonnull Class<?> beanClass
    );

    @Nonnull
    default List<Method> resolveMethod(
            @Nonnull String beanClassName,
            @Nonnull String methodName
    ) throws ClassNotFoundException {
        var beanClass = getClass().getClassLoader().loadClass(beanClassName);
        return resolveMethod(beanClass, methodName);
    }

    @Nonnull
    List<Method> resolveMethod(
            @Nonnull Class<?> beanClass, @Nonnull String methodName
    );

}
