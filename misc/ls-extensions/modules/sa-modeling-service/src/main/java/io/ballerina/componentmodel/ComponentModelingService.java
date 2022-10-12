/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

import com.google.gson.JsonObject;
import io.ballerina.componentmodel.diagnostics.ComponentModelException;
import io.ballerina.componentmodel.diagnostics.DiagnosticMessage;
import io.ballerina.componentmodel.diagnostics.DiagnosticUtils;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public CompletableFuture<ComponentModelingServiceResponse> getPackageComponentModels
            (ComponentModelingServiceRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            ComponentModelingServiceResponse response = new ComponentModelingServiceResponse();
            Map<String, JsonObject> componentModelMap = new HashMap<>();
            for (TextDocumentIdentifier documentIdentifier : request.getTextDocumentIdentifiers()) {
                ComponentModelingServiceResponse componentModelingServiceResponse =
                        new ComponentModelingServiceResponse();
                String fileUri = documentIdentifier.getUri();
                Path path = Path.of(fileUri);
                try {
                    Project project = getCurrentProject(path);
                    ComponentModelBuilder componentModelBuilder = new ComponentModelBuilder();
                    componentModelBuilder.constructComponentModel(project, componentModelMap);
                } catch (ComponentModelException | WorkspaceDocumentException | EventSyncException e) {
                    DiagnosticMessage message = DiagnosticMessage.componentModellingService001(fileUri);
                    componentModelingServiceResponse.setDiagnostics
                            (DiagnosticUtils.getDiagnosticResponse(List.of(message), componentModelingServiceResponse));
                    response.addDiagnostics
                            (DiagnosticUtils.getDiagnosticResponse(List.of(message), componentModelingServiceResponse));
                }
            }
            response.setComponentModels(componentModelMap);
            return response;
        });
    }

    private Project getCurrentProject(Path path) throws ComponentModelException, WorkspaceDocumentException,
            EventSyncException {
        Optional<Project> project = workspaceManager.project(path);
        if (project.isEmpty()) {
            return workspaceManager.loadProject(path);
        }
        return project.get();
    }
}
