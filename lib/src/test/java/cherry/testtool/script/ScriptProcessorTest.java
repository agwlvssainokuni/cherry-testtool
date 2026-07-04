/*
 * Copyright 2023,2026 agwlvssainokuni
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

import cherry.testtool.TesttoolConfiguration;
import cherry.testtool.ToolTesterImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TesttoolConfiguration.class, ToolTesterImpl.class})
@SpringBootApplication()
@ImportResource({"classpath:spring/appctx-trace.xml", "classpath:spring/appctx-stub.xml"})
public class ScriptProcessorTest {

    @Autowired
    private ScriptProcessor scriptProcessor;

    @Test
    public void testJsArray() throws Exception {
        var result = (List<?>) scriptProcessor.eval("[1, 2, 3]", "");
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    public void testJavaType() throws Exception {
        var result = (List<?>) scriptProcessor.eval(
                "const Arrays = Java.type('java.util.Arrays'); Arrays.asList(1, 2, 3);", "");
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    public void testEngineVersion() {
        // GraalVM JSのバージョンを固定検証する。依存関係更新時にここが変化し、
        // 破壊的変更の有無を再確認するきっかけとする。
        var factory = new ScriptEngineManager().getEngineFactories().stream()
                .filter(f -> f.getNames().contains("js"))
                .findFirst().orElseThrow();
        assertEquals("Graal.js", factory.getEngineName());
        assertEquals("25.1.3", factory.getEngineVersion());
    }

    @Test
    public void testArgsBinding() throws Exception {
        // stub設定で使われる「function(args) { return args[0] ... }」形式のパターン。
        var result = scriptProcessor.eval("args[0] + ' processed'", "", "hello");
        assertEquals("hello processed", result);
    }

    @Test
    public void testArgNBinding() throws Exception {
        // 引数を arg0, arg1, ... で個別参照するパターン。
        var result = scriptProcessor.eval("arg0 + arg1", "", 1, 2);
        assertEquals(3, result);
    }

    @Test
    public void testObjectLiteral() throws Exception {
        // README記載の「オブジェクト生成」パターン(引数生成スクリプトの複雑な例)。
        var result = (Map<?, ?>) scriptProcessor.eval("({name: 'test', value: 42})", "");
        assertEquals("test", result.get("name"));
        assertEquals(42, result.get("value"));
    }

    @Test
    public void testAppctxBinding() throws Exception {
        // appctxバインディング経由でSpringのApplicationContextを呼び出せること。
        var result = scriptProcessor.eval("appctx.getBeanDefinitionCount()", "");
        assertTrue(result instanceof Integer);
        assertTrue((Integer) result > 0);
    }

}
