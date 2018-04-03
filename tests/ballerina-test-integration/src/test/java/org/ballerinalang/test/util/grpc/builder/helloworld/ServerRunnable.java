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

public class ServerRunnable extends Thread {
    private static final Logger log = LoggerFactory.getLogger(ServerRunnable.class);
    private ServerInstance ballerinaServer;
    private File serverBalFile;

    public ServerRunnable(ServerInstance ballerinaServer, File serverBalFile) {
        this.ballerinaServer = ballerinaServer;
        this.serverBalFile = serverBalFile;
    }

    @Override
    public void run() {
        if (serverBalFile != null && serverBalFile.exists()) {
            String balFile = serverBalFile.getAbsolutePath();
            try {
                ballerinaServer.startBallerinaServer(balFile);
            } catch (BallerinaTestException e) {
                log.error("Error in running grpc server connector main function.", e);
            }
        }
    }

    public void stopServer() {
        if (ballerinaServer != null && ballerinaServer.isRunning()) {
            try {
                ballerinaServer.stopServer();
            } catch (BallerinaTestException e) {
                log.error("Error in stopping grpc server connector main function.", e);
            }
        }
    }
}
