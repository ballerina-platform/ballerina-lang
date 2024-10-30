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
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.TEST_DIR_NAME));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.BALLERINA_TOML));
        Files.createFile(buildProjectPath.resolve("main.bal"));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.TEST_DIR_NAME).resolve("main_test.bal"));

        // Create another module
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("module1")
                .resolve(ProjectConstants.TEST_DIR_NAME));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("module1/main.bal"));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("module1")
                        .resolve(ProjectConstants.TEST_DIR_NAME).resolve("main_test.bal"));

        // Create standalone files outside project directory
        Files.createFile(tempDir.resolve("test.bal")); // path - /tmp/ballerina-test-223233/test.bal
        tempStandAloneFileInTmpDir = Files.createTempFile("temp-test", ".bal"); // path - /tmp/temp-test.bal

        // Create standalone file inside project directory
        Files.createDirectory(buildProjectPath.resolve("test-utils"));
        Files.createFile(buildProjectPath.resolve("test-utils/utils.bal")); // path - /tmp/testProj/test-utils/utils.bal

        // Create generated files
        // Generated file for default module
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT));
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).
                resolve(ProjectConstants.TEST_DIR_NAME));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("gen.bal"));
        // Generate test file for default module
        Files.createFile(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT)
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("gen_test.bal"));

        // Generated file for module 01
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module1"));
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module1")
                .resolve(ProjectConstants.TEST_DIR_NAME));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module1")
                .resolve("gen_mod1.bal"));
        // Generate test file for module 01
        Files.createFile(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module1")
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("gen_test_mod1.bal"));

        // Generated file for module 02 (A new generated module)
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module2"));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module2")
                .resolve("gen_mod2.bal"));

        // Generated test file for module 03 (A new generated module)
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module3"));
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module3")
                .resolve(ProjectConstants.TEST_DIR_NAME));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module3")
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("gen_test_mod3.bal"));

        // Create a tool specification file
        Files.createDirectories(buildProjectPath.resolve(ProjectConstants.PERSIST_DIR_NAME));
        Files.createFile(buildProjectPath.resolve(ProjectConstants.PERSIST_DIR_NAME).resolve("model.bal"));

        // Create a file that isn't a tool specification file
        Files.createDirectories(buildProjectPath.resolve("invalid-tool"));
        Files.createFile(buildProjectPath.resolve("invalid-tool").resolve("model.bal"));

        // Create a bala project
        balaProjectPath = tempDir.resolve("testBalaProj");
        Files.createDirectories(balaProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("mod1"));
        Files.createFile(balaProjectPath.resolve(ProjectConstants.PACKAGE_JSON));
        Files.createFile(balaProjectPath.resolve(ProjectConstants.BALA_JSON));
        Files.createFile(balaProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("mod1/mod1.bal"));
    }

    @Test
    public void testPackageRoot() {
        // test package root of build project
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.BALLERINA_TOML)), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve("main.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("main_test.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.MODULES_ROOT).resolve("module1/main.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.MODULES_ROOT).resolve("module1")
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("main_test.bal")), buildProjectPath);

        // test package root of generated files
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.GENERATED_MODULES_ROOT)
                .resolve("module1/gen_mod1.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module2/gen_mod2.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                        .resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("gen.bal")), buildProjectPath);

        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.MODULES_ROOT).resolve("module1").resolve(ProjectConstants.TEST_DIR_NAME)
                .resolve("main_test.bal")), buildProjectPath);
        // test package root of bala project
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath
                .resolve(ProjectConstants.MODULES_ROOT).resolve("mod1/mod1.bal")), balaProjectPath);

        // test package root of generated test files
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module1")
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("gen_test_mod1.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT)
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("gen_test.bal")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module3")
                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("gen_test_mod3.bal")), buildProjectPath);

        // test package root of tool specification file
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.PERSIST_DIR_NAME).resolve("model.bal")), buildProjectPath);
        Path invalidToolSpecPath = buildProjectPath.resolve("invalid-tool").resolve("model.bal");
        try {
            ProjectPaths.packageRoot(invalidToolSpecPath);
            Assert.fail("Expected ProjectException was not thrown");
        } catch (ProjectException e) {
            Assert.assertEquals(e.getMessage(), "provided file path does not belong to a Ballerina package: " +
                    invalidToolSpecPath);
        }
    }

    @Test
    public void testPackageRootWithDirectory() {
        // test package root of build project
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.TEST_DIR_NAME)), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.MODULES_ROOT).resolve("module1")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.MODULES_ROOT).resolve("module1").
                resolve(ProjectConstants.TEST_DIR_NAME)), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.MODULES_ROOT)), buildProjectPath);
        // Generated source directories
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.GENERATED_MODULES_ROOT)), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module1")), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath
                .resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module2")), buildProjectPath);
        // Generated tests directories
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT)
                .resolve(ProjectConstants.TEST_DIR_NAME)), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT)
                .resolve("module1").resolve(ProjectConstants.TEST_DIR_NAME)), buildProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).
                resolve("module3").resolve(ProjectConstants.TEST_DIR_NAME)), buildProjectPath);
        // test package root of bala project
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath), balaProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath
                .resolve(ProjectConstants.MODULES_ROOT).resolve("mod1")), balaProjectPath);
        Assert.assertEquals(ProjectPaths.packageRoot(balaProjectPath
                .resolve(ProjectConstants.MODULES_ROOT)), balaProjectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative() {
        Assert.assertEquals(ProjectPaths.packageRoot(Path.of("/tmp")), buildProjectPath);
    }

    @Test(expectedExceptions = ProjectException.class)
    public void testPackageRootNegative2() {
        Assert.assertEquals(ProjectPaths.packageRoot(buildProjectPath.resolve("test-utils/utils.bal")),
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
        Assert.assertFalse(ProjectPaths.isBalFile(Path.of("/tmp/non-existent-path")));
    }

    @Test
    public void testIsBallerinaStandaloneFile() {
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(buildProjectPath));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(buildProjectPath.resolve("main.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(buildProjectPath.resolve("Ballerina.toml")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                buildProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("module1/main.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                buildProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("module1")
                                .resolve(ProjectConstants.TEST_DIR_NAME).resolve("main_test.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(Path.of("/tmp/non-existent-path")));

        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(buildProjectPath.resolve("test-utils/utils.bal")));
        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(tempDir.resolve("test.bal")));

        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                balaProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("mod1/mod1.bal")));

        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(
                balaProjectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve("module1")
                        .resolve("gen_mod1.bal")));
        Assert.assertFalse(ProjectPaths.isStandaloneBalFile(balaProjectPath.resolve(
                ProjectConstants.GENERATED_MODULES_ROOT).resolve("module3").resolve(
                        ProjectConstants.TEST_DIR_NAME).resolve("gen_test_mod3.bal")));
    }

    @Test
    public void testIsBallerinaStandAloneFileInTmpDirRoot() throws IOException {
        Assert.assertTrue(ProjectPaths.isStandaloneBalFile(tempStandAloneFileInTmpDir));
        Path ballerinaToml = Files.createFile(tempStandAloneFileInTmpDir.getParent().resolve("Ballerina.toml"));
        boolean standaloneBalFile = ProjectPaths.isStandaloneBalFile(tempStandAloneFileInTmpDir);
        Files.deleteIfExists(ballerinaToml);
        Assert.assertFalse(standaloneBalFile);
    }

    @Test
    public void testIsToolSpecificationBalFile() {
        Assert.assertTrue(ProjectPaths.isToolSpecificationBalFile(buildProjectPath
                .resolve(ProjectConstants.PERSIST_DIR_NAME).resolve("model.bal")));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(buildProjectPath
                .resolve("invalid-tool").resolve("model.bal")));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(buildProjectPath.resolve("main.bal")));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(buildProjectPath.resolve("Ballerina.toml")));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(
                buildProjectPath.resolve(ProjectConstants.TEST_DIR_NAME).resolve("main_test.bal")));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(tempStandAloneFileInTmpDir));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(
                buildProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("module1/main.bal")));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(
                buildProjectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("module1")
                        .resolve(ProjectConstants.TEST_DIR_NAME).resolve("main_test.bal")));
        Assert.assertFalse(ProjectPaths.isToolSpecificationBalFile(tempDir.resolve("test.bal")));
    }

    @AfterClass (alwaysRun = true)
    public void cleanUp() {
        FileUtil.deleteDirectory(tempDir);
    }
}
