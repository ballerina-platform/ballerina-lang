/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.Settings;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.model.ConnectorInfo;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.diagramutil.connector.generator.ConnectorGenerator;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.exception.LSConnectorException;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * Implementation of the BallerinaConnectorService.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("ballerinaConnector")
public class BallerinaConnectorService implements ExtendedLanguageServerService {

    public static final String DEFAULT_CONNECTOR_FILE_KEY = "DEFAULT_CONNECTOR_FILE";
    private static final Path STD_LIB_SOURCE_ROOT = Paths.get(CommonUtil.BALLERINA_HOME)
            .resolve("repo")
            .resolve("bala");
    private String connectorConfig;
    private ConnectorExtContext connectorExtContext;
    private LSClientLogger clientLogger;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.connectorExtContext = new ConnectorExtContext();
        connectorConfig = System.getenv(DEFAULT_CONNECTOR_FILE_KEY);
        this.clientLogger = LSClientLogger.getInstance(serverContext);
        if (connectorConfig == null) {
            connectorConfig = System.getProperty(DEFAULT_CONNECTOR_FILE_KEY);
        }
    }

    @JsonRequest
    public CompletableFuture<BallerinaConnectorListResponse> connectors(BallerinaConnectorListRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaConnectorListResponse connectorList = new BallerinaConnectorListResponse();
            try {
                Settings settings = RepoUtils.readSettings();
                CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                        initializeProxy(settings.getProxy()), getAccessTokenOfCLI(settings));
                JsonElement connectorSearchResult = client.getConnectors(request.getQueryMap(),
                        "any", RepoUtils.getBallerinaVersion());
                CentralConnectorListResult centralConnectorListResult = new Gson().fromJson(
                        connectorSearchResult.getAsString(), CentralConnectorListResult.class);
                connectorList.setCentralConnectors(centralConnectorListResult.getConnectors());

                // Fetch local project connectors.
                if (request.getTargetFile() != null) {
                    Path filePath = Paths.get(request.getTargetFile());
                    List<Connector> localConnectors = fetchLocalConnectors(filePath, false);
                    connectorList.setLocalConnectors(localConnectors);
                }
            } catch (Exception e) {
                String msg = "Operation 'ballerinaConnector/connectors' failed!";
                this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
            }
            return connectorList;
        });
    }

    /**
     * Fetch ballerina connector form local file.
     *
     * @param filePath file path
     * @param detailed detailed connector out put or not
     * @return connector list
     * @throws IOException
     */
    private List<Connector> fetchLocalConnectors(Path filePath, boolean detailed) {
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        Project balaProject = ProjectLoader.loadProject(filePath, defaultBuilder);
        List<Connector> connectors = new ArrayList<>();
        try {
            connectors = ConnectorGenerator.getProjectConnectors(balaProject, detailed);
        } catch (IOException e) {
            String msg = "Local connector fetching operation failed!";
            this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
        }

        List<Connector> localConnectors = new ArrayList<>();
        for (Connector conn : connectors) {
            localConnectors.add(conn);
        }

        return localConnectors;
    }

    private Path getBalaPath(String org, String module, String version) throws LSConnectorException, IOException {
        Path platformBalaPath;

        Path rootBalaPath = STD_LIB_SOURCE_ROOT.resolve(org).resolve(module)
                .resolve(version.isEmpty() ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : version);
        if (!Files.exists(rootBalaPath)) {
            Path homeRepoPath = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALA_CACHE_DIR_NAME);
            rootBalaPath = homeRepoPath.resolve(org).resolve(module)
                    .resolve(version.isEmpty() ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : version);
        }
        Path anyPlatformBala = rootBalaPath.resolve("any");
        Path jvmBala = rootBalaPath.resolve(JvmTarget.JAVA_11.code());
        if (Files.exists(anyPlatformBala)) {
            platformBalaPath = anyPlatformBala;
        } else if (Files.exists(jvmBala)) {
            platformBalaPath = jvmBala;
        } else {
            throw new LSConnectorException("No bala project found for package at '"
                    + rootBalaPath.toString() + "'");
        }
        return platformBalaPath;
    }

    private Path resolveBalaPath(String org, String pkgName, String version) throws LSConnectorException {
        Environment environment = EnvironmentBuilder.buildDefault();

        PackageDescriptor packageDescriptor = PackageDescriptor.from(
                PackageOrg.from(org), PackageName.from(pkgName), PackageVersion.from(version));
        ResolutionRequest resolutionRequest = ResolutionRequest.from(packageDescriptor);

        PackageResolver packageResolver = environment.getService(PackageResolver.class);
        Collection<ResolutionResponse> resolutionResponses = packageResolver.resolvePackages(
                Collections.singletonList(resolutionRequest), ResolutionOptions.builder().setOffline(false).build());
        ResolutionResponse resolutionResponse = resolutionResponses.stream().findFirst().orElse(null);

        if (resolutionResponse != null && resolutionResponse.resolutionStatus().equals(
                ResolutionResponse.ResolutionStatus.RESOLVED)) {
            Package resolvedPackage = resolutionResponse.resolvedPackage();
            if (resolvedPackage != null) {
                return resolvedPackage.project().sourceRoot();
            }
        }
        throw new LSConnectorException("No bala project found for package '"
                + packageDescriptor.toString() + "'");
    }

    @JsonRequest
    public CompletableFuture<JsonObject> connector(BallerinaConnectorRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<JsonObject> connector = getConnectorFromCentral(request);
            if (connector.isPresent()) {
                return connector.get();
            }
            connector = generateFromLocalFiles(request);
            if (connector.isPresent()) {
                return connector.get();
            }
            return new JsonObject();
        });
    }

    private Optional<JsonObject> getConnectorFromCentral(BallerinaConnectorRequest request) {
        JsonObject connector;
        try {
            Settings settings = RepoUtils.readSettings();
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                    initializeProxy(settings.getProxy()),
                    getAccessTokenOfCLI(settings));
            if (request.getConnectorId() != null) {
                // Fetch connector by connector Id.
                connector = client.getConnector(request.getConnectorId(), "any", RepoUtils.getBallerinaVersion());
                return Optional.of(connector);
            }
            if (request.isFullConnector()) {
                // Fetch connector by connector FQN.
                ConnectorInfo connectorInfo = new ConnectorInfo(request.getOrgName(), request.getPackageName(),
                        request.getModuleName(), request.getVersion(), request.getName());
                connector = client.getConnector(connectorInfo, "any", RepoUtils.getBallerinaVersion());
                return Optional.of(connector);
            }

        } catch (Exception e) {
            String msg = "Operation 'ballerinaConnector/connector' failed!";
            this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
        }
        return Optional.empty();
    }

    private Optional<JsonObject> generateFromLocalFiles(BallerinaConnectorRequest request) {
        JsonObject connector;
        Path filePath;
        try {
            if (request.getTargetFile() != null) {
                // Generate local connector metadata.
                filePath = Paths.get(request.getTargetFile());
            } else {
                // Generate connector metadata by connector FQN.
                filePath = resolveBalaPath(request.getOrgName(), request.getModuleName(), request.getVersion());
            }
            List<Connector> localConnectors = fetchLocalConnectors(filePath, true);
            for (Connector conn : localConnectors) {
                if (conn.name.equals(request.getName())) {
                    Gson gson = new Gson();
                    connector = gson.fromJson(gson.toJson(conn), JsonObject.class);
                    return Optional.of(connector);
                }
            }
        } catch (Exception e) {
            String connectorId = getCacheableKey(request.getOrgName(), request.getModuleName(),
                    request.getVersion());
            String msg = "Operation 'ballerinaConnector/connector' for " + connectorId + ":" +
                    request.getName() + " failed!";
            this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
        }
        return Optional.empty();
    }

    private void populateConnectorFunctionParamRecords(Node parameterNode, SemanticModel semanticModel,
                                                       Map<String, TypeDefinitionNode> jsonRecords,
                                                       Map<String, ClassDefinitionNode> classDefinitions,
                                                       Map<String, JsonElement> connectorRecords) {
        Optional<TypeSymbol> paramType = semanticModel.type(parameterNode.lineRange());
        if (paramType.isPresent()) {
            if (paramType.get().typeKind() == TypeDescKind.UNION) {
                String parameterTypeName = "";
                if (parameterNode instanceof RequiredParameterNode) {
                    Optional<Symbol> paramSymbol = semanticModel.symbol(parameterNode);
                    if (paramSymbol.isPresent()) {
                        parameterTypeName = String.format("%s:%s", paramSymbol.get().getModule().get().id(),
                                ((RequiredParameterNode) parameterNode).typeName());
                    }
                } else if (parameterNode instanceof DefaultableParameterNode) {
                    Optional<Symbol> paramSymbol = semanticModel.symbol(parameterNode);
                    if (paramSymbol.isPresent()) {
                        parameterTypeName = String.format("%s:%s", paramSymbol.get().getModule().get().id(),
                                ((DefaultableParameterNode) parameterNode).typeName());
                    }
                } else if (parameterNode instanceof RestParameterNode) {
                    Optional<Symbol> paramSymbol = semanticModel.symbol(parameterNode);
                    if (paramSymbol.isPresent()) {
                        parameterTypeName = String.format("%s:%s", paramSymbol.get().getModule().get().id(),
                                ((RestParameterNode) parameterNode).typeName());
                    }

                }

                if (jsonRecords.get(parameterTypeName) != null) {
                    connectorRecords.put(parameterTypeName,
                            DiagramUtil
                                    .getTypeDefinitionSyntaxJson(jsonRecords.get(parameterTypeName), semanticModel));
                }
                Arrays.stream(paramType.get().signature().split("\\|")).forEach(type -> {
                    String refinedType = type.replace("?", "");
                    TypeDefinitionNode record = jsonRecords.get(refinedType);
                    if (record != null) {
                        connectorRecords.put(refinedType, DiagramUtil
                                .getTypeDefinitionSyntaxJson(record, semanticModel));

                        if (record.typeDescriptor() instanceof RecordTypeDescriptorNode) {
                            populateConnectorTypeDef((RecordTypeDescriptorNode) record.typeDescriptor(),
                                    semanticModel, jsonRecords, connectorRecords, record.typeName().text());
                        }
                    }
                });
            } else if (paramType.get().typeKind() == TypeDescKind.ARRAY) {
                TypeDefinitionNode record = jsonRecords.get(paramType.get().signature());
                if (record != null) {
                    connectorRecords.put(paramType.get().signature(),
                            DiagramUtil.getTypeDefinitionSyntaxJson(record, semanticModel));

                    if (record.typeDescriptor() instanceof RecordTypeDescriptorNode) {
                        populateConnectorTypeDef((RecordTypeDescriptorNode) record.typeDescriptor(),
                                semanticModel, jsonRecords, connectorRecords, record.typeName().text());
                    }
                }
            } else if (paramType.get().typeKind() == TypeDescKind.TYPE_REFERENCE) {
                if (jsonRecords.containsKey(paramType.get().signature())) {
                    TypeDefinitionNode record = jsonRecords.get(paramType.get().signature());

                    connectorRecords.put(paramType.get().signature(),
                            DiagramUtil.getTypeDefinitionSyntaxJson(record, semanticModel));
                    if (record.typeDescriptor() instanceof RecordTypeDescriptorNode) {
                        populateConnectorTypeDef((RecordTypeDescriptorNode) record.typeDescriptor(), semanticModel,
                                jsonRecords, connectorRecords, record.typeName().text());
                    }
                }

                if (classDefinitions.containsKey(paramType.get().signature())) {
                    ClassDefinitionNode classDefinition = classDefinitions.get(paramType.get().signature());
                    connectorRecords.put(paramType.get().signature(),
                            DiagramUtil.getClassDefinitionSyntaxJson(classDefinition, semanticModel));
                }
            }
        }
    }

    private void populateConnectorTypeDef(RecordTypeDescriptorNode recordTypeDescriptorNode,
                                          SemanticModel semanticModel,
                                          Map<String, TypeDefinitionNode> jsonRecords,
                                          Map<String, JsonElement> connectorRecords, String fieldTypeName) {

        recordTypeDescriptorNode.fields().forEach(field -> {

            Optional<Symbol> fieldType;
            if (field instanceof TypeReferenceNode) {
                fieldType = semanticModel.symbol(((TypeReferenceNode) field).typeName());
            } else {
                fieldType = semanticModel.symbol(field);
            }

            if (fieldType.isPresent() && fieldType.get() instanceof TypeReferenceTypeSymbol) {
                TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) fieldType.get();
                String typeName = typeReferenceTypeSymbol.signature();
                TypeDefinitionNode record = jsonRecords.get(typeName);
                if (record != null && !fieldType.equals(typeName)) {
                    connectorRecords.put(typeName, DiagramUtil.getTypeDefinitionSyntaxJson(record, semanticModel));
                    if (record.typeDescriptor() instanceof RecordTypeDescriptorNode) {
                        populateConnectorTypeDef((RecordTypeDescriptorNode) record.typeDescriptor(),
                                semanticModel, jsonRecords, connectorRecords, typeName);
                    }
                }
            }
        });
    }

    @JsonRequest
    public CompletableFuture<BallerinaRecordResponse> record(BallerinaRecordRequest request) {
        String cacheableKey = getCacheableKey(request.getOrg(), request.getModule(), request.getVersion());
        LSRecordCache recordCache = LSRecordCache.getInstance(connectorExtContext);

        JsonElement ast = recordCache.getRecordAST(request.getOrg(), request.getModule(),
                request.getVersion(), request.getName());
        String error = "";
        if (ast == null) {
            try {
                Path balaPath = resolveBalaPath(request.getOrg(), request.getModule(), request.getVersion());
                ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
                defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
                Project balaProject = ProjectLoader.loadProject(balaPath, defaultBuilder);
                ModuleId moduleId = balaProject.currentPackage().moduleIds().stream().findFirst().get();
                Module module = balaProject.currentPackage().module(moduleId);
                PackageCompilation packageCompilation = balaProject.currentPackage().getCompilation();
                SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);

                Map<String, JsonElement> recordDefJsonMap = new HashMap<>();
                ConnectorNodeVisitor connectorNodeVisitor = new ConnectorNodeVisitor(request.getName(), semanticModel);
                module.documentIds().forEach(documentId -> {
                    module.document(documentId).syntaxTree().rootNode().accept(connectorNodeVisitor);
                });


                TypeDefinitionNode recordNode = null;
                JsonElement recordJson = null;

                for (Map.Entry<String, TypeDefinitionNode> recordEntry
                        : connectorNodeVisitor.getRecords().entrySet()) {
                    String key = recordEntry.getKey();
                    TypeDefinitionNode record = recordEntry.getValue();

                    JsonElement jsonST = DiagramUtil.getTypeDefinitionSyntaxJson(record, semanticModel);

                    if (record.typeName().text().equals(request.getName())) {
                        recordNode = record;
                        recordJson = jsonST;
                    } else {
                        recordDefJsonMap.put(key, jsonST);
                    }
                }

                Gson gson = new Gson();
                if (recordNode != null) {
                    if (recordJson instanceof JsonObject) {
                        JsonElement recordsJson = gson.toJsonTree(recordDefJsonMap);
                        ((JsonObject) recordJson).add("records", recordsJson);
                    }
                    recordCache.addRecordAST(request.getOrg(), request.getModule(),
                            request.getVersion(), request.getName(), recordJson);
                }

                ast = recordCache.getRecordAST(request.getOrg(), request.getModule(),
                        request.getVersion(), request.getName());
            } catch (Exception e) {
                String msg = "Operation 'ballerinaConnector/record' for " + cacheableKey + ":" +
                        request.getName() + " failed!";
                error = e.getMessage();
                this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
            }

        }
        BallerinaRecordResponse response = new BallerinaRecordResponse(request.getOrg(), request.getModule(),
                request.getVersion(), request.getName(), ast, error, request.getBeta());
        return CompletableFuture.supplyAsync(() -> response);
    }


    private String getCacheableKey(String orgName, String moduleName, String version) {
        return orgName + "_" + moduleName + "_" +
                (version.isEmpty() ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : version);
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }
}
