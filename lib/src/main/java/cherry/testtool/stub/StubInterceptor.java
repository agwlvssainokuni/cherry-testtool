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
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class StubInterceptor implements MethodInterceptor {

    private final StubResolver stubResolver;

    public StubInterceptor(
            @Nonnull StubResolver stubResolver
    ) {
        this.stubResolver = stubResolver;
    }

    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        var stubOpt = stubResolver.getStubInvocation(invocation);
        if (stubOpt.isPresent()) {
            return stubOpt.get().invoke(invocation.getArguments());
        }
        return invocation.proceed();
    }

}
