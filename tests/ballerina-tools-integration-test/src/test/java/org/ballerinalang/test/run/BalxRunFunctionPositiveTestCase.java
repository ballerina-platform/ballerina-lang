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
import org.ballerinalang.test.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class tests invoking the main function in a balx via the Ballerina Run Command and the data binding
 * functionality.
 */
public class BalxRunFunctionPositiveTestCase extends BaseTest {

    private static final String ENTRY_NAME = "entry";
    private Path tempProjectDir;
    private Path tempProjectDirTwo;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        // set up for testNoArg
        tempProjectDir = Files.createTempDirectory("temp-entry-func-test");
        String[] clientArgs = {"-o", Paths.get(tempProjectDir.toString(), ENTRY_NAME).toString(),
                (new File("src/test/resources/run/balx/no_params/test_main_with_no_params.bal")).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0],
                          new LogLeecher[0], tempProjectDir.toString());

        // set up for testMultipleParams
        tempProjectDirTwo = Files.createTempDirectory("temp-entry-func-test-two");
        clientArgs = new String[]{"-o", Paths.get(tempProjectDirTwo.toString(), ENTRY_NAME).toString(),
                (new File("src/test/resources/run/balx/multiple_params/" +
                                  "test_main_with_multiple_typed_params.bal")).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0],
                          new LogLeecher[0], tempProjectDirTwo.toString());
    }

    @Test
    public void testNoArg() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("run", new String[]{ ENTRY_NAME + ".balx" },
                                                       tempProjectDir.toString());
        Assert.assertEquals(output, "1");
    }

    @Test
    public void testMultipleParams() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("run", new String[]{ ENTRY_NAME + ".balx", "1000", "1.0",
                "Hello Ballerina", "255", "true", "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>",
                "{ \"name\": \"Em\" }", "just", "the", "rest"}, tempProjectDirTwo.toString());
        Assert.assertEquals(output, "integer: 1000, float: 1.0, string: Hello Ballerina, byte: 255, "
                + "boolean: true, JSON Name Field: Maryam, XML Element Name: "
                + "book, Employee Name Field: Em, string rest args: just the"
                + " rest ");
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
