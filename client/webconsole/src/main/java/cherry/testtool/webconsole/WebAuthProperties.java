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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * webconsoleのBasic認証({@link WebSecurityConfig})向けユーザー一覧設定。
 * {@code users}が空の場合は認証を無効化する(既存動作維持、後方互換)。
 */
@ConfigurationProperties(prefix = "cherry.testtool.web.auth")
public record WebAuthProperties(List<UserEntry> users) {

    public WebAuthProperties {
        if (users == null) {
            users = List.of();
        }
    }

    /**
     * @param password 平文、または{@code {bcrypt}}プレフィックス付きのBCryptハッシュ
     */
    public record UserEntry(String username, String password) {
    }

}
