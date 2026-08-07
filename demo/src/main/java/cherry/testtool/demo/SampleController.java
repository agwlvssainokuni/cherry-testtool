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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * {@link SampleService}の{@code toBeStubbed}系メソッドを、実アプリらしい通常のREST APIとして公開する。
 * <p>
 * {@code cherry-testtool}自身のAPI({@code /testtool/**})とは独立した、一般的な業務APIの体裁を取ることで、
 * {@link cherry.testtool.demo.aspect.StubAspect}によるスタブ介入が、通常のHTTPリクエスト/レスポンスの
 * 変化として外部から観測できることを示す。
 */
@RestController
@RequestMapping("/api/sample")
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    /**
     * {@link SampleService#toBeStubbed1(Integer, Integer)}を呼び出す。
     */
    @GetMapping("/stubbed1/int")
    public Integer stubbed1Int(
            @RequestParam("p1") Integer p1,
            @RequestParam("p2") Integer p2) {
        return sampleService.toBeStubbed1(p1, p2);
    }

    /**
     * {@link SampleService#toBeStubbed1(BigDecimal, BigDecimal)}を呼び出す。
     */
    @GetMapping("/stubbed1/decimal")
    public BigDecimal stubbed1Decimal(
            @RequestParam("p1") BigDecimal p1,
            @RequestParam("p2") BigDecimal p2) {
        return sampleService.toBeStubbed1(p1, p2);
    }

    /**
     * {@link SampleService#toBeStubbed2(LocalDate, LocalTime)}を呼び出す。
     */
    @GetMapping("/stubbed2")
    public LocalDateTime stubbed2(
            @RequestParam("p1") LocalDate p1,
            @RequestParam("p2") LocalTime p2) {
        return sampleService.toBeStubbed2(p1, p2);
    }

}
