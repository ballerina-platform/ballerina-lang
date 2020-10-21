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
import org.wso2.ballerinalang.programfile.ProgramFileConstants;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

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
    @BeforeClass()
    public void setUp() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
    }

    /**
     * Run TestProject which has main file inside abc directory and check whether the expected output received.
     *
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test building and running TestProject")
    public void testRunDirectory() throws BallerinaTestException {
        testProjectPath = Paths.get("src", "test", "resources", "packaging", "executable", "TestProject")
                               .toAbsolutePath();
        String fooModuleBaloFileName = "foo-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-9.9.9"
                + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooModuleJarFileName = "foo" + BLANG_COMPILED_JAR_EXT;
        // Run and see output
        String buildMsg1 = "target" + File.separator + "balo" + File.separator + fooModuleBaloFileName;
        String buildMsg2 = "target" + File.separator + "bin" + File.separator + fooModuleJarFileName;

        LogLeecher fooBaloLeecher = new LogLeecher(buildMsg1);
        LogLeecher fooJarLeecher = new LogLeecher(buildMsg2);
        balClient.runMain("build", new String[] { "foo" }, new HashMap<>(), new String[0],
                          new LogLeecher[] { fooBaloLeecher, fooJarLeecher}, testProjectPath.toString());
        fooBaloLeecher.waitForText(10000);
        fooJarLeecher.waitForText(10000);
    }

    @AfterClass()
    private void cleanup() throws Exception {
        deleteFiles(Paths.get(this.testProjectPath.toString(), "target").toAbsolutePath());
    }
}
