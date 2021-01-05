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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.ProjectLoader;

import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Ballerina Project extension for Language Server.
 *
 * @since 1.0.0
 */
public class BallerinaProjectServiceImpl implements BallerinaProjectService {

    @Override
    public CompletableFuture<PackagesResponse> packages(PackagesRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            PackagesResponse response = new PackagesResponse();
            response.setPackages(createJSONResponse(request.getSourceRoot()));
            response.setParseSuccess(true);
            return response;
        });
    }

    /**
     * Creates a JSON response with packages details.
     *
     * @param sourceRoot Project root path
     * @return {@link JsonArray} Array of packages
     */
    private JsonArray createJSONResponse(String sourceRoot) {
        Package currentPackage = ProjectLoader.loadProject(Paths.get(URI.create(sourceRoot))).currentPackage();
        JsonObject jsonPackage = new JsonObject();
        jsonPackage.addProperty(ProjectConstants.NAME, currentPackage.packageName().value());
        JsonArray jsonModules = new JsonArray();
        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);
            JsonObject jsonModule = new JsonObject();
            if (module.isDefaultModule()) {
                jsonModule.addProperty(ProjectConstants.DEFAULT, true);
            } else {
                jsonModule.addProperty(ProjectConstants.NAME, module.moduleName().moduleNamePart());
            }
            ModuleVisitor moduleVisitor = new ModuleVisitor(jsonModule);
            for (DocumentId documentId : module.documentIds()) {
                Document document = module.document(documentId);
                SyntaxTree syntaxTree = document.syntaxTree();
                moduleVisitor.visitPackage(syntaxTree.rootNode(), syntaxTree.filePath());
            }
            jsonModules.add(jsonModule);
        }
        jsonPackage.add(ProjectConstants.MODULES, jsonModules);
        JsonArray jsonPackages = new JsonArray();
        jsonPackages.add(jsonPackage);
        return jsonPackages;
    }
}
