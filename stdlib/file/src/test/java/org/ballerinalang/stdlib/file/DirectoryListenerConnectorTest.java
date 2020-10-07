/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.file;

import org.awaitility.Awaitility;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Test class for Directory Listener connector.
 */
@Test()
public class DirectoryListenerConnectorTest {

    private File rootDirectory;
    private Path testResourceRoot;

    @BeforeClass
    public void init() {
        try {
            Path rootListenFolderPath = Files.createDirectory(Paths.get("src", "test", "resources", "fs"));
            rootDirectory = rootListenFolderPath.toFile();
            rootDirectory.deleteOnExit();
            String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
            testResourceRoot = Paths.get(resourceRoot, "test-src");
        } catch (IOException e) {
            Assert.fail("Unable to create root folder to setup watch.", e);
        }
    }

    @AfterClass
    public void cleanup() {
        if (rootDirectory != null) {
            try {
                Files.walk(rootDirectory.toPath(), FileVisitOption.FOLLOW_LINKS)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException ignore) {
                //Ignore
            }
        }
    }

    @Test(description = "Check the valid successful use case.")
    public void testValidLocalFileSystemServerConnectorSyntax() {
        CompileResult compileResult = BCompileUtil
                .compileOffline(true, testResourceRoot.resolve("file-system.bal").toString());
        try {
            final Path file = Files.createFile(Paths.get("src", "test", "resources", "fs", "temp.txt"));
            Awaitility.await().atMost(1, MINUTES).until(() -> {
                BValue[] result = BRunUtil.invoke(compileResult, "isCreateInvoked");
                return ((BBoolean) result[0]).booleanValue();
            });
            Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis()));
            Awaitility.await().atMost(1, MINUTES).until(() -> {
                BValue[] result = BRunUtil.invoke(compileResult, "isModifyInvoked");
                return ((BBoolean) result[0]).booleanValue();
            });
            Files.deleteIfExists(file);
            Awaitility.await().atMost(1, MINUTES).until(() -> {
                BValue[] result = BRunUtil.invoke(compileResult, "isDeleteInvoked");
                return ((BBoolean) result[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test(description = "Check the negative test for non valid resources.")
    public void testNegativeWithoutResource() {
        try {
            BCompileUtil.compileOffline(testResourceRoot.
                    resolve("file-system-negative-without-resource.bal").toString());
        } catch (BLangRuntimeException e) {
            String actualMsg = e.getMessage();
            String expectedErrorMsg = "At least a single resource required from following: "
                    + "onCreate ,onDelete ,onModify. Parameter should be of type - file:FileEvent";
            Assert.assertTrue(actualMsg.contains(expectedErrorMsg));
        }
    }

    @Test(description = "Check the negative test for invalid resource param count.")
    public void testNegativeWithoutInvalidParamCount() {
        try {
            BCompileUtil.compileOffline(testResourceRoot.resolve("file-system-negative-invalid-param-count.bal").
                    toString());
        } catch (Throwable e) {
            String actualMsg = e.getMessage();
            String expectedErrorMsg = "Compilation Failed:\nERROR: .::"
                    + "file-system-negative-invalid-param-count.bal:25:5:: Invalid resource signature for onCreate in "
                    + "service fileSystem. The parameter should be a file:FileEvent with no returns.\n";
            Assert.assertEquals(actualMsg, expectedErrorMsg, "Didn't get expected error for invalid resource param.");
        }
    }

    @Test(description = "Check the negative test for invalid resource param type.")
    public void testNegativeWithoutInvalidParamType() {
        try {
            BCompileUtil.compileOffline(testResourceRoot.resolve("file-system-negative-invalid-param-type.bal").
                    toString());
        } catch (Throwable e) {
            String actualMsg = e.getMessage();
            String expectedErrorMsg = "Compilation Failed:\nERROR: .::"
                    + "file-system-negative-invalid-param-type.bal:26:5:: Invalid resource signature for onCreate in "
                    + "service fileSystem. The parameter should be a file:FileEvent\n";
            Assert.assertEquals(actualMsg, expectedErrorMsg, "Didn't get expected error for invalid resource type.");
        }
    }

    @Test(description = "Check the negative test for invalid resource name.")
    public void testNegativeWithoutInvalidResourceName() {
        try {
            BCompileUtil.compileOffline(testResourceRoot.resolve("file-system-negative-invalid-resource-name.bal")
                    .toString());
        } catch (Throwable e) {
            String actualMsg = e.getMessage();
            String expectedErrorMsg = "Compilation Failed:\nERROR: .::"
                    + "file-system-negative-invalid-resource-name.bal:25:5:: Invalid resource name onCreate1 in "
                    + "service fileSystem\n";
            Assert.assertEquals(actualMsg, expectedErrorMsg, "Didn't get expected error for invalid resource name.");
        }
    }

    @Test(description = "Check the negative test for not a folder situation.")
    public void testNegativeNotDirectory() {
        try {
            Files.createFile(Paths.get("src", "test", "resources", "fs", "file.txt"));
            BCompileUtil.compileOffline(testResourceRoot.resolve("file-system-negative-not-folder.bal").toString());
        } catch (Throwable e) {
            String actualMsg = e.getMessage().substring(47, 51 + 54);
            String expectedErrorMsg = "Unable to find a directory: src/test/resources/fs/file.txt";
            Assert.assertTrue(e.getMessage().contains(expectedErrorMsg),
                    "Didn't get expected error for invalid folder.");
        }
    }

    @Test(description = "Check the negative test for folder not exist.")
    public void testNegativeDirectoryNotExist() {
        try {
            BCompileUtil.compileOffline(testResourceRoot.resolve("file-system-negative-folder-exist.bal").toString());
        } catch (BLangRuntimeException e) {
            String expectedErrorMsg = "Folder does not exist: hello/ballerina";
            Assert.assertTrue(e.getMessage().contains(expectedErrorMsg),
                    "Didn't get expected error for non-exist folder.");
        }
    }

    @Test(description = "Check the negative test for endpoint config variable")
    public void testNegativeMissingEndpointVariable() {
        try {
            BCompileUtil.compileOffline(testResourceRoot.resolve("file-system-negative-missing-variable.bal")
                    .toString());
        } catch (Throwable e) {
            String expectedErrorMsg = "'path' field is empty";
            Assert.assertTrue(e.getMessage().contains(expectedErrorMsg), "Didn't get expected error for empty path.");
        }
    }

    @Test(description = "Check the negative test for invalid returns")
    public void testNegativeInvalidReturn() {
        try {
            BCompileUtil.compileOffline(testResourceRoot.resolve("file-system-negative-invalid-returns.bal")
                    .toString());
        } catch (Throwable e) {
            String actualMsg = e.getMessage();
            String expect = "Compilation Failed:\n" + "ERROR: .::file-system-negative-invalid-returns.bal:25:5:: "
                    + "Invalid resource signature for onCreate in service fileSystem. "
                    + "The parameter should be a file:FileEvent with no returns.\n";
            Assert.assertEquals(actualMsg, expect, "Didn't get expected error for invalid returns.");
        }
    }
}
