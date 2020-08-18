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

import org.ballerinalang.tool.BLauncherException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Format CLI tool test suit for testing tool's exceptions.
 */
public class FormatCmdTest {
    private static final Path RES_DIR = Paths.get("src").resolve("test").resolve("resources").toAbsolutePath();
    private static final String NOT_A_PROJECT = "notAProject";

    @Test(description = "Test to check the exception for too many argument provided.")
    public void formatCLITooManyArgumentsTest() {
        Path sourceRoot = RES_DIR.resolve(NOT_A_PROJECT);
        List<String> argList = new ArrayList<>();
        argList.add("pkg2");
        argList.add("asd");
        try {
            FormatUtil.execute(argList, false, false, sourceRoot);
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
        Path sourceRoot = RES_DIR.resolve(NOT_A_PROJECT);
        try {
            FormatUtil.execute(null, false, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: " + Messages.getNotBallerinaProject(),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for not a ballerina project when given a module name.")
    public void formatCLINotAProjectInModuleTest() {
        Path sourceRoot = RES_DIR.resolve(NOT_A_PROJECT);
        List<String> argList = new ArrayList<>();
        argList.add("pkg1");
        try {
            FormatUtil.execute(argList, false, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: " + Messages.getNotBallerinaProject(),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for no ballerina module found for a given module name.")
    public void formatCLINotAModuleTest() {
        Path sourceRoot = RES_DIR.resolve("project");
        List<String> argList = new ArrayList<>();
        argList.add("pkg2");
        try {
            FormatUtil.execute(argList, false, false, sourceRoot);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: " + Messages.getNoModuleFound("pkg2"),
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
            FormatUtil.execute(argList, false, false, RES_DIR);
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
            FormatUtil.execute(argList, false, false, RES_DIR);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: "
                                + Messages.getNoBallerinaFile(RES_DIR.resolve("invalidFile.bal").toString()),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for no existing ballerina file or module.")
    public void formatCLINotABallerinaFileOrModuleTest() {
        List<String> argList = new ArrayList<>();
        argList.add("invalid.pkg2");
        try {
            FormatUtil.execute(argList, false, false, RES_DIR);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertEquals(exception.get(0), "error: "
                                + Messages.getNoBallerinaModuleOrFile("invalid.pkg2"),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }

    @Test(description = "Test to check the exception for general error in File IO or a argument.")
    public void formatCLIGeneralExceptionTest() {
        try {
            FormatUtil.execute(null, false, false, null);
        } catch (BLauncherException e) {
            List<String> exception = e.getMessages();
            if (exception.size() == 1) {
                Assert.assertTrue(exception.get(0).contains("error: " + Messages.getException()),
                        "actual exception didn't match the expected.");
            } else {
                Assert.fail("failed the test with " + exception.size()
                        + " exceptions where there needs to be 1 exception");
            }
        }
    }
}
