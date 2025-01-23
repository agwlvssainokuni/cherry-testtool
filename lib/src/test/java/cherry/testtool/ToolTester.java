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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ToolTester {

    void toBeInvoked0();

    long toBeInvoked1(long a, long b);

    Long toBeInvoked2(Long a, Long b);

    LocalDateTime toBeInvoked3(LocalDate dt, LocalTime tm);

    Dto1 toBeInvoked4(Dto1 a, Dto1 b);

    Dto2 toBeInvoked5(Dto2 a, Dto2 b);

    long toBeInvoked6(long a, long b);

    int toBeInvoked6(int a, int b);

    Integer toBeStubbed1(Integer p1, Integer p2);

    BigDecimal toBeStubbed1(BigDecimal p1, BigDecimal p2);

    LocalDateTime toBeStubbed2(LocalDate p1, LocalTime p2);

    public static record Dto1(Long val1, Long val2) {
    }

    public static record Dto2(Dto1 val1, Dto1 val2) {
    }

}
