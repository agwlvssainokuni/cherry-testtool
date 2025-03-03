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

package cherry.testtool.web;

import cherry.testtool.invoker.InvokerService;
import cherry.testtool.reflect.ReflectionResolver;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cherry.testtool.util.ReflectionUtil.getMethodDescription;

@RestController
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(prefix = "cherry.testtool.web", name = "invoker", havingValue = "true", matchIfMissing = true)
@RequestMapping("/testtool/invoker")
public class InvokerController {

    private final InvokerService invokerService;

    private final ReflectionResolver reflectionResolver;

    public InvokerController(
            @Nonnull InvokerService invokerService,
            @Nonnull ReflectionResolver reflectionResolver
    ) {
        this.invokerService = invokerService;
        this.reflectionResolver = reflectionResolver;
    }

    @RequestMapping("invoke")
    public String invoke(@RequestParam(value = "beanName", required = false) String beanName,
                         @RequestParam("className") String className,
                         @RequestParam("methodName") String methodName,
                         @RequestParam(value = "methodIndex", defaultValue = "0") int methodIndex,
                         @RequestParam("script") String script,
                         @RequestParam(value = "engine", defaultValue = "") String engine) {
        return invokerService.invoke(beanName, className, methodName, methodIndex, script, engine);
    }

    @RequestMapping("bean")
    public List<String> resolveBeanName(
            @RequestParam("className") String className) {
        try {
            return reflectionResolver.resolveBeanName(className);
        } catch (ClassNotFoundException ex) {
            return new ArrayList<>();
        }
    }

    @RequestMapping("method")
    public List<String> resolveMethod(
            @RequestParam("className") String className,
            @RequestParam("methodName") String methodName) {
        try {
            return reflectionResolver.resolveMethod(className, methodName).stream()
                    .map(m -> getMethodDescription(m, false, false, false, true, false)).collect(Collectors.toList());
        } catch (ClassNotFoundException ex) {
            return new ArrayList<>();
        }
    }

}
