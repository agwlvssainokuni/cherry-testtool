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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
public class StubRepositoryTest {

	@Autowired
	private StubRepository repository;

	private Method method;

	@BeforeEach
	public void before() throws NoSuchMethodException {
		method = ToolTester.class.getDeclaredMethod("toBeStubbed1", Integer.class, Integer.class);
	}

	@AfterEach
	public void after() {
		for (Method m : repository.getStubbedMethod()) {
			repository.clear(m);
		}
	}

	@Test
	public void test() throws NoSuchMethodException {
		// 事前：定義なし
		assertFalse(repository.contains(method));
		assertTrue(repository.getStubbedMethod().isEmpty());
		// 実行
		repository.put(method, new StubConfig("123", ""));
		// 検証：定義あり
		assertTrue(repository.contains(method));
		assertNotNull(repository.get(method));
		assertEquals(1, repository.getStubbedMethod().size());
		assertEquals(method, repository.getStubbedMethod().get(0));
		// 実行
		repository.clear(method);
		// 検証：定義なし
		assertFalse(repository.contains(method));
		assertTrue(repository.getStubbedMethod().isEmpty());
	}

}
