/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.componentmodel.diagnostics.ComponentModelException;
import io.ballerina.componentmodel.diagnostics.DiagnosticMessage;
import io.ballerina.componentmodel.diagnostics.DiagnosticUtils;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The extended service for generation solution architecture model.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("solutionArchitectureModelingService")
public class ComponentModelingService implements ExtendedLanguageServerService {
    private WorkspaceManager workspaceManager;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonNotification
    public CompletableFuture<ComponentModelResponse> getComponentModel(ComponentModelRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ComponentModelResponse componentModelResponse = new ComponentModelResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Path path = Path.of(fileUri);
            try {
                Project project = getCurrentProject(path);
                ComponentModelBuilder componentModelBuilder = new ComponentModelBuilder();
                ComponentModel multiServiceModel = componentModelBuilder.constructComponentModel(project);
                componentModelResponse.setComponentModel((JsonObject) new Gson().toJsonTree(multiServiceModel));
            } catch (ComponentModelException | WorkspaceDocumentException e) {
                DiagnosticMessage message = DiagnosticMessage.componentModellingService001(fileUri);
                return DiagnosticUtils.getDiagnosticResponse(List.of(message), componentModelResponse);
            }
            return componentModelResponse;
        });
    }

    private Project getCurrentProject(Path path) throws ComponentModelException, WorkspaceDocumentException {
        Optional<Project> project = workspaceManager.project(path);
        if (project.isEmpty()) {
            DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
            workspaceManager.didOpen(path, params);
            project = workspaceManager.project(path);
            if (project.isPresent()) {
                return project.get();
            } else {
                throw new ComponentModelException(
                        String.format("Ballerina project not found in the path : %s", path.toString()));
            }
        }
        return project.get();
    }
}
