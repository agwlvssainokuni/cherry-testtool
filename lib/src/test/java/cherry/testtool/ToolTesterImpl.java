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

package cherry.testtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class ToolTesterImpl implements ToolTester {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void toBeInvoked0() {
        log.debug("method0");
    }

    @Override
    public long toBeInvoked1(long a, long b) {
        log.debug("method1");
        return a + b;
    }

    @Override
    public Long toBeInvoked2(Long a, Long b) {
        log.debug("method2");
        return submethod(a, b);
    }

    @Override
    public LocalDateTime toBeInvoked3(LocalDate dt, LocalTime tm) {
        log.debug("method3");
        return dt.atTime(tm);
    }

    @Override
    public Dto1 toBeInvoked4(Dto1 a, Dto1 b) {
        log.debug("method4");
        return submethod(a, b);
    }

    @Override
    public Dto2 toBeInvoked5(Dto2 a, Dto2 b) {
        log.debug("method5");
        return new Dto2(
                submethod(a.val1(), b.val1()),
                submethod(a.val2(), b.val2())
        );
    }

    @Override
    public long toBeInvoked6(long a, long b) {
        return a - b;
    }

    @Override
    public int toBeInvoked6(int a, int b) {
        return b - a;
    }

    @Override
    public Integer toBeStubbed1(Integer p1, Integer p2) {
        return Integer.valueOf(p1.intValue() + p2.intValue());
    }

    @Override
    public BigDecimal toBeStubbed1(BigDecimal p1, BigDecimal p2) {
        return p1.add(p2);
    }

    @Override
    public LocalDateTime toBeStubbed2(LocalDate p1, LocalTime p2) {
        return p1.atTime(p2);
    }

    private Long submethod(Long a, Long b) {
        if (a == null || b == null) {
            return null;
        }
        return Long.valueOf(a.longValue() + b.longValue());
    }

    private Dto1 submethod(Dto1 a, Dto1 b) {
        return new Dto1(
                submethod(a.val1(), b.val1()),
                submethod(a.val2(), b.val2()));
    }

}
