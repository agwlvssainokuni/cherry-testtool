/*
 * Copyright 2026 agwlvssainokuni
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

package cherry.testtool.demo;

import cherry.testtool.stub.StubConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 起動時に{@code lib}の{@link StubConfigLoader}を使い、指定ディレクトリ配下のスタブ設定を
 * 一括読込みするランナー。
 * <p>
 * {@code demo.stub-loader.enabled=true}のときのみ有効化される(既定は無効)。読込み先ディレクトリ・
 * 対象拡張子は{@code demo.stub-loader.directory}・{@code demo.stub-loader.ext}で変更できる。
 */
@Component
@ConditionalOnProperty(prefix = "demo.stub-loader", name = "enabled", havingValue = "true")
public class StubAutoLoadRunner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final StubConfigLoader stubConfigLoader;

    private final String directory;

    private final String ext;

    public StubAutoLoadRunner(
            StubConfigLoader stubConfigLoader,
            @Value("${demo.stub-loader.directory:stub-samples}") String directory,
            @Value("${demo.stub-loader.ext:.js}") String ext
    ) {
        this.stubConfigLoader = stubConfigLoader;
        this.directory = directory;
        this.ext = ext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Loading stub configurations from {} (ext={})", directory, ext);
        stubConfigLoader.load(new File(directory), ext);
    }

}
