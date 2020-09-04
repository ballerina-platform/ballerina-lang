/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.compiler.workspace;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

/**
 * Workspace document manager test class.
 *
 * @since 0.982.0
 */
public class WorkspaceDocumentManagerImplTest {

    private WorkspaceDocumentManagerImpl documentManager;
    private Path filePath;
    private Path docUpdatePath1;
    private Path docUpdatePath2;
    private Path docUpdatePath3;

    public static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @BeforeClass
    public void setUp() {
        documentManager = WorkspaceDocumentManagerImpl.getInstance();
        documentManager.clearAllFilePaths();
        filePath = new File(getClass().getClassLoader().getResource("source").getFile()).toPath()
                .resolve("singlepackage").resolve("io-sample.bal");
        docUpdatePath1 = RES_DIR.resolve("source").resolve("docUpdateSource1.bal");
        docUpdatePath2 = RES_DIR.resolve("source").resolve("docUpdateSource2.bal");
        docUpdatePath3 = RES_DIR.resolve("source").resolve("docUpdateSource3.bal");
    }

    @Test
    public void testOpenFile() throws IOException, WorkspaceDocumentException {
        // Call open file
        Optional<Lock> lock = documentManager.lockFile(filePath);
        try {
            documentManager.openFile(filePath, readAll(filePath.toFile()));
            documentManager.openFile(docUpdatePath1, new String(Files.readAllBytes(docUpdatePath1)));
            documentManager.openFile(docUpdatePath2, new String(Files.readAllBytes(docUpdatePath2)));
            documentManager.openFile(docUpdatePath3, new String(Files.readAllBytes(docUpdatePath3)));
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

    @Test(dependsOnMethods = "testOpenFile", expectedExceptions = WorkspaceDocumentException.class)
    public void testOpenFileOnAlreadyOpenFile() throws IOException, WorkspaceDocumentException {
        // Call open file on already open file
        documentManager.openFile(filePath, readAll(filePath.toFile()));
    }

    @Test(dependsOnMethods = "testOpenFile")
    public void testGetAllFilePaths() {
        Set<Path> allFilePaths = documentManager.getAllFilePaths();
        //  Test returned list size is one
        Assert.assertEquals(allFilePaths.size(), 4);
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

    @Test(dependsOnMethods = "testOpenFile")
    public void testIsFileOpen() throws WorkspaceDocumentException, IOException {
        // Test is file open returns false
        documentManager.closeFile(filePath);
        Assert.assertFalse(documentManager.isFileOpen(filePath));
        // Test is file open returns true
        documentManager.openFile(filePath, readAll(filePath.toFile()));
        Assert.assertTrue(documentManager.isFileOpen(filePath));
    }

    @Test(dependsOnMethods = "testGetAllFilePaths")
    public void testGetFileContent() throws IOException, WorkspaceDocumentException {
        // Read Actual content
        String expectedContent = readAll(filePath.toFile());
        // Call get file content
        String actualContent = documentManager.getFileContent(filePath);
        // Test actual against expected content
        Assert.assertEquals(actualContent, expectedContent);
    }

    @Test(dependsOnMethods = "testGetFileContent")
    public void testUpdateFile() throws IOException, WorkspaceDocumentException {
        String updateContent = readAll(filePath.toFile()) + "\nfunction foo(){\n}\n";
        // Update the file
        Optional<Lock> lock = documentManager.lockFile(filePath);
        try {
            TextDocumentContentChangeEvent changeEvent = new TextDocumentContentChangeEvent(updateContent);
            documentManager.updateFile(filePath, Collections.singletonList(changeEvent));
        } finally {
            lock.ifPresent(Lock::unlock);
        }
        // Call get file content
        String actualContent = documentManager.getFileContent(filePath);
        // Test actual against expected content
        Assert.assertEquals(actualContent, updateContent);
    }

    @Test(dependsOnMethods = "testUpdateFile")
    public void testLockFile() {
        Optional<Lock> lock = Optional.empty();
        try {
            lock = documentManager.lockFile(filePath);
            Assert.assertTrue(lock.isPresent());
        } finally {
            lock.ifPresent(Lock::unlock);
        }
    }

    @Test(dependsOnMethods = "testLockFile")
    public void testCloseFile() throws WorkspaceDocumentException {
        documentManager.closeFile(filePath);
        boolean fileOpen = documentManager.isFileOpen(filePath);
        Assert.assertFalse(fileOpen);
    }

    @Test(dependsOnMethods = "testCloseFile", expectedExceptions = WorkspaceDocumentException.class)
    public void testGetFileContentOnNonExistentFile() throws WorkspaceDocumentException {
        documentManager.getFileContent(filePath.resolve("non-existent"));
    }

    @Test(dependsOnMethods = "testCloseFile", expectedExceptions = WorkspaceDocumentException.class)
    public void testUpdateFileOnClosedFile() throws WorkspaceDocumentException {
        TextDocumentContentChangeEvent changeEvent = new TextDocumentContentChangeEvent("");
        documentManager.updateFile(filePath, Collections.singletonList(changeEvent));
    }

    private String readAll(File filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(filePath.toPath(), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        return contentBuilder.toString().trim();
    }
}
