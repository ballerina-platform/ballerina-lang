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
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.diagramutil.connector.generator.ConnectorGenerator;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.exception.LSConnectorException;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the BallerinaConnectorService.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorServiceImpl implements BallerinaConnectorService {

    public static final String BALLERINA_CENTRAL_URL = "BALLERINA_CENTRAL_URL";
    private static final Path STD_LIB_SOURCE_ROOT = Paths.get(CommonUtil.BALLERINA_HOME)
            .resolve("repo")
            .resolve("bala");
    private String ballerinaCentralUrl;
    private final ConnectorExtContext connectorExtContext;
    private final LSClientLogger clientLogger;

    public BallerinaConnectorServiceImpl(LanguageServerContext serverContext) {
        this.connectorExtContext = new ConnectorExtContext();
        ballerinaCentralUrl = System.getenv(BALLERINA_CENTRAL_URL);
        if (ballerinaCentralUrl == null) {
            ballerinaCentralUrl = "https://api.central.ballerina.io/2.0/registry";
        }
        this.clientLogger = LSClientLogger.getInstance(serverContext);
    }

    @Override
    public CompletableFuture<BallerinaConnectorsResponse> connectors() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(
                    URI.create(this.ballerinaCentralUrl + "/connectors"))
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                BallerinaConnectorInfo[] connectorInfos = new Gson().fromJson(response.body(),
                        BallerinaConnectorInfo[].class);
                BallerinaConnectorsResponse connectorsResponse = new BallerinaConnectorsResponse(
                        Arrays.asList(connectorInfos));
                return CompletableFuture.supplyAsync(() -> connectorsResponse);
            }

        } catch (Exception e) {
            String msg = "Operation 'ballerinaConnector/connectors' failed!";
            this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
        }

        return CompletableFuture.supplyAsync(BallerinaConnectorsResponse::new);
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
        // TODO this is to reset offline flag modified in BuildProject#load and SingleFileProject#load
        System.setProperty(ProjectConstants.BALLERINA_OFFLINE_FLAG, String.valueOf(false));
        Environment environment = EnvironmentBuilder.buildDefault();

        PackageDescriptor packageDescriptor = PackageDescriptor.from(
        PackageOrg.from(org), PackageName.from(pkgName), PackageVersion.from(version));
        ResolutionRequest resolutionRequest = ResolutionRequest.from(packageDescriptor, PackageDependencyScope.DEFAULT);

        PackageResolver packageResolver = environment.getService(PackageResolver.class);
        List<ResolutionResponse> resolutionResponses = packageResolver.resolvePackages(
            Collections.singletonList(resolutionRequest));
        ResolutionResponse resolutionResponse = resolutionResponses.stream().findFirst().get();

        if (resolutionResponse.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.RESOLVED)) {
            Package resolvedPackage = resolutionResponse.resolvedPackage();
            if (resolvedPackage != null) {
                return resolvedPackage.project().sourceRoot();
            }
        }
        throw new LSConnectorException("No bala project found for package '"
                    + packageDescriptor.toString() + "'");
    }

    @Override
    public CompletableFuture<BallerinaConnectorResponse> connector(BallerinaConnectorRequest request) {
        String error = "";
        Connector connector = null;

//        TODO: Read connector metadata in disk cache
//        Optional<Connector> optionalConnector = readConnectorFile(connector);
//        if (optionalConnector.isPresent()) {
//            connector = optionalConnector.get();
//        }

        if (connector == null && request.getId() != null) {
            try {
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest connectorRequest = HttpRequest.newBuilder(
                        URI.create(this.ballerinaCentralUrl + "/connectors/" + request.getId()))
                        .header("accept", "application/json")
                        .build();

                HttpResponse<String> response = client.send(connectorRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    connector = new Gson().fromJson(response.body(), Connector.class);
                }

            } catch (Exception e) {
                String msg = "Operation 'ballerinaConnector/connectors' failed!";
                this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
            }
        }

        if (connector == null) {
            try {
                Path balaPath = resolveBalaPath(request.getOrgName(), request.getModuleName(), request.getVersion());
                ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
                defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);

                Project balaProject = ProjectLoader.loadProject(balaPath, defaultBuilder);

                List<Connector> connectors = ConnectorGenerator.generateConnectorModel(balaProject);
                for (Connector conn : connectors) {
                    if (conn.name.equals(request.getName())) {
                        connector = conn;
//                         TODO: Write connector metadata in disk cache
//                         writeConnectorFile(connector);
                        break;
                    }
                }

            } catch (Exception e) {
                String connectorId = getCacheableKey(request.getOrgName(), request.getModuleName(),
                        request.getVersion());
                String msg = "Operation 'ballerinaConnector/connector' for " + connectorId + ":" +
                        request.getName() + " failed!";
                error = e.getMessage();
                this.clientLogger.logError(this.connectorExtContext, msg, e, null, (Position) null);
            }
        }


        BallerinaConnectorResponse response = new BallerinaConnectorResponse(connector, error);
        return CompletableFuture.supplyAsync(() -> response);
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

    @Override
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

    private String getCacheFileName(Connector connector) {
        return connector.orgName + "/" + connector.moduleName + "/" +
                (connector.version.isEmpty() ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : connector.version) +
                "_" + connector.name + ".json";
    }

    private String getCacheFilePath(Connector connector) {
        String ballerinaHome = System.getProperty("ballerina.home");
        return ballerinaHome + "metadata/" + connector.orgName + "/" + connector.moduleName + "/" +
                (connector.version.isEmpty() ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : connector.version);
    }

    private Boolean writeConnectorFile(Connector connector) {
        try {
            String filePath = getCacheFilePath(connector);
            String fileName = getCacheFileName(connector);

            File connectorFile = new File(filePath + "/" + fileName);
            if (connectorFile.createNewFile()) {
                FileWriter fileWriter = new FileWriter(connectorFile);
                fileWriter.write(new Gson().toJson(connector));
                fileWriter.close();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private Optional<Connector> readConnectorFile(Connector connector) {
        try {
            String filePath = getCacheFilePath(connector);
            String fileName = getCacheFileName(connector);

            File connectorFile = new File(filePath + "/" + fileName);
            if (connectorFile.exists() && connectorFile.canRead()) {
                BufferedReader fileReader = new BufferedReader(new FileReader(connectorFile));
                Connector connector1 = new Gson().fromJson(fileReader, Connector.class);
                return Optional.ofNullable(connector1);
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

}
