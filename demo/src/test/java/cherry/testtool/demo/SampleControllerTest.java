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

import cherry.testtool.stub.StubConfig;
import cherry.testtool.stub.StubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link SampleController}経由で、{@link cherry.testtool.demo.aspect.StubAspect}による
 * スタブ介入前後の挙動差が、通常のHTTPリクエスト/レスポンスとして観測できることを検証する
 * (デモの中核的価値の検証)。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SampleControllerTest {

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
    void testStubbed1Int() throws Exception {
        Method method = SampleService.class.getDeclaredMethod("toBeStubbed1", Integer.class, Integer.class);

        // スタブ未登録: 実際の計算結果(1030 + 204 = 1234)
        mockMvc.perform(get("/api/sample/stubbed1/int").param("p1", "1030").param("p2", "204"))
                .andExpect(status().isOk())
                .andExpect(content().string("1234"));

        // スタブ登録後: スタブされた結果
        stubRepository.put(method, new StubConfig("1", ""));
        mockMvc.perform(get("/api/sample/stubbed1/int").param("p1", "1030").param("p2", "204"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        // 解除後: 元の計算結果に戻る
        stubRepository.clear(method);
        mockMvc.perform(get("/api/sample/stubbed1/int").param("p1", "1030").param("p2", "204"))
                .andExpect(status().isOk())
                .andExpect(content().string("1234"));
    }

}
