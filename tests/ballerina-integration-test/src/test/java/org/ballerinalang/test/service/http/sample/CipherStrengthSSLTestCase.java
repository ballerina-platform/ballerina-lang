/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Testing the default behaviour of SSL cipher support.
 */
@Test(groups = "http-test")
public class CipherStrengthSSLTestCase extends HttpBaseTest {

    private final String strongCipherServiceUrl = "https://localhost:9226";
    private final String weakCipherServiceUrl = "https://localhost:9227";
    private String strongCipherClient;
    private String weakCipherClient;

    @BeforeClass
    public void init() {
        strongCipherClient = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "mutualSSL"
                        + File.separator + "ssl_client_with_strong_cipher.bal").getAbsolutePath();
        weakCipherClient = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "mutualSSL"
                        + File.separator + "ssl_client_with_weak_cipher.bal").getAbsolutePath();
    }

    @Test(description = "Test ssl with strong cipher service with strong cipher client")
    public void testWithStrongClientWithStrongService() throws Exception {
        String serverMessage = "successful";
        String serverResponse = "hello world";
        validateClientExecution(serverMessage, serverResponse, strongCipherClient, strongCipherServiceUrl);
    }

    @Test(description = "Test ssl with weak cipher service with weak cipher client")
    public void testWithWeakClientWithWeakService() throws Exception {
        String serverMessage = "successful";
        String serverResponse = "hello world";
        validateClientExecution(serverMessage, serverResponse, weakCipherClient, weakCipherServiceUrl);
    }

    @Test(description = "Test ssl with strong cipher service with weak cipher client")
    public void testWithStrongClientWithWeakService() throws Exception {
        String serverResponse = "Received fatal alert: handshake_failurelocalhost/127.0.0.1:9226";
        validateClientExecution(serverResponse, weakCipherClient, strongCipherServiceUrl);
    }

    @Test(description = "Test ssl with weak cipher service with strong cipher client")
    public void testWithWeakClientWithStrongService() throws Exception {
        String serverResponse = "Received fatal alert: handshake_failurelocalhost/127.0.0.1:9227";
        validateClientExecution(serverResponse, strongCipherClient, weakCipherServiceUrl);
    }

    private void validateClientExecution(String serverMessage, String serverResponse, String clientProgram,
            String serverUrl) throws BallerinaTestException {
        LogLeecher serverLeecher = new LogLeecher(serverMessage);
        serverInstance.addLogLeecher(serverLeecher);
        validateClientExecution(serverResponse, clientProgram, serverUrl);
        serverLeecher.waitForText(20000);
    }

    private void validateClientExecution(String serverResponse, String clientProgram, String serverUrl)
            throws BallerinaTestException {
        BMainInstance ballerinaClient = new BMainInstance(balServer);
        LogLeecher clientLeecher = new LogLeecher(serverResponse);
        ballerinaClient.runMain(clientProgram, null, new String[] { serverUrl }, new LogLeecher[] { clientLeecher });
        clientLeecher.waitForText(20000);
    }
}
