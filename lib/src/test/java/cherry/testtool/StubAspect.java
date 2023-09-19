/*
 * Copyright 2021,2023 agwlvssainokuni
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

package cherry.testtool;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import cherry.testtool.stub.StubResolver;

@Order(200)
@Aspect
@Component
public class StubAspect {

    private final StubResolver stubResolver;

    public StubAspect(StubResolver stubResolver) {
        this.stubResolver = stubResolver;
    }

    @Around("""
            execution(* cherry.testtool.ToolTester.*(..))
            """)
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        var stubOpt = stubResolver.getStubInvocation(pjp);
        if (stubOpt.isPresent()) {
            return stubOpt.get().invoke(pjp.getArgs());
        }
        return pjp.proceed();
    }

}
