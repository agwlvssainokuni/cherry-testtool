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
import {getStub, getStubbedMethod, putStub, resolveBeanName, resolveMethod} from "./api"
import "./StubconfigPage.css"

const StubconfigPage = () => {

    const [className, setClassName] = useState("")
    const [beanName, setBeanName] = useState("(参考)")
    const [beanNameList, setBeanNameList] = useState(["(参考)"])
    const [methodName, setMethodName] = useState("")
    const [methodNameList, setMethodNameList] = useState(["メソッドを引数のパターンで指定"])
    const [methodIndex, setMethodIndex] = useState("0")
    const [script, setScript] = useState("")
    const [engine, setEngine] = useState("")
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
    const handlePeekBtn = () => getStub(className, methodName, methodIndex)
        .then((r: string[]) => {
            setScript(r[0])
            setEngine(r[1])
            setResult(r[2])
        })
        .catch((r) => {
            setResult(r)
        })
    const handleClearBtn = () => {
        setScript("")
        setEngine("")
    }
    const handleRegisterBtn = () => putStub(className, methodName, methodIndex, script, engine)
        .then((r: string) => {
            setResult(r)
        })
        .catch((r) => {
            setResult(r)
        })
    const handleListBtn = () => getStubbedMethod(className)
        .then((r: string[]) => {
            setResult(r.join("\n"))
        })
        .catch((r) => {
            setResult(r)
        })

    return (
        <div className="stubconfig-page">
            <h1 className="stubconfig-page-title">
                スタブ設定ツール
            </h1>

            <div className="stubconfig-form">

                <div className="stubconfig-form-row">
                    <FormField label="クラス" className="stubconfig-field-wide">
                        <TextInput
                            placeholder="BeanのFQCNを指定してください"
                            value={className}
                            onChange={(v) => setClassName(v)}
                            onBlur={handleClassName}
                            data-testid="stubconfig-class-name-input"/>
                    </FormField>
                    <FormField label="Bean名称" className="stubconfig-field-narrow">
                        <Select
                            options={beanNameList.map((e) => ({label: e, value: e}))}
                            value={beanName}
                            onChange={(v) => setBeanName(v)}
                            data-testid="stubconfig-bean-name-select"/>
                    </FormField>
                </div>

                <div className="stubconfig-form-row">
                    <FormField label="メソッド" className="stubconfig-field-wide">
                        <TextInput
                            placeholder="メソッドの名称を指定してください"
                            value={methodName}
                            onChange={(v) => setMethodName(v)}
                            onBlur={handleMethodName}
                            data-testid="stubconfig-method-name-input"/>
                    </FormField>
                    <FormField label="メソッド候補" className="stubconfig-field-narrow">
                        <Select
                            options={methodNameList.map((e, i) => ({label: e, value: String(i)}))}
                            value={methodIndex}
                            onChange={(v) => setMethodIndex(v)}
                            data-testid="stubconfig-method-index-select"/>
                    </FormField>
                </div>

                <div className="stubconfig-return-row">
                    <div className="stubconfig-return-actions">
                        <Button variant="secondary" size="sm" onClick={handleClearBtn}
                                data-testid="stubconfig-clear-button">
                            クリア
                        </Button>
                        <Button variant="secondary" size="sm" onClick={handlePeekBtn}
                                data-testid="stubconfig-peek-button">
                            現在値
                        </Button>
                    </div>
                    <FormField label="返却値" className="stubconfig-field-wide">
                        <Textarea
                            rows={3}
                            placeholder="返却値を生成するスクリプトを記述"
                            value={script}
                            onChange={(v) => setScript(v)}
                            className="stubconfig-code-textarea"
                            data-testid="stubconfig-script-textarea"/>
                    </FormField>
                </div>

                <div className="stubconfig-actions-row">
                    <Button variant="secondary" onClick={handleListBtn} data-testid="stubconfig-list-button">
                        一覧
                    </Button>
                    <Button variant="primary" onClick={handleRegisterBtn}
                            className="stubconfig-register-button" data-testid="stubconfig-register-button">
                        登録
                    </Button>
                </div>

                <FormField label="登録結果">
                    <Textarea
                        rows={5}
                        value={result}
                        onChange={(v) => setResult(v)}
                        className="stubconfig-code-textarea"
                        data-testid="stubconfig-result-textarea"/>
                </FormField>
            </div>

            <p className="stubconfig-page-footer">
                Copyright &copy;, 2015,2026, agwlvssainokuni
            </p>
        </div>
    )
}

export default StubconfigPage
