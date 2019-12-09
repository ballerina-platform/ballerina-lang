/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
import org.ballerinalang.langserver.completions.sourceprune.CompletionsTokenTraverserFactory;
import org.ballerinalang.langserver.completions.util.SourcePruneException;
import org.ballerinalang.langserver.util.FileUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Test the source prune operation with specific sources individual from the completion operation.
 * 
 * @since 0.995.0
 */
public class SourcePruneTest {
    private Path configRoot;
    private Path sourceRoot;
    private Path expectedRoot;
    private WorkspaceDocumentManagerImpl documentManager;
    private Gson gson = new Gson();
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @BeforeClass
    public void init() throws Exception {
        configRoot = FileUtils.RES_DIR.resolve("sourceprune").resolve("config");
        expectedRoot = FileUtils.RES_DIR.resolve("sourceprune").resolve("expected");
        sourceRoot = FileUtils.RES_DIR.resolve("sourceprune").resolve("sources");
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
            TokenTraverserFactory traverserFactory = new CompletionsTokenTraverserFactory(filePath.get(),
                                                                                          documentManager,
                                                                                          SourcePruner.newContext());
            SourcePruner.pruneSource(lsContext, traverserFactory);
            String prunedSource = traverserFactory.getTokenStream().getText();
            Path expectedPath = expectedRoot.resolve(configObject.getAsJsonPrimitive("expected").getAsString());
            String expected = new String(Files.readAllBytes(expectedPath)).replaceAll("\r?\n", LINE_SEPARATOR);
            boolean sourceMatch = prunedSource.equals(expected);
            if (!sourceMatch) {
                Assert.fail("Sources Does not Match for " + configPath + System.lineSeparator()
                        + "Pruned Source [" + prunedSource + "]" + System.lineSeparator()
                        + "Expected Source [" + expected + "]");
            }
            Assert.assertEquals(prunedSource, expected);
            BallerinaParser parser = CommonUtil.prepareParser(prunedSource);
            parser.compilationUnit();
            boolean prunedSourceErrors = parser.getNumberOfSyntaxErrors() != 0;
            if (prunedSourceErrors) {
                Assert.fail("Pruned Source Contains errors for " + configPath + System.lineSeparator()
                        + "Pruned Source [" + prunedSource + "]" + System.lineSeparator()
                        + "Expected Source [" + expected + "]");
            }
            Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 0);
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
                // Top Level
                {"src_prune_config2.json"},
                // Import statement and xml namespace are similar
                {"src_prune_config3.json"},
                {"src_prune_config4.json"},
                {"src_prune_config5.json"},
                {"src_prune_config6.json"},
                // Annotation attachments
                {"src_prune_config7.json"},
                {"src_prune_config8.json"},
                {"src_prune_config9.json"},
                {"src_prune_config10.json"},
                {"src_prune_config11.json"},
                {"src_prune_config12.json"},
                // Service definition
                {"src_prune_config13.json"},
                {"src_prune_config14.json"},
                {"src_prune_config15.json"},
//                {"src_prune_config16.json"},
                // Function Definition
                {"src_prune_config17.json"},
                {"src_prune_config18.json"},
                {"src_prune_config19.json"},
                {"src_prune_config20.json"},
                {"src_prune_config21.json"},
                // Type Definition
                {"src_prune_config22.json"},
                {"src_prune_config23.json"},
                {"src_prune_config24.json"},
                // Annotation Definition
                {"src_prune_config25.json"},
//                {"src_prune_config26.json"},
                {"src_prune_config78.json"},
                {"src_prune_config79.json"},
                // Global variable Definition
                {"src_prune_config27.json"},
                {"src_prune_config28.json"},
                {"src_prune_config29.json"},
                // Error Destructure Statement
                {"src_prune_config30.json"},
                {"src_prune_config31.json"},
                // Variable Definition statement
                {"src_prune_config32.json"},
                {"src_prune_config33.json"},
                {"src_prune_config34.json"},
                {"src_prune_config35.json"},
                {"src_prune_config36.json"},
                {"src_prune_config37.json"},
                {"src_prune_config80.json"},
                {"src_prune_config81.json"},
                // Compound assignment statement
                {"src_prune_config38.json"},
                // If else statement conditions
                {"src_prune_config39.json"},
                {"src_prune_config40.json"},
                {"src_prune_config41.json"},
                // Match Statement
                {"src_prune_config42.json"},
                // Foreach statement
                {"src_prune_config47.json"},
                {"src_prune_config48.json"},
                {"src_prune_config49.json"},
                {"src_prune_config50.json"},
                {"src_prune_config51.json"},
                // Worker Declaration
                {"src_prune_config52.json"},
                {"src_prune_config53.json"},
                // Transaction statement
                {"src_prune_config54.json"},
                {"src_prune_config55.json"},
                {"src_prune_config56.json"},
                // XML Namespace Declaration statement
//                {"src_prune_config57.json"},
                // Array Literal Expression
                {"src_prune_config58.json"},
                // Record Literal Expression
                {"src_prune_config59.json"},
                {"src_prune_config60.json"},
                {"src_prune_config61.json"},
                // Table Literal expression
                {"src_prune_config62.json"},
                {"src_prune_config63.json"},
                {"src_prune_config64.json"},
                // Action invocation
                {"src_prune_config65.json"},
                {"src_prune_config66.json"},
                // Function invocation
                {"src_prune_config67.json"},
                // Function Pointer
                {"src_prune_config68.json"},
                {"src_prune_config69.json"},
                {"src_prune_config70.json"},
                {"src_prune_config71.json"},
                // Arrow Function
                {"src_prune_config72.json"},
                {"src_prune_config73.json"},
                {"src_prune_config74.json"},
                {"src_prune_config75.json"},
                {"src_prune_config76.json"},
                // Iterable Operators
                {"src_prune_config77.json"},
                // Type Descriptor
                {"src_prune_config82.json"},
                // Invalid source without semicolon after close parenthesis
                {"src_prune_config83.json"},
                {"src_prune_config84.json"},
                {"src_prune_config85.json"},
                {"src_prune_config86.json"},
                {"src_prune_config87.json"},
                {"src_prune_config88.json"},
        };
    }
}
