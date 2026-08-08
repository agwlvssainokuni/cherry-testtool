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

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Picocliのルートコマンド。接続先URL・BASIC認証・追加ヘッダを共通オプション(BR8、{@code scope = INHERIT})として定義し、
 * {@code invoke}・{@code stubconfig}サブコマンドへディスパッチする。
 */
@Command(
        name = "cherry-testtool-cli",
        mixinStandardHelpOptions = true,
        subcommands = {InvokeCommand.class, StubConfigCommand.class}
)
@Component
public class RootCommand implements Callable<Integer> {

    @Option(names = "--url", scope = ScopeType.INHERIT, defaultValue = "http://localhost:8080",
            description = "接続先ベースURL(既定: ${DEFAULT-VALUE})")
    URI baseUrl;

    @Option(names = "--basic-auth", scope = ScopeType.INHERIT, description = "BASIC認証情報(user:pass形式)")
    @Nullable String basicAuth;

    @Option(names = "--header", scope = ScopeType.INHERIT, description = "追加リクエストヘッダ(Name: Value形式、複数指定可)")
    List<String> headers = new ArrayList<>();

    @Spec
    CommandSpec spec;

    /**
     * サブコマンドが指定されなかった場合はUsageを表示する。
     */
    @Override
    public Integer call() {
        spec.commandLine().usage(System.err);
        return ExitCode.USAGE;
    }

}
