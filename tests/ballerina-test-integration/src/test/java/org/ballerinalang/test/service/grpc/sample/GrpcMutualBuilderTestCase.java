/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.util.grpc.builder.helloworld.ClientRunnable;
import org.ballerinalang.test.util.grpc.builder.helloworld.ServerRunnable;
import org.ballerinalang.test.util.grpc.client.helloworld.HelloClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for WebSocket client connector.
 * This test the mediation of wsClient <-> balServer <-> balWSClient <-> remoteServer.
 */
public class GrpcMutualBuilderTestCase extends IntegrationTestCase {
    Thread t1,t2;
    ClientRunnable clientRunnable;
    ServerRunnable serverRunnable;
    @BeforeClass
    private void setup() throws Exception {
        ServerInstance ballerinaServer = ServerInstance.initBallerinaServer();
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        ServerInstance ballerinaClient = new ServerInstance(serverZipPath);
        clientRunnable = new ClientRunnable(ballerinaClient);
        serverRunnable = new ServerRunnable(ballerinaServer);
        t2 = new Thread(clientRunnable);
        t2.start();
        serverRunnable.run();
    }
    
    @Test
    public void testBlockingServer() {
    
    }
    
    @AfterClass
    private void cleanup(){
    
    }
}
