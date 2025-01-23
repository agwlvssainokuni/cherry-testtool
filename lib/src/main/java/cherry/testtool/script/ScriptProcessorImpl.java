/*
 * Copyright 2021,2025 agwlvssainokuni
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
import org.springframework.context.ApplicationContext;

import javax.script.*;
import java.util.Optional;

public class ScriptProcessorImpl implements ScriptProcessor {

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private final String defaultEngineName = scriptEngineManager.getEngineFactories().stream()
            .map(ScriptEngineFactory::getEngineName).findFirst().get();

    private final ApplicationContext applicationContext;

    public ScriptProcessorImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T eval(String script, String engineName, Object... args) throws ScriptException {
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
