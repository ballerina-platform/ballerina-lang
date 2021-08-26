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
package org.ballerinalang.langserver.performance;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test performance of openDocument in completions feature in language server.
 */
public class CompletionOpenDocPerformanceTest extends CompletionPerformanceTest {

    private Endpoint serviceEndpoint;
    private final Path testRoot = FileUtils.RES_DIR.resolve("performance");

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }
    
    @Test(dataProvider = "performance-data-provider")
    public void testCompletion(String config, String configPath) throws IOException, WorkspaceDocumentException {
        super.testCompletion(config, configPath);
    }

    @Override
    public long getResponseTimeCompletion(JsonObject configJsonObject) throws IOException {
        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());
        String responseString;
        Position position = new Position();
        JsonObject positionObj = configJsonObject.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        JsonElement triggerCharElement = configJsonObject.get("triggerCharacter");
        String triggerChar = triggerCharElement == null ? "" : triggerCharElement.getAsString();

        long start = System.currentTimeMillis();
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        long end = System.currentTimeMillis();
        long responseTime = end - start;
        responseString = TestUtil.getCompletionResponse(sourcePath.toString(), position,
                this.serviceEndpoint, triggerChar);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        return responseTime;
    }
    
    @Override
    String getResponseCompletion(JsonObject configJsonObject) throws IOException {
        Path sourcePath = testRoot.resolve(configJsonObject.get("source").getAsString());
        String responseString;
        Position position = new Position();
        JsonObject positionObj = configJsonObject.get("position").getAsJsonObject();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        JsonElement triggerCharElement = configJsonObject.get("triggerCharacter");
        String triggerChar = triggerCharElement == null ? "" : triggerCharElement.getAsString();

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        responseString = TestUtil.getCompletionResponse(sourcePath.toString(), position,
                this.serviceEndpoint, triggerChar);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        return responseString;
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
    
    @DataProvider(name = "performance-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return "performance_completion";
    }
}
