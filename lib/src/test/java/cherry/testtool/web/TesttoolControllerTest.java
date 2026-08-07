/*
 * Copyright 2026 agwlvssainokuni
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link TesttoolController}のWeb層単体テスト。
 * <p>
 * 各エンドポイントが対応するサービス(具象クラス)へ正しく委譲すること、
 * および統合後の共通パス({@code /testtool/resolve/**})が機能することを検証する(FR8)。
 */
@WebMvcTest(TesttoolController.class)
class TesttoolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvokerService invokerService;

    @MockitoBean
    private ReflectionResolver reflectionResolver;

    @MockitoBean
    private StubRepository stubRepository;

    @MockitoBean
    private ScriptProcessor scriptProcessor;

    private static Method dummyMethod() throws NoSuchMethodException {
        return Object.class.getMethod("toString");
    }

    @Test
    void testInvoke() throws Exception {
        when(invokerService.invoke(eq("bean"), eq("Foo"), eq("bar"), eq(0), eq("[]"), eq("")))
                .thenReturn("--- ok\n");

        mockMvc.perform(post("/testtool/invoker/invoke")
                        .param("beanName", "bean")
                        .param("className", "Foo")
                        .param("methodName", "bar")
                        .param("script", "[]"))
                .andExpect(status().isOk())
                .andExpect(content().string("--- ok\n"));
    }

    @Test
    void testPutStubConfig_register() throws Exception {
        var method = dummyMethod();
        when(reflectionResolver.resolveMethod("Foo", "bar")).thenReturn(List.of(method));

        mockMvc.perform(post("/testtool/stubconfig/put")
                        .param("className", "Foo")
                        .param("methodName", "bar")
                        .param("script", "1")
                        .param("engine", ""))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(stubRepository).put(eq(method), any(StubConfig.class));
    }

    @Test
    void testPutStubConfig_clear() throws Exception {
        var method = dummyMethod();
        when(reflectionResolver.resolveMethod("Foo", "bar")).thenReturn(List.of(method));

        mockMvc.perform(post("/testtool/stubconfig/put")
                        .param("className", "Foo")
                        .param("methodName", "bar")
                        .param("script", "")
                        .param("engine", ""))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(stubRepository).clear(method);
    }

    @Test
    void testGetStubConfig() throws Exception {
        var method = dummyMethod();
        when(reflectionResolver.resolveMethod("Foo", "bar")).thenReturn(List.of(method));
        when(stubRepository.get(method)).thenReturn(new StubConfig("1", ""));
        when(scriptProcessor.eval("1", "")).thenReturn(1);

        mockMvc.perform(post("/testtool/stubconfig/get")
                        .param("className", "Foo")
                        .param("methodName", "bar"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStubbedMethod() throws Exception {
        when(stubRepository.getStubbedMethod()).thenReturn(List.of(dummyMethod()));

        mockMvc.perform(post("/testtool/stubconfig/list")
                        .param("className", ""))
                .andExpect(status().isOk());
    }

    @Test
    void testResolveBeanName() throws Exception {
        when(reflectionResolver.resolveBeanName("Foo")).thenReturn(List.of("fooBean"));

        mockMvc.perform(post("/testtool/resolve/bean")
                        .param("className", "Foo"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"fooBean\"]"));
    }

    @Test
    void testResolveMethod() throws Exception {
        when(reflectionResolver.resolveMethod("Foo", "bar")).thenReturn(List.of(dummyMethod()));

        mockMvc.perform(post("/testtool/resolve/method")
                        .param("className", "Foo")
                        .param("methodName", "bar"))
                .andExpect(status().isOk());
    }

}
