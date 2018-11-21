/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.formatting;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test suit for source code formatting.
 */
public class FormattingTest {
    private Path formattingDirectory = FileUtils.RES_DIR.resolve("formatting");
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void loadLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "test formatting functionality on functions", dataProvider = "fileProvider", enabled = false)
    public void formatTestSuit(String expectedFile, String testFile) throws IOException {
        Path expectedFilePath = formattingDirectory.resolve("expected").resolve(expectedFile);
        Path inputFilePath = formattingDirectory.resolve(testFile);

        String expected = new String(Files.readAllBytes(expectedFilePath));
        DocumentFormattingParams documentFormattingParams = new DocumentFormattingParams();

        TextDocumentIdentifier textDocumentIdentifier1 = new TextDocumentIdentifier();
        textDocumentIdentifier1.setUri(Paths.get(inputFilePath.toString()).toUri().toString());

        FormattingOptions formattingOptions = new FormattingOptions();
        formattingOptions.setInsertSpaces(true);
        formattingOptions.setTabSize(4);

        documentFormattingParams.setOptions(formattingOptions);
        documentFormattingParams.setTextDocument(textDocumentIdentifier1);

        TestUtil.openDocument(this.serviceEndpoint, inputFilePath);

        String result = TestUtil.getFormattingResponse(documentFormattingParams, this.serviceEndpoint);
        Gson gson = new Gson();
        ResponseMessage responseMessage = gson.fromJson(result, ResponseMessage.class);
        String actual = (String) ((LinkedTreeMap) ((List) responseMessage.getResult()).get(0)).get("newText");
        TestUtil.closeDocument(this.serviceEndpoint, inputFilePath);
        Assert.assertEquals(actual, expected, "Did not match: " + expectedFile);
    }

    @DataProvider(name = "fileProvider")
    public Object[][] fileProvider() {
        return new Object[][]{
                {"expectedFunction.bal", "function.bal"},
                {"expectedEndpoint.bal", "endpoint.bal"},
                {"expectedWorker.bal", "worker.bal"},
                {"expectedExpressionStatement.bal", "expressionStatement.bal"},
                {"expectedService.bal", "service.bal"},
                {"expectedReturn.bal", "return.bal"},
                {"expectedCompilationUnit.bal", "compilationUnit.bal"},
                {"expectedImport.bal", "import.bal"},
                {"expectedTupleType.bal", "tupleType.bal"},
                {"expectedVariableDefinition.bal", "variableDefinition.bal"},
                {"expectedUnionType.bal", "unionType.bal"},
                {"expectedRecord.bal", "record.bal"},
                {"expectedObject.bal", "object.bal"},
                {"expectedFieldBasedAccess.bal", "fieldBasedAccess.bal"},
                {"expectedFunctionType.bal", "functionType.bal"},
                {"expectedWhile.bal", "while.bal"},
                {"expectedIf.bal", "if.bal"},
                {"expectedTryCatch.bal", "tryCatch.bal"},
                {"expectedBinaryExpr.bal", "binaryExpr.bal"},
                {"expectedArrayLiteralExpr.bal", "arrayLiteralExpr.bal"},
                {"expectedDone.bal", "done.bal"},
                {"expectedForeach.bal", "foreach.bal"},
                {"expectedConstrainedType.bal", "constrainedType.bal"},
                {"expectedBreak.bal", "break.bal"},
                {"expectedMatchStmt.bal", "matchStmt.bal"},
                {"expectedMatchExpr.bal", "matchExpr.bal"},
                {"expectedAbort.bal", "abort.bal"},
                {"expectedTransaction.bal", "transaction.bal"},
                {"expectedContinue.bal", "continue.bal"},
                {"expectedTypeDefinition.bal", "typeDefinition.bal"},
                {"expectedTable.bal", "table.bal"},
                {"expectedCompilationUnitMultiEOF.bal", "compilationUnitMultiEOF.bal"},
                {"expectedAnnotation.bal", "annotation.bal"},
                {"expectedArrowExpr.bal", "arrowExpr.bal"},
        };
    }

    @AfterClass
    public void shutdownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
