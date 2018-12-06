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

package org.ballerinalang.test.packaging;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Integration test cases for module imports from test sources.
 *
 * @since 0.985
 */
public class ImportFromTestsCase extends BaseTest {
    private Path tempProjectDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-import-from-tests-project-");

        String projectPath = (new File("src/test/resources/import-from-test")).getAbsolutePath();
        FileUtils.copyDirectory(Paths.get(projectPath).toFile(), tempProjectDirectory.toFile());
        Files.createDirectories(tempProjectDirectory.resolve(".ballerina"));

        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test importing modules among test sources in the same project")
    public void testImportingModulesFromTestSources() throws BallerinaTestException {
        LogLeecher leecher = new LogLeecher("Addition is == 100", LogLeecher.LeecherType.INFO);
        balClient.runMain("build", new String[]{}, envVariables, new String[]{}, new LogLeecher[]{leecher},
                          tempProjectDirectory.toString());

        Path genPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                    ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, "my-org", "foo", "1.0.0");
        Assert.assertTrue(Files.exists(tempProjectDirectory.resolve(genPkgPath)));
        Assert.assertTrue(Files.exists(tempProjectDirectory.resolve(genPkgPath).resolve("foo.zip")));
        Assert.assertTrue(Files.exists(tempProjectDirectory.resolve("target").resolve("foo.balx")));

    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
