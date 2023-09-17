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

package cherry.testtool.invoker;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cherry.testtool.reflect.ReflectionResolver;
import cherry.testtool.script.ScriptProcessor;
import cherry.testtool.util.ToMapUtil;

public class InvokerServiceImpl implements InvokerService {

	private final ReflectionResolver reflectionResolver;

	private final ScriptProcessor scriptProcessor;

	private final ConversionService conversionService;

	private ApplicationContext appCtx;

	private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().modules(new JavaTimeModule())
			.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).factory(new YAMLFactory()).build();

	public InvokerServiceImpl(
			ReflectionResolver reflectionResolver,
			ScriptProcessor scriptProcessor,
			ConversionService conversionService,
			ApplicationContext applicationContext) {
		this.reflectionResolver = reflectionResolver;
		this.scriptProcessor = scriptProcessor;
		this.conversionService = conversionService;
		this.appCtx = applicationContext;
	}

	@Override
	public String invoke(String beanName, Class<?> beanClass, Method method, String script, String engine) {
		try {

			Object targetBean;
			if (StringUtils.isEmpty(beanName)) {
				targetBean = appCtx.getBean(beanClass);
			} else {
				targetBean = appCtx.getBean(beanName, beanClass);
			}

			final List<?> argList;
			if (StringUtils.isBlank(script)) {
				argList = Collections.emptyList();
			} else {
				var v = scriptProcessor.eval(script, engine);
				if (v == null) {
					argList = Collections.emptyList();
				} else if (v instanceof List) {
					argList = (List<?>) v;
				} else {
					argList = Collections.singletonList(v);
				}
			}

			var param = new Object[method.getParameterCount()];
			for (int i = 0; i < param.length; i++) {
				if (i >= argList.size()) {
					param[i] = null;
					continue;
				}
				var arg = argList.get(i);
				if (arg == null) {
					param[i] = null;
				} else {
					param[i] = conversionService.convert(
							arg,
							new TypeDescriptor(ResolvableType.forClass(arg.getClass()), null, null),
							new TypeDescriptor(new MethodParameter(method, i)));
				}
			}

			Object result = method.invoke(targetBean, param);
			return objectMapper.writeValueAsString(result);
		} catch (ScriptException | InvocationTargetException | IllegalAccessException | IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public String invoke(String beanName, String className, String methodName, int methodIndex, String script,
			String engine) {
		try {

			var beanClass = getClass().getClassLoader().loadClass(className);
			var methodOpt = reflectionResolver.resolveMethod(beanClass, methodName).stream()
					.skip(methodIndex).findFirst();
			if (methodOpt.isEmpty()) {
				throw new NoSuchMethodException(format("{0}#{1}() not found", className, methodName));
			}

			return invoke(beanName, beanClass, methodOpt.get(), script, engine);
		} catch (ClassNotFoundException | NoSuchMethodException ex) {
			return fromThrowableToString(ex);
		} catch (IllegalStateException ex) {
			if (ex.getCause() instanceof ScriptException
					|| ex.getCause() instanceof InvocationTargetException
					|| ex.getCause() instanceof IllegalAccessException
					|| ex.getCause() instanceof IOException) {
				return fromThrowableToString(ex.getCause());
			} else {
				return fromThrowableToString(ex);
			}
		} catch (Exception ex) {
			return fromThrowableToString(ex);
		}
	}

	private String fromThrowableToString(Throwable ex) {
		var map = ToMapUtil.fromThrowable(ex, Integer.MAX_VALUE);
		try {
			return objectMapper.writeValueAsString(map);
		} catch (IOException ex2) {
			return ex.getMessage();
		}
	}

}
