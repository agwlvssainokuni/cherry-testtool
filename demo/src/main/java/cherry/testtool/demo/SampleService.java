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

package cherry.testtool.demo;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 実アプリが持つであろうサンプル業務サービス。
 * <p>
 * {@code lib}の{@code InvokerService}によるリフレクション呼出し(様々な引数パターンの
 * {@code toBeInvoked*}メソッド)、および{@link cherry.testtool.demo.aspect.StubAspect}による
 * スタブ介入(スタブ対象の{@code toBeStubbed*}メソッド、{@link SampleController}経由で
 * 通常のREST APIとしても観測できる)の両方を示すために用意している。
 */
@Component
public class SampleService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void toBeInvoked0() {
        log.debug("method0");
    }

    public long toBeInvoked1(long a, long b) {
        log.debug("method1");
        return a + b;
    }

    public Long toBeInvoked2(Long a, Long b) {
        log.debug("method2");
        return submethod(a, b);
    }

    public LocalDateTime toBeInvoked3(LocalDate dt, LocalTime tm) {
        log.debug("method3");
        return dt.atTime(tm);
    }

    public Dto1 toBeInvoked4(Dto1 a, Dto1 b) {
        log.debug("method4");
        return submethod(a, b);
    }

    public Dto2 toBeInvoked5(Dto2 a, Dto2 b) {
        log.debug("method5");
        return new Dto2(
                submethod(a.val1(), b.val1()),
                submethod(a.val2(), b.val2())
        );
    }

    public long toBeInvoked6(long a, long b) {
        return a - b;
    }

    public int toBeInvoked6(int a, int b) {
        return b - a;
    }

    /**
     * {@link SampleController}経由でスタブ介入前後の挙動差を観測できるサンプルメソッド(Integer版)。
     */
    public Integer toBeStubbed1(Integer p1, Integer p2) {
        return p1 + p2;
    }

    /**
     * {@link SampleController}経由でスタブ介入前後の挙動差を観測できるサンプルメソッド(BigDecimal版)。
     */
    public BigDecimal toBeStubbed1(BigDecimal p1, BigDecimal p2) {
        return p1.add(p2);
    }

    /**
     * {@link SampleController}経由でスタブ介入前後の挙動差を観測できるサンプルメソッド。
     */
    public LocalDateTime toBeStubbed2(LocalDate p1, LocalTime p2) {
        return p1.atTime(p2);
    }

    private @Nullable Long submethod(@Nullable Long a, @Nullable Long b) {
        if (a == null || b == null) {
            return null;
        }
        return a + b;
    }

    private Dto1 submethod(Dto1 a, Dto1 b) {
        return new Dto1(
                submethod(a.val1(), b.val1()),
                submethod(a.val2(), b.val2()));
    }

    public record Dto1(@Nullable Long val1, @Nullable Long val2) {
    }

    public record Dto2(Dto1 val1, Dto1 val2) {
    }

}
