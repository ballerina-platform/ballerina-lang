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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.test.utils.PackagingTestUtils.deleteFiles;

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
public class BalxRunFunctionPositiveTestCase extends BaseTest {

    private static final int LOG_LEECHER_TIMEOUT = 10000;

    private String sourceRoot = (new File("src/test/resources/run/balx/simple/")).getAbsolutePath();

    private String balxPath;
    private String balxPathTwo;
    private Path tempProjectDir;
    private Path tempProjectDirTwo;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        // set up for testNoArg
        tempProjectDir = Files.createTempDirectory("temp-entry-func-test");
        String[] clientArgs = {"-o", Paths.get(tempProjectDir.toString(), "entry").toString(),
                (new File("src/test/resources/run/balx/no_params/test_main_with_no_params.bal")).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0],
                          new LogLeecher[0], tempProjectDir.toString());
        Path generatedBalx = tempProjectDir.resolve("entry.balx");
        balxPath = generatedBalx.toString();

        // set up for testMultipleParams
        tempProjectDirTwo = Files.createTempDirectory("temp-entry-func-test-two");
        clientArgs = new String[]{"-o", Paths.get(tempProjectDirTwo.toString(), "entry").toString(),
                (new File("src/test/resources/run/balx/multiple_params/" +
                                  "test_main_with_multiple_typed_params.bal")).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0],
                          new LogLeecher[0], tempProjectDirTwo.toString());
        generatedBalx = tempProjectDirTwo.resolve("entry.balx");
        balxPathTwo = generatedBalx.toString();
    }

    @Test
    public void testNoArg() throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher("1");
        balClient.runMain(balxPath, new String[]{}, new String[0], new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test
    public void testMultipleParams() throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher("integer: 1000, float: 1.0, string: Hello Ballerina, byte: 255, "
                                                          + "boolean: true, JSON Name Field: Maryam, XML Element Name: "
                                                          + "book, Employee Name Field: Em, string rest args: just the"
                                                          + " rest ");
        balClient.runMain(balxPathTwo, new String[]{}, new String[]{"1000", "1.0", "Hello Ballerina", "255", "true",
                "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>", "{ \"name\": \"Em\" }", "just", "the", "rest"},
                          new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        try {
            deleteFiles(tempProjectDir);
            deleteFiles(tempProjectDirTwo);
        } catch (IOException e) {
            throw new BallerinaTestException("Error deleting files");
        }
    }
}
