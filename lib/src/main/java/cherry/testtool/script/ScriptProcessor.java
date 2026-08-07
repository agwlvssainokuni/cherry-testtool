/*
 * Copyright 2021,2026 agwlvssainokuni
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

package cherry.testtool.script;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;

import javax.script.*;
import java.util.Optional;

/**
 * {@link ScriptEngineManager}を用いてスクリプト(既定はGraalVM JS)を実行するサービス。
 * <p>
 * スクリプト内からはバインディング変数として、Spring{@link ApplicationContext}を
 * {@code appctx}、可変長引数一式を{@code args}、各引数を{@code arg0}, {@code arg1}, ...
 * として参照できる。
 */
public class ScriptProcessor {

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private final String defaultEngineName = scriptEngineManager.getEngineFactories().stream()
            .map(ScriptEngineFactory::getEngineName).findFirst().get();

    private final ApplicationContext applicationContext;

    public ScriptProcessor(
            ApplicationContext applicationContext
    ) {
        this.applicationContext = applicationContext;
    }

    /**
     * スクリプトを評価し、結果を返す。
     *
     * @param script 評価するスクリプト本文
     * @param engineName スクリプトエンジン名。未指定の場合はJVM上で見つかった最初のエンジンを使用する
     * @param args スクリプトから{@code args}/{@code argN}として参照可能な可変長引数(スタブ実行時に使用)
     * @param <T> 評価結果の型
     * @return スクリプトの評価結果
     * @throws ScriptException スクリプトの評価中にエラーが発生した場合
     */
    public <T> T eval(
            String script,
            @Nullable String engineName,
            Object... args
    ) throws ScriptException {
        ScriptEngine engine = scriptEngineManager.getEngineByName(
                Optional.ofNullable(engineName).filter(StringUtils::isNotBlank).orElse(defaultEngineName));
        configureScriptEngine(engine, args);
        @SuppressWarnings("unchecked")
        T result = (T) engine.eval(script);
        return result;
    }

    private void configureScriptEngine(ScriptEngine engine, Object[] args) {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowAllAccess", true);
        bindings.put("appctx", applicationContext);
        bindings.put("args", args);
        for (int i = 0; i < args.length; i++) {
            bindings.put("arg" + i, args[i]);
        }
    }
}
