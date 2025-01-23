/*
 * Copyright 2021,2025 agwlvssainokuni
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

import {uri} from "../common";

export {invoke, resolveBeanName, resolveMethod};

const invoke = ((action: string) => {
    return async (beanName: string, className: string, methodName: string, methodIndex: string, script: string, engine: string) => {
        const response = await fetch(action, {
            method: "POST",
            body: new URLSearchParams({
                beanName: beanName,
                className: className,
                methodName: methodName,
                methodIndex: methodIndex,
                script: script,
                engine: engine,
            }),
        });
        const result = await response.text();
        return result as string;
    }
})(uri("/invoker/invoke"));

const resolveBeanName = ((action: string) => {
    return async (className: string) => {
        const response = await fetch(action, {
            method: "POST",
            body: new URLSearchParams({
                className: className,
            }),
        });
        const result = await response.json();
        return result as string[];
    }
})(uri("/invoker/bean"));

const resolveMethod = ((action: string) => {
    return async (className: string, methodName: string) => {
        const response = await fetch(action, {
            method: "POST",
            body: new URLSearchParams({
                className: className,
                methodName: methodName,
            }),
        });
        const result = await response.json();
        return result as string[];
    }
})(uri("/invoker/method"));
