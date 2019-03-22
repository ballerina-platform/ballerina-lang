/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler.workspace;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test document content update based on diff.
 */
public class WorkspaceDocumentContentUpdateTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    public static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private WorkspaceDocumentManagerImpl documentManager;

    @BeforeClass
    public void setUp() {
        documentManager = WorkspaceDocumentManagerImpl.getInstance();
        documentManager.clearAllFilePaths();
    }

    @Test(description = "Test the document range updating")
    public void testDocumentRangeUpdate() throws IOException, WorkspaceDocumentException {
        Path sourcePath1 = RES_DIR.resolve("source").resolve("docUpdateSource1.bal");
        Path sourcePath2 = RES_DIR.resolve("source").resolve("docUpdateSource2.bal");
        Path sourcePath3 = RES_DIR.resolve("source").resolve("docUpdateSource3.bal");
        Path expected1Path = RES_DIR.resolve("expected").resolve("expected1.bal");
        Path expected2Path = RES_DIR.resolve("expected").resolve("expected2.bal");
        Path expected3Path = RES_DIR.resolve("expected").resolve("expected3.bal");
        Path update1Path = RES_DIR.resolve("updatecontent").resolve("updateContent1.txt");
        Path update2Path = RES_DIR.resolve("updatecontent").resolve("updateContent2.txt");
        Path update3Path = RES_DIR.resolve("updatecontent").resolve("updateContent3.txt");
        documentManager.openFile(sourcePath1, new String(Files.readAllBytes(sourcePath1)));
        documentManager.openFile(sourcePath2, new String(Files.readAllBytes(sourcePath2)));
        documentManager.openFile(sourcePath3, new String(Files.readAllBytes(sourcePath3)));

        Range range1 = new Range(new Position(19, 0), new Position(19, 0));
        Range range2 = new Range(new Position(6, 0), new Position(11, 1));
        Range range3 = new Range(new Position(6, 0), new Position(6, 0));
        String expected1 = new String(Files.readAllBytes(expected1Path)).replace("\n", LINE_SEPARATOR);
        String expected2 = new String(Files.readAllBytes(expected2Path)).replace("\n", LINE_SEPARATOR);
        String expected3 = new String(Files.readAllBytes(expected3Path)).replace("\n", LINE_SEPARATOR);
        String update1 = new String(Files.readAllBytes(update1Path)).replace("\n", LINE_SEPARATOR);
        String update2 = new String(Files.readAllBytes(update2Path)).replace("\n", LINE_SEPARATOR);
        String update3 = new String(Files.readAllBytes(update3Path)).replace("\n", LINE_SEPARATOR);
        documentManager.updateFileRange(sourcePath1, range1, update1);
        documentManager.updateFileRange(sourcePath2, range2, update2);
        documentManager.updateFileRange(sourcePath3, range3, update3);

        Assert.assertEquals(expected1, documentManager.getFileContent(sourcePath1),
                "Test Failed for: " + "docUpdateSource1");
        Assert.assertEquals(expected2, documentManager.getFileContent(sourcePath2),
                "Test Failed for: " + "docUpdateSource2");
        Assert.assertEquals(expected3, documentManager.getFileContent(sourcePath3),
                "Test Failed for: " + "docUpdateSource3");
    }
}
