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

package cherry.testtool.demo;

import cherry.testtool.stub.StubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code demo.stub-loader.enabled=true}のとき、起動時に{@code stub-samples/}配下のスタブ設定が
 * {@link StubAutoLoadRunner}により自動登録されることを検証する。
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "demo.stub-loader.enabled=true")
class StubAutoLoadRunnerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StubRepository stubRepository;

    @AfterEach
    void after() {
        for (Method m : stubRepository.getStubbedMethod()) {
            stubRepository.clear(m);
        }
    }

    @Test
    void testAutoLoadedStub() throws Exception {
        // stub-samples/cherry.testtool.demo.SampleService/toBeStubbed1.1.js(Integer版)が
        // 起動時に自動登録され、実際の計算結果ではなくスタブ値(9999)が返る
        mockMvc.perform(get("/api/sample/stubbed1/int").param("p1", "1030").param("p2", "204"))
                .andExpect(status().isOk())
                .andExpect(content().string("9999"));
    }

}
