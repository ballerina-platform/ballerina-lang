/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.datamapper.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Common utils that are reused within data-mapper test suits.
 */
public class DataMapperTestUtils {

    private static JsonParser parser = new JsonParser();
    private static Path sourcesPath = new File(DataMapperTestUtils.class.getClassLoader().getResource("codeaction")
            .getFile()).toPath();
    private static final LanguageServerContext serverContext = new LanguageServerContextImpl();
    private static final WorkspaceManager workspaceManager = BallerinaWorkspaceManager.getInstance(serverContext);


    /**
     * Convert Data-mapper response to Jason Object.
     *
     * @param response       Code action response
     * @return {@link JsonObject}   Response as Jason Object
     */
    private static JsonObject getResponseJson(String response) {
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }


    /**
     * Get code action response.
     *
     * @param source       Ballerina source file
     * @param configJsonObject      Test config
     * @param serviceEndpoint       Language server service Endpoint
     * @return {@link JsonObject}   Code action response
     */
    public static JsonObject getCodeActionResponse(String source, JsonObject configJsonObject, Endpoint serviceEndpoint)
            throws IOException, WorkspaceDocumentException {

        // Read expected results
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        // Filter diagnostics for the cursor position
        List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics
                = TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager, serverContext);
        List<Diagnostic> diags = new ArrayList<>(CodeActionUtil.toDiagnostics(diagnostics));
        Position pos = new Position(configJsonObject.get("line").getAsInt(),
                configJsonObject.get("character").getAsInt());
        diags = diags.stream().
                filter(diag -> CommonUtil.isWithinRange(pos, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);
        Range range = new Range(pos, pos);
        String response = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range,
                codeActionContext);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        return getResponseJson(response);
    }
}
