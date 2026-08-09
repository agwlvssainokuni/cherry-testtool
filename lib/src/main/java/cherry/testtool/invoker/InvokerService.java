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

package cherry.testtool.invoker;

import cherry.testtool.reflect.ReflectionResolver;
import cherry.testtool.script.ScriptProcessor;
import cherry.testtool.util.ToMapUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static java.text.MessageFormat.format;

/**
 * Spring Beanのメソッドをリフレクションで動的に呼び出すサービス。
 * <p>
 * 呼出し対象メソッドの引数は、{@link ScriptProcessor}で評価したスクリプトの結果を
 * 各引数の型に変換して生成する。呼出し結果(または例外)はYAML形式の文字列へ
 * シリアライズして返す。
 */
public class InvokerService {

    private final ReflectionResolver reflectionResolver;

    private final ScriptProcessor scriptProcessor;

    private final ConversionService conversionService;

    private final ApplicationContext appCtx;

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public InvokerService(
            ReflectionResolver reflectionResolver,
            ScriptProcessor scriptProcessor,
            ConversionService conversionService,
            ApplicationContext applicationContext
    ) {
        this.reflectionResolver = reflectionResolver;
        this.scriptProcessor = scriptProcessor;
        this.conversionService = conversionService;
        this.appCtx = applicationContext;
    }

    /**
     * 解決済みのBean・メソッドに対して、スクリプトで生成した引数を使いリフレクション呼出しを行う。
     *
     * @param beanName Bean名。未指定(null/空)の場合は型のみで解決する
     * @param beanClass 呼出し対象BeanのClass
     * @param method 呼出し対象メソッド
     * @param script 引数リストを生成するスクリプト(空の場合は引数なし)
     * @param engine スクリプトエンジン名。未指定の場合は既定のエンジンを使用する
     * @return 呼出し結果をYAML形式でシリアライズした文字列
     */
    public String invoke(
            @Nullable String beanName,
            Class<?> beanClass,
            Method method,
            String script,
            @Nullable String engine
    ) {
        try {

            Object targetBean;
            if (!StringUtils.hasLength(beanName)) {
                targetBean = appCtx.getBean(beanClass);
            } else {
                targetBean = appCtx.getBean(beanName, beanClass);
            }

            final List<?> argList;
            if (!StringUtils.hasText(script)) {
                argList = Collections.emptyList();
            } else {
                var v = scriptProcessor.eval(script, engine);
                if (v == null) {
                    argList = Collections.emptyList();
                } else if (v instanceof List) {
                    argList = (List<?>) v;
                } else {
                    argList = Collections.singletonList(v);
                }
            }

            var param = new Object[method.getParameterCount()];
            for (int i = 0; i < param.length; i++) {
                if (i >= argList.size()) {
                    param[i] = null;
                    continue;
                }
                var arg = argList.get(i);
                if (arg == null) {
                    param[i] = null;
                } else {
                    param[i] = conversionService.convert(
                            arg,
                            new TypeDescriptor(ResolvableType.forClass(arg.getClass()), null, null),
                            new TypeDescriptor(new MethodParameter(method, i))
                    );
                }
            }

            Object result = method.invoke(targetBean, param);
            return objectMapper.writeValueAsString(result);
        } catch (ScriptException | InvocationTargetException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * クラス名・メソッド名からメソッドを解決した上で呼出しを行う。
     * <p>
     * クラス未検出・メソッド未検出、およびスクリプト実行・呼出し中に発生した例外は、
     * 呼出し元(REST API等)へ例外をスローせず、テストツールとしての結果表示のために
     * 例外情報をYAML化した文字列として返す(想定外の例外も含めて表示する意図的な仕様)。
     *
     * @param beanName Bean名。未指定(null/空)の場合は型のみで解決する
     * @param className 呼出し対象BeanのFQCN
     * @param methodName 呼出し対象メソッド名
     * @param methodIndex 同名メソッドが複数存在する場合の0始まりのインデックス
     * @param script 引数リストを生成するスクリプト(空の場合は引数なし)
     * @param engine スクリプトエンジン名。未指定の場合は既定のエンジンを使用する
     * @return 呼出し結果、または例外情報をYAML形式でシリアライズした文字列
     */
    public String invoke(
            @Nullable String beanName,
            String className,
            String methodName,
            int methodIndex,
            String script,
            @Nullable String engine
    ) {
        try {

            var beanClass = getClass().getClassLoader().loadClass(className);
            var methodOpt = reflectionResolver.resolveMethod(beanClass, methodName).stream()
                    .skip(methodIndex).findFirst();
            if (methodOpt.isEmpty()) {
                throw new NoSuchMethodException(format("{0}#{1}() not found", className, methodName));
            }

            return invoke(beanName, beanClass, methodOpt.get(), script, engine);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            return fromThrowableToString(ex);
        } catch (IllegalStateException ex) {
            if (ex.getCause() instanceof ScriptException
                    || ex.getCause() instanceof InvocationTargetException
                    || ex.getCause() instanceof IllegalAccessException) {
                return fromThrowableToString(ex.getCause());
            } else {
                return fromThrowableToString(ex);
            }
        } catch (Exception ex) {
            // テストツールとしての性質上、想定外の例外も握りつぶさずに結果表示へ含める(意図的な仕様)。
            return fromThrowableToString(ex);
        }
    }

    private String fromThrowableToString(Throwable ex) {
        var map = ToMapUtil.fromThrowable(ex, Integer.MAX_VALUE);
        return objectMapper.writeValueAsString(map);
    }

}
