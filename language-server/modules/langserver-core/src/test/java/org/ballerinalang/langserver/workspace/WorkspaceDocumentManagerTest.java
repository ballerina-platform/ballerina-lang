/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.workspace;

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test in-memory document manager.
 *
 */
public class WorkspaceDocumentManagerTest {

    private static final Path FILE_PATH = Paths.get("temp/untitled.bal");
    private static final String FILE_CONTENT = "function main() {}";

    private WorkspaceDocumentManager documentManager;

    @BeforeClass
    public void setup() {
        documentManager = WorkspaceDocumentManagerImpl.getInstance();
    }

    @Test
    public void testFileOpenAndClose() {
        documentManager.openFile(FILE_PATH, FILE_CONTENT);
        Assert.assertTrue(documentManager.isFileOpen(FILE_PATH), "File should be opened in doc manager.");
        documentManager.closeFile(FILE_PATH);
        Assert.assertFalse(documentManager.isFileOpen(FILE_PATH), "File should be closed in doc manager.");
    }

    @Test
    public void testFileUpdate() {
        documentManager.openFile(FILE_PATH, FILE_CONTENT);
        Assert.assertEquals(documentManager.getFileContent(FILE_PATH), FILE_CONTENT,
                "File content should not be updated by other means.");
        documentManager.updateFile(FILE_PATH, "function main2() {}");
        Assert.assertEquals(documentManager.getFileContent(FILE_PATH), "function main2() {}",
                "File content should be updated upon file update.");
        documentManager.closeFile(FILE_PATH);
    }
}
