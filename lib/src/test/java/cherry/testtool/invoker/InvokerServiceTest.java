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

package cherry.testtool.invoker;

import cherry.testtool.TesttoolConfiguration;
import cherry.testtool.ToolTester;
import cherry.testtool.ToolTesterImpl;
import cherry.testtool.reflect.ReflectionResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TesttoolConfiguration.class, ToolTesterImpl.class})
@SpringBootApplication()
@ImportResource(locations = {"classpath:spring/appctx-trace.xml", "classpath:spring/appctx-stub.xml"})
public class InvokerServiceTest {

    @Autowired
    private InvokerService invokerService;

    @Autowired
    private ReflectionResolver resolver;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().modules(new JavaTimeModule())
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).factory(new YAMLFactory()).build();

    @Test
    public void testNoArgNoRet() throws Exception {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked0");
        assertEquals("--- null\n", invokerService.invoke(null, ToolTester.class, list.get(0), null, ""));
        assertEquals("--- null\n", invokerService.invoke("toolTesterImpl", ToolTester.class, list.get(0), null, ""));
    }

    @Test
    public void testPrimitive() throws Exception {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked1");
        assertEquals("--- 579\n",
                invokerService.invoke(null, ToolTester.class, list.get(0), "[123, 456]", ""));
    }

    @Test
    public void testLong() throws Exception {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked2");
        assertEquals("--- 579\n",
                invokerService.invoke(null, ToolTester.class, list.get(0), "[123, 456]", ""));
        assertEquals("--- null\n",
                invokerService.invoke(null, ToolTester.class, list.get(0), "[null, 456]", ""));
        assertEquals("--- null\n", invokerService.invoke(null, ToolTester.class, list.get(0), "123", ""));
        assertEquals("--- null\n", invokerService.invoke(null, ToolTester.class, list.get(0), null, ""));
    }

    @Test
    public void testJavaTime() throws Exception {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked3");
        assertEquals("--- \"2015-01-23T12:34:56\"\n", invokerService.invoke(null, ToolTester.class, list.get(0),
                "[\"2015-01-23\", \"12:34:56\"]", ""));
    }

    @Test
    public void testFlatDto() throws Exception {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked4");
        assertEquals("---\nval1: 68\nval2: 112\n",
                invokerService.invoke(null, ToolTester.class, list.get(0),
                        """
                                const Dto1 = Java.type("cherry.testtool.ToolTester.Dto1");
                                [new Dto1(12, 34), new Dto1(56, 78)]
                                """, ""));
    }

    @Test
    public void testNestedDto() throws Exception {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked5");
        assertEquals("---\nval1:\n  val1: 6\n  val2: 8\nval2:\n  val1: 10\n  val2: 12\n",
                invokerService.invoke(null, ToolTester.class, list.get(0),
                        """
                                const Dto1 = Java.type("cherry.testtool.ToolTester.Dto1");
                                const Dto2 = Java.type("cherry.testtool.ToolTester.Dto2");
                                [
                                	new Dto2(new Dto1(1, 2), new Dto1(3, 4)),
                                	new Dto2(new Dto1(5, 6), new Dto1(7, 8))
                                ]
                                """, ""));
    }

    @Test
    public void testMethodIndex() throws Exception {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked6");
        int m0;
        int m1;
        if (list.get(0).getReturnType() == Long.TYPE) {
            m0 = 0;
            m1 = 1;
        } else {
            m0 = 1;
            m1 = 0;
        }
        assertEquals("--- -1\n", invokerService.invoke(null, ToolTester.class, list.get(m0), "[1, 2]", ""));
        assertEquals("--- 1\n", invokerService.invoke(null, ToolTester.class, list.get(m1), "[1, 2]", ""));
    }

    @Test
    public void testInvoke_NORMAL2_NullArgs() {
        assertEquals("--- 579\n", invokerService.invoke("toolTesterImpl", ToolTester.class.getName(), "toBeInvoked2", 0,
                "[123, 456]", ""));
        assertEquals("--- null\n",
                invokerService.invoke("toolTesterImpl", ToolTester.class.getName(), "toBeInvoked2", 0, null, ""));
    }

    @Test
    public void testInvoke_NORMAL3_MultiMethod() {
        List<Method> list = resolver.resolveMethod(ToolTester.class, "toBeInvoked6");
        int index0 = list.get(0).getReturnType() == Integer.TYPE ? 0 : 1;
        assertEquals("--- 333\n", invokerService.invoke("toolTesterImpl", ToolTester.class.getName(), "toBeInvoked6",
                index0, "[123, 456]", ""));
        int index1 = list.get(0).getReturnType() == Long.TYPE ? 0 : 1;
        assertEquals("--- -333\n", invokerService.invoke("toolTesterImpl", ToolTester.class.getName(), "toBeInvoked6",
                index1, "[123, 456]", ""));
    }

    @Test
    public void testInvoke_NoSuchMethod() throws IOException {
        String result = invokerService.invoke("toolTesterImpl", ToolTester.class.getName(), "noSuchMethod", 0, null,
                "");
        Map<?, ?> map = objectMapper.readValue(result, Map.class);
        assertEquals("java.lang.NoSuchMethodException", map.get("type"));
    }

}
