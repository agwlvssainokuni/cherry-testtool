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
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Optional;

public interface StubResolver {

    @Nonnull
    Optional<StubInvocation> getStubInvocation(@Nonnull Method method);

    @Nonnull
    default Optional<StubInvocation> getStubInvocation(@Nonnull MethodInvocation invocation) {
        return Optional.of(invocation).map(MethodInvocation::getMethod)
                .flatMap(this::getStubInvocation);
    }

    @Nonnull
    default Optional<StubInvocation> getStubInvocation(@Nonnull ProceedingJoinPoint pjp) {
        return Optional.of(pjp).map(ProceedingJoinPoint::getSignature)
                .filter(MethodSignature.class::isInstance).map(MethodSignature.class::cast)
                .map(MethodSignature::getMethod)
                .flatMap(this::getStubInvocation);
    }

}
