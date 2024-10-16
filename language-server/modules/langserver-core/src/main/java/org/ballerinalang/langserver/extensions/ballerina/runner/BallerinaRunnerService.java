/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.runner;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Ballerina runner extension for Language Server.
 *
 * @since 2201.11.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.BallerinaRunnerService")
@JsonSegment("ballerinaRunner")
public class BallerinaRunnerService implements ExtendedLanguageServerService {

    private WorkspaceManager workspaceManager;
    private LanguageServerContext serverContext;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.workspaceManager = workspaceManager;
        this.serverContext = serverContext;
    }

    /**
     * Get all the diagnostics of the project.
     *
     * @param request {@link ProjectDiagnosticsRequest}
     * @return {@link ProjectDiagnosticsResponse}
     */
    @JsonRequest
    public CompletableFuture<ProjectDiagnosticsResponse> diagnostics(ProjectDiagnosticsRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ProjectDiagnosticsResponse projectDiagnosticsResponse = new ProjectDiagnosticsResponse();
            Optional<Path> filePath = PathUtil.getPathFromURI(request.getProjectRootIdentifier().getUri());
            if (filePath.isEmpty()) {
                return projectDiagnosticsResponse;
            }
            Optional<Project> project = this.workspaceManager.project(filePath.get());
            if (project.isEmpty()) {
                return projectDiagnosticsResponse;
            }
            DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(filePath.get().toString(),
                    this.workspaceManager,
                    LSContextOperation.DOC_DIAGNOSTICS,
                    this.serverContext);
            Map<String, List<Diagnostic>> latestDiagnostics = DiagnosticsHelper.getInstance(this.serverContext)
                    .getLatestDiagnostics(context);
            projectDiagnosticsResponse.setDiagnostics(latestDiagnostics);
            return projectDiagnosticsResponse;
        });
    }

    /**
     * Get the main function parameters.
     *
     * @param request {@link MainFunctionParamsRequest}
     * @return {@link MainFunctionParamsResponse}
     */
    @JsonRequest
    public CompletableFuture<MainFunctionParamsResponse> mainFunctionParams(MainFunctionParamsRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Path> filePath = PathUtil.getPathFromURI(request.getProjectRootIdentifier().getUri());
            if (filePath.isEmpty()) {
                return new MainFunctionParamsResponse(false, null);
            }
            Optional<Project> project = this.workspaceManager.project(filePath.get());
            if (project.isEmpty()) {
                return new MainFunctionParamsResponse(false, null);
            }
            Package currentPackage = project.get().currentPackage();
            for (DocumentId documentId : currentPackage.getDefaultModule().documentIds()) {
                Document document = currentPackage.getDefaultModule().document(documentId);
                Node node = document.syntaxTree().rootNode();
                if (node instanceof ModulePartNode modulePartNode) {
                    for (ModuleMemberDeclarationNode member : modulePartNode.members()) {
                        if (member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) member;
                            if (functionDefinitionNode.functionName().text()
                                    .equals(BallerinaRunnerServiceConstants.MAIN_FUNCTION)) {
                                List<String> params = new ArrayList<>();
                                functionDefinitionNode.functionSignature().parameters().forEach(param -> {
                                    params.add(param.toString());
                                });
                                return new MainFunctionParamsResponse(true, params);
                            }
                        }
                    }
                }
            }
            return new MainFunctionParamsResponse(false, null);
        });
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    public record MainFunctionParamsResponse(boolean hasMain, List<String> params) {
    }
}
