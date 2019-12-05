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
 * Test case for running multiple bal files which imports undefined modules.
 */
public class UndefinedModuleNegativeTestCase extends BaseTest {
    private BMainInstance balClient;
    private Path testProjectPath;

    @BeforeClass()
    public void setUp() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test building bal files with undefined modules")
    public void testBuildWithUndefinedModules() throws BallerinaTestException {
        testProjectPath = Paths.get("src", "test", "resources", "packaging", "executable", "TestProject2")
                               .toAbsolutePath();
        // Run and see output
        String msg1 = "error: bar/foo:9.9.9::b-bar.bal:16:1: cannot resolve module 'gg/y'";
        String msg2 = "error: bar/foo:9.9.9::f-foo.bal:16:1: cannot resolve module 'gg/y'";
        LogLeecher fooRunLeecher = new LogLeecher(msg1, LogLeecher.LeecherType.ERROR);
        LogLeecher barRunLeecher = new LogLeecher(msg2, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[] { "foo" }, new HashMap<>(), new String[0],
                          new LogLeecher[] { fooRunLeecher, barRunLeecher}, testProjectPath.toString());
        fooRunLeecher.waitForText(1000);
        barRunLeecher.waitForText(1000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        deleteFiles(Paths.get(this.testProjectPath.toString(), "target").toAbsolutePath());
    }
}
