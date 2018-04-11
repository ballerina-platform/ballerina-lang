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
 *
 */

package org.ballerinalang.test.jms.util;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class BallerinaClientHandler {
    private final String filename;
    private final String expectedLog;
    private LogLeecher clientLeecher;
    private ServerInstance ballerinaClient;

    public BallerinaClientHandler(String filename, String expectedLog) {
        this.filename = filename;
        this.expectedLog = expectedLog;
    }

    public void start() throws BallerinaTestException {
        String[] clientArgs = {
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms"
                                 + File.separator + filename).getAbsolutePath()
        };

        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        ballerinaClient = new ServerInstance(serverZipPath);

        clientLeecher = new LogLeecher(expectedLog);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs);
    }

    public void waitForText(TimeUnit timeUnit, int length) throws BallerinaTestException {
        clientLeecher.waitForText(timeUnit.toMillis(length));
    }

    public void stop() throws BallerinaTestException {
        ballerinaClient.stopServer();
    }
}
