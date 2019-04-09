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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.SourcePruneException;
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
        String documentContent = new String(Files.readAllBytes(sourcePath)).replaceAll("\r?\n", LINE_SEPARATOR);
        JsonObject configObject = FileUtils.fileContentAsObject(sourcePath.toString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        String source = configObject.getAsJsonPrimitive("source").getAsString();
        LSContext lsContext = this.getLSContext(source, position);

        this.documentManager.openFile(sourcePath, documentContent);
        try {
            String prunedSource = CompletionUtil.getPrunedSource(lsContext);
            Path expectedPath = expectedRoot.resolve(configObject.getAsJsonPrimitive("expected").getAsString());
            String expected = new String(Files.readAllBytes(expectedPath));
            System.out.println("** Pruned Source **\n" + prunedSource + "\n***********");
            Assert.assertEquals(prunedSource, expected);
        } catch (SourcePruneException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    private LSContext getLSContext(String source, Position position) {
        LSContext lsContext = new LSServiceOperationContext();
        URI fileUri = sourceRoot.resolve(source).toUri();
        TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
        positionParams.setPosition(position);
        lsContext.put(CompletionKeys.DOC_MANAGER_KEY, documentManager);
        lsContext.put(DocumentServiceKeys.POSITION_KEY, positionParams);
        lsContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri.toString());
        
        return lsContext;
    }

    @DataProvider
    public Object[][] testDataProvider() {
        return new Object[][] {
//                {"src_prune_config1.json"},
                // Top Level
//                {"src_prune_config2.json"},
                // Import statement and xml namespace are similar
//                {"src_prune_config3.json"},
//                {"src_prune_config4.json"},
//                {"src_prune_config5.json"},
//                {"src_prune_config6.json"},
                // Annotation attachments
//                {"src_prune_config7.json"},
//                {"src_prune_config8.json"},
//                {"src_prune_config9.json"},
//                {"src_prune_config10.json"},
                {"src_prune_config11.json"},
                
        };
    }
}
