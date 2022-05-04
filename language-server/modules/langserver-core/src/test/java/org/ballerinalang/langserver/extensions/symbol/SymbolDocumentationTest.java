package org.ballerinalang.langserver.extensions.symbol;

import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.symbol.SymbolInfoResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

public class SymbolDocumentationTest {
    private Endpoint serviceEndpoint;

    private final Path symbolDocumentBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("symbol")
            .resolve("symbolDocumentation.bal");

    @BeforeClass
    public void startLangServer() throws IOException {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "documentation response generated for user defined documentation")
    public void testUserDefinedSymbolDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(15);
        functionPos.setCharacter(19);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertEquals(symbolInfoResponse.getDocumentation().description,"Adds two integers.");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().returnValueDescription,"the sum of `x` and `y`");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(0).name,"x");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(0).description,"an integer");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(1).name,"y");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(1).description,"another integer");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "documentation response generated for ballerina standardLib function")
    public void testStandardLibSymbolDocumentation() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(symbolDocumentBalFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Position functionPos = new Position();
        functionPos.setLine(16);
        functionPos.setCharacter(16);
        SymbolInfoResponse symbolInfoResponse = LSExtensionTestUtil.getSymbolDocumentation(
                inputFile.toString(), functionPos, this.serviceEndpoint);

        Assert.assertEquals(symbolInfoResponse.getDocumentation().description,
                "Prints info logs.\n```ballerina\nlog:printInfo(\"info message\", id = 845315)\n```\n");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(0).name,"msg");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(0).description,"The message to be logged");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(0).kind,"REQUIRED");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().parameters.get(0).type,"string");
        Assert.assertEquals(symbolInfoResponse.getDocumentation().returnValueDescription,null);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);

    }
}
