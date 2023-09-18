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

import { Button, Container, Grid, InputLabel, MenuItem, Select, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { getStub, getStubbedMethod, putStub, resolveBeanName, resolveMethod } from "./api";

const App = () => {

    let [className, setClassName] = useState("");
    let [beanName, setBeanName] = useState("(参考)");
    let [beanNameList, setBeanNameList] = useState(["(参考)"]);
    let [methodName, setMethodName] = useState("");
    let [methodNameList, setMethodNameList] = useState(["メソッドを引数のパターンで指定"]);
    let [methodIndex, setMethodIndex] = useState("0");
    let [script, setScript] = useState("");
    let [engine, setEngine] = useState("");
    let [result, setResult] = useState("");

    const handleClassName = () => resolveBeanName(className)
        .then((r: any) => {
            setBeanNameList(r);
            setBeanName(r[0]);
        })
        .catch((r: any) => {
            setResult(r);
        });
    const handleMethodName = () => resolveMethod(className, methodName)
        .then((r: any) => {
            setMethodNameList(r);
            setMethodIndex("0");
        })
        .catch((r: any) => {
            setResult(r);
        });
    const handlePeekBtn = () => getStub(className, methodName, methodIndex)
        .then((r: any) => {
            setScript(r[0]);
            setEngine(r[1]);
            setResult(r[2]);
        })
        .catch((r: any) => {
            setResult(r);
        });
    const handleClearBtn = () => {
        setScript("");
        setEngine("");
    };
    const handleRegisterBtn = () => putStub(className, methodName, methodIndex, script, engine)
        .then((r: any) => {
            setResult(r);
        })
        .catch((r: any) => {
            setResult(r);
        });
    const handleListBtn = () => getStubbedMethod(className)
        .then((r: any) => {
            setResult(r);
        })
        .catch((r: any) => {
            setResult(r);
        });

    return (
        <Container>
            <Typography variant="h4" marginTop={1} marginBottom={2}>
                スタブ設定ツール
            </Typography>

            <Grid container spacing={1}>

                <Grid item lg={1}>
                    <InputLabel>クラス</InputLabel>
                </Grid>
                <Grid item lg={7}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        label="BeanのFQCNを指定してください"
                        value={className}
                        onChange={(e) => setClassName(e.target.value)}
                        onBlur={handleClassName}>
                    </TextField>
                </Grid>
                <Grid item lg={4}>
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
                </Grid>

                <Grid item lg={1}>
                    <InputLabel>メソッド</InputLabel>
                </Grid>
                <Grid item lg={7}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        label="メソッドの名称を指定してください"
                        value={methodName}
                        onChange={(e) => setMethodName(e.target.value)}
                        onBlur={handleMethodName}>
                    </TextField>
                </Grid>
                <Grid item lg={4}>
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
                </Grid>

                <Grid item lg={1}>
                    <InputLabel>返却値</InputLabel>
                    <Button
                        variant="outlined"
                        size="small"
                        onClick={handleClearBtn}>
                        クリア
                    </Button>
                    <Button
                        variant="outlined"
                        size="small"
                        onClick={handlePeekBtn}>
                        現在値
                    </Button>
                </Grid>
                <Grid item lg={11}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        multiline
                        minRows={3}
                        label="返却値を生成するスクリプトを記述"
                        value={script}
                        onChange={(e) => setScript(e.target.value)}>
                    </TextField>
                </Grid>

                <Grid item lg={1}>
                    <Button
                        variant="outlined"
                        onClick={handleListBtn}>
                        一覧
                    </Button>
                </Grid>
                <Grid item lg={11}>
                    <Button
                        fullWidth
                        variant="contained"
                        onClick={handleRegisterBtn}>
                        登録
                    </Button>
                </Grid>

                <Grid item lg={1}>
                    <InputLabel>登録結果</InputLabel>
                </Grid>
                <Grid item lg={11}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        size="small"
                        multiline
                        minRows={5}
                        value={result}
                        onChange={(e) => setResult(e.target.value)}>
                    </TextField>
                </Grid>
            </Grid>

            <Typography align="center" marginTop={2}>
                Copyright &copy;, 2015,2023, agwlvssainokuni
            </Typography>
        </Container>
    );
}

export default App;
