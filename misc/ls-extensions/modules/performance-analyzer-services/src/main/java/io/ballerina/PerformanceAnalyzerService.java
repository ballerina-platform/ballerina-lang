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

package io.ballerina;

import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.ballerina.Constants.ENDPOINT_RESOLVE_ERROR;
import static io.ballerina.Constants.ERROR;
import static io.ballerina.Constants.MESSAGE;
import static io.ballerina.Constants.NO_DATA;
import static io.ballerina.Constants.SUCCESS;
import static io.ballerina.Constants.TYPE;
import static io.ballerina.PerformanceAnalyzerNodeVisitor.ACTION_INVOCATION_KEY;
import static io.ballerina.PerformanceAnalyzerNodeVisitor.ENDPOINTS_KEY;

/**
 * The extended service for the performance analyzer.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("performanceAnalyzer")
public class PerformanceAnalyzerService implements ExtendedLanguageServerService {

    private WorkspaceManager workspaceManager;

    @Override
    public Class<?> getRemoteInterface() {

        return getClass();
    }

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {

        this.workspaceManager = workspaceManager;
    }

    @Deprecated
    @JsonNotification
    public CompletableFuture<JsonObject> getEndpoints(PerformanceAnalyzerGraphRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            String fileUri = request.getDocumentIdentifier().getUri();
            JsonObject data = EndpointsFinder.getEndpoints(fileUri, this.workspaceManager, request.getRange());

            if (data.entrySet().isEmpty()) {
                JsonObject obj = new JsonObject();
                obj.addProperty(TYPE, ERROR);
                obj.addProperty(MESSAGE, ENDPOINT_RESOLVE_ERROR);
                return obj;
            }

            if (data.get(ACTION_INVOCATION_KEY).getAsJsonObject().get("nextNode").isJsonNull()) {
                JsonObject obj = new JsonObject();
                obj.addProperty(TYPE, ERROR);
                obj.addProperty(MESSAGE, NO_DATA);
                return obj;
            }

            if (data.get(TYPE) == null) {
                data.addProperty(TYPE, SUCCESS);
                data.addProperty(MESSAGE, SUCCESS);
            }
            return data;
        });
    }

    @JsonNotification
    public CompletableFuture<List<PerformanceAnalyzerResponse>> getResourcesWithEndpoints(
            PerformanceAnalyzerRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            List<PerformanceAnalyzerResponse> resourcesWithEndpoints = new ArrayList<>();

            String fileUri = request.getDocumentIdentifier().getUri();
            Path path = Path.of(fileUri);

            Optional<SemanticModel> semanticModel = this.workspaceManager.semanticModel(path);
            Optional<Module> module = this.workspaceManager.module(path);

            if (semanticModel.isEmpty() || module.isEmpty()) {
                return resourcesWithEndpoints;
            }

            ResourceFinder nodeVisitor = new ResourceFinder();

            Optional<Document> document = this.workspaceManager.document(path);
            if (document.isEmpty()) {
                return resourcesWithEndpoints;
            }

            SyntaxTree syntaxTree = document.get().syntaxTree();
            syntaxTree.rootNode().accept(nodeVisitor);
            List<Resource> resourceRanges = nodeVisitor.getResources();

            for (Resource resource : resourceRanges) {

                LineRange range = resource.getLineRange();
                Range lineRange = new Range(new Position(range.startLine().line(), range.startLine().offset()),
                        new Position(range.endLine().line(), range.endLine().offset()));

                PerformanceAnalyzerResponse response = new PerformanceAnalyzerResponse();
                response.setName(resource.getName());
                response.setResourcePos(lineRange);

                JsonObject data = EndpointsFinder.getEndpoints(fileUri, this.workspaceManager, lineRange);

                if (data.entrySet().isEmpty()) {
                    response.setType(ERROR);
                    response.setMessage(ENDPOINT_RESOLVE_ERROR);
                    resourcesWithEndpoints.add(response);
                    return resourcesWithEndpoints;
                }

                if (data.get(ACTION_INVOCATION_KEY).getAsJsonObject().get("nextNode").isJsonNull()) {
                    response.setType(ERROR);
                    response.setMessage(NO_DATA);
                    resourcesWithEndpoints.add(response);
                    return resourcesWithEndpoints;
                }

                response.setType(SUCCESS);
                response.setMessage(SUCCESS);

                response.setEndpoints(data.get(ENDPOINTS_KEY).getAsJsonObject());
                response.setActionInvocations(data.get(ACTION_INVOCATION_KEY).getAsJsonObject());
                resourcesWithEndpoints.add(response);
            }
            return resourcesWithEndpoints;
        });
    }
}
