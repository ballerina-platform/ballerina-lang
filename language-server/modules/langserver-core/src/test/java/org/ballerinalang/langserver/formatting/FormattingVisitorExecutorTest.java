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

package org.ballerinalang.langserver.formatting;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.SourceGen;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.workspace.ExtendedWorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for the formatting content.
 */
public class FormattingVisitorExecutorTest {

    private Path formattingPath = new File(getClass().getClassLoader().getResource("formatting").getFile()).toPath();
    private Path sourceDirPath = formattingPath.resolve("source");

    @Test(dataProvider = "formatting-data-provider")
    public void testAccept(String configName, String sourceName)
            throws WorkspaceDocumentException, JSONGenerationException, IOException {
        Path sourcePath = sourceDirPath.resolve(sourceName);
        String content = getFormattedContent(sourcePath);
        Path expectedFilePath = formattingPath.resolve(configName);

        byte[] expectedByte = Files.readAllBytes(expectedFilePath);
        String expect = new String(expectedByte);

        Assert.assertEquals(content, expect);
    }

    private String getFormattedContent(Path sourcePath) throws JSONGenerationException, WorkspaceDocumentException {
        String fileUri = sourcePath.toUri().toString();
        LSServiceOperationContext formatContext = new LSServiceOperationContext();
        formatContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);

        ExtendedWorkspaceDocumentManagerImpl documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        LSCompiler lsCompiler = new LSCompiler(documentManager);

        // Source generation for given ast.
        JsonObject ast = TextDocumentFormatUtil.getAST(fileUri, lsCompiler, documentManager, formatContext);
        SourceGen sourceGen = new SourceGen(0);
        sourceGen.build(ast.getAsJsonObject("model"), null, "CompilationUnit");
        FormattingVisitorExecutor formattingVisitorExecutor = new FormattingVisitorExecutor();
        formattingVisitorExecutor.accept(ast.getAsJsonObject("model"));
        return sourceGen.getSourceOf(ast.getAsJsonObject("model"), false, false);
    }

    @DataProvider(name = "formatting-data-provider")
    public Object[][] getLocalFunctionPositions() {
        return new Object[][]{
                {"formatted1.bal", "unformatted1.bal"},
                {"formatted2.bal", "unformatted2.bal"},
                {"formatted3.bal", "unformatted3.bal"},
                {"formatted4.bal", "unformatted4.bal"}
        };
    }
}
