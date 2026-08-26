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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * webconsole全体(SPA配信・{@code /testtool/**}含む全パス)へのBasic認証設定。
 * <p>
 * {@code cherry.testtool.web.auth.users}が1件以上設定されている場合のみ認証を有効化する。
 * 既存のAPIキー保護({@code cherry.testtool.web.api-key}、{@code lib}の{@code ApiKeyFilter})と同じく、
 * 未設定時は現状通り認証なしで動作する(後方互換)。
 */
@Configuration
@EnableConfigurationProperties(WebAuthProperties.class)
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            WebAuthProperties properties
    ) throws Exception {
        if (!properties.users().isEmpty()) {
            var userDetails = properties.users().stream()
                    .map(u -> (UserDetails) User.withUsername(u.username()).password(u.password()).roles("USER").build())
                    .toList();
            var userDetailsManager = new InMemoryUserDetailsManager(userDetails);
            var provider = new DaoAuthenticationProvider(userDetailsManager);
            provider.setPasswordEncoder(passwordEncoder());
            http
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .csrf(AbstractHttpConfigurer::disable)
                    .authenticationProvider(provider);
        } else {
            http
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(AbstractHttpConfigurer::disable);
        }
        return http.build();
    }

    /**
     * {@code cherry.testtool.web.auth.users[].password}の値をそのまま(エンコードせず){@link org.springframework.security.core.userdetails.UserDetails}へ
     * 保持するため、標準の{@link DelegatingPasswordEncoder}に、プレフィックス({@code {bcrypt}}等)無しの値を
     * 平文として定数時間比較する{@link PasswordEncoder}をデフォルトマッチとして組み込む。
     * これにより、設定値がプレフィックス無しなら平文比較、{@code {bcrypt}}プレフィックス付きならBCrypt照合、という
     * 両対応を実現する(非推奨の{@code NoOpPasswordEncoder}は使用しない)。
     */
    private PasswordEncoder passwordEncoder() {
        var delegating = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        delegating.setDefaultPasswordEncoderForMatches(new PlainTextPasswordEncoder());
        return delegating;
    }

    /**
     * プレフィックス無しの設定値を平文として扱う{@link PasswordEncoder}。
     * タイミング攻撃を避けるため、{@link MessageDigest#isEqual(byte[], byte[])}による定数時間比較で照合する。
     */
    private static final class PlainTextPasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            var rawBytes = rawPassword.toString().getBytes(StandardCharsets.UTF_8);
            var encodedBytes = encodedPassword.getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(rawBytes, encodedBytes);
        }

    }

}
