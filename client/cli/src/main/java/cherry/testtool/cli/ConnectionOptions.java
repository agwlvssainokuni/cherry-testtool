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

package cherry.testtool.cli;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.List;

/**
 * {@link RootCommand}で解析される、全サブコマンド共通の接続情報。
 *
 * @param baseUrl    接続先ベースURL
 * @param basicAuth  {@code user:pass}形式。{@code null}の場合はBASIC認証ヘッダを付加しない
 * @param headers    {@code Name: Value}形式の生ヘッダ文字列(複数可)
 */
public record ConnectionOptions(
        URI baseUrl,
        @Nullable String basicAuth,
        List<String> headers
) {
}
