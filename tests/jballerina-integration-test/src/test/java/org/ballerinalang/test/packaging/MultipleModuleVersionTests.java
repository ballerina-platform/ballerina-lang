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

import java.nio.file.Path;


/**
 * Tests usage of multiple package versions.
 */
public class MultipleModuleVersionTests extends BaseTest {

    private static final Path testFileLocation = Path.of("src/test/resources/packaging/versions");
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        // Build and push down stream packages.
        compilePackageAndPushToLocal(testFileLocation.resolve("http1.1.1"), "waruna-http-any-1.1.1");
        compilePackageAndPushToLocal(testFileLocation.resolve("http1.1.2"), "waruna-http-any-1.1.2");
        compilePackageAndPushToLocal(testFileLocation.resolve("websub"), "waruna-websub-any-1.0.1");
    }

    private void compilePackageAndPushToLocal(Path packagePath, String balaFileName) throws BallerinaTestException {
        String targetBala = Path.of("target/bala/" + balaFileName + ".bala").toString();
        LogLeecher buildLeecher = new LogLeecher(targetBala);
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed " + targetBala + " to 'local' repository.");
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
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
        bMainInstance.runMain(testFileLocation, packageName, null, new String[]{}, null, null,
                new LogLeecher[]{logLeecher});
        logLeecher.waitForText(5000);
    }
}
