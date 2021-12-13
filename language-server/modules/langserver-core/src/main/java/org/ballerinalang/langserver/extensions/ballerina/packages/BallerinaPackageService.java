/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.packages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
/**
 * Implementation of Ballerina package extension for Language Server.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("ballerinaPackage")
public class BallerinaPackageService implements ExtendedLanguageServerService {

    private WorkspaceManager workspaceManager;
    private LSClientLogger clientLogger;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.workspaceManager = workspaceManager;
        this.clientLogger = LSClientLogger.getInstance(serverContext);
    }

    @JsonRequest
    public CompletableFuture<PackageMetadataResponse> metadata(PackageMetadataRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            PackageMetadataResponse metadata = new PackageMetadataResponse();
            try {
                Optional<Path> filePath = CommonUtil.getPathFromURI(request.getDocumentIdentifier().getUri());
                if (filePath.isEmpty()) {
                    return metadata;
                }
                Optional<Project> project = this.workspaceManager.project(filePath.get());
                if (project.isEmpty()) {
                    return metadata;
                }
                metadata.setPath(project.get().sourceRoot().toString());
                ProjectKind projectKind = project.get().kind();
                if (projectKind != ProjectKind.SINGLE_FILE_PROJECT) {
                    metadata.setPackageName(project.get().currentPackage().packageName().value());
                }
                metadata.setKind(projectKind.name());
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaPackage/metadata' failed!";
                this.clientLogger.logError(PackageContext.PACKAGE_METADATA, msg, e, request.getDocumentIdentifier(),
                        (Position) null);
            }
            return metadata;
        });
    }

    @JsonRequest
    public CompletableFuture<PackageComponentsResponse> components(PackageComponentsRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            PackageComponentsResponse response = new PackageComponentsResponse();
            JsonArray jsonPackages = new JsonArray();
            TextDocumentIdentifier[] documentIdentifiers = request.getDocumentIdentifiers();
            try {
                Arrays.stream(documentIdentifiers).iterator().forEachRemaining(documentIdentifier -> {
                    CommonUtil.getPathFromURI(documentIdentifier.getUri()).ifPresent(path -> {
                        Optional<Project> project = this.workspaceManager.project(path);
                        project.ifPresent(value -> jsonPackages.add(getPackageComponents(value)));
                    });
                });
                response.setProjectPackages(jsonPackages);
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaPackage/components' failed!";
                this.clientLogger.logError(PackageContext.PACKAGE_COMPONENTS, msg, e, null, (Position) null);
            }
            return response;
        });
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    /**
     * Generate a JSON Object with package component data including functions, services, enums, constants,
     * records, objects, classes, listeners, module level variables, and other types.
     *
     * @param project {@link Project}
     * @return {@link JsonObject} with package components
     */
    private JsonObject getPackageComponents(Project project) {
        Package currentPackage = project.currentPackage();
        PackageObject packageObject = new PackageObject(currentPackage.packageName().value(),
                project.sourceRoot().toUri().toString());
        currentPackage.moduleIds().forEach(moduleId -> {
            ModuleObject moduleObject = new ModuleObject();
            Module module = project.currentPackage().module(moduleId);
            if (module.moduleName().moduleNamePart() != null) {
                moduleObject.setName(module.moduleName().moduleNamePart());
            }
            module.documentIds().forEach(documentId -> {
                new DocumentComponentTransformer(moduleObject)
                        .getModuleObject(module.document(documentId).syntaxTree().rootNode());
            });
            packageObject.addModule(moduleObject);
        });
        return new Gson().toJsonTree(packageObject).getAsJsonObject();
    }
}
