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

package cherry.testtool.reflect;

import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * Spring Bean名・メソッドを{@link ApplicationContext}経由で解決するユーティリティ。
 */
public class ReflectionResolver {

    private final ApplicationContext appCtx;

    public ReflectionResolver(
            ApplicationContext applicationContext
    ) {
        appCtx = applicationContext;
    }

    /**
     * FQCN文字列からクラスをロードした上で{@link #resolveBeanName(Class)}に委譲する。
     *
     * @param beanClassName 解決対象クラスのFQCN
     * @return 該当するBean名の一覧
     * @throws ClassNotFoundException 指定クラスが見つからない場合
     */
    public List<String> resolveBeanName(
            String beanClassName
    ) throws ClassNotFoundException {
        var beanClass = getClass().getClassLoader().loadClass(beanClassName);
        return resolveBeanName(beanClass);
    }

    /**
     * 指定クラスに対応するBean名の一覧を解決する。
     *
     * @param beanClass 解決対象クラス
     * @return 該当するBean名の一覧
     */
    public List<String> resolveBeanName(
            Class<?> beanClass
    ) {
        return asList(appCtx.getBeanNamesForType(beanClass));
    }

    /**
     * FQCN文字列からクラスをロードした上で{@link #resolveMethod(Class, String)}に委譲する。
     *
     * @param beanClassName 解決対象クラスのFQCN
     * @param methodName 解決対象メソッド名
     * @return 該当する(オーバーロードを含む)メソッドの一覧
     * @throws ClassNotFoundException 指定クラスが見つからない場合
     */
    public List<Method> resolveMethod(
            String beanClassName,
            String methodName
    ) throws ClassNotFoundException {
        var beanClass = getClass().getClassLoader().loadClass(beanClassName);
        return resolveMethod(beanClass, methodName);
    }

    /**
     * 指定クラス・メソッド名に一致する(オーバーロードを含む)宣言メソッドの一覧を解決する。
     * <p>
     * {@link Class#getDeclaredMethods()}が返す順序はJVM仕様上保証されておらず、実行毎に変動しうる。
     * 呼出し元(WebコントローラのmethodIndexパラメータ、{@code StubConfigLoader}のファイル名末尾の
     * インデックス等)はこの一覧の並び順を前提に位置指定でメソッドを特定するため、パラメータ型名で
     * ソートして決定的な順序を保証する。
     *
     * @param beanClass 解決対象クラス
     * @param methodName 解決対象メソッド名
     * @return 該当するメソッドの一覧(パラメータ型名の昇順)
     */
    public List<Method> resolveMethod(
            Class<?> beanClass, String methodName
    ) {
        return Stream.of(beanClass.getDeclaredMethods()).filter(m -> m.getName().equals(methodName))
                .sorted(Comparator.<Method, String>comparing(m -> parameterTypeNames(m, Class::getSimpleName))
                        .thenComparing(m -> parameterTypeNames(m, Class::getName)))
                .collect(Collectors.toList());
    }

    private static String parameterTypeNames(
            Method method, Function<Class<?>, String> nameExtractor
    ) {
        return Stream.of(method.getParameterTypes()).map(nameExtractor).collect(joining(","));
    }

}
