/*
 * Copyright 2021,2026 agwlvssainokuni
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

import {Button, FormField, Select, Textarea, TextInput} from "make-you-chic-ui"
import {useState} from "react"
import {invoke, resolveBeanName, resolveMethod} from "./api"
import "./InvokerPage.css"

const InvokerPage = () => {

    const [className, setClassName] = useState("")
    const [beanName, setBeanName] = useState("Bean名称(非必須)")
    const [beanNameList, setBeanNameList] = useState(["Bean名称(非必須)"])
    const [methodName, setMethodName] = useState("")
    const [methodNameList, setMethodNameList] = useState(["メソッドを引数のパターンで指定"])
    const [methodIndex, setMethodIndex] = useState("0")
    const [script, setScript] = useState("")
    const engine = ""    // [engine, setEngine] = useState("")
    const [result, setResult] = useState("")

    const handleClassName = () => resolveBeanName(className)
        .then((r: string[]) => {
            setBeanNameList(r)
            setBeanName(r[0])
        })
        .catch((r) => {
            setResult(r)
        })
    const handleMethodName = () => resolveMethod(className, methodName)
        .then((r: string[]) => {
            setMethodNameList(r)
            setMethodIndex("0")
        })
        .catch((r) => {
            setResult(r)
        })
    const handleInvoke = () => invoke(beanName, className, methodName, methodIndex, script, engine)
        .then((r: string) => {
            setResult(r)
        })
        .catch((r) => {
            setResult(r)
        })

    return (
        <div className="invoker-page">
            <h1 className="invoker-page-title">
                呼出しツール
            </h1>

            <div className="invoker-form">

                <div className="invoker-form-row">
                    <FormField label="クラス" className="invoker-field-wide">
                        <TextInput
                            placeholder="BeanのFQCNを指定してください"
                            value={className}
                            onChange={(v) => setClassName(v)}
                            onBlur={handleClassName}
                            data-testid="invoker-class-name-input"/>
                    </FormField>
                    <FormField label="Bean名称" className="invoker-field-narrow">
                        <Select
                            options={beanNameList.map((e) => ({label: e, value: e}))}
                            value={beanName}
                            onChange={(v) => setBeanName(v)}
                            data-testid="invoker-bean-name-select"/>
                    </FormField>
                </div>

                <div className="invoker-form-row">
                    <FormField label="メソッド" className="invoker-field-wide">
                        <TextInput
                            placeholder="メソッドの名称を指定してください"
                            value={methodName}
                            onChange={(v) => setMethodName(v)}
                            onBlur={handleMethodName}
                            data-testid="invoker-method-name-input"/>
                    </FormField>
                    <FormField label="メソッド候補" className="invoker-field-narrow">
                        <Select
                            options={methodNameList.map((e, i) => ({label: e, value: String(i)}))}
                            value={methodIndex}
                            onChange={(v) => setMethodIndex(v)}
                            data-testid="invoker-method-index-select"/>
                    </FormField>
                </div>

                <FormField label="引数">
                    <Textarea
                        rows={3}
                        placeholder="引数のリストを生成するスクリプトを記述"
                        value={script}
                        onChange={(v) => setScript(v)}
                        className="invoker-code-textarea"
                        data-testid="invoker-script-textarea"/>
                </FormField>

                <Button variant="primary" onClick={handleInvoke} data-testid="invoker-invoke-button">
                    実行
                </Button>

                <FormField label="実行結果">
                    <Textarea
                        rows={5}
                        value={result}
                        onChange={(v) => setResult(v)}
                        className="invoker-code-textarea"
                        data-testid="invoker-result-textarea"/>
                </FormField>
            </div>

            <p className="invoker-page-footer">
                Copyright &copy;, 2015,2026, agwlvssainokuni
            </p>
        </div>
    )
}

export default InvokerPage
