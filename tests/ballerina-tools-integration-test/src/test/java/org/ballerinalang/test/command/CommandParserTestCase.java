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
package org.ballerinalang.test.command;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * This class tests CLI parsing.
 */
public class CommandParserTestCase {

    private String sourceArg = (new File("src/test/resources/command/test_cmd_parser.bal")).getAbsolutePath();
    private ServerInstance serverInstance;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        serverInstance = ServerInstance.initBallerinaServer();
    }

    @Test (description = "Tests correct identification of empty strings as args")
    public void testEmptyStringArg() throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher("empty_string");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, ""});
        outLogLeecher.waitForText(2000);
    }

    @Test (description = "Tests enforcing CLI option/param ordering: any argument specified after the first "
            + "non-option argument (the source to run) needs to be identified as a param, and not an option, even if "
            + "an option of the same name exists",
           dataProvider = "runCmdOptions")
    public void testCliParamOrderEnforcement(String cmdOption) throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher(cmdOption);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, cmdOption});
        outLogLeecher.waitForText(2000);
    }

    @Test (description = "Test arguments starting with a dash", dataProvider = "valuesWithDashPrefix")
    public void testDashPrefixedArg(String argWithDashPrefix) throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher(argWithDashPrefix);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argWithDashPrefix});
        outLogLeecher.waitForText(2000);
    }

    @AfterMethod
    public void stopServer() throws BallerinaTestException {
        serverInstance.stopServer();
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        serverInstance.cleanup();
    }

    @DataProvider(name = "runCmdOptions")
    public Object[][] runCmdOptions() {
        return new Object[][] {
                { "--config" },
                { "--debug" },
                { "--offline" },
                { "-e" }
        };
    }

    @DataProvider(name = "valuesWithDashPrefix")
    public Object[][] valuesWithDashPrefix() {
        return new Object[][] {
                { "-5" },
                { "-1.0" },
                { "-config" }
        };
    }
}
