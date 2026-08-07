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

/**
 * デモアプリケーションのAspect(スタブ介入・トレース)を提供するパッケージ。
 * <p>
 * このパッケージ配下は{@link org.jspecify.annotations.NullMarked}により、
 * 明示的に{@link org.jspecify.annotations.Nullable}が付与されていない限り
 * 非nullを既定とする(NFR5)。
 */
@NullMarked
package cherry.testtool.demo.aspect;

import org.jspecify.annotations.NullMarked;
