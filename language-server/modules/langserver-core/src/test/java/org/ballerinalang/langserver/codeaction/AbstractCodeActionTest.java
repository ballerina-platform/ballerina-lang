/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public abstract class AbstractCodeActionTest {
    private Endpoint serviceEndpoint;

    private final JsonParser parser = new JsonParser();

    private final Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    private static final WorkspaceManager workspaceManager = new BallerinaWorkspaceManager();

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException {
        String configJsonPath =
                "codeaction" + File.separator + getResourceDir() + File.separator + "config" + File.separator + config;
        Path sourcePath = sourcesPath.resolve(getResourceDir()).resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        // Filter diagnostics for the cursor position
        List<Diagnostic> diags = new ArrayList<>(
                CodeActionUtil.toDiagnostics(TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager)));
        Position pos = new Position(configJsonObject.get("line").getAsInt(),
                                    configJsonObject.get("character").getAsInt());
        diags = diags.stream().
                filter(diag -> CommonUtil.isWithinRange(pos, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);

        Range range = new Range(pos, pos);
        String res = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, codeActionContext);

        for (JsonElement element : configJsonObject.get("expected").getAsJsonArray()) {
            JsonObject expected = element.getAsJsonObject();
            String expTitle = expected.get("title").getAsString();

            boolean codeActionFound = false;
            JsonObject responseJson = this.getResponseJson(res);
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
                JsonObject right = jsonElement.getAsJsonObject().get("right").getAsJsonObject();
                if (right != null) {
                    // Match title
                    String actualTitle = right.get("title").getAsString();
                    if (!expTitle.equals(actualTitle)) {
                        continue;
                    }
                    // Match edits
                    if (expected.get("edits") != null) {
                        JsonArray actualEdit = right.get("edit").getAsJsonObject().get("documentChanges")
                                .getAsJsonArray().get(0).getAsJsonObject().get("edits").getAsJsonArray();
                        JsonArray expEdit = expected.get("edits").getAsJsonArray();
                        if (!expEdit.equals(actualEdit)) {
                            continue;
                        }
                    }
                    // Match args
                    if (expected.get("arguments") != null) {
                        JsonArray actualArgs = expected.get("arguments").getAsJsonArray();
                        JsonArray expArgs = expected.getAsJsonArray("arguments");
                        if (!TestUtil.isArgumentsSubArray(actualArgs, expArgs)) {
                            continue;
                        }
                    }
                    // Code-action matched
                    codeActionFound = true;
                    break;
                }
            }
            String cursorStr = range.getStart().getLine() + ":" + range.getEnd().getCharacter();
            Assert.assertTrue(codeActionFound,
                              "Cannot find expected Code Action for: " + expTitle + ", cursor at " + cursorStr);
        }
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private JsonObject getResponseJson(String response) {
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }

    @DataProvider(name = "codeaction-data-provider")
    public abstract Object[][] dataProvider();

    public abstract String getResourceDir();
}
