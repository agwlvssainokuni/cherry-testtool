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

package cherry.testtool.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * クラス名・メソッドシグネチャの文字列表現を組み立てるユーティリティ。
 */
public class ReflectionUtil {

    /**
     * クラスの文字列表現(完全修飾名または単純名)を返す。
     *
     * @param klass 対象クラス
     * @param canonical {@code true}の場合は完全修飾名、{@code false}の場合は単純名を返す
     * @return クラスの文字列表現
     */
    public static String getClassDescription(Class<?> klass, boolean canonical) {
        if (canonical) {
            return klass.getCanonicalName();
        } else {
            return klass.getSimpleName();
        }
    }

    /**
     * メソッドシグネチャの文字列表現を、指定した構成要素の組合せで組み立てる。
     *
     * @param method 対象メソッド
     * @param returnType 戻り値の型を含めるか
     * @param declaringClass 宣言クラス名を含めるか
     * @param methodName メソッド名を含めるか
     * @param paramType 引数型一覧を含めるか
     * @param canonical クラス名を完全修飾名で表すか(単純名にするか)
     * @return 組み立てたメソッドシグネチャの文字列表現
     */
    public static String getMethodDescription(
            Method method,
            boolean returnType,
            boolean declaringClass,
            boolean methodName,
            boolean paramType,
            boolean canonical) {

        List<String> desc = new ArrayList<>();
        if (returnType) {
            desc.add(getClassDescription(method.getReturnType(), canonical));
        }

        StringBuilder sb = new StringBuilder();
        if (declaringClass) {
            sb.append(getClassDescription(method.getDeclaringClass(), canonical));
        }
        if (declaringClass && methodName) {
            sb.append(".");
        }
        if (methodName) {
            sb.append(method.getName());
        }
        if (paramType) {
            sb.append(Stream.of(method.getParameterTypes()).map(klass -> getClassDescription(klass, canonical))
                    .collect(Collectors.joining(",", "(", ")")));
        }
        desc.add(sb.toString());

        return String.join(" ", desc);
    }

}
