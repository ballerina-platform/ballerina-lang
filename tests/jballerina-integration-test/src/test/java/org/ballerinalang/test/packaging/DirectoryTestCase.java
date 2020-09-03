/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;

/**
 * Test case for running bal files inside sub directories.
 */
public class DirectoryTestCase extends BaseTest {
    private BMainInstance balClient;
    private Path testProjectPath;

    /**
     * Copy the TestProject1 to a temp folder.
     *
     * @throws BallerinaTestException When creating the ballerina client.
     */
    @BeforeClass(enabled = false)
    public void setUp() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
    }

    /**
     * Run TestProject which has main file inside abc directory and check whether the expected output received.
     *
     * @throws BallerinaTestException When running commands.
     */
    @Test(enabled = false, description = "Test building and running TestProject")
    public void testRunDirectory() throws BallerinaTestException {
        testProjectPath = Paths.get("src", "test", "resources", "packaging", "executable", "TestProject")
                               .toAbsolutePath();
        // Run and see output
        String msg = "Main inside directory and File name with . and File name with -";
        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[] { "foo" }, new HashMap<>(), new String[0],
                          new LogLeecher[] { fooRunLeecher }, testProjectPath.toString());
        fooRunLeecher.waitForText(10000);
    }

    @AfterClass(enabled = false)
    private void cleanup() throws Exception {
        deleteFiles(Paths.get(this.testProjectPath.toString(), "target").toAbsolutePath());
    }
}
