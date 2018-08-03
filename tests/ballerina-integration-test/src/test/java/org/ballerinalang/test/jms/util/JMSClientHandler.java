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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.jms.util;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Client handler class to handle JMS client related operations.
 */
public class JMSClientHandler {
    private ServerInstance ballerinaClient;

    public JMSClientHandler() throws BallerinaTestException {
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        ballerinaClient = new ServerInstance(serverZipPath);
    }

    public LogLeecher start(String filename, String expectedLog) throws BallerinaTestException {
        String[] clientArgs = {
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms" +
                        File.separator + "clients" + File.separator + filename).getAbsolutePath()
        };
        LogLeecher clientLeecher = new LogLeecher(expectedLog);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs);
        return clientLeecher;
    }

    public void stop() throws BallerinaTestException {
        ballerinaClient.removeAllLeechers();
        ballerinaClient.stopServer();
    }
}
