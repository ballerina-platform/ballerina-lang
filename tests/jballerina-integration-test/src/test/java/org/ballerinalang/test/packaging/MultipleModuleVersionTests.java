/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Tests usage of multiple package versions.
 */
public class MultipleModuleVersionTests extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "packaging", "versions")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        // Build and push down stream packages.
        compilePackageAndPushToLocal(Paths.get(testFileLocation, "http1.1.1").toString(), "waruna-http-any-1.1.1");
        compilePackageAndPushToLocal(Paths.get(testFileLocation, "http1.1.2").toString(), "waruna-http-any-1.1.2");
        compilePackageAndPushToLocal(Paths.get(testFileLocation, "websub").toString(), "waruna-websub-any-1.0.1");
    }

    private void compilePackageAndPushToLocal(String packagePath, String balaFileName) throws BallerinaTestException {
        LogLeecher buildLeecher = new LogLeecher("target/bala/" + balaFileName + ".bala");
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed target/bala/" + balaFileName + ".bala to " +
                "'local' repository.");
        bMainInstance.runMain("build", new String[]{"-c"}, null, null, new LogLeecher[]{buildLeecher},
                packagePath);
        buildLeecher.waitForText(5000);
        bMainInstance.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                packagePath);
        pushLeecher.waitForText(5000);
    }

    @Test
    public void testUsageOfOldPackageVersion() throws BallerinaTestException {
        executeBalCommand("myPackage");
    }

    @Test
    public void testUsageOfNewPackageVersion() throws BallerinaTestException {
        executeBalCommand("myPackage2");
    }

    private void executeBalCommand(String packageName) throws BallerinaTestException {
        String testsPassed = "Tests passed";
        LogLeecher logLeecher = new LogLeecher(testsPassed);
        bMainInstance.runMain(testFileLocation + "/", packageName, null, new String[]{}, null, null,
                new LogLeecher[]{logLeecher});
        logLeecher.waitForText(5000);
    }
}
