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

import {Button, Container, Grid2, InputLabel, MenuItem, Select, TextField, Typography} from "@mui/material"
import {useState} from "react"
import {invoke, resolveBeanName, resolveMethod} from "./api"

const App = () => {

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
        <Container>
            <Typography variant="h4" marginTop={1} marginBottom={2}>
                呼出しツール
            </Typography>

            <Grid2 container spacing={1}>

                <Grid2 size={1}>
                    <InputLabel>クラス</InputLabel>
                </Grid2>
                <Grid2 size={7}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        label="BeanのFQCNを指定してください"
                        value={className}
                        onChange={(e) => setClassName(e.target.value)}
                        onBlur={handleClassName}>
                    </TextField>
                </Grid2>
                <Grid2 size={4}>
                    <Select
                        fullWidth
                        variant="outlined"
                        size="small"
                        value={beanName}
                        onChange={(e) => setBeanName(e.target.value)}>
                        {
                            beanNameList.map((e) =>
                                <MenuItem value={e}>
                                    {e}
                                </MenuItem>
                            )
                        }
                    </Select>
                </Grid2>

                <Grid2 size={1}>
                    <InputLabel>メソッド</InputLabel>
                </Grid2>
                <Grid2 size={7}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        label="メソッドの名称を指定してください"
                        value={methodName}
                        onChange={(e) => setMethodName(e.target.value)}
                        onBlur={handleMethodName}>
                    </TextField>
                </Grid2>
                <Grid2 size={4}>
                    <Select
                        fullWidth
                        variant="outlined"
                        size="small"
                        value={methodIndex}
                        onChange={(e) => setMethodIndex(e.target.value)}>
                        {
                            methodNameList.map((e, i) =>
                                <MenuItem value={i}>
                                    {e}
                                </MenuItem>
                            )
                        }
                    </Select>
                </Grid2>

                <Grid2 size={1}>
                    <InputLabel>引数</InputLabel>
                </Grid2>
                <Grid2 size={11}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        multiline
                        minRows={3}
                        label="引数のリストを生成するスクリプトを記述"
                        value={script}
                        onChange={(e) => setScript(e.target.value)}>
                    </TextField>
                </Grid2>

                <Grid2 size={1}></Grid2>
                <Grid2 size={11}>
                    <Button
                        fullWidth
                        variant="contained"
                        onClick={handleInvoke}>
                        実行
                    </Button>
                </Grid2>

                <Grid2 size={1}>
                    <InputLabel>実行結果</InputLabel>
                </Grid2>
                <Grid2 size={11}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        multiline
                        minRows={5}
                        value={result}
                        onChange={(e) => setResult(e.target.value)}>
                    </TextField>
                </Grid2>
            </Grid2>

            <Typography align="center" marginTop={2}>
                Copyright &copy;, 2015,2025, agwlvssainokuni
            </Typography>
        </Container>
    )
}

export default App
