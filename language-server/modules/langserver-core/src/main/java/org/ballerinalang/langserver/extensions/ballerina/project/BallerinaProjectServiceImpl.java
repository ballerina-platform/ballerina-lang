/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.project;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.extensions.VisibleEndpointVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Ballerina Project extension for Language Server.
 *
 * @since 1.0.0
 */
public class BallerinaProjectServiceImpl implements BallerinaProjectService {
    private final WorkspaceDocumentManager documentManager;

    public BallerinaProjectServiceImpl(LSGlobalContext globalContext) {
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
    }

    @Override
    public CompletableFuture<ModulesResponse> modules (ModulesRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ModulesResponse reply = new ModulesResponse();
            String sourceRoot = request.getSourceRoot();
            if (CommonUtil.isCachedExternalSource(sourceRoot)) {
                reply.setParseSuccess(false);
                return reply;
            }
            try {
                LSContext astContext = new ProjectServiceOperationContext
                        .ProjectServiceContextBuilder(LSContextOperation.PROJ_MODULES)
                        .withModulesParams(sourceRoot, documentManager)
                        .build();
                List<BLangPackage> modules = LSModuleCompiler.getBLangModules(astContext, this.documentManager, false);
                JsonObject jsonModulesInfo = getJsonReply(astContext, modules);
                reply.setModules(jsonModulesInfo);
                reply.setParseSuccess(true);
            } catch (CompilationFailedException | JSONGenerationException | URISyntaxException e) {
                reply.setParseSuccess(false);
            }
            return reply;
        });
    }

    private JsonObject getJsonReply(LSContext astContext, List<BLangPackage> modules)
            throws JSONGenerationException, URISyntaxException {
        JsonObject jsonModules = new JsonObject();

        for (BLangPackage module : modules) {
            JsonObject jsonModule = new JsonObject();

            if (module.symbol == null) {
                continue;
            }

            jsonModule.addProperty("name", module.symbol.name.value);
            CompilerContext compilerContext = astContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);

            VisibleEndpointVisitor visibleEndpointVisitor = new VisibleEndpointVisitor(compilerContext);
            visibleEndpointVisitor.visit(module);
            Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode = visibleEndpointVisitor.getVisibleEPsByNode();

            JsonObject jsonCUnits = new JsonObject();
            List<BLangCompilationUnit> compilationUnits = module.getCompilationUnits();
            // add all cunits in testable package as well
            module.testablePkgs.forEach(bLangTestablePackage ->
                    compilationUnits.addAll(bLangTestablePackage.getCompilationUnits()));
            for (BLangCompilationUnit cUnit: compilationUnits) {
                JsonObject jsonCUnit = new JsonObject();
                jsonCUnit.addProperty("name", cUnit.name);
                Path sourceRoot = Paths.get(new URI(astContext.get(DocumentServiceKeys.SOURCE_ROOT_KEY)));
                String uri = sourceRoot.resolve(
                        Paths.get("src", module.getPosition().getSource().cUnitName,
                        cUnit.getPosition().getSource().cUnitName)).toUri().toString();
                jsonCUnit.addProperty("uri", uri);
                JsonElement jsonAST = TextDocumentFormatUtil.generateJSON(cUnit, new HashMap<>(), visibleEPsByNode);
                jsonCUnit.add("ast", jsonAST);
                jsonCUnits.add(cUnit.name, jsonCUnit);
            }
            jsonModule.add("compilationUnits", jsonCUnits);
            jsonModules.add(module.symbol.name.value, jsonModule);
        }

        return jsonModules;
    }
}
