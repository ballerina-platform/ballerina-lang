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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * This class tests CLI parsing.
 *
 * @since 0.981.2
 */
public class CommandParserTestCase extends BaseTest {

    private String sourceArg = (new File("src/test/resources/command/test_cmd_parser.bal")).getAbsolutePath();
    private BMainInstance mainInstance;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        mainInstance = new BMainInstance(balServer);
    }

    @Test (description = "Tests correct identification of empty strings as args")
    public void testEmptyStringArg() throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher("empty_string");
        mainInstance.runMain(sourceArg, null, new String[]{""}, new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
    }

    @Test (description = "Tests enforcing CLI option/param ordering: any argument specified after the first "
            + "non-option argument (the source to run) needs to be identified as a param, and not an option, even if "
            + "an option of the same name exists",
           dataProvider = "runCmdOptions")
    public void testCliParamOrderEnforcement(String cmdOption) throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher(cmdOption);
        mainInstance.runMain(sourceArg, null, new String[]{cmdOption}, new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
    }

    @Test (description = "Test arguments starting with a dash", dataProvider = "valuesWithDashPrefix")
    public void testDashPrefixedArg(String argWithDashPrefix) throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher(argWithDashPrefix);
        mainInstance.runMain(sourceArg, null, new String[]{argWithDashPrefix}, new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
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
