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

package org.ballerinalang.test.isolated;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Tests isolated inference when the module contains tests.
 *
 * @since 2.0.0
 */
public class IsolatedInferenceWithTestsTest extends BaseTest {

    private static final String testFileLocation =
            Paths.get("src", "test", "resources", "isolated-inference-projects")
                    .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test(dataProvider = "pkgNames")
    public void testIsolatedInferenceWithTests(String pkgName, int testCount) throws BallerinaTestException {
        testPkg(pkgName, testCount);
    }

    @DataProvider
    public Object[][] pkgNames() {
        return new Object[][] {
                {"foo", 2},
                {"bar", 2},
                {"baz", 2},
                {"qux", 12}
        };
    }

    private void testPkg(String pkg, int testCount) throws BallerinaTestException {
        LogLeecher passedLeecher = new LogLeecher(testCount + " passing");
        LogLeecher failedLeecher = new LogLeecher("0 failing");
        LogLeecher jarGenerationLeecher = new LogLeecher("target/bin/" + pkg + ".jar");
        bMainInstance.runMain("build", new String[]{"--with-tests"}, null, null,
                              new LogLeecher[]{passedLeecher, failedLeecher, jarGenerationLeecher},
                              Paths.get(testFileLocation, pkg).toString());
        passedLeecher.waitForText(5000);
        failedLeecher.waitForText(5000);
        jarGenerationLeecher.waitForText(5000);
    }
}
