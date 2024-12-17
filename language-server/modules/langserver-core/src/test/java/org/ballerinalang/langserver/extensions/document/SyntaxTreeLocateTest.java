package org.ballerinalang.langserver.extensions.document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

import java.io.FileReader;
import java.nio.file.Path;

/**
 * Test syntaxTreeLocate call.
 * @since 2.0.0
 */

public class SyntaxTreeLocateTest {
    private Endpoint serviceEndpoint;
    private static final Path RESOURCE_DIRECTORY = Path.of(
            "src/test/resources/extensions/document/syntaxTree/locate/");

    private final Path mainFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("syntaxTree")
            .resolve("main.bal");

    private final Path emptyFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("syntaxTree")
            .resolve("empty.bal");

    private final Path sourceFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("syntaxTree")
            .resolve("locate")
            .resolve("source.bal");

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test (description = "Request for an empty Ballerina file")
    public void testEmptySourceFile() throws Exception {
        TestUtil.openDocument(serviceEndpoint, emptyFile);
        Range range = new Range(new Position(0, 0), new Position(0, 10));
        BallerinaSyntaxTreeResponse response = LSExtensionTestUtil.getBallerinaSyntaxTreeLocate(emptyFile.toString(),
                range, this.serviceEndpoint);
        Assert.assertFalse(response.isParseSuccess());
        Assert.assertNull(response.getSyntaxTree());
        Assert.assertNull(response.getSource());
        TestUtil.closeDocument(this.serviceEndpoint, emptyFile);
    }

    @Test (description = "Locate imports node")
    public void testLocateImportsNode() throws Exception {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        Range range = new Range(new Position(0, 0), new Position(0, 20));
        BallerinaSyntaxTreeResponse response = LSExtensionTestUtil.getBallerinaSyntaxTreeLocate(mainFile.toString(),
                range, this.serviceEndpoint);
        Assert.assertTrue(response.isParseSuccess());

        Path filePath = RESOURCE_DIRECTORY.resolve("locateImportsNode.json");
        Gson gson = new Gson();
        JsonObject expectedSyntaxTree = gson.fromJson(new FileReader(filePath.toFile()), JsonObject.class);
        Assert.assertEquals(response.getSyntaxTree().getAsJsonObject(), expectedSyntaxTree.get("syntaxTree"));
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    @Test (description = "Locate a member node")
    public void testLocateMemberNode() throws Exception {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        Range range = new Range(new Position(3, 7), new Position(3, 14));
        BallerinaSyntaxTreeResponse response = LSExtensionTestUtil.getBallerinaSyntaxTreeLocate(mainFile.toString(),
                range, this.serviceEndpoint);
        Assert.assertTrue(response.isParseSuccess());

        Path filePath = RESOURCE_DIRECTORY.resolve("locateMemberNode.json");
        Gson gson = new Gson();
        JsonObject expectedSyntaxTree = gson.fromJson(new FileReader(filePath.toFile()), JsonObject.class);
        Assert.assertEquals(response.getSyntaxTree().getAsJsonObject(), expectedSyntaxTree.get("syntaxTree"));
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    @Test (description = "Locate a list of nodes that leads to finding the parent node")
    public void testLocateNodeList() throws Exception {
        TestUtil.openDocument(serviceEndpoint, sourceFile);
        Range range = new Range(new Position(3, 4), new Position(4, 15));
        BallerinaSyntaxTreeResponse response = LSExtensionTestUtil.getBallerinaSyntaxTreeLocate(sourceFile.toString(),
                range, this.serviceEndpoint);
        Assert.assertTrue(response.isParseSuccess());

        Path filePath = RESOURCE_DIRECTORY.resolve("locateNodeList.json");
        Gson gson = new Gson();
        JsonObject expectedSyntaxTree = gson.fromJson(new FileReader(filePath.toFile()), JsonObject.class);
        Assert.assertEquals(response.getSyntaxTree().getAsJsonObject(), expectedSyntaxTree.get("syntaxTree"));
        TestUtil.closeDocument(this.serviceEndpoint, sourceFile);
    }

    @Test (description = "Locate a list of nodes that leads to finding the module part node")
    public void testModulePartNode() throws Exception {
        TestUtil.openDocument(serviceEndpoint, sourceFile);
        Range range = new Range(new Position(0, 4), new Position(4, 15));
        BallerinaSyntaxTreeResponse response = LSExtensionTestUtil.getBallerinaSyntaxTreeLocate(sourceFile.toString(),
                range, this.serviceEndpoint);
        Assert.assertTrue(response.isParseSuccess());

        Path filePath = RESOURCE_DIRECTORY.resolve("locateModulePartNode.json");
        Gson gson = new Gson();
        JsonObject expectedSyntaxTree = gson.fromJson(new FileReader(filePath.toFile()), JsonObject.class);
        Assert.assertEquals(response.getSyntaxTree().getAsJsonObject(), expectedSyntaxTree.get("syntaxTree"));
        TestUtil.closeDocument(this.serviceEndpoint, sourceFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
