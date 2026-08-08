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

package cherry.testtool.cli.command;

import cherry.testtool.cli.service.StubConfigService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 指定ディレクトリ群配下のスクリプトファイルに対応するスタブ登録を一括解除する。
 */
@Command(name = "clear", description = "スタブを一括解除する")
@Component
public class StubConfigClearCommand implements Callable<Integer> {

    @ParentCommand
    StubConfigCommand parent;

    @Parameters(paramLabel = "DIR", arity = "1..*", description = "スクリプト設定ディレクトリ")
    List<Path> directories;

    private final StubConfigService stubConfigService;

    public StubConfigClearCommand(StubConfigService stubConfigService) {
        this.stubConfigService = stubConfigService;
    }

    @Override
    public Integer call() {
        var rc = parent.rootCommand;
        var result = stubConfigService.clearAll(rc.baseUrl, directories, rc.basicAuth, rc.headers);
        return result.failureCount() == 0 ? 0 : 1;
    }

}
