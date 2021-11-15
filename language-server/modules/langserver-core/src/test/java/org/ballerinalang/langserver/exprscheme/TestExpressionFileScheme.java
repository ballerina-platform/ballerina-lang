package org.ballerinalang.langserver.exprscheme;

import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
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
    public void testExprScheme() throws IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject2").resolve("main.bal");

        byte[] encodedContent = Files.readAllBytes(filePath);
        String originalContent = new String(encodedContent);
        URI originalUri = URI.create(filePath.toUri().toString());
        URI exprUri = URI.create(filePath.toUri().toString().replace("file:///", "expr:///"));

        TestUtil.openDocument(serviceEndpoint, originalUri.toString(), originalContent);
        TestUtil.openDocument(serviceEndpoint, exprUri.toString(), originalContent);

        // Get the completions over the original content.
        Position pos1 = new Position(1, 2);
        String completionRes1 = TestUtil.getCompletionResponse(originalUri, pos1, serviceEndpoint, "");

        // send a did change with the expr scheme
        String exprContent = originalContent + System.lineSeparator() + "function exprEdit() {}";
        TestUtil.didChangeDocument(serviceEndpoint, exprUri, exprContent);

        // Get completions over the original content after changing the content over expr scheme
        String completionRes2 = TestUtil.getCompletionResponse(originalUri, pos1, serviceEndpoint, "");
        // Both the responses should be the same and ensures the original content is not affected 
        Assert.assertEquals(completionRes1, completionRes2);

        // Get completions over the expr content after changing the content over expr scheme
        String completionRes3 = TestUtil.getCompletionResponse(exprUri, pos1, serviceEndpoint, "");
        // Check whether the expr content change has modified the content
        Assert.assertNotEquals(completionRes1, completionRes3);
    }
}
