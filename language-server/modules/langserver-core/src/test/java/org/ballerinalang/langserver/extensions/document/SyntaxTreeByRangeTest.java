package org.ballerinalang.langserver.extensions.document;

import com.google.gson.JsonElement;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test syntaxTreeByRange call.
 * @since 2.0.0
 */
public class SyntaxTreeByRangeTest {
    private Endpoint serviceEndpoint;

    private final Path mainFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("syntaxTree")
            .resolve("main.bal");

    private final Path importDeclarationFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("syntaxTree")
            .resolve("byRange")
            .resolve("importDeclaration.bal");

    private final Path emptyFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("syntaxTree")
            .resolve("empty.bal");

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Request for an empty ballerina file")
    public void testEmptyFile() throws Exception {
        TestUtil.openDocument(serviceEndpoint, emptyFile);
        Range range = new Range(new Position(0, 0), new Position(0, 1));
        BallerinaSyntaxTreeResponse response = LSExtensionTestUtil.getBallerinaSyntaxTreeByRange(emptyFile.toString(),
                range, this.serviceEndpoint);
        Assert.assertFalse(response.isParseSuccess());
        Assert.assertNull(response.getSyntaxTree());
        Assert.assertNull(response.getSource());
        TestUtil.closeDocument(this.serviceEndpoint, emptyFile);
    }

    @Test(description = "Generate tree for a range that is a sub tree of the file's syntax tree")
    public void testSubTreeNode() throws Exception {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        Range range = new Range(new Position(0, 0), new Position(0, 20));
        BallerinaSyntaxTreeResponse syntaxTreeByRangeResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByRange(
                mainFile.toString(), range, this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeByRangeResponse.isParseSuccess());
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);

        TestUtil.openDocument(serviceEndpoint, importDeclarationFile);
        BallerinaSyntaxTreeResponse syntaxTreeResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                importDeclarationFile.toString(), this.serviceEndpoint);
        JsonElement importsNode = syntaxTreeResponse.getSyntaxTree().getAsJsonObject().get("imports");
        Assert.assertTrue(syntaxTreeResponse.isParseSuccess());
        TestUtil.closeDocument(this.serviceEndpoint, importDeclarationFile);

        Assert.assertEquals(syntaxTreeByRangeResponse.getSyntaxTree(),
                importsNode.getAsJsonArray().get(0).getAsJsonObject());
    }

    @Test(description = "Generate tree for a range that captures a list of nodes")
    public void testModulePartNode() throws Exception {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        Range range = new Range(new Position(0, 4), new Position(3, 15));
        BallerinaSyntaxTreeResponse syntaxTreeByRangeResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByRange(
                mainFile.toString(), range, this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeByRangeResponse.isParseSuccess());

        BallerinaSyntaxTreeResponse syntaxTreeResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(mainFile.toString(),
                this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeResponse.isParseSuccess());

        Assert.assertEquals(syntaxTreeByRangeResponse.getSyntaxTree(), syntaxTreeResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
