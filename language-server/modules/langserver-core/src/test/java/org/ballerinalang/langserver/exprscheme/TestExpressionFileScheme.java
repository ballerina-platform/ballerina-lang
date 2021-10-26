package org.ballerinalang.langserver.exprscheme;

import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestExpressionFileScheme {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/project");
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
    
    @Test
    public void testProjectOpen() throws IOException {
        // Inputs from lang server
        openFile(RESOURCE_DIRECTORY.resolve("myproject2").resolve("main.bal"));
        Assert.fail("Failed Forced");
    }

    private void openFile(Path filePath) throws IOException {
        DidOpenTextDocumentParams documentParams = new DidOpenTextDocumentParams();
        TextDocumentItem textDocumentItem = new TextDocumentItem();
        TextDocumentIdentifier identifier = new TextDocumentIdentifier();

        byte[] encodedContent = Files.readAllBytes(filePath);
        identifier.setUri(filePath.toUri().toString().replace("file:///", "expr:///"));
        textDocumentItem.setUri(identifier.getUri());
        textDocumentItem.setText(new String(encodedContent));
        documentParams.setTextDocument(textDocumentItem);

        serviceEndpoint.notify("textDocument/didOpen", documentParams);
    }
}
