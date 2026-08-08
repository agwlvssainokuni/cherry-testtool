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

package cherry.testtool.cli;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * {@code lib}が提供するREST API({@code /testtool/invoker/invoke}、{@code /testtool/stubconfig/put,get})を
 * 呼び出す宣言的HTTPクライアント(Application Design参照)。Bean名・メソッド解決系のAPI(
 * {@code /testtool/resolve/**})は、旧CLIも使用していなかったため本インタフェースには含めない。
 */
@HttpExchange("/testtool")
public interface TesttoolApiClient {

    /**
     * 指定クラス・メソッドをリフレクションで呼び出す(lib側{@code TesttoolController#invoke}に対応)。
     */
    @PostExchange("/invoker/invoke")
    String invoke(@RequestParam("className") String className,
                  @RequestParam("methodName") String methodName,
                  @RequestParam("methodIndex") int methodIndex,
                  @RequestParam("script") String script,
                  @RequestParam("engine") String engine,
                  @RequestHeader MultiValueMap<String, String> headers);

    /**
     * 指定メソッドへスタブ設定を登録する。{@code script}が空の場合は登録を解除する
     * (lib側{@code TesttoolController#putStubConfig}に対応)。
     */
    @PostExchange("/stubconfig/put")
    String putStub(@RequestParam("className") String className,
                   @RequestParam("methodName") String methodName,
                   @RequestParam("methodIndex") int methodIndex,
                   @RequestParam("script") String script,
                   @RequestParam("engine") String engine,
                   @RequestHeader MultiValueMap<String, String> headers);

    /**
     * 登録済みスタブのスクリプト・エンジン・現在の評価結果を取得する
     * (lib側{@code TesttoolController#getStubConfig}に対応)。
     */
    @PostExchange("/stubconfig/get")
    List<String> getStub(@RequestParam("className") String className,
                          @RequestParam("methodName") String methodName,
                          @RequestParam("methodIndex") int methodIndex,
                          @RequestHeader MultiValueMap<String, String> headers);

}
