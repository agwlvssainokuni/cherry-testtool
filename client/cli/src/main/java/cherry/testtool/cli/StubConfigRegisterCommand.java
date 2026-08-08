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

package cherry.testtool.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 指定ディレクトリ群配下のスクリプトファイルの内容をスタブとして一括登録する。
 */
@Command(name = "register", description = "スタブを一括登録する")
@Component
public class StubConfigRegisterCommand implements Callable<Integer> {

    @ParentCommand
    StubConfigCommand parent;

    @Parameters(paramLabel = "DIR", arity = "1..*", description = "スクリプト設定ディレクトリ")
    List<Path> directories;

    private final StubConfigService stubConfigService;

    public StubConfigRegisterCommand(StubConfigService stubConfigService) {
        this.stubConfigService = stubConfigService;
    }

    @Override
    public Integer call() {
        var rc = parent.rootCommand;
        var result = stubConfigService.registerAll(rc.baseUrl, directories, rc.basicAuth, rc.headers);
        return result.failureCount() == 0 ? 0 : 1;
    }

}
