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

    private static final String PRINT_RETURN = "--printreturn";

    private String sourceRoot = (new File("src/test/resources/run/balx/")).getAbsolutePath();

    private Path tempProjectDir;
    private String balxPath;
    private String sourceArg;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDir = Files.createTempDirectory("temp-entry-func-test");
        String fileName = "test_entry_function.bal";
        String[] clientArgs = {"-o", tempProjectDir.toString().concat(File.separator).concat("entry"),
                                sourceRoot.concat(File.separator).concat(fileName)};
        balClient.runMain("build", clientArgs, null, new String[0],
                new LogLeecher[0], tempProjectDir.toString());
        Path generatedBalx = tempProjectDir.resolve("entry.balx");
        balxPath = generatedBalx.toString();
    }

    @Test
    public void testNoArg() throws BallerinaTestException {
        String functionName = "noParamEntry";
        sourceArg = balxPath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("1");
        balClient.runMain(sourceArg, new String[]{PRINT_RETURN}, new String[0], new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testMultipleParam() throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        sourceArg = balxPath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("integer: 1000, float: 1.0, string: Hello Ballerina, byte: 255, "
                                                          + "boolean: true, JSON Name Field: Maryam, XML Element Name: "
                                                          + "book, Employee Name Field: Em, string rest args: just the"
                                                          + " rest ");
        balClient.runMain(sourceArg, new String[]{PRINT_RETURN}, new String[]{"1000", "1.0",
                        "Hello Ballerina", "255", "true", "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>",
                        "{ \"name\": \"Em\" }", "just", "the", "rest"}, new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        try {
            deleteFiles(tempProjectDir);
        } catch (IOException e) {
            throw new BallerinaTestException("Error deleting files");
        }
    }
}
