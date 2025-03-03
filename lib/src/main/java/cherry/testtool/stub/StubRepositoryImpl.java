/*
 * Copyright 2015,2025 agwlvssainokuni
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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StubRepositoryImpl implements StubRepository {

    private final Map<Method, StubConfig> stubmap = new HashMap<>();

    @Nonnull
    @Override
    public List<Method> getStubbedMethod() {
        return new ArrayList<>(stubmap.keySet());
    }

    @Override
    public boolean contains(@Nonnull Method method) {
        return stubmap.containsKey(method);
    }

    @Override
    public void clear(@Nonnull Method method) {
        stubmap.remove(method);
    }

    @Nullable
    @Override
    public StubConfig get(@Nonnull Method method) {
        return stubmap.get(method);
    }

    @Override
    public void put(@Nonnull Method method, @Nonnull StubConfig stubConfig) {
        stubmap.put(method, stubConfig);
    }

}
