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
    Path tempStandAloneFileInTmpDir;
    Path buildProjectPath;
    Path balaProjectPath;

    @BeforeClass
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("ballerina-test-" + System.nanoTime());

        // Create a build project
        buildProjectPath = tempDir.resolve("testProj");
        // Create default module
        Files.createDirectories(buildProjectPath.resolve("tests"));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.BALLERINA_TOML));
        Files.createFile(buildProjectPath.resolve("main.bal"));
        Files.createFile(buildProjectPath.resolve("tests").resolve("main_test.bal"));

        // Create another module
        Files.createDirectories(buildProjectPath.resolve("modules").resolve("module1").resolve("tests"));
        Files.createFile(buildProjectPath.resolve("modules").resolve("module1").resolve("main.bal"));
        Files.createFile(
                buildProjectPath.resolve("modules").resolve("module1").resolve("tests").resolve("main_test.bal"));

        // Create standalone files outside project directory
        Files.createFile(tempDir.resolve("test.bal")); // path - /tmp/ballerina-test-223233/test.bal
        tempStandAloneFileInTmpDir = Files.createTempFile("temp-test", ".bal"); // path - /tmp/temp-test.bal

        // Create standalone file inside project directory
        Files.createDirectory(buildProjectPath.resolve("test-utils"));
        Files.createFile(buildProjectPath.resolve("test-utils")
                .resolve("utils.bal")); // path - /tmp/testProj/test-utils/utils.bal

        // Create a bala project
        balaProjectPath = tempDir.resolve("testBalaProj");
        Files.createDirectories(balaProjectPath.resolve("modules").resolve("mod1"));
        Files.createFile(balaProjectPath.resolve(ProjectConstants.PACKAGE_JSON));
        Files.createFile(balaProjectPath.resolve(ProjectConstants.BALA_JSON));
        Files.createFile(balaProjectPath.resolve("modules").resolve("mod1").resolve("mod1.bal"));
    }

    @Test
    public void testPackageRoot() {
        // test package root of build project
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.BALLERINA_TOML)), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("main.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("tests").resolve("main_test.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("modules").resolve("module1").resolve("main.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("modules").resolve("module1").resolve("tests").resolve("main_test.bal")), buildProjectPath);

        // test package root of bala project
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath
                .resolve("modules").resolve("mod1").resolve("mod1.bal")), balaProjectPath);
    }

    @Test
    public void testPackageRootWithDirectory() {
        // test package root of build project
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("tests")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("modules").resolve("module1")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("modules").resolve("module1").resolve("tests")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("modules")), buildProjectPath);

        // test package root of bala project
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath), balaProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath
                .resolve("modules").resolve("mod1")), balaProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath
                .resolve("modules")), balaProjectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative() {
        Assert.assertEquals(ProjectPaths.packageRoot(Paths.get("/tmp")), buildProjectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative2() {
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath.resolve("test-utils").resolve("utils.bal")),
                buildProjectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative3() {
        Assert.assertEquals(ProjectPaths.packageRoot(tempDir.resolve("test.bal")), buildProjectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative4() {
        Assert.assertEquals(ProjectPaths.packageRoot(tempStandAloneFileInTmpDir), buildProjectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative5() {
        ProjectPaths.packageRoot(balaProjectPath.resolve(ProjectConstants.PACKAGE_JSON));
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative6() {
        ProjectPaths.packageRoot(tempDir);
    }

    @Test
    public void testIsBallerinaSourceFile() {
        Assert.assertTrue(ProjectPaths.isBalFile(buildProjectPath.resolve("main.bal")));
        Assert.assertTrue(ProjectPaths.isBalFile(tempDir.resolve("test.bal")));
        Assert.assertTrue(ProjectPaths.isBalFile(tempStandAloneFileInTmpDir));

        Assert.assertFalse(ProjectPaths.isBalFile(buildProjectPath.resolve("Ballerina.toml")));
        Assert.assertFalse(ProjectPaths.isBalFile(buildProjectPath));
        Assert.assertFalse(ProjectPaths.isBalFile(Paths.get("/tmp/non-existent-path")));
    }

    @Test
    public void testIsBallerinaStandaloneFile() {
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(buildProjectPath));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(buildProjectPath.resolve("main.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(buildProjectPath.resolve("Ballerina.toml")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                buildProjectPath.resolve("modules").resolve("module1").resolve("main.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                buildProjectPath.resolve("modules").resolve("module1").resolve("tests").resolve("main_test.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(Paths.get("/tmp/non-existent-path")));

        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(
                buildProjectPath.resolve("test-utils").resolve("utils.bal")));
        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(tempDir.resolve("test.bal")));

        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                balaProjectPath.resolve("modules").resolve("mod1").resolve("mod1.bal")));
    }

    @Test
    public void testIsBallerinaStandAloneFileInTmpDirRoot() throws IOException {
        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(tempStandAloneFileInTmpDir));
        Path ballerinaToml = Files.createFile(tempStandAloneFileInTmpDir.getParent().resolve("Ballerina.toml"));
        boolean standaloneBalFile = ProjectPaths.isStandaloneBalFile(tempStandAloneFileInTmpDir);
        Files.deleteIfExists(ballerinaToml);
        Assert.assertFalse(standaloneBalFile);
    }

    @AfterClass (alwaysRun = true)
    public void cleanUp() {
        FileUtil.deleteDirectory(tempDir);
    }
}
