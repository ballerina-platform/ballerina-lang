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
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test case for running bal files.
 */
public class SingleBalFileTestCase extends BaseTest {

    /**
     * Run bal with relative path.
     *
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test running bal with relative path")
    public void testRunWithRelativePath() throws BallerinaTestException {
        BMainInstance balClient = new BMainInstance(balServer);
        String testBalFile = "hello_world.bal";
        Path testRelativeFilePath = Paths.get("src", "test", "resources", "packaging", "singleBalFile",
                "relative", "testDir");
        // Run and see output
        String msg = "Hello, World!";
        LogLeecher fooRunLeecher = new LogLeecher(msg);
        String runCommandPath =  ".." + File.separator + "testBal" + File.separator + testBalFile;
        balClient.runMain("run", new String[]{runCommandPath}, new HashMap<>(), new String[0],
                new LogLeecher[]{fooRunLeecher}, testRelativeFilePath.toString());
        fooRunLeecher.waitForText(10000);
    }
}
