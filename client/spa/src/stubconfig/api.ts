/*
 * Copyright 2021,2023 agwlvssainokuni
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

import { uri } from "../common";

export { getStub, getStubbedMethod, putStub, resolveBeanName, resolveMethod };

type PutStubType = (c: string, m: string, i: string, s: string, e: string) => Promise<string>;
const putStub: PutStubType = ((action: string) => {
	return async (className: string, methodName: string, methodIndex: string, script: string, engine: string) => {
		let response = await fetch(action, {
			method: "POST",
			body: new URLSearchParams({
				className: className,
				methodName: methodName,
				methodIndex: methodIndex,
				script: script,
				engine: engine,
			}),
		});
		let result = await response.text();
		return result as string;
	}
})(uri("/stubconfig/put"));

type GetStubType = (c: string, m: string, i: string) => Promise<JSON>;
const getStub: GetStubType = ((action: string) => {
	return async (className: string, methodName: string, methodIndex: string) => {
		let response = await fetch(action, {
			method: "POST",
			body: new URLSearchParams({
				className: className,
				methodName: methodName,
				methodIndex: methodIndex,
			}),
		});
		let result = await response.json();
		return result as JSON;
	}
})(uri("/stubconfig/get"));

type ResolveBeanNameType = (c: string) => Promise<JSON>
const resolveBeanName: ResolveBeanNameType = ((action: string) => {
	return async (className: string) => {
		let response = await fetch(action, {
			method: "POST",
			body: new URLSearchParams({
				className: className,
			}),
		});
		let result = await response.json();
		return result as JSON;
	}
})(uri("/stubconfig/bean"));

type ResolveMethodType = (c: string, m: string) => Promise<JSON>;
const resolveMethod: ResolveMethodType = ((action: string) => {
	return async (className: string, methodName: string) => {
		let response = await fetch(action, {
			method: "POST",
			body: new URLSearchParams({
				className: className,
				methodName: methodName,
			}),
		});
		let result = await response.json();
		return result as JSON;
	}
})(uri("/stubconfig/method"));

type GetStubbedMethodType = (c: string) => Promise<JSON>;
const getStubbedMethod: GetStubbedMethodType = ((action: string) => {
	return async (className: string) => {
		let response = await fetch(action, {
			method: "POST",
			body: new URLSearchParams({
				className: className,
			}),
		});
		let result = await response.json();
		return result as JSON;
	}
})(uri("/stubconfig/list"));
