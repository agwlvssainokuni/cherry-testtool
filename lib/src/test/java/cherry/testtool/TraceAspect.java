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

package cherry.testtool;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * メソッド呼出しのトレースログを出力するAspect(旧{@code appctx-trace.xml}のアノテーションベース版)。
 * <p>
 * {@link CustomizableTraceInterceptor}(AOP Alliance{@link MethodInvocation})を、AspectJの
 * {@link ProceedingJoinPoint}から呼び出せるようブリッジする。
 */
@Order(100)
@Aspect
@Component
public class TraceAspect {

    private final CustomizableTraceInterceptor traceInterceptor;

    public TraceAspect(
            @Value("${trace.useDynamicLogger:true}") boolean useDynamicLogger,
            @Value("${trace.hideProxyClassNames:true}") boolean hideProxyClassNames,
            @Value("${trace.logExceptionStackTrace:true}") boolean logExceptionStackTrace,
            @Value("${trace.enterMessage:ENTER $[targetClassShortName]#$[methodName]($[arguments])}") String enterMessage,
            @Value("${trace.exitMessage:EXIT  $[targetClassShortName]#$[methodName](): $[returnValue]}") String exitMessage,
            @Value("${trace.exceptionMessage:EXCEPTION $[targetClassShortName]#$[methodName](): $[exception]}") String exceptionMessage
    ) {
        traceInterceptor = new CustomizableTraceInterceptor();
        traceInterceptor.setUseDynamicLogger(useDynamicLogger);
        traceInterceptor.setHideProxyClassNames(hideProxyClassNames);
        traceInterceptor.setLogExceptionStackTrace(logExceptionStackTrace);
        traceInterceptor.setEnterMessage(enterMessage);
        traceInterceptor.setExitMessage(exitMessage);
        traceInterceptor.setExceptionMessage(exceptionMessage);
    }

    @Around("""
            execution(* cherry..*.*(..))
            """)
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        return traceInterceptor.invoke(
                new ProceedingJoinPointMethodInvocation(joinPoint)
        );
    }

    static class ProceedingJoinPointMethodInvocation implements MethodInvocation {
        private final ProceedingJoinPoint joinPoint;

        ProceedingJoinPointMethodInvocation(ProceedingJoinPoint joinPoint) {
            this.joinPoint = joinPoint;
        }

        @Override
        public Object proceed() throws Throwable {
            return joinPoint.proceed();
        }

        @Override
        public Object[] getArguments() {
            return joinPoint.getArgs();
        }

        @Override
        public Object getThis() {
            return joinPoint.getThis();
        }

        @Override
        public Method getMethod() {
            return Optional.of(joinPoint).map(ProceedingJoinPoint::getSignature)
                    .filter(MethodSignature.class::isInstance).map(MethodSignature.class::cast)
                    .map(MethodSignature::getMethod)
                    .get();
        }

        @Override
        public AccessibleObject getStaticPart() {
            return getMethod();
        }
    }

}
