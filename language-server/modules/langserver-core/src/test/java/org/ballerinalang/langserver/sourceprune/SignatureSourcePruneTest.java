package org.ballerinalang.langserver.sourceprune;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.completions.util.SourcePruneException;
import org.ballerinalang.langserver.signature.sourceprune.SignatureTokenTraverserFactory;
import org.ballerinalang.langserver.util.FileUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Test the source prune operation with specific sources individual from the signature operation.
 *
 * @since 1.1.0
 */
public class SignatureSourcePruneTest {
    private Path configRoot;
    private Path sourceRoot;
    private Path expectedRoot;
    private WorkspaceDocumentManagerImpl documentManager;
    private Gson gson = new Gson();
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @BeforeClass
    public void init() throws Exception {
        configRoot = FileUtils.RES_DIR.resolve("signature").resolve("sourceprune").resolve("config");
        expectedRoot = FileUtils.RES_DIR.resolve("signature").resolve("sourceprune").resolve("expected");
        sourceRoot = FileUtils.RES_DIR.resolve("signature").resolve("sourceprune").resolve("sources");
        documentManager = WorkspaceDocumentManagerImpl.getInstance();
    }

    @Test(dataProvider = "testDataProvider")
    public void testSourcePrune(String configPath) throws IOException, WorkspaceDocumentException {
        Path sourcePath = configRoot.resolve(configPath);
        JsonObject configObject = FileUtils.fileContentAsObject(sourcePath.toString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        String source = configObject.getAsJsonPrimitive("source").getAsString();
        LSContext lsContext = this.getLSContext(source, position);
        String fileUri = lsContext.get(DocumentServiceKeys.FILE_URI_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (!filePath.isPresent()) {
            Assert.fail("Invalid File path: [" + fileUri + "]");
        }
        String documentContent = new String(Files.readAllBytes(filePath.get())).replaceAll("\r?\n", LINE_SEPARATOR);

        this.documentManager.openFile(filePath.get(), documentContent);
        try {
            TokenTraverserFactory tokenTraverserFactory = new SignatureTokenTraverserFactory(filePath.get(),
                                                                                             documentManager,
                                                                                             SourcePruner.newContext());
            SourcePruner.pruneSource(lsContext, tokenTraverserFactory);
            String prunedSource = tokenTraverserFactory.getTokenStream().getText();
            Path expectedPath = expectedRoot.resolve(configObject.getAsJsonPrimitive("expected").getAsString());
            String expected = new String(Files.readAllBytes(expectedPath)).replaceAll("\r?\n", LINE_SEPARATOR);
            boolean sourceMatch = prunedSource.equals(expected);
            if (!sourceMatch) {
                Assert.fail("Sources Does not Match for " + configPath + System.lineSeparator()
                                    + "Pruned Source [" + prunedSource + "]" + System.lineSeparator()
                                    + "Expected Source [" + expected + "]");
            }
            Assert.assertEquals(prunedSource, expected);
        } catch (SourcePruneException e) {
            Assert.fail(e.getMessage());
        }
    }

    private LSContext getLSContext(String source, Position position) {
        LSContext lsContext = new LSServiceOperationContext(LSContextOperation.SOURCE_PRUNER);
        URI fileUri = sourceRoot.resolve(source).toUri();
        TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
        positionParams.setPosition(position);
        lsContext.put(CommonKeys.DOC_MANAGER_KEY, documentManager);
        lsContext.put(DocumentServiceKeys.POSITION_KEY, positionParams);
        lsContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri.toString());

        return lsContext;
    }

    @DataProvider
    public Object[][] testDataProvider() {
        return new Object[][] {
                {"src_prune_config1.json"},
                {"src_prune_config2.json"},
                {"src_prune_config3.json"},
                {"src_prune_config4.json"}
        };
    }
}
