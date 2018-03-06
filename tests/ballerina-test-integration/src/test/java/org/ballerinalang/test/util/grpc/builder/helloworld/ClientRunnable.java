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

package org.ballerinalang.test.util.grpc.builder.helloworld;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ClientRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ClientRunnable.class);
    private ServerInstance ballerinaClient;
    
    public ClientRunnable(ServerInstance ballerinaClient) {
        this.ballerinaClient = ballerinaClient;
    }
    
    @Override
    public void run() {
        String[] clientArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "grpcService" + File.separator + "helloWorldClient.bal").getAbsolutePath()};
        try {
            ballerinaClient.runMain(clientArgs);
        } catch (BallerinaTestException e) {
            log.error("Error in running grpc client connector main function.", e);
        }
    }
    
    public void stop() {
        try {
            ballerinaClient.stopServer();
        } catch (BallerinaTestException e) {
            log.error("Error in stopping grpc client connector main function.", e);
        }
    }
}
