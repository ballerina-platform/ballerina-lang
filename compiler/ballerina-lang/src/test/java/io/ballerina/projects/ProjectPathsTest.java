/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.projects.utils.FileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests the ProjectPaths util.
 */
public class ProjectPathsTest {
    Path tempDir;
    Path projectPath;

    @BeforeClass
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("ballerina-test-" + System.nanoTime());
        projectPath = tempDir.resolve("testProj");
        Files.createFile(tempDir.resolve("test.bal"));

        Files.createDirectories(projectPath.resolve("tests"));
        Files.createFile(projectPath.resolve(ProjectConstants.BALLERINA_TOML));
        Files.createFile(projectPath.resolve("main.bal"));
        Files.createFile(projectPath.resolve("tests").resolve("main_test.bal"));

        Files.createDirectories(projectPath.resolve("modules").resolve("module1").resolve("tests"));
        Files.createFile(projectPath.resolve("modules").resolve("module1").resolve("main.bal"));
        Files.createFile(projectPath.resolve("modules").resolve("module1").resolve("tests").resolve("main_test.bal"));

        Files.createDirectory(projectPath.resolve("test-utils"));
        Files.createFile(projectPath.resolve("test-utils").resolve("utils.bal"));
    }

    @Test
    public void testPackageRoot() {
        Assert.assertEquals(ProjectPaths.packageRoot(projectPath
                .resolve(ProjectConstants.BALLERINA_TOML)), projectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(projectPath
                .resolve("main.bal")), projectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(projectPath
                .resolve("tests").resolve("main_test.bal")), projectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(projectPath
                .resolve("modules").resolve("module1").resolve("main.bal")), projectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(projectPath
                .resolve("modules").resolve("module1").resolve("tests").resolve("main_test.bal")), projectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative() {
        Assert.assertEquals(ProjectPaths.packageRoot(projectPath), projectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative2() {
        Assert.assertEquals(ProjectPaths.packageRoot(projectPath.resolve("test-utils").resolve("utils.bal")),
                projectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative3() {
        Assert.assertEquals(ProjectPaths.packageRoot(tempDir.resolve("test.bal")), projectPath);
    }

    @Test
    public void testIsBallerinaSourceFile() {
        Assert.assertTrue(ProjectPaths.isBalFile(projectPath.resolve("main.bal")));
        Assert.assertTrue(ProjectPaths.isBalFile(tempDir.resolve("test.bal")));

        Assert.assertFalse(ProjectPaths.isBalFile(projectPath.resolve("Ballerina.toml")));
        Assert.assertFalse(ProjectPaths.isBalFile(projectPath));
        Assert.assertFalse(ProjectPaths.isBalFile(Paths.get("/tmp/non-existent-path")));
    }

    @Test
    public void testIsBallerinaStandaloneFile() {
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(projectPath));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(projectPath.resolve("main.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(projectPath.resolve("Ballerina.toml")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                projectPath.resolve("modules").resolve("module1").resolve("main.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                projectPath.resolve("modules").resolve("module1").resolve("tests").resolve("main_test.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(Paths.get("/tmp/non-existent-path")));

        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(
                projectPath.resolve("test-utils").resolve("utils.bal")));
        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(tempDir.resolve("test.bal")));
    }

    @AfterClass
    public void cleanUp() {
        FileUtil.deleteDirectory(tempDir);
    }
}
