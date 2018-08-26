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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for WorkspaceDocument.
 */
public class WorkspaceDocumentTest {

    private WorkspaceDocument document;
    private Path filePath1;
    private Path filePath2;
    private static final String INITIAL_CONTENT = "initial content";

    @BeforeMethod
    public void setUp() {
        File source = new File(getClass().getClassLoader().getResource("source").getFile());
        filePath1 = source.toPath().resolve("singlepackage").resolve("io-sample.bal");
        filePath2 = source.toPath().resolve("multipackages").resolve("sample").resolve("main.bal");
        document = new WorkspaceDocument(filePath1, INITIAL_CONTENT);
    }

    @Test
    public void testGetPath() throws IOException {
        Assert.assertTrue(Files.isSameFile(document.getPath(), filePath1));
    }

    @Test
    public void testSetPath() throws IOException {
        document.setPath(filePath2);
        Assert.assertTrue(Files.isSameFile(document.getPath(), filePath2));
        document.setPath(filePath1);
    }

    @Test
    public void testGetContent() {
        Assert.assertEquals(document.getContent(), INITIAL_CONTENT);
    }

    @Test
    public void testSetContent() {
        String newContent = "new content";
        document.setContent(newContent);
        Assert.assertEquals(newContent, document.getContent());
        document.setContent(INITIAL_CONTENT);
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(document.toString());
    }
}
