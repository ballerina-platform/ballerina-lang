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

import com.google.gson.JsonElement;
import com.moandjiezana.toml.Toml;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.CollectDiagnosticListener;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.exception.LSConnectorException;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
import org.ballerinalang.langserver.extensions.VisibleEndpointVisitor;
import org.ballerinalang.langserver.util.definition.LSStdLibCacheUtil;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeBaloRepo;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
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
    private static final Path STD_LIB_SOURCE_ROOT = Paths.get(CommonUtil.BALLERINA_HOME).resolve("lib").resolve("repo");
    private String connectorConfig;
    private LSGlobalContext lsContext;

    public BallerinaConnectorServiceImpl(LSGlobalContext lsContext) {
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
                resolve(module + ProjectDirConstants.BLANG_COMPILED_PKG_EXT);
        if (!Files.exists(baloPath.toAbsolutePath())) {
            //check external modules
            PackageID packageID = new PackageID(new Name(org), new Name(module), new Name(version));
            HomeBaloRepo homeBaloRepo = new HomeBaloRepo(new HashMap<>());
            Patten patten = homeBaloRepo.calculate(packageID);
            Stream<Path> s = patten.convert(new BaloConverter(), packageID);
            Optional<Path> path = s.reduce(Path::resolve);
            if (path.isPresent() && Files.exists(path.get().toAbsolutePath())) {
                baloPath = path.get().toAbsolutePath();
            } else {
                throw new LSConnectorException("No file exist in '" + path.get().toAbsolutePath() + "'");
            }
        }
        return baloPath;
    }

    @Override
    public CompletableFuture<BallerinaConnectorResponse> connector(BallerinaConnectorRequest request) {

        String cacheableKey = getCacheableKey(request.getOrg(), request.getModule(), request.getVersion());
        LSConnectorCache connectorCache = LSConnectorCache.getInstance(lsContext);

        JsonElement ast = connectorCache.getConnectorConfig(request.getOrg(), request.getModule(),
                request.getVersion(), request.getName());

        if (ast == null) {
            try {
                Path baloPath = getBaloPath(request.getOrg(), request.getModule(), request.getVersion());
                boolean isExternalModule = baloPath.toString().endsWith(".balo");
                String moduleName = request.getModule();
                String projectDir = CommonUtil.LS_CONNECTOR_CACHE_DIR.resolve(cacheableKey).toString();
                if (isExternalModule) {
                    LSConnectorUtil.extract(baloPath, cacheableKey);
                } else {
                    Path destinationRoot = CommonUtil.LS_STDLIB_CACHE_DIR.resolve(cacheableKey).
                            resolve(ProjectDirConstants.SOURCE_DIR_NAME);
                    if (!Files.exists(destinationRoot)) {
                        LSStdLibCacheUtil.extract(baloPath, destinationRoot, moduleName, cacheableKey);
                    }
                    projectDir = CommonUtil.LS_STDLIB_CACHE_DIR.resolve(cacheableKey).toString();
                    moduleName = cacheableKey;
                }
                CompilerContext compilerContext = createNewCompilerContext(projectDir);
                Compiler compiler = LSStdLibCacheUtil.getCompiler(compilerContext);
                BLangPackage bLangPackage = compiler.compile(moduleName);

                ConnectorNodeVisitor visitor = new ConnectorNodeVisitor(request.getName());
                bLangPackage.accept(visitor);
                List<BLangTypeDefinition> connectorNode = visitor.getConnectors();

                VisibleEndpointVisitor visibleEndpointVisitor = new VisibleEndpointVisitor(compilerContext);
                visibleEndpointVisitor.visit(bLangPackage);

                connectorNode.forEach(connector -> {
                    JsonElement jsonAST = null;
                    try {
                        jsonAST = TextDocumentFormatUtil.generateJSON(connector, new HashMap<>(),
                                visibleEndpointVisitor.getVisibleEPsByNode());
                    } catch (JSONGenerationException e) {
                        String msg = "Operation 'ballerinaConnector/connector' loading " + cacheableKey + ":" +
                                connector.getName().getValue() + " failed!";
                        logError(msg, e, null, (Position) null);
                    }
                    connectorCache.addConnectorConfig(request.getOrg(), request.getModule(),
                            request.getVersion(), connector.getName().getValue(), jsonAST);
                });
                ast = connectorCache.getConnectorConfig(request.getOrg(), request.getModule(),
                        request.getVersion(), request.getName());
            } catch (UnsupportedEncodingException | LSStdlibCacheException | LSConnectorException e) {
                String msg = "Operation 'ballerinaConnector/connector' for " + cacheableKey + ":" +
                        request.getName() + " failed!";
                logError(msg, e, null, (Position) null);
            }
        }
        BallerinaConnectorResponse response = new BallerinaConnectorResponse(request.getOrg(), request.getModule(),
                request.getVersion(), request.getName(), ast);
        return CompletableFuture.supplyAsync(() -> response);
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
        context.put(DiagnosticListener.class, new CollectDiagnosticListener());
        return context;
    }
}
