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
package org.ballerinalang.stdlib.system;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

/**
 * Test class for file system package.
 *
 * @since 0.995.0
 */
public class FileSystemTest {

    private CompileResult compileResult;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private Path tempDirPath;
    private static final Logger log = LoggerFactory.getLogger(FileSystemTest.class);

    @BeforeClass
    public void setup() throws IOException {
        compileResult = BCompileUtil.compile(Paths.get("test-src", "file-system-test.bal").toString());
        tempDirPath = Paths.get(TEMP_DIR, "data-files");
        if (Files.notExists(tempDirPath)) {
            Files.createDirectory(tempDirPath);
        }
    }

    @Test(description = "Test for retrieving temporary directory")
    public void testTempDir() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTempDir");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("java.io.tmpdir");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test(description = "Test for changing file path")
    public void testFileRename() throws IOException {
        Path tempSourcePath = null;
        Path tempDestPath = null;
        try {
            tempSourcePath = tempDirPath.resolve("src-file.txt");
            tempDestPath = tempDirPath.resolve("dest-file.txt");
            Files.copy(Paths.get("src", "test", "resources", "data-files", "src-file.txt"), tempSourcePath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            
            BValue[] args = {new BString(tempSourcePath.toString()), new BString(tempDestPath.toString())};
            BRunUtil.invoke(compileResult, "testRename", args);
            Assert.assertTrue(Files.exists(tempDestPath));
            assertFalse(Files.exists(tempSourcePath));
        } finally {
            if (tempSourcePath != null) {
                Files.deleteIfExists(tempSourcePath);
            }
            if (tempDestPath != null) {
                Files.deleteIfExists(tempDestPath);
            }
        }
    }

    @Test(description = "Test for changing file path to already existing file path")
    public void testFileRenameWithInvalidPath() throws IOException {
        Path tempSourcePath = null;
        Path tempDestPath = null;
        try {
            tempSourcePath = tempDirPath.resolve("src-file.txt");
            tempDestPath = tempDirPath.resolve("dest-file.txt");
            Files.copy(Paths.get("src", "test", "resources", "data-files", "src-file.txt"), tempSourcePath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            Files.copy(Paths.get("src", "test", "resources", "data-files", "src-file.txt"), tempDestPath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

            BValue[] args = {new BString(tempSourcePath.toString()), new BString(tempDestPath.toString())};
            BValue[] returns = BRunUtil.invoke(compileResult, "testRename", args);
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/system}OPERATION_FAILED");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } finally {
            if (tempSourcePath != null) {
                Files.deleteIfExists(tempSourcePath);
            }
            if (tempDestPath != null) {
                Files.deleteIfExists(tempDestPath);
            }
        }
    }

    @Test(description = "Test for removing file/directory from system")
    public void testFileRemove() throws IOException {
        Path tempSourceFilePath = null;
        Path tempSourceDirPath = null;
        try {
            tempSourceFilePath = tempDirPath.resolve("src-file.txt");
            tempSourceDirPath = tempDirPath.resolve("src-dir");
            FileUtils.copyDirectory(Paths.get("src", "test", "resources", "data-files", "src-dir").toFile(),
                    tempSourceDirPath.toFile());
            Files.copy(Paths.get("src", "test", "resources", "data-files", "src-file.txt"), tempSourceFilePath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            // Remove source file
            BValue[] args = {new BString(tempSourceFilePath.toString()), new BBoolean(false)};
            BRunUtil.invoke(compileResult, "testRemove", args);
            assertFalse(Files.exists(tempSourceFilePath));

            // Remove directory with recursive false
            BValue[] args1 = {new BString(tempSourceDirPath.toString()), new BBoolean(false)};
            BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", args1);
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/system}OPERATION_FAILED");
            log.info("Ballerina error: " + error.getDetails().stringValue());

            // Remove directory with recursive true
            BValue[] args2 = {new BString(tempSourceDirPath.toString()), new BBoolean(true)};
            returns = BRunUtil.invoke(compileResult, "testRemove", args2);
            assertNull(returns[0]);
            assertFalse(Files.exists(tempSourceDirPath));

        } finally {
            if (tempSourceFilePath != null) {
                Files.deleteIfExists(tempSourceFilePath);
            }
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
        assertEquals(error.getReason(), "{ballerina/system}INVALID_OPERATION");
        log.info("Ballerina error: " + error.getDetails().stringValue());
    }

    @Test(description = "Test for retrieving file info from system")
    public void testGetFileInfo() {
        Path filepath = Paths.get("src", "test", "resources", "data-files", "src-file.txt");
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFileInfo", args);
        assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> bvalue = (BMap) returns[0];
        assertEquals(bvalue.get("name").stringValue(), "src-file.txt");
        assertEquals(bvalue.get("size").stringValue(), "55");
        assertEquals(bvalue.get("isDir").stringValue(), "false");
    }

    @Test(description = "Test for retrieving file info from non existence file")
    public void testGetFileInfoNonExistFile() {
        Path tempSourceFilePath = tempDirPath.resolve("dest-file.txt");
        BValue[] args1 = {new BString(tempSourceFilePath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFileInfo", args1);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertEquals(error.getReason(), "{ballerina/system}INVALID_OPERATION");
        log.info("Ballerina error: " + error.getDetails().stringValue());
    }

    @Test(description = "Test for retrieving files inside directory")
    public void testReadDir() {
        Path filepath = Paths.get("src", "test", "resources", "data-files", "src-dir");
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDir", args);
        assertTrue(returns[0].getType() instanceof BObjectType);
    }

    @Test(description = "Test for reading file info from non existence directory")
    public void testReadNonExistDirectory() throws IOException {
        Path filepath = tempDirPath.resolve("dest-dir");
        FileUtils.deleteDirectory(filepath.toFile());
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDir", args);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertEquals(error.getReason(), "{ballerina/system}INVALID_OPERATION");
        log.info("Ballerina error: " + error.getDetails().stringValue());
    }

    @Test(description = "Test for reading file info from non existence directory")
    public void testReadFile() {
        Path filepath = Paths.get("src", "test", "resources", "data-files", "src-file.txt");
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadDir", args);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertEquals(error.getReason(), "{ballerina/system}INVALID_OPERATION");
        log.info("Ballerina error: " + error.getDetails().stringValue());
    }

    @Test(description = "Test for checking whether file exists")
    public void testFileExists() {
        Path filepath = Paths.get("src", "test", "resources", "data-files", "src-file.txt");
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFileExists", args);
        assertTrue(returns[0] instanceof BBoolean);
        BBoolean value = (BBoolean) returns[0];
        assertTrue(value.booleanValue());
    }

    @Test(description = "Test for checking whether file exists in non existent file")
    public void testExistsNonExistFile() {
        Path filepath = Paths.get("src", "test", "resources", "data-files", "dest-file.txt");
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFileExists", args);
        assertTrue(returns[0] instanceof BBoolean);
        BBoolean value = (BBoolean) returns[0];
        assertFalse(value.booleanValue());
    }

    @Test(description = "Test for creating new file")
    public void testCreateFile() throws IOException {
        Path filepath = tempDirPath.resolve("dest-file.txt");
        Files.deleteIfExists(filepath);
        try {
            BValue[] args = {new BString(filepath.toString())};
            BValue[] returns = BRunUtil.invoke(compileResult, "testCreateFile", args);
            assertTrue(returns[0] instanceof BString);
            assertTrue(Files.exists(filepath));
        } finally {
            Files.deleteIfExists(filepath);
        }
    }

    @Test(description = "Test for creating new file in already exist path")
    public void testCreateFileAlreadyExistPath() {
        Path filepath = Paths.get("src", "test", "resources", "data-files", "src-file.txt");
        BValue[] args = {new BString(filepath.toString())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateFile", args);
        assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        assertEquals(error.getReason(), "{ballerina/system}INVALID_OPERATION");
        log.info("Ballerina error: " + error.getDetails().stringValue());
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
            assertEquals(error.getReason(), "{ballerina/system}FILE_SYSTEM_ERROR");
            log.info("Ballerina error: " + error.getDetails().stringValue());
            assertFalse(Files.exists(filepath));
        } finally {
            FileUtils.deleteDirectory(filepath.getParent().toFile());
        }
    }

    @Test(description = "Test for copying a file to new location")
    public void testCopyFile() throws IOException, InterruptedException {
        Path tempDestPath = null;
        try {
            Path sourcePath = Paths.get("src", "test", "resources", "data-files", "src-file.txt");
            tempDestPath = tempDirPath.resolve("dest-file.txt");

            BValue[] args = {new BString(sourcePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(false)};
            BRunUtil.invoke(compileResult, "testCopy", args);
            assertTrue(Files.exists(tempDestPath));
            assertTrue(Files.exists(sourcePath));
            assertEquals(tempDestPath.toFile().length(), sourcePath.toFile().length());

            // Execute same with replaceExist false
            long modifiedTime1 = tempDestPath.toFile().lastModified();
            BRunUtil.invoke(compileResult, "testCopy", args);
            long modifiedTime2 = tempDestPath.toFile().lastModified();
            assertEquals(modifiedTime2, modifiedTime1);

            // Execute same with replaceExist true
            BValue[] args1 = {new BString(sourcePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(true)};
            Thread.sleep(1000);
            BRunUtil.invoke(compileResult, "testCopy", args1);
            long modifiedTime3 = tempDestPath.toFile().lastModified();
            assertNotEquals(modifiedTime3, modifiedTime2);
        } finally {
            if (tempDestPath != null) {
                Files.deleteIfExists(tempDestPath);
            }
        }
    }

    @Test(description = "Test for copying a file with non existent source file")
    public void testCopyFileNonExistSource() throws IOException {
        Path tempDestPath = null;
        try {
            Path sourcePath = Paths.get("src", "test", "resources", "data-files", "dest-file.txt");
            tempDestPath = tempDirPath.resolve("dest-file.txt");

            BValue[] args = {new BString(sourcePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(false)};
            BValue[] returns = BRunUtil.invoke(compileResult, "testCopy", args);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/system}INVALID_OPERATION");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } finally {
            if (tempDestPath != null) {
                Files.deleteIfExists(tempDestPath);
            }
        }
    }

    @Test(description = "Test for copying a directory to new location")
    public void testCopyDir() throws IOException {
        Path tempDestPath = null;
        try {
            Path sourcePath = Paths.get("src", "test", "resources", "data-files", "src-dir");
            tempDestPath = tempDirPath.resolve("dest-dir");

            BValue[] args = {new BString(sourcePath.toString()), new BString(tempDestPath.toString()),
                    new BBoolean(false)};
            BRunUtil.invoke(compileResult, "testCopy", args);
            assertTrue(Files.exists(tempDestPath));
            assertTrue(Files.exists(sourcePath));
            assertEquals(tempDestPath.toFile().length(), sourcePath.toFile().length());
        } finally {
            if (tempDestPath != null) {
                FileUtils.deleteDirectory(tempDestPath.toFile());
            }
        }
    }
}
