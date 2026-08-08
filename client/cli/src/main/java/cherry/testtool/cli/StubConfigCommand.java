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
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

/**
 * 旧{@code stubconfig.sh}相当のサブコマンド。{@code register}/{@code clear}/{@code show}への
 * ディスパッチのみを担う(BR9)。自身は実行ロジックを持たない。
 */
@Command(
        name = "stubconfig",
        subcommands = {StubConfigRegisterCommand.class, StubConfigClearCommand.class, StubConfigShowCommand.class}
)
@Component
public class StubConfigCommand implements Callable<Integer> {

    @ParentCommand
    RootCommand rootCommand;

    @Spec
    CommandSpec spec;

    /**
     * サブサブコマンド(register/clear/show)が指定されなかった場合はUsageを表示する。
     */
    @Override
    public Integer call() {
        spec.commandLine().usage(System.err);
        return ExitCode.USAGE;
    }

}
