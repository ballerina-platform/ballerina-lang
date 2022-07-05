/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.async;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Class to test functionality of futures.
 */
public class AsyncFunctionsTest extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "async")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        // Build and push config Lib project.
        LogLeecher buildLeecher = new LogLeecher("target/bala/testOrg-functionsLib-java11-0.1.0.bala");
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed target/bala/testOrg-functionsLib-java11-0.1.0" +
                ".bala to 'local' repository.");
        LogLeecher runLeecher = new LogLeecher("Run the library package to fix code coverage");
        bMainInstance.runMain(testFileLocation + "/", "functionsLib", null, new String[]{}, null, null,
                new LogLeecher[]{runLeecher});
        runLeecher.waitForText(5000);
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                testFileLocation + "/functionsLib");
        buildLeecher.waitForText(5000);
        bMainInstance.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                testFileLocation + "/functionsLib");
        pushLeecher.waitForText(5000);
    }

    @Test
    public void testRunFunctionsFromDifferentPackageAsynchronously() throws BallerinaTestException {
        String testsPassed = "Tests passed";
        LogLeecher logLeecher = new LogLeecher(testsPassed);
        bMainInstance.runMain(testFileLocation + "/", "asyncFunctionPackage", null, new String[]{}, null, null,
                new LogLeecher[]{logLeecher});
        logLeecher.waitForText(5000);
    }
}
