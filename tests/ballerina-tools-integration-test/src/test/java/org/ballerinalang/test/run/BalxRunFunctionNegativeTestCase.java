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

    private String sourceRoot = (new File("src/test/resources/run/balx/")).getAbsolutePath();

    private Path tempProjectDir;
    private String balxPath;

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
    public void testEmptyEntryFunctionName() throws BallerinaTestException {
        String sourceArg = balxPath + ":";
        LogLeecher errLogLeecher = new LogLeecher("ballerina: expected function name after final ':'",
                LeecherType.ERROR);
        balClient.runMain(sourceArg, new LogLeecher[]{errLogLeecher});
        errLogLeecher.waitForText(2000);
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
