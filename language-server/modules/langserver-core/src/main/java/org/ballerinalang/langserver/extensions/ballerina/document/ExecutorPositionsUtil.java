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
package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AbsResourcePathAttachPoint;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.LiteralAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceAttachPointKind;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Executor positions evaluation utilities.
 *
 * @since 2.0.0
 */
public class ExecutorPositionsUtil {

    protected static final String FILE_PATH = "filePath";
    protected static final String KIND = "kind";
    protected static final String FUNC_MAIN = "main";
    protected static final String MODULES = "modules";
    protected static final String NAME = "name";
    protected static final String RANGE = "range";
    protected static final String SOURCE = "source";
    protected static final String TEST = "test";
    protected static final String TEST_CONFIG = "test:Config";

    protected static final Gson GSON = new Gson();

    private ExecutorPositionsUtil() {
    }

    public static JsonArray getExecutorPositions(WorkspaceManager workspaceManager,
                                                 Path filePath) {
        JsonArray execPositions = new JsonArray();
        Optional<Project> optionalProject = workspaceManager.project(filePath);
        Optional<Module> module = workspaceManager.module(filePath);
        if (optionalProject.isEmpty() || module.isEmpty()) {
            return execPositions;
        }
        Project project = optionalProject.get();

        if (!module.get().isDefaultModule()) {
            getTestCasePositions(execPositions, project, filePath, module.get());
            return execPositions;
        }

        Optional<SemanticModel> semanticModel = workspaceManager.semanticModel(filePath);
        if (semanticModel.isEmpty()) {
            return execPositions;
        }
        List<Symbol> symbolList = semanticModel.get().moduleSymbols();

        List<FunctionSymbol> defaultModuleFunctionList = symbolList.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION &&
                        symbol.getName().isPresent() &&
                        symbol.getName().get().equals(FUNC_MAIN) &&
                        symbol.getLocation().isPresent())
                .filter(symbol -> {
                    try {
                        return isLocationInFile(module.get(), symbol.getLocation().get(), filePath);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .map(symbol -> (FunctionSymbol) symbol)
                .collect(Collectors.toList());

        if (defaultModuleFunctionList.size() == 1) {
            JsonObject mainFunctionObject = new JsonObject();
            mainFunctionObject.addProperty(KIND, SOURCE);
            mainFunctionObject.addProperty(NAME, FUNC_MAIN);
            if (defaultModuleFunctionList.get(0).getLocation().isPresent()) {
                Location location = defaultModuleFunctionList.get(0).getLocation().get();
                mainFunctionObject.add(RANGE, GSON.toJsonTree(location.lineRange()));
                mainFunctionObject.addProperty(FILE_PATH, location.lineRange().filePath());
            }
            execPositions.add(mainFunctionObject);
        }

        symbolList.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.SERVICE_DECLARATION &&
                        symbol.getLocation().isPresent())
                .filter(symbol -> {
                    try {
                        return isLocationInFile(module.get(), symbol.getLocation().get(), filePath);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .map(symbol -> (ServiceDeclarationSymbol) symbol)
                .collect(Collectors.toList())
                .forEach(serviceSymbol -> {
                    JsonObject serviceObject = new JsonObject();
                    serviceObject.addProperty(KIND, SOURCE);
                    if (serviceSymbol.getLocation().isPresent()) {
                        Location location = serviceSymbol.getLocation().get();
                        serviceObject.add(RANGE, GSON.toJsonTree(location.lineRange()));
                        serviceObject.addProperty(FILE_PATH, location.lineRange().filePath());
                    }
                    Optional<ServiceAttachPoint> serviceAttachPoint = serviceSymbol.attachPoint();
                    if (serviceAttachPoint.isPresent()) {
                        ServiceAttachPointKind kind = serviceAttachPoint.get().kind();
                        if (kind == ServiceAttachPointKind.ABSOLUTE_RESOURCE_PATH) {
                            List<String> segments =
                                    ((AbsResourcePathAttachPoint) serviceAttachPoint.get()).segments();
                            serviceObject.addProperty(NAME, String.join("_", segments));
                        } else if (kind == ServiceAttachPointKind.STRING_LITERAL) {
                            serviceObject.addProperty(NAME,
                                    ((LiteralAttachPoint) serviceAttachPoint.get()).literal());
                        }
                    }
                    execPositions.add(serviceObject);
                });

        getTestCasePositions(execPositions, project, filePath, module.get());
        return execPositions;
    }

    /**
     * Checks if the provided location (interpreted relatively to the provided module) is location in the same file
     * provided.
     *
     * @param module   Module where the location resides
     * @param location Location
     * @param filePath File path to check against
     * @return True if the location resides in the provided file path
     * @throws IOException On IO errors
     */
    private static boolean isLocationInFile(Module module, Location location, Path filePath) throws IOException {
        Path symbolPath = PathUtil.getPathFromLocation(module, location);
        return Files.isSameFile(symbolPath, filePath);
    }

    private static void getTestCasePositions(JsonArray execPositions, Project project, Path filePath, Module module) {
        String moduleName = module.moduleName().moduleNamePart() == null ? "" :
                module.moduleName().moduleNamePart();
        TestCaseVisitor testCaseVisitor = new TestCaseVisitor(execPositions, filePath);
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            module.documentIds().forEach(documentId -> {
                testCaseVisitor.visitTestCases(module.document(documentId).syntaxTree().rootNode());
            });

        } else if (project.kind() == ProjectKind.BUILD_PROJECT) {
            module.testDocumentIds().forEach(testDocumentId -> {
                Document testDocument = module.document(testDocumentId);
                Path testDocPath;
                if (module.isDefaultModule()) {
                    testDocPath = project.sourceRoot().resolve(testDocument.name());
                } else {
                    testDocPath = project.sourceRoot()
                            .resolve(MODULES)
                            .resolve(moduleName)
                            .resolve(testDocument.name());
                }
                try {
                    if (Files.isSameFile(filePath, testDocPath)) {
                        testCaseVisitor.visitTestCases(testDocument.syntaxTree().rootNode());
                    }
                } catch (IOException ignored) {
                }
            });
        }
    }
}
