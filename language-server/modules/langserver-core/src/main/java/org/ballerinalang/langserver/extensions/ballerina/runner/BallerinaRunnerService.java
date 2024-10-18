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
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
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
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("ballerinaRunner")
public class BallerinaRunnerService implements ExtendedLanguageServerService {

    private WorkspaceManager workspaceManager;
    private LSClientLogger clientLogger;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.workspaceManager = workspaceManager;
        this.clientLogger = LSClientLogger.getInstance(serverContext);
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
            try {
                ProjectDiagnosticsResponse projectDiagnosticsResponse = new ProjectDiagnosticsResponse();
                Optional<Path> filePath = PathUtil.getPathFromURI(request.getProjectRootIdentifier().getUri());
                if (filePath.isEmpty()) {
                    return projectDiagnosticsResponse;
                }
                Project project = this.workspaceManager.loadProject(filePath.get());
                Map<String, List<Diagnostic>> errorDiagnosticMap =
                        BallerinaRunnerUtil.getErrorDiagnosticMap(this.workspaceManager, project, filePath.get());
                projectDiagnosticsResponse.setErrorDiagnosticMap(errorDiagnosticMap);
                return projectDiagnosticsResponse;
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaRunner/diagnostics' failed!";
                this.clientLogger.logError(RunnerContext.RUNNER_DIAGNOSTICS, msg, e, request.getProjectRootIdentifier(),
                        (Position) null);
            }
            return new ProjectDiagnosticsResponse();
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
            try {
                Optional<Path> filePath = PathUtil.getPathFromURI(request.getProjectRootIdentifier().getUri());
                if (filePath.isEmpty()) {
                    return new MainFunctionParamsResponse(false, null, null);
                }
                Project project = this.workspaceManager.loadProject(filePath.get());
                Package currentPackage = project.currentPackage();
                for (DocumentId documentId : currentPackage.getDefaultModule().documentIds()) {
                    Document document = currentPackage.getDefaultModule().document(documentId);
                    Node node = document.syntaxTree().rootNode();
                    if (node instanceof ModulePartNode modulePartNode) {
                        for (ModuleMemberDeclarationNode member : modulePartNode.members()) {
                            if (member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) member;
                                if (functionDefinitionNode.functionName().text()
                                        .equals(BallerinaRunnerServiceConstants.MAIN_FUNCTION)) {
                                    List<TypeBindingPair> params = new ArrayList<>();
                                    for (ParameterNode param:functionDefinitionNode.functionSignature().parameters()) {
                                        if (param.kind() == SyntaxKind.REST_PARAM) {
                                            return new MainFunctionParamsResponse(true, params,
                                                    BallerinaRunnerUtil.extractParamDetails(param));
                                        } else {
                                            params.add(BallerinaRunnerUtil.extractParamDetails(param));
                                        }
                                    }
                                    return new MainFunctionParamsResponse(true, params, null);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaRunner/mainFunctionParams' failed!";
                this.clientLogger.logError(RunnerContext.RUNNER_MAIN_FUNCTION_PARAMS, msg, e,
                        request.getProjectRootIdentifier(), (Position) null);
            }
            return new MainFunctionParamsResponse(false, null, null);
        });
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    public record MainFunctionParamsResponse(boolean hasMain, List<TypeBindingPair> params,
                                             TypeBindingPair restParams) {
    }

    public record TypeBindingPair(String type, String paramName, String defaultValue) {
    }

    private enum RunnerContext implements LSOperation {
        RUNNER_DIAGNOSTICS("ballerinaRunner/diagnostics"),
        RUNNER_MAIN_FUNCTION_PARAMS("ballerinaRunner/mainFunctionParams");

        private final String name;

        RunnerContext(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

}
