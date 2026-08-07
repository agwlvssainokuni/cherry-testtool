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

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@code cherry.testtool.web}パッケージのテスト用メイン設定クラス。
 * <p>
 * {@link TesttoolControllerTest}で{@code @WebMvcTest}を使う際、Spring Bootの
 * メイン設定クラス自動探索が同一パッケージ内で完結するようにするための最小限のクラス。
 * {@code @WebMvcTest}と{@code @SpringBootApplication}は同一クラスへ同時に付与できないため
 * 分離している。
 */
@SpringBootApplication
class TestApplication {
}
