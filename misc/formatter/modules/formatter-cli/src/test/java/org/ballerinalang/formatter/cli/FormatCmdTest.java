/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.formatter.cli;

import io.ballerina.cli.launcher.BLauncherException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Format CLI tool test suit for testing tool's exceptions.
 */
public class FormatCmdTest {
    private static final Path RES_DIR = Paths.get("src").resolve("test").resolve("resources").toAbsolutePath();
    private static final String NOT_A_BAL_PROJECT = "notAProject";
    private static final String BAL_PROJECT = "project";

    private static final Path SINGLE_FILE_PROJECT_SOURCE =
            RES_DIR.resolve("SingleFileProject/source/singleFileProject.bal");
    private static final Path SINGLE_FILE_PROJECT_TEMP_SOURCE =
            RES_DIR.resolve("SingleFileProject/source/singleFileProjectTemp.bal");
    private static final Path SINGLE_FILE_PROJECT_ASSERT =
            RES_DIR.resolve("SingleFileProject/assert/singleFileProject.bal");

    private static final Path BALLERINA_PROJECT_SOURCE =
            RES_DIR.resolve("BallerinaProject/source/Project");
    private static final Path BALLERINA_PROJECT_TEMP_SOURCE =
            RES_DIR.resolve("BallerinaProject/source/ProjectTemp");
    private static final Path BALLERINA_PROJECT_ASSERT =
            RES_DIR.resolve("BallerinaProject/assert/Project");

    @Test(description = "Test single file project formatting")
    public void formatCLIOnASingleFileProject() {
        List<String> argList = new ArrayList<>();
        argList.add(SINGLE_FILE_PROJECT_SOURCE.toString());
        try {
            FormatUtil.execute(argList, false, null, null, false, null);
            Assert.assertEquals(Files.readString(SINGLE_FILE_PROJECT_SOURCE),
                    Files.readString(SINGLE_FILE_PROJECT_ASSERT));
            Files.copy(SINGLE_FILE_PROJECT_TEMP_SOURCE, SINGLE_FILE_PROJECT_SOURCE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Assert.fail("Failed to update the source file.");
        }
    }

    @Test(description = "Test ballerina project formatting")
    public void formatCLIOnBallerinaProject() {
        List<String> argList = new ArrayList<>();
        try {
            FormatUtil.execute(argList, false, null, null, false, BALLERINA_PROJECT_SOURCE);
            Assert.assertEquals(Files.readString(BALLERINA_PROJECT_SOURCE.resolve("main.bal")),
                    Files.readString(BALLERINA_PROJECT_ASSERT.resolve("main.bal")));
            Assert.assertEquals(Files.readString(BALLERINA_PROJECT_SOURCE.resolve("modules/util/util.bal")),
                    Files.readString(BALLERINA_PROJECT_ASSERT.resolve("modules/util/util.bal")));
            FileUtils.copyDirectory(BALLERINA_PROJECT_TEMP_SOURCE.toFile(), BALLERINA_PROJECT_SOURCE.toFile());
        } catch (IOException e) {
            Assert.fail("Failed to update the source file.");
        }
    }

    @Test(description = "Test ballerina project formatting with configurations",
            dataProvider = "provideConfigurationProjects")
    public void formatCLIOnBallerinaProjectWithConfigurations(String testCase, List<Path> dirs) {
        List<String> argList = new ArrayList<>();
        try {
            for (Path dir : dirs) {
                Path tempDir = dir.resolveSibling(dir.getFileName() + "Temp");
                Path assertDir = Paths.get(dir.toString().replace("/source/", "/assert/"));
                FormatUtil.execute(argList, false, null, null, false, dir);
                Assert.assertEquals(Files.readString(dir.resolve("main.bal")),
                        Files.readString(assertDir.resolve("main.bal")));
                FileUtils.copyDirectory(tempDir.toFile(), dir.toFile());
            }
        } catch (IOException e) {
            Assert.fail(testCase + " test failed to update the source file.");
        }
    }

    @DataProvider(name = "provideConfigurationProjects")
    private Object[][] provideConfigurationProjects() {
        Path basePath = RES_DIR.resolve(Path.of("configurations", "options"));
        return new Object[][]{
                {"brace", List.of(
                        basePath.resolve(Path.of("brace", "source", "project"))
                )},
                {"functionCall", List.of(
                        basePath.resolve(Path.of("functionCall", "source", "chopDown")),
                        basePath.resolve(Path.of("functionCall", "source", "noWrap")),
                        basePath.resolve(Path.of("functionCall", "source", "wrap"))
                )},
                {"functionDefinition", List.of(
                        basePath.resolve(Path.of("functionDefinition", "source", "chopDown")),
                        basePath.resolve(Path.of("functionDefinition", "source", "noWrap")),
                        basePath.resolve(Path.of("functionDefinition", "source", "wrap"))
                )},
                {"ifStatement", List.of(
                        basePath.resolve(Path.of("ifStatement", "source", "ifelse"))
                )},
                {"imports", List.of(
                        basePath.resolve(Path.of("imports", "source", "project"))
                )},
                {"indent", List.of(
                        basePath.resolve(Path.of("indent", "source", "project"))
                )},
                {"query", List.of(
                        basePath.resolve(Path.of("query", "source", "project"))
                )}
                ,
                {"spacing", List.of(
                        basePath.resolve(Path.of("spacing", "source", "project"))
                )},
                {"wrapping", List.of(
                        basePath.resolve(Path.of("wrapping", "source", "project"))
                )}
        };
    }

    @Test(description = "Test ballerina project formatting with dot op",
            dependsOnMethods = "formatCLIOnBallerinaProject")
    public void formatCLIOnBallerinaProjectWithDotOp() {
        List<String> argList = new ArrayList<>();
        argList.add(".");
        try {
            FormatUtil.execute(argList, false, null, null, false, BALLERINA_PROJECT_SOURCE);
            Assert.assertEquals(Files.readString(BALLERINA_PROJECT_SOURCE.resolve("main.bal")),
                    Files.readString(BALLERINA_PROJECT_ASSERT.resolve("main.bal")));
            Assert.assertEquals(Files.readString(BALLERINA_PROJECT_SOURCE.resolve("modules/util/util.bal")),
                    Files.readString(BALLERINA_PROJECT_ASSERT.resolve("modules/util/util.bal")));
            FileUtils.copyDirectory(BALLERINA_PROJECT_TEMP_SOURCE.toFile(), BALLERINA_PROJECT_SOURCE.toFile());
        } catch (IOException e) {
            Assert.fail("Failed to update the source file.");
        }
    }

    @Test(description = "Test to check the exception for too many argument provided.")
    public void formatCLITooManyArgumentsTest() {
        Path sourceRoot = RES_DIR.resolve(NOT_A_BAL_PROJECT);
        List<String> argList = new ArrayList<>();
        argList.add("pkg2");
        argList.add("asd");
        try {
            FormatUtil.execute(argList, false, null, null, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: " + Messages.getArgumentError(),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for not a ballerina project.")
    public void formatCLINotAProjectTest() {
        Path sourceRoot = RES_DIR.resolve(NOT_A_BAL_PROJECT);
        try {
            FormatUtil.execute(null, false, null, null, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertTrue(exception.get(0).contains("error: "),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for not a ballerina project when given a module name.")
    public void formatCLINotAProjectInModuleTest() {
        Path sourceRoot = RES_DIR.resolve(NOT_A_BAL_PROJECT);
        List<String> argList = new ArrayList<>();
        argList.add("pkg1");
        try {
            FormatUtil.execute(argList, false, null, null, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertTrue(exception.get(0).contains("error: "),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for no ballerina module found for a given module name.")
    public void formatCLINotAModuleTest() {
        Path sourceRoot = RES_DIR.resolve(BAL_PROJECT);
        List<String> argList = new ArrayList<>();
        argList.add("pkg2");
        try {
            FormatUtil.execute(argList, false, null, null, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertTrue(exception.get(0).contains("error: "),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for file that is not a ballerina file.")
    public void formatCLINotABalFileTest() {
        List<String> argList = new ArrayList<>();
        argList.add(RES_DIR.resolve("invalidFile.txt").toString());
        try {
            FormatUtil.execute(argList, false, null, null, false, RES_DIR);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: "
                                + Messages.getNotABallerinaFile(),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for no existing ballerina file.")
    public void formatCLINoBallerinaFileTest() {
        List<String> argList = new ArrayList<>();
        argList.add(RES_DIR.resolve("invalidFile.bal").toString());
        try {
            FormatUtil.execute(argList, false, null, null, false, RES_DIR);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: The file does not exist: "
                                + RES_DIR.resolve("invalidFile.bal"),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for no existing ballerina file or module.")
    public void formatCLINotABallerinaFileOrModuleTest() {
        Path sourceRoot = RES_DIR.resolve(BAL_PROJECT);
        List<String> argList = new ArrayList<>();
        argList.add("invalid.pkg2");
        try {
            FormatUtil.execute(argList, false, null, null, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertTrue(exception.get(0).contains("error: "),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for invalid source root path")
    public void formatCLIGeneralExceptionTest() {
        try {
            FormatUtil.execute(null, false, null, null, false, null);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertTrue(exception.get(0).contains("error: "),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check for multiple options")
    public void formatCLIMultipleOptionsTest() {
        List<String> argList = new ArrayList<>();
        argList.add(BAL_PROJECT);
        try {
            FormatUtil.execute(argList, false, "pkg1", "main.bal", false, RES_DIR);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: " + Messages.getCantAllowBothModuleAndFileOptions(),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check formatting a single file within a project")
    public void formatCLISingleFileInProjectTest() {
        List<String> argList = new ArrayList<>();
        argList.add(BAL_PROJECT);
        try {
            FormatUtil.execute(argList, false, null, "main.bal", false, RES_DIR);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertTrue(exception.get(0).contains("error: "),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test formatting of a module in a ballerina project with configurations")
    public void formatCLIOnBallerinaProjectWithModulesWithConfigurations() {
        List<String> argList = new ArrayList<>();
        String module = "core";
        Path sourceDir = RES_DIR.resolve(Path.of("configurations", "module"));
        Path projectDir = sourceDir.resolve(Path.of("source", "project"));
        Path assertDir = sourceDir.resolve("assert");
        Path moduleRelativePath = Path.of("modules", module, "core.bal");
        try {
            FormatUtil.execute(argList, false, module, null, false, projectDir);
            Assert.assertEquals(Files.readString(projectDir.resolve("main.bal")),
                    Files.readString(assertDir.resolve("main.bal")));
            Assert.assertEquals(Files.readString(projectDir.resolve(moduleRelativePath)),
                    Files.readString(assertDir.resolve(moduleRelativePath)));
            FileUtils.copyDirectory(sourceDir.resolve(Path.of("source", "projectTemp")).toFile(), projectDir.toFile());
        } catch (IOException e) {
            String exception = e.getMessage();
            Assert.assertTrue(exception.contains("error: "), "actual exception didn't match the expected.");
        }
    }

    @Test(description = "Test formatting of a module with specified ballerina project with configurations")
    public void formatCLIOnBallernaProjectWithModulesWithConfigurations() {
        List<String> argList = new ArrayList<>();
        argList.add("project");
        String module = "mod";
        Path sourceDir = RES_DIR.resolve(Path.of("configurations", "projectWithModule"));
        Path projectDir = sourceDir.resolve(Path.of("source", "project"));
        Path assertDir = sourceDir.resolve("assert");
        Path moduleRelativePath = Path.of("modules", module, "mod.bal");
        try {
            FormatUtil.execute(argList, false, module, null, false, sourceDir.resolve("source"));
            Assert.assertEquals(Files.readString(projectDir.resolve("main.bal")),
                    Files.readString(assertDir.resolve("main.bal")));
            Assert.assertEquals(Files.readString(projectDir.resolve(moduleRelativePath)),
                    Files.readString(assertDir.resolve(moduleRelativePath)));
            FileUtils.copyDirectory(sourceDir.resolve(Path.of("source", "projectTemp")).toFile(), projectDir.toFile());
        } catch (IOException e) {
            String exception = e.getMessage();
            Assert.assertTrue(exception.contains("error: "), "actual exception didn't match the expected.");
        }
    }

}
