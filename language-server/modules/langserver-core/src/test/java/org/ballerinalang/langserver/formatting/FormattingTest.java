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
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(FormattingTest.class);

    @BeforeClass
    public void loadLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "test formatting functionality on functions", dataProvider = "fileProvider")
    public void formatTestSuit(String expectedFile, String testFile) throws IOException {
        Path expectedFilePath = formattingDirectory.resolve("expected").resolve(expectedFile);
        Path inputFilePath = formattingDirectory.resolve(testFile);

        String expected = new String(Files.readAllBytes(expectedFilePath));
        expected = expected.replaceAll("\\r\\n", "\n");
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
        actual = actual.replaceAll("\\r\\n", "\n");
        TestUtil.closeDocument(this.serviceEndpoint, inputFilePath);
        Assert.assertEquals(actual, expected, "Did not match: " + expectedFile);
    }

    @DataProvider(name = "fileProvider")
    public Object[][] fileProvider() {
        log.info("Test textDocument/format");
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
                {"expectedBinaryExpr.bal", "binaryExpr.bal"},
                {"expectedArrayLiteralExpr.bal", "arrayLiteralExpr.bal"},
                {"expectedForeach.bal", "foreach.bal"},
                {"expectedConstrainedType.bal", "constrainedType.bal"},
                {"expectedBreak.bal", "break.bal"},
                {"expectedMatchStmt.bal", "matchStmt.bal"},
                {"expectedAbort.bal", "abort.bal"},
                {"expectedTransaction.bal", "transaction.bal"},
                {"expectedContinue.bal", "continue.bal"},
                {"expectedTypeDefinition.bal", "typeDefinition.bal"},
                {"expectedTable.bal", "table.bal"},
                {"expectedCompilationUnitMultiEOF.bal", "compilationUnitMultiEOF.bal"},
                {"expectedAnnotation.bal", "annotation.bal"},
                {"expectedArrowExpr.bal", "arrowExpr.bal"},
                {"expectedAsyncExpr.bal", "asyncExpr.bal"},
                {"expectedBindingPatterns.bal", "bindingPatterns.bal"},
                {"expectedDocumentation.bal", "documentation.bal"},
                {"expectedWorkerInteractions.bal", "workerInteractions.bal"},
                {"expectedWait.bal", "wait.bal"},
                {"expectedCheck.bal", "check.bal"},
                {"expectedCompoundAssignment.bal", "compoundAssignment.bal"},
                {"expectedConstant.bal", "constant.bal"},
                {"expectedElvisExpr.bal", "elvisExpr.bal"},
                {"expectedErrorConstructor.bal", "errorConstructor.bal"},
                {"expectedRecordLiteralExpr.bal", "recordLiteralExpr.bal"},
                {"expectedRecordVariable.bal", "recordVariable.bal"},
                {"expectedTypeGuard.bal", "typeGuard.bal"},
                {"expectedTernaryExpr.bal", "ternaryExpr.bal"},
                {"expectedStringTemplateLiteral.bal", "stringTemplateLiteral.bal"},
                {"expectedTrap.bal", "trap.bal"},
                {"expectedPanic.bal", "panic.bal"},
                {"expectedErrorVariableDefinition.bal", "errorVariableDefinition.bal"},
                {"expectedErrorVariableReference.bal", "errorVariableReference.bal"},
                {"expectedErrorType.bal", "errorType.bal"},
                {"expectedIndexBasedAccess.bal", "indexBasedAccess.bal"},
                {"expectedIntegerRangeExpression.bal", "integerRangeExpression.bal"},
                {"expectedLock.bal", "lock.bal"},
                {"expectedImportOrder.bal", "importOrder.bal"},
                {"expectedBlockExpandOnDemand.bal", "blockExpandOnDemand.bal"},
                {"expectedNamedArgsExpr.bal", "namedArgsExpr.bal"},
                {"expectedRestArgsExpr.bal", "restArgsExpr.bal"},
                {"expectedRecordDestructure.bal", "recordDestructure.bal"},
                {"expectedCheckPanic.bal", "checkPanic.bal"},
                {"expectedAnonRecord.bal", "anonRecord.bal"},
                {"expectedInvocation.bal", "invocation.bal"},
                {"expectedTypeDesc.bal", "typeDesc.bal"},
                {"expectedAnonObject.bal", "anonObject.bal"},
                {"expectedUnaryExpr.bal", "unaryExpr.bal"},
                {"expectedTypeInitExpr.bal", "typeInitExpr.bal"},
                {"expectedServiceConstruct.bal", "serviceConstruct.bal"},
                {"expectedForkJoin.bal", "forkJoin.bal"},
                {"expectedEmpty.bal", "empty.bal"},
                {"expectedDelimiter.bal", "delimiter.bal"},
                {"expectedXmlns.bal", "xmlns.bal"},
                {"expectedXMLTextLiteral.bal", "xmlTextLiteral.bal"},
                {"expectedXMLCommentLiteral.bal", "xmlCommentLiteral.bal"},
                {"expectedXMLPILiteral.bal", "xmlPILiteral.bal"},
                //{"expectedXMLElementLiteral.bal", "xmlElementLiteral.bal"},
                {"expectedXMLAttribute.bal", "xmlAttribute.bal"},
                {"expectedXMLAttributeAccessExpr.bal", "xmlAttributeAccessExpr.bal"},
                {"expectedXMLQName.bal", "xmlQName.bal"},
                {"expectedUnicodeCharTest.bal", "unicodeCharTest.bal"},
                {"expectedTupleTypeRest.bal", "tupleTypeRest.bal"},
                {"expectedTupleDestructure.bal", "tupleDestructure.bal"},
                {"expectedNegativeEOF.bal", "negativeEOF.bal"},
                {"expectedListConstructorExpr.bal", "listConstructorExpr.bal"},
        };
    }

    @AfterClass
    public void shutdownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
