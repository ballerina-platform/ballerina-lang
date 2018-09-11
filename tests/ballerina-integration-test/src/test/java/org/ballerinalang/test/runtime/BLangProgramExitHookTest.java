/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BLangProgramExitHookTest extends BaseTest {

    private LogLeecher serverLogLeecher1 = new LogLeecher("hook one invoked");
    private LogLeecher serverLogLeecher2 = new LogLeecher("hook two invoked with var : 4");

    @BeforeTest
    public void setup() throws BallerinaTestException, IOException {
        BServerInstance serverInstance = new BServerInstance(balServer);
        String exitHookTestFile = new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "runtime" + File.separator + "exit_hook.bal").getAbsolutePath();
        serverInstance.startServer(exitHookTestFile, new int[]{9090});

        serverInstance.addLogLeecher(serverLogLeecher1);
        serverInstance.addLogLeecher(serverLogLeecher2);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        String requestMessage = "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}";
        HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9090, "echo"), requestMessage, headers);
        serverInstance.shutdownServer();
    }

    @Test
    public void testAddExitHook() throws BallerinaTestException {
        serverLogLeecher1.waitForText(30000);
        serverLogLeecher2.waitForText(30000);
    }
}
