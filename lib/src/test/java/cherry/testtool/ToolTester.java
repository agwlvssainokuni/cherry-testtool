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

import jakarta.annotation.Nonnull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ToolTester {

    void toBeInvoked0();

    long toBeInvoked1(long a, long b);

    @Nonnull
    Long toBeInvoked2(@Nonnull Long a, @Nonnull Long b);

    @Nonnull
    LocalDateTime toBeInvoked3(@Nonnull LocalDate dt, @Nonnull LocalTime tm);

    @Nonnull
    Dto1 toBeInvoked4(@Nonnull Dto1 a, @Nonnull Dto1 b);

    @Nonnull
    Dto2 toBeInvoked5(@Nonnull Dto2 a, @Nonnull Dto2 b);

    long toBeInvoked6(long a, long b);

    int toBeInvoked6(int a, int b);

    @Nonnull
    Integer toBeStubbed1(@Nonnull Integer p1, @Nonnull Integer p2);

    @Nonnull
    BigDecimal toBeStubbed1(@Nonnull BigDecimal p1, @Nonnull BigDecimal p2);

    @Nonnull
    LocalDateTime toBeStubbed2(@Nonnull LocalDate p1, @Nonnull LocalTime p2);

    record Dto1(Long val1, Long val2) {
    }

    record Dto2(Dto1 val1, Dto1 val2) {
    }

}
