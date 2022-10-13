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

package io.ballerina.projectdesign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.ballerina.projectdesign.diagnostics.ComponentModelException;
import io.ballerina.projectdesign.diagnostics.DiagnosticMessage;
import io.ballerina.projectdesign.diagnostics.DiagnosticUtils;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The extended service for generation solution architecture model.
 *
 * @since 2201.2.2
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("projectDesignService")
public class ProjectDesignService implements ExtendedLanguageServerService {

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
    public CompletableFuture<ProjectDesignServiceResponse> getPackageComponentModels
            (ProjectDesignServiceRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            ProjectDesignServiceResponse response = new ProjectDesignServiceResponse();
            Map<String, JsonObject> componentModelMap = new HashMap<>();
            for (String documentUri : request.getDocumentUris()) {
                Path path = Path.of(documentUri);
                try {
                    Project project = getCurrentProject(path);
                    if (!Utils.modelAlreadyExists(componentModelMap, project.currentPackage())) {
                        ComponentModelBuilder componentModelBuilder = new ComponentModelBuilder();
                        ComponentModel projectModel = componentModelBuilder
                                .constructComponentModel(project.currentPackage());
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject componentModelJson = (JsonObject) gson.toJsonTree(projectModel);
                        componentModelMap.put(Utils.getQualifiedPackageName(
                                projectModel.getPackageId()), componentModelJson);
                    }
                } catch (ComponentModelException | WorkspaceDocumentException | EventSyncException e) {
                    // todo : Improve error messages
                    DiagnosticMessage message = DiagnosticMessage.componentModellingService001(documentUri);
                    response.addDiagnostics
                            (DiagnosticUtils.getDiagnosticResponse(List.of(message), response));
                } catch (Exception e) {
                    DiagnosticMessage message = DiagnosticMessage.componentModellingService002(
                            e.getMessage(), Arrays.toString(e.getStackTrace()), documentUri);
                    response.addDiagnostics
                            (DiagnosticUtils.getDiagnosticResponse(List.of(message), response));
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
