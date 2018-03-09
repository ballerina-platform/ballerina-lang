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

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.grpc.server.helloworld.GrpcServer;
import org.ballerinalang.test.util.grpc.server.helloworld.HelloWorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for WebSocket client connector.
 * This test the mediation of wsClient <-> balServer <-> balWSClient <-> remoteServer.
 */
public class GrpcClientTestCase extends IntegrationTestCase {
    private static final Logger log = LoggerFactory.getLogger(GrpcClientTestCase.class);
    private ServerInstance ballerinaClient;
    private GrpcServer grpcServer;
    
    @BeforeClass
    private void setup() throws Exception {
        grpcServer = new GrpcServer(9098);
        Thread t = new Thread(grpcServer);
        t.start();
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        String[] clientArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "grpcService" + File.separator + "helloWorldClient.bal").getAbsolutePath()};
        ballerinaClient = new ServerInstance(serverZipPath);
        ballerinaClient.runMain(clientArgs);
    }
    
    @Test
    public void testBlockingClient() {
        Assert.assertEquals(HelloWorldService.getName(), "WSO2");
    }
    
    @AfterClass
    private void cleanup() throws Exception {
        grpcServer.stop();
        ballerinaClient.stopServer();
    }
}
