/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.nio.file.Paths;

/**
 * Parent test class for all integration test cases under test-integration module. This will provide basic
 * functionality for integration test. This will initialize a single ballerina instance which will be used
 * by all the test cases throughout.
 */
public class BaseTest {

    public static BalServer balServer;
    private BMainInstance bMainInstance;

    private static final String testFileLocation = Paths.get("../misc/io-internal/src/main/ballerina").toAbsolutePath()
            .toAbsolutePath().toString();

    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException {
        balServer = new BalServer();
        bMainInstance = new BMainInstance(balServer);
        compilePackageAndPushToLocal(testFileLocation, "ballerinai-io" +
                "-java11-0.0.0");
    }

    @AfterSuite(alwaysRun = true)
    public void destroy() throws BallerinaTestException {
        balServer.cleanup();
    }

    private void compilePackageAndPushToLocal(String packagePath, String balaFileName) throws BallerinaTestException {
        LogLeecher buildLeecher = new LogLeecher("target/bala/" + balaFileName + ".bala");
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed target/bala/" + balaFileName + ".bala to " +
                "'local' repository.");
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                packagePath);
        buildLeecher.waitForText(5000);
        bMainInstance.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                packagePath);
        pushLeecher.waitForText(5000);
    }
}
