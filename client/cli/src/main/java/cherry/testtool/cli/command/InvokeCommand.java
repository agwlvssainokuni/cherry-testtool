/*
 * Copyright 2023,2026 agwlvssainokuni
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

package cherry.testtool.cli.command;

import cherry.testtool.cli.service.InvokeService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 旧{@code invoker.sh}相当のサブコマンド。指定ディレクトリ群配下のスクリプトを一括呼出しする。
 */
@Command(name = "invoke", description = "スクリプトを一括呼び出しする")
@Component
public class InvokeCommand implements Callable<Integer> {

    @ParentCommand
    RootCommand rootCommand;

    @Parameters(paramLabel = "DIR", arity = "1..*", description = "スクリプト設定ディレクトリ")
    List<Path> directories;

    private final InvokeService invokeService;

    public InvokeCommand(InvokeService invokeService) {
        this.invokeService = invokeService;
    }

    @Override
    public Integer call() {
        var result = invokeService.invokeAll(rootCommand.baseUrl, directories, rootCommand.basicAuth, rootCommand.effectiveHeaders());
        return result.failureCount() == 0 ? 0 : 1;
    }

}
