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
public class BalxRunFunctionNegativeTestCase extends BaseTest {

    private String sourceRoot = (new File("src/test/resources/run/balx/simple")).getAbsolutePath();

    private String balxPath;
    private Path tempProjectDir;
    private Path tempProjectDirTwo;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        // set up for testInvalidSourceArg
        tempProjectDir = Files.createTempDirectory("temp-entry-func-test");
        String fileName = "test_entry_function.bal";
        String[] clientArgs = {"-o", Paths.get(tempProjectDir.toString(), "entry").toString(),
                new File(sourceRoot + "/"  + fileName).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0],
                new LogLeecher[0], tempProjectDir.toString());
        Path generatedBalx = tempProjectDir.resolve("entry.balx");
        balxPath = generatedBalx.toString();
        // create a temp directory for testWrongEntryFunctionNameWithColons
        tempProjectDirTwo = Files.createTempDirectory("temp-entry-func-test-complex");
    }

    @Test(description = "test an invalid source argument, ending with a colon, e.g., ballerina run <FILE_NAME>:")
    public void testInvalidSourceArg() throws BallerinaTestException {
        String sourceArg = balxPath + ":";
        LogLeecher errLogLeecher = new LogLeecher("error: no ballerina source files found in module " + balxPath +
                                                          ":", LeecherType.ERROR);
        balClient.runMain(sourceArg, new LogLeecher[]{errLogLeecher});
        errLogLeecher.waitForText(2000);
    }

    @Test(description = "test an invalid function name with ballerina run, where the function name includes colons")
    public void testWrongEntryFunctionNameWithColons() throws BallerinaTestException {
        String fileName = "test_entry_function_with_colons.bal";
        String[] clientArgs = {"-o",
                Paths.get(tempProjectDirTwo.toString(), "test_entry_function_with_colons").toString(),
                new File("src/test/resources/run/balx/complex/" + fileName).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[0], new LogLeecher[0],
                          tempProjectDirTwo.toString());
        Path generatedBalx = tempProjectDirTwo.resolve(fileName.replace("bal", "balx"));
        String sourceArg = generatedBalx.toString() + ":colonsInName:WrongFunction";
        LogLeecher errLogLeecher = new LogLeecher("ballerina: 'colonsInName:WrongFunction' function not found in '"
                                                          + tempProjectDirTwo.resolve(fileName.replace("bal", "balx"))
                                                          + "'", LeecherType.ERROR);
        balClient.runMain(sourceArg, new LogLeecher[]{errLogLeecher});
        errLogLeecher.waitForText(2000);
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
