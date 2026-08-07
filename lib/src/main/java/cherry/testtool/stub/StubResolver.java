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

import cherry.testtool.script.ScriptProcessor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * メソッド呼出しを、登録済みスタブ設定に基づく{@link StubInvocation}へ解決するサービス。
 * <p>
 * AOP Alliance({@link MethodInvocation})・AspectJ({@link ProceedingJoinPoint})いずれの
 * 呼出し表現からも解決できるよう、{@link Method}を起点とするオーバーロードを提供する。
 */
public class StubResolver {

    private final StubRepository repository;

    private final ScriptProcessor scriptProcessor;

    public StubResolver(
            StubRepository repository,
            ScriptProcessor scriptProcessor
    ) {
        this.repository = repository;
        this.scriptProcessor = scriptProcessor;
    }

    /**
     * 指定メソッドにスタブ設定が登録されていれば、スタブ実行({@link StubInvocation})へ解決する。
     *
     * @param method 解決対象メソッド
     * @return スタブ設定が登録されていれば{@link StubInvocation}を含む{@link Optional}、未登録なら空
     */
    public Optional<StubInvocation> getStubInvocation(Method method) {
        return Optional.of(method).filter(repository::contains).map(repository::get)
                .map(stub -> args -> {
                    var script = stub.script();
                    var engine = stub.engine();
                    try {
                        return scriptProcessor.eval(script, engine, args);
                    } catch (ScriptException ex) {
                        if (ex.getCause() != null) {
                            throw ex.getCause();
                        }
                        throw ex;
                    }
                });
    }

    /**
     * AOP Alliance{@link MethodInvocation}から対象メソッドを取り出し{@link #getStubInvocation(Method)}に委譲する。
     *
     * @param invocation AOP Allianceのメソッド呼出し
     * @return {@link #getStubInvocation(Method)}と同様
     */
    public Optional<StubInvocation> getStubInvocation(MethodInvocation invocation) {
        return Optional.of(invocation).map(MethodInvocation::getMethod)
                .flatMap(this::getStubInvocation);
    }

    /**
     * AspectJ{@link ProceedingJoinPoint}から対象メソッドを取り出し{@link #getStubInvocation(Method)}に委譲する。
     *
     * @param pjp AspectJの呼出しジョインポイント
     * @return {@link #getStubInvocation(Method)}と同様。シグネチャがメソッドシグネチャでない場合は空
     */
    public Optional<StubInvocation> getStubInvocation(ProceedingJoinPoint pjp) {
        return Optional.of(pjp).map(ProceedingJoinPoint::getSignature)
                .filter(MethodSignature.class::isInstance).map(MethodSignature.class::cast)
                .map(MethodSignature::getMethod)
                .flatMap(this::getStubInvocation);
    }

}
