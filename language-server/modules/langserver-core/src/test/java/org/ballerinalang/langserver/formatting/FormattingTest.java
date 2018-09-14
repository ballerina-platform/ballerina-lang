package org.ballerinalang.langserver.formatting;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FormattingTest {
    private Path formattingDirectory = FileUtils.RES_DIR.resolve("formatting");
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void loadLangServer() {
        LSAnnotationCache.initiate();
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @BeforeMethod
    public void clearPackageCache() {
        LSContextManager.getInstance().clearAllContexts();
    }

    @Test(description = "test formatting functionality on functions", dataProvider = "fileProvider")
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
        };
    }
}
