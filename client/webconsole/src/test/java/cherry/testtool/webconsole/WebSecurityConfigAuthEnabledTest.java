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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code cherry.testtool.web.auth.users}が設定されている場合、
 * {@link WebSecurityConfig}がBasic認証を有効化し、認証ヘッダ無し・誤ったパスワードを拒否、
 * 正しい認証情報のみを許可することを検証する。複数ユーザーを登録し、いずれの認証情報でも
 * アクセスできることも合わせて検証する。
 */
@SpringBootTest(properties = {
        "cherry.testtool.web.auth.users[0].username=testuser",
        "cherry.testtool.web.auth.users[0].password=testpass",
        "cherry.testtool.web.auth.users[1].username=seconduser",
        "cherry.testtool.web.auth.users[1].password=secondpass",
})
@AutoConfigureMockMvc
class WebSecurityConfigAuthEnabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testNoAuthHeader_Returns401() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCorrectCredentials_ReturnsOk() throws Exception {
        mockMvc.perform(get("/").header("Authorization", basicAuthHeader("testuser", "testpass")))
                .andExpect(status().isOk());
    }

    @Test
    void testSecondUserCredentials_ReturnsOk() throws Exception {
        mockMvc.perform(get("/").header("Authorization", basicAuthHeader("seconduser", "secondpass")))
                .andExpect(status().isOk());
    }

    @Test
    void testWrongPassword_Returns401() throws Exception {
        mockMvc.perform(get("/").header("Authorization", basicAuthHeader("testuser", "wrongpass")))
                .andExpect(status().isUnauthorized());
    }

    private String basicAuthHeader(String username, String password) {
        var credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

}
