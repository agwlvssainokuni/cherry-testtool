/*
 * Copyright 2015,2026 agwlvssainokuni
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

package cherry.testtool.stub;

import org.jspecify.annotations.Nullable;

/**
 * スタブ設定に基づく実行を表す関数型インタフェース。
 * <p>
 * {@link StubResolver}が解決し、{@link StubInterceptor}等の呼出し元が実行する。
 */
@FunctionalInterface
public interface StubInvocation {

    /**
     * スタブとしての実行を行う。
     *
     * @param args 元のメソッド呼出しの引数
     * @return スタブとしての戻り値
     * @throws Throwable スタブ実行(スクリプト評価)中に発生した例外
     */
    @Nullable
    Object invoke(@Nullable Object[] args) throws Throwable;

}
