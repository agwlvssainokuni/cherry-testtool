/*
 * Copyright 2016,2023 agwlvssainokuni
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

package cherry.testtool.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ReflectionUtilTest {

	@Test
	public void getFieldByMark() {

		Optional<Field> aaa = ReflectionUtil.getFieldByMark(A.class, 0);
		assertTrue(aaa.isPresent());
		assertEquals("aaa", aaa.get().getName());
		assertEquals(0, aaa.get().getModifiers());

		Optional<Field> bbb = ReflectionUtil.getFieldByMark(A.class, 1);
		assertTrue(bbb.isPresent());
		assertEquals("bbb", bbb.get().getName());
		assertTrue(Modifier.isPrivate(bbb.get().getModifiers()));

		Optional<Field> ccc = ReflectionUtil.getFieldByMark(A.class, 2);
		assertTrue(ccc.isPresent());
		assertEquals("ccc", ccc.get().getName());
		assertTrue(Modifier.isProtected(ccc.get().getModifiers()));

		Optional<Field> ddd = ReflectionUtil.getFieldByMark(A.class, 3);
		assertTrue(ddd.isPresent());
		assertEquals("ddd", ddd.get().getName());
		assertTrue(Modifier.isPublic(ddd.get().getModifiers()));

		Optional<Field> eee = ReflectionUtil.getFieldByMark(A.class, 4);
		assertFalse(eee.isPresent());
	}

	@Test
	public void getMethodByMark() {

		Optional<Method> method0 = ReflectionUtil.getMethodByMark(A.class, 0);
		assertTrue(method0.isPresent());
		assertEquals("method0", method0.get().getName());
		assertEquals(0, method0.get().getModifiers());

		Optional<Method> method1 = ReflectionUtil.getMethodByMark(A.class, 1);
		assertTrue(method1.isPresent());
		assertEquals("method1", method1.get().getName());
		assertTrue(Modifier.isPrivate(method1.get().getModifiers()));

		Optional<Method> method2 = ReflectionUtil.getMethodByMark(A.class, 2);
		assertTrue(method2.isPresent());
		assertEquals("method2", method2.get().getName());
		assertTrue(Modifier.isProtected(method2.get().getModifiers()));

		Optional<Method> method3 = ReflectionUtil.getMethodByMark(A.class, 3);
		assertTrue(method3.isPresent());
		assertEquals("method3", method3.get().getName());
		assertTrue(Modifier.isPublic(method3.get().getModifiers()));

		Optional<Method> method4 = ReflectionUtil.getMethodByMark(A.class, 4);
		assertFalse(method4.isPresent());
	}

	public static class A {

		@ReflectionMark(0)
		int aaa;
		@ReflectionMark(1)
		private int bbb;
		@ReflectionMark(2)
		protected int ccc;
		@ReflectionMark(3)
		public int ddd;

		@ReflectionMark(0)
		int method0(int a) {
			return a;
		}

		@ReflectionMark(1)
		private int method1(int a) {
			return a;
		}

		@ReflectionMark(2)
		protected int method2(int a) {
			return a;
		}

		@ReflectionMark(3)
		public int method3(int a) {
			return a;
		}
	}

}
