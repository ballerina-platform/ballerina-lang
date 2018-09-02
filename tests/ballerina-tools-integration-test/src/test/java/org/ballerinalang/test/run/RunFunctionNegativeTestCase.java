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
package org.ballerinalang.test.run;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * This class tests invoking an entry function via the Ballerina Run Command and the data binding functionality.
 *
 * e.g., ballerina run abc:nomoremain 1 "Hello World" data binding main
 *  where nomoremain is the following function
 *      public function nomoremain(int i, string s, string... args) {
 *          ...
 *      }
 */
public class RunFunctionNegativeTestCase {

    private ServerInstance serverInstance;

    private String fileName = "test_entry_function.bal";
    private String filePath = (new File("src/test/resources/run/file/" + fileName)).getAbsolutePath();

    private String sourceArg;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        serverInstance = ServerInstance.initBallerinaServer();
    }

    @Test
    public void testEmptyEntryFunctionName() throws BallerinaTestException {
        sourceArg = filePath + ":";
        LogLeecher errLogLeecher = new LogLeecher("usage error: expected function name after final ':'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg});
        errLogLeecher.waitForText(2000);
    }

    @AfterMethod
    public void stopServer() throws BallerinaTestException {
        serverInstance.stopServer();
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        serverInstance.cleanup();
    }
}
