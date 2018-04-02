/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.grpc.sample;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.grpc.client.helloworld.HelloClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for WebSocket client connector.
 * This test the mediation of wsClient <-> balServer <-> balWSClient <-> remoteServer.
 */
public class UnaryBlockingBasicTestCase extends IntegrationTestCase {

    private ServerInstance ballerinaServer;
    
    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer(9090);
        Path serviceBalPath = Paths.get("src", "test", "resources", "grpcService", "unary-server1.bal");
        ballerinaServer.startBallerinaServer(serviceBalPath.toAbsolutePath().toString());
    }
    
    @Test
    public void testBlockingJavaClient() throws Exception {
        HelloClient client = new HelloClient("localhost", 9090);
        try {
            String response = client.greet("WSO2");
            Assert.assertEquals(response, "Hello WSO2");
        } finally {
            client.shutdown();
        }
    }

    @Test
    public void testBlockingBallerinaClient() {

        Path balFilePath = Paths.get("src", "test", "resources", "grpcService", "unary1-blocking-client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        final String serverMsg = "Hello WSO2";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingClient", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), "Client got response: " + serverMsg);

    }

    @Test
    public void testNonBlockingBallerinaClient() {

        Path balFilePath = Paths.get("src", "test", "resources", "grpcService", "unary1-nonblocking-client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        final String serverMsg = "Hello WSO2";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryNonBlockingClient", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStringArray);
        BStringArray responseValues = (BStringArray) responses[0];
        Assert.assertEquals(responseValues.size(), 2);
        Assert.assertEquals(responseValues.get(0), serverMsg);
        Assert.assertEquals(responseValues.get(1), "Server Complete Sending Response.");
    }
    
    @AfterClass
    private void cleanup() throws BallerinaTestException {
        ballerinaServer.stopServer();
    }
}
