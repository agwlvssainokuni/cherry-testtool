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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * スタブ設定を保持するインメモリリポジトリ。
 * <p>
 * アプリケーションプロセス内の{@link HashMap}で状態を保持するのみであり、永続化は行わない
 * (アプリケーション再起動でスタブ設定は失われる)。
 */
public class StubRepository {

    private final Map<Method, StubConfig> stubmap = new HashMap<>();

    /**
     * スタブ登録済みの全メソッドを返す。
     *
     * @return 登録済みメソッドの一覧
     */
    public List<Method> getStubbedMethod() {
        return new ArrayList<>(stubmap.keySet());
    }

    /**
     * 指定メソッドにスタブ設定が登録されているかを判定する。
     *
     * @param method 判定対象メソッド
     * @return 登録されていれば{@code true}
     */
    public boolean contains(Method method) {
        return stubmap.containsKey(method);
    }

    /**
     * 指定メソッドのスタブ設定を解除する。
     *
     * @param method 解除対象メソッド
     */
    public void clear(Method method) {
        stubmap.remove(method);
    }

    /**
     * 指定メソッドのスタブ設定を取得する。
     *
     * @param method 取得対象メソッド
     * @return 登録済みのスタブ設定。未登録の場合は{@code null}
     */
    @Nullable
    public StubConfig get(Method method) {
        return stubmap.get(method);
    }

    /**
     * 指定メソッドへスタブ設定を登録(または上書き)する。
     *
     * @param method 登録対象メソッド
     * @param stubConfig 登録するスタブ設定
     */
    public void put(Method method, StubConfig stubConfig) {
        stubmap.put(method, stubConfig);
    }

}
