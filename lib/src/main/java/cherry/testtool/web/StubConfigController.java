/*
 * Copyright 2015,2023 agwlvssainokuni
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

import static cherry.testtool.util.ReflectionUtil.getMethodDescription;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cherry.testtool.reflect.ReflectionResolver;
import cherry.testtool.script.ScriptProcessor;
import cherry.testtool.stub.StubConfig;
import cherry.testtool.stub.StubRepository;
import cherry.testtool.util.ToMapUtil;

@Controller
@ConditionalOnProperty(prefix = "cherry.testtool.web", name = "stubconfig", havingValue = "true", matchIfMissing = true)
@RequestMapping("/testtool/stubconfig")
public class StubConfigController {

	private final StubRepository repository;

	private final ScriptProcessor scriptProcessor;

	private final ReflectionResolver reflectionResolver;

	private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().modules(new JavaTimeModule())
			.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).factory(new YAMLFactory()).build();

	public StubConfigController(
			StubRepository repository,
			ScriptProcessor scriptProcessor,
			ReflectionResolver reflectionResolver) {
		this.repository = repository;
		this.scriptProcessor = scriptProcessor;
		this.reflectionResolver = reflectionResolver;
	}

	@RequestMapping("put")
	@ResponseBody()
	public String putStubConfig(
			@RequestParam("className") String className,
			@RequestParam("methodName") String methodName,
			@RequestParam(value = "methodIndex", defaultValue = "0") int methodIndex,
			@RequestParam("script") String script,
			@RequestParam("engine") String engine) {

		final Optional<Method> methodOpt;
		try {
			methodOpt = reflectionResolver.resolveMethod(className, methodName).stream()
					.skip(methodIndex).findFirst();
		} catch (ClassNotFoundException ex) {
			return ex.getMessage();
		}

		if (methodOpt.isEmpty()) {
			return String.valueOf(false);
		}
		var method = methodOpt.get();

		if (StringUtils.isBlank(script)) {
			repository.clear(method);
			return String.valueOf(true);
		}

		repository.put(method, new StubConfig(script, engine));
		return String.valueOf(true);
	}

	@RequestMapping("get")
	@ResponseBody()
	public List<String> getStubConfig(
			@RequestParam("className") String className,
			@RequestParam("methodName") String methodName,
			@RequestParam(value = "methodIndex", defaultValue = "0") int methodIndex) {

		var list = new ArrayList<String>();

		final Optional<Method> methodOpt;
		try {
			methodOpt = reflectionResolver.resolveMethod(className, methodName).stream()
					.skip(methodIndex).findFirst();
		} catch (ClassNotFoundException ex) {
			list.add("");
			list.add("");
			list.add(ex.getMessage());
			return list;
		}

		if (methodOpt.isEmpty()) {
			list.add("");
			list.add("");
			list.add("");
			return list;
		}
		var method = methodOpt.get();

		var stubConfig = repository.get(method);
		if (stubConfig == null) {
			list.add("");
			list.add("");
			list.add("");
			return list;
		}

		list.add(stubConfig.getScript());
		list.add(stubConfig.getEngine());
		try {
			var result = scriptProcessor.eval(stubConfig.getScript(), stubConfig.getEngine());
			list.add(objectMapper.writeValueAsString(result));
		} catch (ScriptException | JsonProcessingException ex) {
			var map = ToMapUtil.fromThrowable(ex, Integer.MAX_VALUE);
			try {
				list.add(objectMapper.writeValueAsString(map));
			} catch (IOException ex2) {
				list.add(ex.getMessage());
			}
		}
		return list;
	}

	@RequestMapping("bean")
	@ResponseBody()
	public List<String> resolveBeanName(
			@RequestParam("className") String className) {
		try {
			return reflectionResolver.resolveBeanName(className);
		} catch (ClassNotFoundException ex) {
			return new ArrayList<>();
		}
	}

	@RequestMapping("method")
	@ResponseBody()
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

	@RequestMapping("list")
	@ResponseBody()
	public List<String> getStubbedMethod(
			@RequestParam(value = "className") String className) {
		return repository.getStubbedMethod().stream()
				.filter(m -> StringUtils.isEmpty(className) || m.getDeclaringClass().getName().equals(className))
				.map(m -> getMethodDescription(m, false, true, true, true, true)).collect(Collectors.toList());
	}

}
