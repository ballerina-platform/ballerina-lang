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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.LogLeecher.LeecherType;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class tests invoking an entry function in a balx via the Ballerina Run Command and the data binding
 * functionality.
 *
 * e.g., ballerina run abc.balx:nomoremain 1 "Hello World" data binding main
 *  where nomoremain is the following function
 *      public function nomoremain(int i, string s, string... args) {
 *          ...
 *      }
 */
public class BalxRunFunctionNegativeTestCase extends BaseTest {

    private static final int LOG_LEECHER_TIMEOUT = 10000;
    private String balxPath;
    private String balxPathTwo;
    private Path tempProjectDir;
    private Path tempProjectDirTwo;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDir = Files.createTempDirectory("temp-entry-func-test");
        String[] clientArgs = {"-o", Paths.get(tempProjectDir.toString(), "entry").toString(),
                (new File("src/test/resources/run/balx/multiple_params/test_main_with_multiple_typed_params.bal"))
                        .getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0], new LogLeecher[0],
                          tempProjectDir.toString());
        Path generatedBalx = tempProjectDir.resolve("entry.balx");
        balxPath = generatedBalx.toString();

        tempProjectDirTwo = Files.createTempDirectory("temp-entry-func-test-two");
        clientArgs = new String[]{"-o", Paths.get(tempProjectDirTwo.toString(), "entry").toString(),
                (new File("src/test/resources/run/balx/no_params/test_main_with_no_params.bal")).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0], new LogLeecher[0], tempProjectDirTwo.toString());
        generatedBalx = tempProjectDirTwo.resolve("entry.balx");
        balxPathTwo = generatedBalx.toString();
    }

    @Test(description = "test insufficient arguments")
    public void testInsufficientArguments() throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher("ballerina: insufficient arguments to call the 'main' function",
                                                  LeecherType.ERROR);
        balClient.runMain(balxPath, new LogLeecher[]{errLogLeecher});
        errLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(description = "test too many arguments")
    public void testTooManyArguments() throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher("ballerina: too many arguments to call the 'main' function",
                                                  LeecherType.ERROR);
        balClient.runMain(balxPathTwo, new String[]{}, new String[]{"extra"}, new LogLeecher[]{errLogLeecher});
        errLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        try {
            TestUtils.deleteFiles(tempProjectDir);
            TestUtils.deleteFiles(tempProjectDirTwo);
        } catch (IOException e) {
            throw new BallerinaTestException("Error deleting files");
        }
    }
}
