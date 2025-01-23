/*
 * Copyright 2023,2025 agwlvssainokuni
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

package cherry.testtool.script;

import cherry.testtool.TesttoolConfiguration;
import cherry.testtool.ToolTesterImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TesttoolConfiguration.class, ToolTesterImpl.class})
@SpringBootApplication()
@ImportResource({"classpath:spring/appctx-trace.xml", "classpath:spring/appctx-stub.xml"})
public class ScriptProcessorTest {

    @Autowired
    private ScriptProcessor scriptProcessor;

    @Test
    public void testJsArray() throws Exception {
        var result = (List<?>) scriptProcessor.eval("[1, 2, 3]", "");
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    public void testJavaType() throws Exception {
        var result = (List<?>) scriptProcessor.eval(
                "const Arrays = Java.type('java.util.Arrays'); Arrays.asList(1, 2, 3);", "");
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

}
