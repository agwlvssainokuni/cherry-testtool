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

package cherry.testtool.stub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cherry.testtool.TesttoolConfiguration;
import cherry.testtool.ToolTester;
import cherry.testtool.ToolTesterImpl;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { TesttoolConfiguration.class, ToolTesterImpl.class })
@SpringBootApplication()
@ImportResource(locations = { "classpath:spring/appctx-trace.xml", "classpath:spring/appctx-stub.xml" })
public class StubInterceptorTest {

	@Autowired
	private StubRepository repository;

	@Autowired
	private ToolTester tester;

	@AfterEach
	public void after() {
		for (Method m : repository.getStubbedMethod()) {
			repository.clear(m);
		}
	}

	@Test
	public void testMethodInt_RETURN() throws NoSuchMethodException {
		Method method = ToolTester.class.getDeclaredMethod("toBeStubbed1", Integer.class, Integer.class);

		assertEquals(Integer.valueOf(1234), tester.toBeStubbed1(1030, 204));

		repository.put(method, new StubConfig("1", ""));
		assertEquals(Integer.valueOf(1), tester.toBeStubbed1(1030, 204));
		assertEquals(Integer.valueOf(1), tester.toBeStubbed1(1030, 204));

		repository.clear(method);
		assertEquals(Integer.valueOf(1234), tester.toBeStubbed1(1030, 204));
		assertEquals(Integer.valueOf(1234), tester.toBeStubbed1(1030, 204));
	}

	@Test
	public void testMethodBigDecimal_THROWABLE() throws NoSuchMethodException {
		Method method = ToolTester.class.getDeclaredMethod("toBeStubbed1", BigDecimal.class, BigDecimal.class);

		assertEquals(BigDecimal.valueOf(1234L),
				tester.toBeStubbed1(BigDecimal.valueOf(1030L), BigDecimal.valueOf(204L)));

		repository.put(method, new StubConfig(
				"const IllegalArgumentException = Java.type('java.lang.IllegalArgumentException'); throw new IllegalArgumentException();",
				""));
		try {
			tester.toBeStubbed1(BigDecimal.valueOf(1030L), BigDecimal.valueOf(204L));
			fail("Exception must be thrown");
		} catch (IllegalArgumentException ex) {
			// OK
		}

		repository.clear(method);
		assertEquals(BigDecimal.valueOf(1234L),
				tester.toBeStubbed1(BigDecimal.valueOf(1030L), BigDecimal.valueOf(204L)));
	}

}
