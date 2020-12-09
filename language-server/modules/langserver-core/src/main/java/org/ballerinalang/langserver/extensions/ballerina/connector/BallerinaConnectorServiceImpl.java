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
import com.moandjiezana.toml.Toml;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.balo.BaloProject;
import io.ballerina.projects.repos.TempDirCompilationCache;
import java.util.ArrayList;
import java.util.Iterator;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.exception.LSConnectorException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.types.RecordTypeNode;
import org.ballerinalang.model.types.ValueType;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeBaloRepo;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.langserver.compiler.LSClientLogger.logError;

/**
 * Implementation of the BallerinaConnectorService.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorServiceImpl implements BallerinaConnectorService {

    public static final String DEFAULT_CONNECTOR_FILE_KEY = "DEFAULT_CONNECTOR_FILE";
    private static final Path STD_LIB_SOURCE_ROOT = Paths.get(CommonUtil.BALLERINA_HOME)
//            .resolve("lib")
            .resolve("repo")
            .resolve("balo");
    private String connectorConfig;
    private LSGlobalContext lsContext;
    private WorkspaceManager workspaceManager;

    public BallerinaConnectorServiceImpl(WorkspaceManager workspaceManager, LSGlobalContext lsContext) {
        this.workspaceManager = workspaceManager;
        this.lsContext = lsContext;
        connectorConfig = System.getenv(DEFAULT_CONNECTOR_FILE_KEY);
        if (connectorConfig == null) {
            connectorConfig = System.getProperty(DEFAULT_CONNECTOR_FILE_KEY);
        }
    }

    @Override
    public CompletableFuture<BallerinaConnectorsResponse> connectors() {
        try {
            BallerinaConnectorsResponse response = getConnectorConfig();
            return CompletableFuture.supplyAsync(() -> response);
        } catch (IOException e) {
            String msg = "Operation 'ballerinaConnector/connectors' failed!";
            logError(msg, e, null, (Position) null);
        }

        return CompletableFuture.supplyAsync(BallerinaConnectorsResponse::new);
    }

    private Path getBaloPath(String org, String module, String version) throws LSConnectorException {
        Path baloPath = STD_LIB_SOURCE_ROOT.resolve(org).resolve(module).
                resolve(version.isEmpty() ?
                        ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : version).
                resolve(String.format("%s-%s-any-%s%s", org, module, version, ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT));
//                resolve(module + ProjectDirConstants.BLANG_COMPILED_PKG_EXT);
        if (!Files.exists(baloPath.toAbsolutePath())) {
            //check external modules
            // todo: use the .balo file in the folder path this happened because of the project structure change
            PackageID packageID = new PackageID(new Name(org), new Name(module), new Name(version));
            HomeBaloRepo homeBaloRepo = new HomeBaloRepo(new HashMap<>());
            Patten patten = homeBaloRepo.calculate(packageID);
            Stream<Path> s = patten.convert(new BaloConverter(), packageID);
            Optional<Path> path = s.reduce(Path::resolve);
            if (path.isPresent() && Files.exists(path.get().toAbsolutePath())) {
                baloPath = path.get().toAbsolutePath();
            } else {
                throw new LSConnectorException("No file exist in '" + ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT + path.get().toAbsolutePath() + "'");
            }
        }
        return baloPath;
    }

    @Override
    public CompletableFuture<BallerinaConnectorResponse> connector(BallerinaConnectorRequest request) {

        String cacheableKey = getCacheableKey(request.getOrg(), request.getModule(), request.getVersion());
        LSConnectorCache connectorCache = LSConnectorCache.getInstance(lsContext);
        JsonElement st = null; // connectorCache.getConnectorConfig(request.getOrg(), request.getModule(),
//                request.getVersion(), request.getName());
//                request.getVersion(), request.getName());
        String error = "";
        if (st == null) {
            try {
//                Path baloPath = getBaloPath(request.getOrg(), request.getModule(), request.getVersion());
                Path baloPath = Paths.get("/Users/charukak/Documents/slp7/ballerina-slp7/repo/balo/ballerina/" +
                        "module1/0.1.0/ballerina-module1-any-0.1.0.balo");
                ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
                defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
                BaloProject baloProject = BaloProject.loadProject(defaultBuilder, baloPath);
                ModuleId moduleId = baloProject.currentPackage().moduleIds().stream().findFirst().get();
                Module module = baloProject.currentPackage().module(moduleId);
                SemanticModel semanticModel = module.getCompilation().getSemanticModel(); // get the semantic model for the module
                List<SyntaxTree> syntaxTreeList= new ArrayList<>();
                // todo : use module1 from the to test out the semantic model stuff


                // todo : get documentids and iterate them one by one

                ConnectorNodeVisitor connectorNodeVisitor = new ConnectorNodeVisitor(request.getName(), semanticModel); // check what type nodes are coming here and fix the visitor based on that

                module.documentIds().forEach(documentId -> {
                    module.document(documentId).syntaxTree().rootNode().accept(connectorNodeVisitor);
                });

                Map<String, JsonElement> jsonRecords = new HashMap<>();
                connectorNodeVisitor.getRecords().forEach((key, record) -> {
                    JsonElement jsonRecordST = DiagramUtil.getSyntaxTreeJSON(record.syntaxTree().filePath(), record.syntaxTree(), semanticModel);
                    jsonRecords.put(key, jsonRecordST);
                });

                Gson gson = new Gson();
                List<ClassDefinitionNode> connectorNodes = connectorNodeVisitor.getConnectors();
                connectorNodes.forEach(connector -> {
                    // todo : preseve the existing logic add the information to the typeData element of the syntax tree JSON and send to front end

                    Map<String, JsonElement> connectorRecords = new HashMap<>();

                    Iterator<Node> connectorChildIterator = connector.children().iterator();

                    while (connectorChildIterator.hasNext()) {
                        Node child = connectorChildIterator.next();
                        Optional<TypeSymbol> typeSymbol = semanticModel.type(child.syntaxTree().filePath(), child.lineRange());


                        if (typeSymbol.isPresent()) {
                            TypeSymbol rawType = getRawType(typeSymbol.get());
                            if (rawType.typeKind() == TypeDescKind.FUNCTION) {
                                if (rawType.typeKind().name() == "init") {
                                    boolean test = false;
                                }
                            }
                        }
                        boolean test = false;
//                        semanticModel.type(child.syntaxTree().filePath(), child.lineRange()).get().typeKind().name();
//                        ((IdentifierToken)((FunctionDefinitionNode) child).childEntries().stream().filter(el -> el.name().equals("functionName")).findFirst().get().node().get()).text();
                    }

//                    Map<String, JsonElement> connectorRecords = new HashMap<>();
//                    FunctionNode initFunction = ((BLangObjectTypeNode) connector.getTypeNode()).getInitFunction();
//                    if (initFunction != null) {
//                        initFunction.getParameters().forEach(
//                                param -> populateConnectorRecords(((BLangVariable) param).type,
//                                        connectorNodeVisitor.getRecords(), jsonRecords, connectorRecords));
//                    }
//                    ((BLangObjectTypeNode) connector.getTypeNode()).getFunctions().forEach(f -> f.getParameters().
//                            forEach(param -> populateConnectorRecords(param.type,
//                                    connectorNodeVisitor.getRecords(), jsonRecords, connectorRecords)));
//
//                    JsonElement jsonAST = null;
//                    try {
//                        jsonAST = TextDocumentFormatUtil.generateJSON(connector, new HashMap<>(),
//                                visibleEndpointVisitor.getVisibleEPsByNode());
//                    } catch (JSONGenerationException e) {
//                        String msg = "Operation 'ballerinaConnector/connector' loading " + cacheableKey + ":" +
//                                connector.getName().getValue() + " failed!";
//                        logError(msg, e, null, (Position) null);
//                    }
//                    if (jsonAST instanceof JsonObject) {
//                        JsonElement recordsJson = gson.toJsonTree(connectorRecords);
//                        ((JsonObject) jsonAST).add("records", recordsJson);
//                    }
//                    connectorCache.addConnectorConfig(request.getOrg(), request.getModule(),
//                            request.getVersion(), connector.getName().getValue(), jsonAST);
                    SyntaxTree syntaxTree = connector.syntaxTree();
                    JsonElement jsonST = DiagramUtil.getSyntaxTreeJSON(syntaxTree.filePath(), syntaxTree, semanticModel);
                    connectorCache.addConnectorConfig(request.getOrg(), request.getModule(),
                            request.getVersion(), connector.className().text(), jsonST);

                });
                st = connectorCache.getConnectorConfig(request.getOrg(), request.getModule(),
                        request.getVersion(), request.getName());
            } catch (Exception e) {
                String msg = "Operation 'ballerinaConnector/connector' for " + cacheableKey + ":" +
                        request.getName() + " failed!";
                error = e.getMessage();
                logError(msg, e, null, (Position) null);
            }
        }
        BallerinaConnectorResponse response = new BallerinaConnectorResponse(request.getOrg(), request.getModule(),
                request.getVersion(), request.getName(), request.getDisplayName(), st, error, request.getBeta());
        return CompletableFuture.supplyAsync(() -> response);
    }

    @Override
    public CompletableFuture<BallerinaRecordResponse> record(BallerinaRecordRequest request) {
    // TODO: 12/7/20 this didn't have any usages hence commented out check if this has any uses

//        String cacheableKey = getCacheableKey(request.getOrg(), request.getModule(), request.getVersion());
////        LSRecordCache recordCache = LSRecordCache.getInstance(lsContext);
//
//        JsonElement ast = null; // recordCache.getRecordAST(request.getOrg(), request.getModule(),
////                request.getVersion(), request.getName());
//        String error = "";
//        if (ast == null) {
//            try {
//                int versionSeparator = request.getModule().lastIndexOf("_");
//                int modNameSeparator = request.getModule().indexOf("_");
//                String version = request.getModule().substring(versionSeparator + 1);
//                String moduleName = request.getModule().substring(modNameSeparator + 1, versionSeparator);
//                String orgName = request.getModule().substring(0, modNameSeparator);
//                Path baloPath = getBaloPath(orgName, moduleName, version);
//                boolean isExternalModule = baloPath.toString().endsWith(".balo");
//
//                String projectDir = CommonUtil.LS_CONNECTOR_CACHE_DIR.resolve(cacheableKey).toString();
//                if (isExternalModule) {
//                    LSConnectorUtil.extract(baloPath, cacheableKey);
//                } else {
//                    Path destinationRoot = CommonUtil.LS_STDLIB_CACHE_DIR.resolve(cacheableKey).
//                            resolve(ProjectDirConstants.SOURCE_DIR_NAME);
//                    if (!Files.exists(destinationRoot)) {
//                        LSStdLibCacheUtil.extract(baloPath, destinationRoot, moduleName, cacheableKey);
//                    }
//                    projectDir = CommonUtil.LS_STDLIB_CACHE_DIR.resolve(cacheableKey).toString();
//                    moduleName = cacheableKey;
//                }
//                CompilerContext compilerContext = createNewCompilerContext(projectDir);
//                Compiler compiler = LSStdLibCacheUtil.getCompiler(compilerContext);
//                BLangPackage bLangPackage = compiler.compile(moduleName);
//
//                ConnectorNodeVisitor connectorNodeVisitor = new ConnectorNodeVisitor(request.getName());
//                bLangPackage.accept(connectorNodeVisitor);
//
//                VisibleEndpointVisitor visibleEndpointVisitor = new VisibleEndpointVisitor(compilerContext);
//                visibleEndpointVisitor.visit(bLangPackage);
//
//                Map<String, JsonElement> jsonRecords = new HashMap<>();
//                BLangTypeDefinition recordNode = null;
//                JsonElement recordJson = null;
//                for (Map.Entry<String, BLangTypeDefinition> recordEntry
//                        : connectorNodeVisitor.getRecords().entrySet()) {
//                    String key = recordEntry.getKey();
//                    BLangTypeDefinition record = recordEntry.getValue();
//                    JsonElement jsonAST = null;
//                    try {
//                        jsonAST = TextDocumentFormatUtil.generateJSON(record, new HashMap<>(),
//                                visibleEndpointVisitor.getVisibleEPsByNode());
//                    } catch (JSONGenerationException e) {
//                        String msg = "Operation 'ballerinaConnector/record' loading records" +
//                                key + " failed!";
//                        logError(msg, e, null, (Position) null);
//                    }
//                    if (record.getName() != null && record.getName().value.equals(request.getName())) {
//                        recordNode = record;
//                        recordJson = jsonAST;
//                    } else {
//                        jsonRecords.put(key, jsonAST);
//                    }
//                }
//                Gson gson = new Gson();
//                if (recordNode != null) {
//                    if (recordJson instanceof JsonObject) {
//                        JsonElement recordsJson = gson.toJsonTree(jsonRecords);
//                        ((JsonObject) recordJson).add("records", recordsJson);
//                    }
////                    recordCache.addRecordAST(request.getOrg(), request.getModule(),
////                            request.getVersion(), request.getName(), recordJson);
//                }
//
////                ast = recordCache.getRecordAST(request.getOrg(), request.getModule(),
////                        request.getVersion(), request.getName());
//            } catch (Exception e) {
//                String msg = "Operation 'ballerinaConnector/record' for " + cacheableKey + ":" +
//                        request.getName() + " failed!";
//                error = e.getMessage();
//                logError(msg, e, null, (Position) null);
//            }
//
//        }
//        BallerinaRecordResponse response = new BallerinaRecordResponse(request.getOrg(), request.getModule(),
//                request.getVersion(), request.getName(), ast, error, request.getBeta());
//        return CompletableFuture.supplyAsync(() -> response);

        return null;
    }

    private void populateConnectorRecords(ValueType type, Map<String, BLangTypeDefinition> records,
                                          Map<String, JsonElement> jsonRecords,
                                          Map<String, JsonElement> connectorRecords) {


        if (type instanceof BUnionType) {
            ((BUnionType) type).getMemberTypes().forEach(
                    m -> populateConnectorRecords(m, records, jsonRecords, connectorRecords));
        } else if (type instanceof BArrayType) {
            populateConnectorRecords(((BArrayType) type).eType, records, jsonRecords, connectorRecords);
        } else if (type instanceof BRecordType) {
            String recordName = type.toString();
            BLangTypeDefinition recordNode = records.get(recordName);
            if (recordNode != null) {
                connectorRecords.put(recordName, jsonRecords.get(recordName));
                ((RecordTypeNode) recordNode.getTypeNode()).getFields().forEach(f -> {
                            populateConnectorRecords(((BLangVariable) f).type, records, jsonRecords, connectorRecords);
                        }
                );

                ((RecordTypeNode) recordNode.getTypeNode()).getTypeReferences().forEach(r -> {
                            populateConnectorRecords(((BLangUserDefinedType) r).type, records,
                                    jsonRecords, connectorRecords);
                        }
                );
            }
        }
    }


    private String getCacheableKey(String orgName, String moduleName, String version) {
        return orgName + "_" + moduleName + "_" +
                (version.isEmpty() ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : version);
    }

    private BallerinaConnectorsResponse getConnectorConfig() throws IOException {
        try (InputStream inputStream = new FileInputStream(new File(connectorConfig));) {
            Toml toml;
            try {
                toml = new Toml().read(inputStream);
            } catch (IllegalStateException e) {
                throw new BLangCompilerException("invalid connector.toml due to " + e.getMessage());
            }
            return toml.to(BallerinaConnectorsResponse.class);
        }
    }

    private CompilerContext createNewCompilerContext(String projectDir) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, projectDir);
        options.put(COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.toString(false));
        options.put(OFFLINE, Boolean.toString(true));
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(true));
        context.put(SourceDirectory.class, new FileSystemProjectDirectory(Paths.get(projectDir)));
        return context;
    }

    private TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        return typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE
                ? ((TypeReferenceTypeSymbol) typeDescriptor).typeDescriptor() : typeDescriptor;
    }
}
