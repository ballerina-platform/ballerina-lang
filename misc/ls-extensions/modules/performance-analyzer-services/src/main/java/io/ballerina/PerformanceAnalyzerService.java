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

import static io.ballerina.Constants.MESSAGE;
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
            JsonObject response = new JsonObject();
            PerformanceAnalyzerResponse data = EndpointsFinder.getEndpoints(fileUri, this.workspaceManager,
                    request.getRange(), false);

            response.addProperty(TYPE, data.getType());
            response.addProperty(MESSAGE, data.getMessage());
            response.add(ACTION_INVOCATION_KEY, data.getActionInvocations());
            response.add(ENDPOINTS_KEY, data.getEndpoints());
            return response;
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

                boolean workerSupported = request.isWorkerSupported();
                PerformanceAnalyzerResponse response = EndpointsFinder.getEndpoints(fileUri,
                        this.workspaceManager, lineRange, workerSupported);

                response.setName(resource.getName());
                response.setResourcePos(lineRange);
                resourcesWithEndpoints.add(response);
            }
            return resourcesWithEndpoints;
        });
    }
}
