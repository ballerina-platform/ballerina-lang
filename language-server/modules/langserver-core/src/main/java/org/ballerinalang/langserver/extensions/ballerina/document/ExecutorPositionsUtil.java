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

    public static JsonArray getExecutorPositions(Project project, Path filePath) {

        JsonArray execPositions = new JsonArray();
        project.currentPackage().modules().forEach(module -> {
            if (module.isDefaultModule()) {
                List<Symbol> symbolList = module.getCompilation().getSemanticModel().moduleSymbols();

                List<FunctionSymbol> defaultModuleFunctionList =
                        symbolList.stream().filter(symbol -> symbol.kind() == SymbolKind.FUNCTION)
                                .filter(symbol -> {
                                    try {
                                        return symbol.getName().isPresent() && symbol.getName().get().equals(FUNC_MAIN)
                                                && symbol.getLocation().isPresent()
                                                && (project.kind() == ProjectKind.BUILD_PROJECT
                                                && Files.isSameFile(filePath,
                                                project.sourceRoot().resolve(symbol.getLocation().get().lineRange()
                                                        .filePath())) || (project.kind()
                                                == ProjectKind.SINGLE_FILE_PROJECT
                                                && Files.isSameFile(filePath, project.sourceRoot())));
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

                symbolList.stream().filter(symbol -> symbol.kind() == SymbolKind.SERVICE_DECLARATION)
                        .filter(symbol -> {
                            try {
                                return symbol.getLocation().isPresent()
                                        && (project.kind() == ProjectKind.BUILD_PROJECT
                                        && Files.isSameFile(filePath,
                                        project.sourceRoot().resolve(symbol.getLocation().get().lineRange()
                                                .filePath())) || (project.kind()
                                        == ProjectKind.SINGLE_FILE_PROJECT
                                        && Files.isSameFile(filePath, project.sourceRoot())));
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
            }
            getTestCasePositions(execPositions, project, filePath, module);
        });
        return execPositions;
    }

    private static void getTestCasePositions(JsonArray execPositions, Project project, Path filePath, Module module) {
        String moduleName = module.moduleName().moduleNamePart() == null ? "" :
                module.moduleName().moduleNamePart();
        TestCaseVisitor testCaseVisitor = new TestCaseVisitor(execPositions, filePath);
        try {
            if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT && Files.isSameFile(filePath,
                    project.sourceRoot())) {
                module.documentIds().forEach(documentId -> {
                    testCaseVisitor.visitTestCases(module.document(documentId).syntaxTree().rootNode());
                });

            } else if (project.kind() == ProjectKind.BUILD_PROJECT) {
                module.testDocumentIds().forEach(testDocumentId -> {
                    try {
                        Document testDocument = module.document(testDocumentId);
                        if ((module.isDefaultModule() && Files.isSameFile(filePath, project.sourceRoot()
                                .resolve(moduleName).resolve(testDocument.name()))) || (!module.isDefaultModule()
                                && Files.isSameFile(filePath, project.sourceRoot().resolve(MODULES)
                                .resolve(moduleName).resolve(testDocument.name())))) {
                            testCaseVisitor.visitTestCases(testDocument.syntaxTree().rootNode());
                        }
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (IOException ignored) {
        }
    }
}
