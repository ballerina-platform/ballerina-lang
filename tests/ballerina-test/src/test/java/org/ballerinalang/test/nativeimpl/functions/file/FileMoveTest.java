/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.nativeimpl.functions.file;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.nativeimpl.functions.file.util.FileTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test cases for file copying.
 */
public class FileMoveTest {

    private CompileResult compileResult;
    private Path moveTestRootDir;
    private Path tmpDir;

    @BeforeClass
    public void setup() throws IOException {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/file-test.bal");
        Path fileDataFileRoot = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                                          "datafiles", "file");
        moveTestRootDir = Files.createTempDirectory("move-tmp-");
        FileTestUtils.copyDir(fileDataFileRoot, moveTestRootDir);
    }

    @AfterClass
    public void cleanup() throws IOException {
        Files.walk(moveTestRootDir)
                .sorted(Comparator.reverseOrder()) // To ensure the root directory is the one deleted last
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @BeforeMethod
    public void createTempDir() throws IOException {
        tmpDir = Files.createTempDirectory(moveTestRootDir, "tmp");
    }

    @AfterMethod
    public void deleteTempDir() throws IOException {
        Files.walk(tmpDir)
                .sorted(Comparator.reverseOrder()) // To ensure the root directory is the one deleted last
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test(description = "Test case for moving a single file")
    public void testMoveSingleFile() throws IOException {
        Path sourcePath = moveTestRootDir.resolve("single-file");
        Path destPath = tmpDir.resolve("moved-file");
        BValue[] args = new BValue[]{new BString(sourcePath.toString()), new BString(destPath.toString())};

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testMove", args);

        Assert.assertNull(returnValues[0]);
        Assert.assertNull(returnValues[1]);
        Assert.assertNull(returnValues[2]);

        Assert.assertTrue(!Files.exists(sourcePath));
        Assert.assertTrue(Files.exists(destPath));
    }

    @Test(description = "Test case for moving a directory")
    public void testMoveDirectory() throws IOException {
        Path sourcePath = moveTestRootDir.resolve("directory");
        Path destPath = tmpDir.resolve("moved-directory");
        BValue[] args = new BValue[]{new BString(sourcePath.toString()), new BString(destPath.toString())};

        Set<Path> originalFiles = Files.walk(sourcePath)
                                        .map(path -> sourcePath.relativize(path))
                                        .collect(Collectors.toSet());

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testMove", args);

        Assert.assertNull(returnValues[0]);
        Assert.assertNull(returnValues[1]);
        Assert.assertNull(returnValues[2]);

        Assert.assertTrue(!Files.exists(sourcePath));
        Assert.assertTrue(Files.exists(destPath));

        Set<Path> copiedFiles = Files.walk(destPath)
                                        .map(path -> destPath.relativize(path))
                                        .collect(Collectors.toSet());

        // If all files were copied correctly, adding the files from the original directory should not change the
        // copiedFiles set.
        Assert.assertFalse(copiedFiles.addAll(originalFiles));
    }
}
