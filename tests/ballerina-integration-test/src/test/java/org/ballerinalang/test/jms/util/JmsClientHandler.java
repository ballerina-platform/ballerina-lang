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

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;

import java.io.File;

/**
 * Client handler class to handle JMS client related operations.
 */
public class JmsClientHandler {
    private BMainInstance ballerinaClient;

    public JmsClientHandler(BalServer balServer) throws BallerinaTestException {
        ballerinaClient = new BMainInstance(balServer);
    }

    public LogLeecher start(String filename, String expectedLog) throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jms" + File.separator + "clients" + File.separator + filename).getAbsolutePath();
        LogLeecher clientLeecher = new LogLeecher(expectedLog);
        ballerinaClient.runMain(balFile, new LogLeecher[]{clientLeecher});
        return clientLeecher;
    }

}
