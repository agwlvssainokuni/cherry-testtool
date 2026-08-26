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

package cherry.testtool.webconsole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code cherry.testtool.web.auth.username}/{@code password}が未設定の場合、
 * {@link WebSecurityConfig}が認証を有効化せず、既存動作(認証なし)を維持することを検証する。
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigAuthDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testNoAuthHeader_AccessibleWhenCredentialsNotConfigured() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

}
