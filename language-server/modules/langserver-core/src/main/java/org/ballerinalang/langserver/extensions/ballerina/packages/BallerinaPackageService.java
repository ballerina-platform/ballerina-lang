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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.symbols.AbsResourcePathAttachPoint;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.LiteralAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceAttachPointKind;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
     * Generate a JSON Object with package component data including functions, services and resources.
     *
     * @param project {@link Project}
     * @return {@link JsonObject} with package components
     */
    private JsonObject getPackageComponents(Project project) {
        JsonObject jsonPackage = new JsonObject();
        JsonArray jsonModules = new JsonArray();
        jsonPackage.addProperty(PackageServiceConstants.FILE_PATH, project.sourceRoot().toUri().toString());
        Package currentPackage = project.currentPackage();
        jsonPackage.addProperty(PackageServiceConstants.NAME, currentPackage.packageName().value());

        currentPackage.moduleIds().forEach(moduleId -> {
            JsonObject jsonModule = new JsonObject();
            Module module = project.currentPackage().module(moduleId);
            if (module.moduleName().moduleNamePart() != null) {
                jsonModule.addProperty(PackageServiceConstants.NAME, module.moduleName().moduleNamePart());
            }
            List<Symbol> symbolList = project.currentPackage()
                    .getCompilation().getSemanticModel(moduleId).moduleSymbols();

            List<FunctionSymbol> functionList =
                    symbolList.stream().filter(symbol -> symbol.kind() == SymbolKind.FUNCTION)
                            .map(symbol -> (FunctionSymbol) symbol)
                            .collect(Collectors.toList());
            JsonArray jsonFunctions = new JsonArray();
            functionList.forEach(function -> {
                JsonObject jsonFunction = new JsonObject();
                if (function.getName().isPresent()) {
                    jsonFunction.addProperty(PackageServiceConstants.NAME, function.getName().get());
                }
                setPositionData(function, jsonFunction);
                jsonFunctions.add(jsonFunction);
            });
            jsonModule.add(PackageServiceConstants.FUNCTIONS, jsonFunctions);

            List<ServiceDeclarationSymbol> serviceList =
                    symbolList.stream().filter(symbol -> symbol.kind() == SymbolKind.SERVICE_DECLARATION)
                            .map(symbol -> (ServiceDeclarationSymbol) symbol)
                            .collect(Collectors.toList());
            JsonArray jsonServices = new JsonArray();
            serviceList.forEach(service -> {
                JsonObject jsonService = new JsonObject();
                if (service.attachPoint().isPresent()) {
                    ServiceAttachPointKind kind = service.attachPoint().get().kind();
                    if (kind == ServiceAttachPointKind.ABSOLUTE_RESOURCE_PATH) {
                        List<String> segments =
                                ((AbsResourcePathAttachPoint) service.attachPoint().get()).segments();
                        jsonService.addProperty(PackageServiceConstants.NAME, String.join("_", segments));
                    } else if (kind == ServiceAttachPointKind.STRING_LITERAL) {
                        jsonService.addProperty(PackageServiceConstants.NAME,
                                ((LiteralAttachPoint) service.attachPoint().get()).literal());
                    }
                }

                JsonArray jsonResources = new JsonArray();
                service.methods().forEach((key, methodSymbol) -> {
                    JsonObject jsonResource = new JsonObject();
                    jsonResource.addProperty(PackageServiceConstants.NAME, key);
                    setPositionData(methodSymbol, jsonResource);
                    jsonResources.add(jsonResource);
                });
                service.getLocation().ifPresent(location -> jsonService.addProperty(PackageServiceConstants.FILE_PATH,
                        location.lineRange().filePath()));
                jsonService.add(PackageServiceConstants.RESOURCES, jsonResources);
                jsonServices.add(jsonService);
            });
            jsonModule.add(PackageServiceConstants.SERVICES, jsonServices);
            jsonModules.add(jsonModule);
        });
        jsonPackage.add(PackageServiceConstants.MODULES, jsonModules);
        return jsonPackage;
    }

    /**
     * Set positional data for a node.
     *
     * @param symbol     {@link Symbol}
     * @param jsonObject JSON Node to set positional data
     */
    private void setPositionData(Symbol symbol, JsonObject jsonObject) {
        Optional<Location> location = symbol.getLocation();
        if (location.isPresent()) {
            LineRange lineRange = location.get().lineRange();
            jsonObject.addProperty(PackageServiceConstants.FILE_PATH, lineRange.filePath());
            jsonObject.addProperty(PackageServiceConstants.START_LINE, lineRange.startLine().line());
            jsonObject.addProperty(PackageServiceConstants.START_COLUMN, lineRange.startLine().offset());
            jsonObject.addProperty(PackageServiceConstants.END_LINE, lineRange.endLine().line());
            jsonObject.addProperty(PackageServiceConstants.END_COLUMN, lineRange.endLine().offset());
        }
    }
}
