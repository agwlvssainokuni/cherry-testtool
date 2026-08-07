/*
 * Copyright 2015,2026 agwlvssainokuni
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

package cherry.testtool.stub;

import cherry.testtool.reflect.ReflectionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * ディレクトリ配下のスクリプトファイルを一括読込みし、{@link StubRepository}へスタブ設定として登録するローダー。
 * <p>
 * ファイルパスの構造(親ディレクトリ名=クラス名、ファイル名=メソッド名[.インデックス])から
 * 呼出し元と同じ規則でスタブ対象メソッドを解決する。
 */
public class StubConfigLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final StubRepository repository;

    private final ReflectionResolver reflectionResolver;

    public StubConfigLoader(
            StubRepository repository,
            ReflectionResolver reflectionResolver
    ) {
        this.repository = repository;
        this.reflectionResolver = reflectionResolver;
    }

    /**
     * 指定ディレクトリ配下の全ファイル(拡張子{@code ext}に一致するもの)を走査し、
     * それぞれをスタブ設定として{@link StubRepository}へ登録する。
     *
     * @param definitionDirectory 走査対象のルートディレクトリ
     * @param ext 対象とするファイル拡張子(例: {@code .js})
     * @throws IOException ファイル走査・読込み中にエラーが発生した場合
     */
    public void load(
            File definitionDirectory,
            String ext
    ) throws IOException {

        try (var files = Files.find(
                definitionDirectory.toPath(),
                Integer.MAX_VALUE, (path, attr) ->
                        attr.isRegularFile() && path.getFileName().toString().endsWith(ext)
        )) {
            for (var file : files.sorted().toList()) {

                logger.info(file.toString());

                var methodOpt = resolveMethod(file.toFile(), ext);
                if (methodOpt.isEmpty()) {
                    continue;
                }

                var script = Files.readString(file, StandardCharsets.UTF_8);
                repository.put(methodOpt.get(), new StubConfig(script, ""));
            }
        }
    }

    /**
     * ファイルのパス構造(親ディレクトリ名・ファイル名)から、対応するスタブ対象メソッドを解決する。
     *
     * @param file 解決対象のスクリプトファイル
     * @param ext ファイル拡張子(ファイル名からの除去用)
     * @return 解決できたメソッド。クラス未検出の場合は空
     */
    private Optional<Method> resolveMethod(
            File file,
            String ext
    ) {

        String className = file.getParentFile().getName();

        String methodName;
        long methodIndex;
        String m = file.getName().substring(0, file.getName().length() - ext.length());
        int pos = m.indexOf(".");
        if (pos < 0) {
            methodName = m;
            methodIndex = 0;
        } else {
            methodName = m.substring(0, pos);
            methodIndex = Long.parseLong(m.substring(pos + 1));
        }

        try {
            return reflectionResolver.resolveMethod(className, methodName).stream().skip(methodIndex).findFirst();
        } catch (ClassNotFoundException ex) {
            logger.warn(String.format("Stub config %s is skipped", file), ex);
            return Optional.empty();
        }
    }

}
