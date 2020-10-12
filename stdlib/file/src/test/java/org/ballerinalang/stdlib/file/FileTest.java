/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.file;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.core.model.types.BObjectType;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

/**
 * Test class for file package.
 *
 * @since 0.995.0
 */
public class FileTest {

    private CompileResult compileResult;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String WORKING_DIRECTORY = System.getProperty("user.dir");
    private Path srcFilePath = Paths.get("src", "test", "resources", "data-files", "src-file.txt");
    private Path destFilePath = Paths.get("src", "test", "resources", "data-files", "dest-file.txt");
    private Path srcModifiedFilePath = Paths.get("src", "test", "resources", "data-files", "src-file-modified.txt");
    private Path srcDirPath = Paths.get("src", "test", "resources", "data-files", "src-dir");
    private Path readDirPath = Paths.get("src", "test", "resources", "data-files", "read-dir");
    private Path emptyDirPath = srcDirPath.resolve("empty-dir");
    private Path errorSrcDirPath = Paths.get("src", "test", "resources", "data-files", "src-dir", "error");
    private Path tempDirPath;
    private Path tempSourcePath;
    private Path tempDestPath;
    private Path errorPath;
    private static final Logger log = LoggerFactory.getLogger(FileTest.class);

    @BeforeClass
    public void setup() throws IOException {
        compileResult = BCompileUtil.compileOffline(Paths.get("test-src", "file-system-test.bal").toString());
        tempDirPath = Paths.get(TEMP_DIR, "data-files");
        if (Files.notExists(tempDirPath)) {
            Files.createDirectory(tempDirPath);
        }
        tempSourcePath = tempDirPath.resolve("src-file.txt");
        tempDestPath = tempDirPath.resolve("dest-file.txt");
        errorPath = errorSrcDirPath.resolve("no-file.txt");
    }

    @Test(description = "Test for retrieving temporary directory")
    public void testTempDir() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTempDir");
        assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("java.io.tmpdir");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test(description = "Test for changing file path")
    public void testFileRename() throws IOException {
        try {
            Files.copy(srcFilePath, tempSourcePath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

            BValue[] args = {new BString(tempSourcePath.toString()), new BString(tempDestPath.toString())};
            BRunUtil.invoke(compileResult, "testRename", args);
            assertTrue(Files.exists(tempDestPath));
            assertFalse(Files.exists(tempSourcePath));
        } finally {
            Files.deleteIfExists(tempSourcePath);
            Files.deleteIfExists(tempDestPath);
        }
    }

    @Test(description = "Test for changing file path to already existing file path")
    public void testFileRenameWithInvalidPath() throws IOException {
        try {
            Files.copy(srcFilePath, tempSourcePath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            Files.copy(srcFilePath, tempDestPath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

            BValue[] args = {new BString(tempSourcePath.toString()), new BString(tempDestPath.toString())};
            BValue[] returns = BRunUtil.invoke(compileResult, "testRename", args);
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertTrue(error.getMessage().contains("File already exists in the new path "));
        } finally {
            Files.deleteIfExists(tempSourcePath);
            Files.deleteIfExists(tempDestPath);
        }
    }

    @Test(description = "Test for removing file/directory from system")
    public void testFileRemove() throws IOException {
        Path tempSourceDirPath = null;
        try {
            tempSourceDirPath = tempDirPath.resolve("src-dir");
            FileUtils.copyDirectory(srcDirPath.toFile(),
                    tempSourceDirPath.toFile());
            Files.copy(srcFilePath, tempSourcePath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            // Remove source file
            BValue[] args = {new BString(tempSourcePath.toString()), new BBoolean(false)};
            BRunUtil.invoke(compileResult, "testRemove", args);
            assertFalse(Files.exists(tempSourcePath));

            // Remove directory with recursive false
            BValue[] args1 = {new BString(tempSourceDirPath.toString()), new BBoolean(false)};
            BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", args1);
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertTrue(error.getMessage().contains("Error while deleting "));

            // Remove directory with recursive true
            BValue[] args2 = {new BString(tempSourceDirPath.toString()), new BBoolean(true)};
            returns = BRunUtil.invoke(compileResult, "testRemove", args2);
            assertNull(returns[0]);
            assertFalse(Files.exists(tempSourceDirPath));

        } finally {
            Files.deleteIfExists(tempSourcePath);
            if (tempSourceDirPath != null) {
                FileUtils.deleteDirectory(tempSourceDirPath.toFile());
            }
        }
    }

    @Test(description = "Test for removing non existence file/directory from system ")
    public void testRemoveNonExistFile() {
        Path tempSourceFilePath = tempDirPath.resolve("src-dir");
        BValue[] args1 = {new BString(tempSourceFilePath.toString()), new BBoolean(false)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", args1);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertTrue(error.getMessage().contains("File not found: "));
    }

    @Test(description = "Test for retrieving file info from system")
    public void testGetFileInfo() {
        BValue[] args = {new BString(srcFilePath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFileInfo", args);
        assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> bvalue = (BMap) returns[0];
        assertEquals(bvalue.get("name").stringValue(), "src-file.txt");
        assertTrue(((BInteger) bvalue.get("size")).intValue() > 0);
        assertEquals(bvalue.get("dir").stringValue(), "false");
        assertEquals(bvalue.get("path").stringValue(), WORKING_DIRECTORY + File.separator + srcFilePath);
    }

    @Test(description = "Test for retrieving file info from non existence file")
    public void testGetFileInfoNonExistFile() {
        BValue[] args1 = {new BString(tempDestPath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFileInfo", args1);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertTrue(error.getMessage().contains("File not found: "));
    }

    @Test(description = "Test for retrieving files inside directory")
    public void testReadDir() {
        BValue[] args = {new BString(srcDirPath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDir", args);
        assertTrue(returns[0].getType() instanceof BObjectType);
    }

    @Test(description = "Test for retrieving files inside directory specifying the depth level - 0")
    public void testReadDirWithMaxDepth() {
        BValue[] args = {new BString(srcDirPath.toString()), new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDirWithMaxDepth", args);
        assertEquals(returns.length, 0);
    }

    @Test(description = "Test for retrieving files inside directory specifying the depth level - 1")
    public void testReadDirWithMaxDepth1() {
        BValue[] args = {new BString(readDirPath.toString()), new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDirWithMaxDepth", args);
        assertTrue(returns[0].getType() instanceof BObjectType);
        assertEquals(returns.length, 2);
    }

    @Test(description = "Test for reading file info from non existence directory")
    public void testReadNonExistDirectory() throws IOException {
        Path filepath = tempDirPath.resolve("dest-dir");
        FileUtils.deleteDirectory(filepath.toFile());
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDir", args);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertTrue(error.getMessage().contains("File not found: "));
    }

    @Test(description = "Test for reading file info from an empty directory")
    public void testReadEmptyDirectory() throws IOException {
        new File(emptyDirPath.toString()).mkdirs();
        BValue[] args = {new BString(emptyDirPath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDir", args);
        assertEquals(returns.length, 0);
    }

    @Test(description = "Test for reading file info from non existence directory")
    public void testReadFile() {
        BValue[] args = {new BString(srcFilePath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDir", args);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertTrue(error.getMessage().contains("File in path "));
    }

    @Test(description = "Test for checking whether file exists")
    public void testFileExists() {
        BValue[] args = {new BString(srcFilePath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFileExists", args);
        assertTrue(returns[0] instanceof BBoolean);
        BBoolean value = (BBoolean) returns[0];
        assertTrue(value.booleanValue());
    }

    @Test(description = "Test for checking whether file exists in non existent file")
    public void testExistsNonExistFile() {
        BValue[] args = {new BString(destFilePath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFileExists", args);
        assertTrue(returns[0] instanceof BBoolean);
        BBoolean value = (BBoolean) returns[0];
        assertFalse(value.booleanValue());
    }

    @Test(description = "Test for creating new file")
    public void testCreateFile() throws IOException {
        Files.deleteIfExists(tempDestPath);
        try {
            BValue[] args = {new BString(tempDestPath.toString())};
            BValue[] returns = BRunUtil.invoke(compileResult, "testCreateFile", args);
            assertTrue(returns[0] instanceof BString);
            assertTrue(Files.exists(tempDestPath));
        } finally {
            Files.deleteIfExists(tempDestPath);
        }
    }

    @Test(description = "Test attempting to access a file that does not exist")
    public void testCreateNonExistingFile() throws IOException {
        try {
            BValue[] args = {new BString(errorPath.toString())};
            BValue[] returns = BRunUtil.invoke(compileResult, "testCreateNonExistingFile", args);
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertTrue(error.getMessage().contains("The file does not exist in path "));
        } finally {
            Files.deleteIfExists(tempDestPath);
        }
    }

    @Test(description = "Test for creating new file in already exist path")
    public void testCreateFileAlreadyExistPath() {
        BValue[] args = {new BString(srcFilePath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateFile", args);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertTrue(error.getMessage().contains("File already exists. Failed to create the file: "));
    }

    @Test(description = "Test for creating new file")
    public void testCreateDirWithParentDir() throws IOException {
        Path filepath = tempDirPath.resolve("temp-dir").resolve("nested-dir");
        FileUtils.deleteDirectory(filepath.toFile());
        try {
            BValue[] args = {new BString(filepath.toString()), new BBoolean(true)};
            BRunUtil.invoke(compileResult, "testCreateDir", args);
            assertTrue(Files.exists(filepath));
        } finally {
            FileUtils.deleteDirectory(filepath.getParent().toFile());
        }
    }

    @Test(description = "Test for creating new file")
    public void testCreateDirWithoutParentDir() throws IOException {
        Path filepath = tempDirPath.resolve("temp-dir").resolve("nested-dir");
        FileUtils.deleteDirectory(filepath.getParent().toFile());
        try {
            BValue[] args = {new BString(filepath.toString()), new BBoolean(false)};
            BValue[] returns = BRunUtil.invoke(compileResult, "testCreateDir", args);
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertTrue(error.getMessage().contains("IO error while creating the file "));
            assertFalse(Files.exists(filepath));
        } finally {
            FileUtils.deleteDirectory(filepath.getParent().toFile());
        }
    }

    @Test(description = "Test for copying a file to new location")
    public void testCopyFile() throws IOException, InterruptedException {
        try {
            BValue[] args = {new BString(srcFilePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(false)};
            String originalContent = new String(Files.readAllBytes(srcFilePath));
            log.info("Source file content: " + originalContent);
            BRunUtil.invoke(compileResult, "testCopy", args);
            assertTrue(Files.exists(tempDestPath));
            assertTrue(Files.exists(srcFilePath));
            assertEquals(tempDestPath.toFile().length(), srcFilePath.toFile().length());

            // Execute same with replaceExist false
            BValue[] args1 = {new BString(srcModifiedFilePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(false)};
            BRunUtil.invoke(compileResult, "testCopy", args1);
            String modifiedWithoutReplace = new String(Files.readAllBytes(tempDestPath));
            log.info("Modified without replace file content: " + modifiedWithoutReplace);
            assertEquals(modifiedWithoutReplace, originalContent);

            // Execute same with replaceExist true
            BValue[] args2 = {new BString(srcModifiedFilePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(true)};
            BRunUtil.invoke(compileResult, "testCopy", args2);
            String modifiedWithReplace = new String(Files.readAllBytes(tempDestPath));
            log.info("Modified with replace file content: " + modifiedWithReplace);
            assertNotEquals(modifiedWithReplace, originalContent);
        } finally {
            Files.deleteIfExists(tempDestPath);
        }
    }

    @Test(description = "Test for copying a file with non existent source file")
    public void testCopyFileNonExistSource() throws IOException {
        try {
            BValue[] args = {new BString(destFilePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(false)};
            BValue[] returns = BRunUtil.invoke(compileResult, "testCopy", args);
            BError error = (BError) returns[0];
            assertTrue(error.getMessage().contains("File not found: "));
        } finally {
            Files.deleteIfExists(tempDestPath);
        }
    }

    @Test(description = "Test for copying a directory to new location")
    public void testCopyDir() throws IOException {
        Path tempDestPath = tempDirPath.resolve("dest-dir");
        try {
            BValue[] args = {new BString(srcDirPath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(false)};
            BRunUtil.invoke(compileResult, "testCopy", args);
            assertTrue(Files.exists(tempDestPath));
            assertTrue(Files.exists(srcDirPath));
            assertTrue(compareFiles(tempDestPath, srcDirPath));
        } finally {
            FileUtils.deleteDirectory(tempDestPath.toFile());
        }
    }

    @Test
    public void testGetCurrentDirectory() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetCurrentDirectory");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.dir");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    private static boolean compareFiles(Path tempDestPath, Path srcDirPath) throws IOException {
        if ((Objects.requireNonNull(tempDestPath.toFile().list()).length !=
                Objects.requireNonNull(srcDirPath.toFile().list()).length)) {
            return false;
        }
        for (File file : Objects.requireNonNull(tempDestPath.toFile().listFiles())) {
            Path path = Paths.get(srcDirPath.toString(), file.getName());
            if (!Files.exists(path)) {
                return false;
            }
            if (file.isFile()) {
                if (!Arrays.equals(Files.readAllBytes(file.toPath()), Files.readAllBytes(path))) {
                    return false;
                }
            } else {
                if (Objects.requireNonNull(file.list()).length > 0) {
                    compareFiles(file.toPath(), path);
                }
            }
        }
        return true;
    }
}
