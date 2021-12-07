/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.workspace;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test the watched file changes.
 * 
 * @since 2.0.0
 */
public class TestDidChangeWatchedFiles {
    protected Endpoint serviceEndpoint;

    private final Path projectRoot = FileUtils.RES_DIR.resolve("workspace")
            .resolve("projects").resolve("prj_ws_did_change");

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test
    public void testWatchedFileChange() {
        try {
            // Open a file to mimic the file open on the editor
            Path openDoc1 = projectRoot.resolve("modules/mod1/mod1.bal");
            TestUtil.openDocument(serviceEndpoint, openDoc1);
            
            /*
            Add a textEdit to the opened document and a watched file change event.
            Watch file event should not affect the model and only the didChange event should should impact.
            Check the change has been reflected with documentSymbol
             */
            TestUtil.didChangeDocument(serviceEndpoint, openDoc1, balDocumentChange1());
            this.sendSingleFileEvent(openDoc1, FileChangeType.Changed);
            String documentSymbolResponse1 = TestUtil.getDocumentSymbolResponse(serviceEndpoint, openDoc1.toString());
            String symbolName = JsonParser.parseString(documentSymbolResponse1).getAsJsonObject().get("result")
                    .getAsJsonArray().get(0).getAsJsonObject().get("right").getAsJsonObject().get("name").getAsString();
            String expectedSymbolResp1 = "testF";
            Assert.assertEquals(symbolName, expectedSymbolResp1);

            // Delete a single file and check the change has been reflected with documentSymbol
            this.sendSingleFileEvent(openDoc1, FileChangeType.Deleted);
            String documentSymbolResponse2 = TestUtil.getDocumentSymbolResponse(serviceEndpoint, openDoc1.toString());
            JsonArray response2 = JsonParser.parseString(documentSymbolResponse2).getAsJsonObject().get("result")
                    .getAsJsonArray();
            Assert.assertEquals(response2.size(), 0);

            Path changeDoc1 = projectRoot.resolve("modules/mod2/mod2.bal");
            Path changeDoc2 = projectRoot.resolve("modules/mod3/mod3.bal");
            /*
            Two watch file events are sent for the file changes
            Check the change has been reflected with documentSymbol. Since the project is loaded from the disk now,
            the original project content should be there.
            Note: Original content is there since we mimic the change event only, without actually changing the disk's 
            content 
             */
            Map<Path, FileChangeType> changesMap = new HashMap<>();
            changesMap.put(changeDoc1, FileChangeType.Changed);
            changesMap.put(changeDoc2, FileChangeType.Changed);
            this.sendMultipleFileChanges(changesMap);
            String documentSymbolResponse3 = TestUtil.getDocumentSymbolResponse(serviceEndpoint, openDoc1.toString());
            String symbolName3 = JsonParser.parseString(documentSymbolResponse3).getAsJsonObject().get("result")
                    .getAsJsonArray().get(0).getAsJsonObject().get("right").getAsJsonObject().get("name").getAsString();
            String expectedSymbolResp3 = "hello";
            Assert.assertEquals(symbolName3, expectedSymbolResp3);
        } catch (IOException e) {
            Assert.fail("Failed to execute test");
            // ignore
        }
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private String balDocumentChange1() {
        return "function testF() {}" + CommonUtil.LINE_SEPARATOR;
    }

    private void sendSingleFileEvent(Path path, FileChangeType fileChangeType) {
        FileEvent event = new FileEvent();
        event.setType(fileChangeType);
        event.setUri(path.toUri().toString());

        DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams();
        params.setChanges(Collections.singletonList(event));

        TestUtil.didChangeWatchedFiles(this.serviceEndpoint, params);
    }

    private void sendMultipleFileChanges(Map<Path, FileChangeType> changesMap) {
        List<FileEvent> fileEvents = changesMap.entrySet().stream()
                .map(entry -> new FileEvent(entry.getKey().toUri().toString(), entry.getValue()))
                .collect(Collectors.toList());

        DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams();
        params.setChanges(fileEvents);

        TestUtil.didChangeWatchedFiles(this.serviceEndpoint, params);
    }
}
