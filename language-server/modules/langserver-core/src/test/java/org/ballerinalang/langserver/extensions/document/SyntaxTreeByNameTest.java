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
 * Test syntaxTreeByName call.
 * @since 2.0.0
 */
public class SyntaxTreeByNameTest {
    private Endpoint serviceEndpoint;

    private final Path projectFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("balProject")
            .resolve("main.bal");

    private final Path sameFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainFunction.bal");

    @BeforeClass
    public void startLanguageServer() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Find ST by range in a file")
    public void testFindFunctionInSameFile() throws Exception {
        TestUtil.openDocument(serviceEndpoint, sameFile);
        Range range = new Range(new Position(4, 13), new Position(4, 26));
        BallerinaSyntaxTreeResponse syntaxTreeByNameResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByName(
                sameFile.toString(), range, this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeByNameResponse.isParseSuccess());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSyntaxTree());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSource());
        TestUtil.closeDocument(this.serviceEndpoint, sameFile);
    }

    @Test(description = "Find ST by range in a project")
    public void testFindFunctionInProject() throws Exception {
        TestUtil.openDocument(serviceEndpoint, projectFile);
        Range range = new Range(new Position(1, 12), new Position(1, 21));
        BallerinaSyntaxTreeResponse syntaxTreeByNameResponse = LSExtensionTestUtil.getBallerinaSyntaxTreeByName(
                projectFile.toString(), range, this.serviceEndpoint);
        Assert.assertTrue(syntaxTreeByNameResponse.isParseSuccess());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSyntaxTree());
        Assert.assertNotNull(syntaxTreeByNameResponse.getSource());
        TestUtil.closeDocument(this.serviceEndpoint, projectFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
