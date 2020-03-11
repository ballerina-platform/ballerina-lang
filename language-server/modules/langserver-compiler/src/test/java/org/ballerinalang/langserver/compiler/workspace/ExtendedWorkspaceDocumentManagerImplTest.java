/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.compiler.workspace;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

/**
 * Tests for the ExtendedWorkspaceDocumentManagerImpl.
 *
 * @since 0.982.0
 */
public class ExtendedWorkspaceDocumentManagerImplTest {
    private ExtendedWorkspaceDocumentManagerImpl documentManager;
    private Path filePath;

    @BeforeClass
    public void setUp() {
        documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        documentManager.clearAllFilePaths();
        filePath = new File(getClass().getClassLoader().getResource("source").getFile()).toPath()
                .resolve("singlepackage").resolve("io-sample.bal");
    }

    @Test(enabled = false)
    public void testOpenFile() throws IOException, WorkspaceDocumentException {
        // Call open file
        Optional<Lock> lock = documentManager.lockFile(filePath);
        try {
            documentManager.openFile(filePath, readAll(filePath.toFile()));
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        // Test file opened
        Path foundPath = documentManager.getAllFilePaths().stream().filter(path -> {
            try {
                return Files.isSameFile(path, filePath);
            } catch (IOException e) {
                return false;
            }
        }).findFirst().orElse(null);
        Assert.assertNotNull(foundPath);
    }

    @Test(dependsOnMethods = "testOpenFile", enabled = false)
    public void testOpenFileOnAlreadyOpenFile() throws IOException, WorkspaceDocumentException {
        // Call open file on already open file
        documentManager.openFile(filePath, readAll(filePath.toFile()));
    }

    @Test(dependsOnMethods = "testOpenFile", enabled = false)
    public void testGetAllFilePaths() {
        Set<Path> allFilePaths = documentManager.getAllFilePaths();
        //  Test returned list size is one
        Assert.assertEquals(allFilePaths.size(), 1);
        // Test list contains the already opened file
        boolean foundFile = allFilePaths.stream().anyMatch(path -> {
            try {
                return Files.isSameFile(path, filePath);
            } catch (IOException e) {
                return false;
            }
        });
        Assert.assertTrue(foundFile);
    }

    @Test(dependsOnMethods = "testOpenFile", enabled = false)
    public void testIsFileOpen() throws WorkspaceDocumentException, IOException {
        // Test is file open returns false
        documentManager.closeFile(filePath);
        Assert.assertFalse(documentManager.isFileOpen(filePath));
        // Test is file open returns true
        documentManager.openFile(filePath, readAll(filePath.toFile()));
        Assert.assertTrue(documentManager.isFileOpen(filePath));
    }

    @Test(dependsOnMethods = "testGetAllFilePaths", enabled = false)
    public void testGetFileContent() throws IOException, WorkspaceDocumentException {
        // Read Actual content
        String expectedContent = readAll(filePath.toFile());
        // Call get file content
        String actualContent = documentManager.getFileContent(filePath);
        // Test actual against expected content
        Assert.assertEquals(actualContent, expectedContent);
    }

    @Test(dependsOnMethods = "testGetFileContent", enabled = false)
    public void testUpdateFile() throws IOException, WorkspaceDocumentException {
        String updateContent = readAll(filePath.toFile()) + "\nfunction foo(){\n}\n";
        // Update the file
        Optional<Lock> lock = documentManager.lockFile(filePath);
        try {
            documentManager.updateFile(filePath, updateContent);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        // Call get file content
        String actualContent = documentManager.getFileContent(filePath);
        // Test actual against expected content
        Assert.assertEquals(actualContent, updateContent);
    }

    @Test(dependsOnMethods = "testUpdateFile", enabled = false)
    public void testLockFile() {
        Optional<Lock> lock = Optional.empty();
        try {
            lock = documentManager.lockFile(filePath);
            Assert.assertTrue(lock.isPresent());
        } finally {
            lock.ifPresent(Lock::unlock);
        }
    }

    @Test(dependsOnMethods = "testLockFile", enabled = false)
    public void testCloseFile() throws WorkspaceDocumentException {
        documentManager.closeFile(filePath);
        boolean fileOpen = documentManager.isFileOpen(filePath);
        Assert.assertFalse(fileOpen);
    }

    @Test(dependsOnMethods = "testCloseFile", expectedExceptions = WorkspaceDocumentException.class, enabled = false)
    public void testGetFileContentOnNonExistentFile() throws WorkspaceDocumentException {
        documentManager.getFileContent(filePath.resolve("non-existent"));
    }

    @Test(dependsOnMethods = "testCloseFile", enabled = false)
    public void testUpdateFileOnClosedFile() throws WorkspaceDocumentException {
        documentManager.updateFile(filePath, "");
    }

    @Test(dependsOnMethods = "testCloseFile", enabled = false)
    public void testExplicitMode() throws WorkspaceDocumentException {
        String newContent = "test";
        Optional<Lock> lock = documentManager.enableExplicitMode(filePath);
        try {
            //Update content in explicit mode
            documentManager.updateFile(filePath, newContent);
            //Check content in explicit mode
            Assert.assertEquals(newContent, documentManager.getFileContent(filePath));
        } finally {
            documentManager.disableExplicitMode(lock.orElse(null));
        }
        //Check the content in other mode
        Assert.assertNotEquals(newContent, documentManager.getFileContent(filePath));
    }

    private String readAll(File filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(filePath.toPath(), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        return contentBuilder.toString().trim();
    }
}
