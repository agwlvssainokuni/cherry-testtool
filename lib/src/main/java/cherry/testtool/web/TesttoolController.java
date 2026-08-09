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

package cherry.testtool.web;

import cherry.testtool.invoker.InvokerService;
import cherry.testtool.reflect.ReflectionResolver;
import cherry.testtool.script.ScriptProcessor;
import cherry.testtool.stub.StubConfig;
import cherry.testtool.stub.StubRepository;
import cherry.testtool.util.ToMapUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cherry.testtool.util.ReflectionUtil.getMethodDescription;

/**
 * {@code lib}が提供するREST APIを集約するController(旧{@code InvokerController}/{@code StubConfigController}を統合、FR8)。
 * <p>
 * {@code invoke}(メソッド呼出し)、{@code put}/{@code get}/{@code list}(スタブ設定)は現行URLのまま維持し、
 * 両者で重複していたBean名・メソッド解決は{@code /testtool/resolve/**}へ一本化した。
 * 有効/無効は単一のプロパティ({@code cherry.testtool.web.enabled}、既定有効)で切り替える。
 * <p>
 * このクラス自体はコンポーネントスキャンに依存せず、{@link cherry.testtool.TesttoolAutoConfiguration}が
 * 明示的に{@code @Bean}登録する(条件判定(Servlet環境・トグル)も同クラスの{@code @Bean}メソッド側で行う)。
 */
@RestController
public class TesttoolController {

    private final InvokerService invokerService;

    private final ReflectionResolver reflectionResolver;

    private final StubRepository stubRepository;

    private final ScriptProcessor scriptProcessor;

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public TesttoolController(
            InvokerService invokerService,
            ReflectionResolver reflectionResolver,
            StubRepository stubRepository,
            ScriptProcessor scriptProcessor
    ) {
        this.invokerService = invokerService;
        this.reflectionResolver = reflectionResolver;
        this.stubRepository = stubRepository;
        this.scriptProcessor = scriptProcessor;
    }

    /**
     * 指定クラス・メソッドをリフレクションで呼び出す。
     *
     * @return 呼出し結果(または例外情報)をYAML形式でシリアライズした文字列
     */
    @RequestMapping("/testtool/invoker/invoke")
    public String invoke(@RequestParam(value = "beanName", required = false) String beanName,
                          @RequestParam("className") String className,
                          @RequestParam("methodName") String methodName,
                          @RequestParam(value = "methodIndex", defaultValue = "0") int methodIndex,
                          @RequestParam("script") String script,
                          @RequestParam(value = "engine", defaultValue = "") String engine) {
        return invokerService.invoke(beanName, className, methodName, methodIndex, script, engine);
    }

    /**
     * 指定メソッドへスタブ設定を登録する。{@code script}が空の場合は登録を解除する。
     *
     * @return 登録/解除に成功すれば{@code "true"}、対象メソッド未検出なら{@code "false"}、クラス未検出なら例外メッセージ
     */
    @RequestMapping("/testtool/stubconfig/put")
    public String putStubConfig(
            @RequestParam("className") String className,
            @RequestParam("methodName") String methodName,
            @RequestParam(value = "methodIndex", defaultValue = "0") int methodIndex,
            @RequestParam("script") String script,
            @RequestParam("engine") String engine) {

        final Optional<Method> methodOpt;
        try {
            methodOpt = reflectionResolver.resolveMethod(className, methodName).stream()
                    .skip(methodIndex).findFirst();
        } catch (ClassNotFoundException ex) {
            return ex.getMessage();
        }

        if (methodOpt.isEmpty()) {
            return String.valueOf(false);
        }
        var method = methodOpt.get();

        if (!StringUtils.hasText(script)) {
            stubRepository.clear(method);
            return String.valueOf(true);
        }

        stubRepository.put(method, new StubConfig(script, engine));
        return String.valueOf(true);
    }

    /**
     * 登録済みスタブのスクリプト・エンジン・現在の評価結果を取得する。
     *
     * @return {@code [script, engine, evaluatedResultOrError]}の3要素配列。未登録/未検出時は空文字の配列
     */
    @RequestMapping("/testtool/stubconfig/get")
    public List<String> getStubConfig(
            @RequestParam("className") String className,
            @RequestParam("methodName") String methodName,
            @RequestParam(value = "methodIndex", defaultValue = "0") int methodIndex) {

        var list = new ArrayList<String>();

        final Optional<Method> methodOpt;
        try {
            methodOpt = reflectionResolver.resolveMethod(className, methodName).stream()
                    .skip(methodIndex).findFirst();
        } catch (ClassNotFoundException ex) {
            list.add("");
            list.add("");
            list.add(ex.getMessage());
            return list;
        }

        if (methodOpt.isEmpty()) {
            list.add("");
            list.add("");
            list.add("");
            return list;
        }
        var method = methodOpt.get();

        var stubConfig = stubRepository.get(method);
        if (stubConfig == null) {
            list.add("");
            list.add("");
            list.add("");
            return list;
        }

        list.add(stubConfig.script());
        list.add(stubConfig.engine());
        try {
            var result = scriptProcessor.eval(stubConfig.script(), stubConfig.engine());
            list.add(objectMapper.writeValueAsString(result));
        } catch (ScriptException | JacksonException ex) {
            var map = ToMapUtil.fromThrowable(ex, Integer.MAX_VALUE);
            list.add(objectMapper.writeValueAsString(map));
        }
        return list;
    }

    /**
     * 登録済みスタブの対象メソッド一覧を取得する。
     *
     * @param className 絞込み対象クラス名。空の場合は全件
     * @return スタブ登録済みメソッドのシグネチャ文字列一覧
     */
    @RequestMapping("/testtool/stubconfig/list")
    public List<String> getStubbedMethod(
            @RequestParam(value = "className") String className) {
        return stubRepository.getStubbedMethod().stream()
                .filter(m -> !StringUtils.hasLength(className) || m.getDeclaringClass().getName().equals(className))
                .map(m -> getMethodDescription(m, false, true, true, true, true)).collect(Collectors.toList());
    }

    /**
     * 指定クラスに対応するSpring Bean名の一覧を取得する。
     * <p>
     * 旧{@code /testtool/invoker/bean}・{@code /testtool/stubconfig/bean}を統合した共通パス(FR8)。
     *
     * @return Bean名の一覧。クラス未検出の場合は空
     */
    @RequestMapping("/testtool/resolve/bean")
    public List<String> resolveBeanName(
            @RequestParam("className") String className) {
        try {
            return reflectionResolver.resolveBeanName(className);
        } catch (ClassNotFoundException ex) {
            return new ArrayList<>();
        }
    }

    /**
     * 指定クラス・メソッド名に一致する(オーバーロード含む)メソッドシグネチャ一覧を取得する。
     * <p>
     * 旧{@code /testtool/invoker/method}・{@code /testtool/stubconfig/method}を統合した共通パス(FR8)。
     *
     * @return メソッドシグネチャ文字列の一覧。クラス未検出の場合は空
     */
    @RequestMapping("/testtool/resolve/method")
    public List<String> resolveMethod(
            @RequestParam("className") String className,
            @RequestParam("methodName") String methodName) {
        try {
            return reflectionResolver.resolveMethod(className, methodName).stream()
                    .map(m -> getMethodDescription(m, false, false, false, true, false)).collect(Collectors.toList());
        } catch (ClassNotFoundException ex) {
            return new ArrayList<>();
        }
    }

}
