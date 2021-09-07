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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.semantic.api.test.util;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.test.BCompileUtil;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Utility functions to support test cases written for the semantic API.
 *
 * @since 2.0.0
 */
public class SemanticAPITestUtils {

    public static Document getDocumentForSingleSource(Project project) {
        Package currentPackage = project.currentPackage();
        DocumentId id = currentPackage.getDefaultModule().documentIds().iterator().next();
        return currentPackage.getDefaultModule().document(id);
    }

    public static Document getDocumentForSingleTestSource(Project project) {
        Package currentPackage = project.currentPackage();
        DocumentId id = currentPackage.getDefaultModule().testDocumentIds().iterator().next();
        return currentPackage.getDefaultModule().document(id);
    }

    public static Optional<Document> getDocument(Project project, String moduleNamePart, String documentPath) {
        Package currentPackage = project.currentPackage();
        Module module = currentPackage.module(ModuleName.from(currentPackage.packageName(), moduleNamePart));
        Document document;
        for (DocumentId docId : module.documentIds()) {
            document = module.document(docId);
            if (document.name().equals(documentPath)) {
                return Optional.of(document);
            }
        }
        return Optional.empty();
    }

    public static Module getModule(Project project, String moduleName) {
        Package currentPackage = project.currentPackage();
        ModuleName modName = ModuleName.from(currentPackage.packageName(), moduleName);
        return currentPackage.module(modName);
    }

    public static SemanticModel getDefaultModulesSemanticModel(Project project) {
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        return currentPackage.getCompilation().getSemanticModel(defaultModuleId);
    }

    public static SemanticModel getDefaultModulesSemanticModel(String sourceFilePath) {
        Project project = BCompileUtil.loadProject(sourceFilePath);
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        return currentPackage.getCompilation().getSemanticModel(defaultModuleId);
    }

    public static SemanticModel getSemanticModelOf(String projectPath, String moduleName) {
        Project project = BCompileUtil.loadProject(projectPath);
        Package currentPackage = project.currentPackage();
        ModuleName modName = ModuleName.from(currentPackage.packageName(), moduleName);
        return currentPackage.getCompilation().getSemanticModel(currentPackage.module(modName).moduleId());
    }

    public static SemanticModel getSemanticModelOf(Project project, String moduleName) {
        Package currentPackage = project.currentPackage();
        ModuleName modName = ModuleName.from(currentPackage.packageName(), moduleName);
        return currentPackage.getCompilation().getSemanticModel(currentPackage.module(modName).moduleId());
    }

    public static void assertList(List<? extends Symbol> actualValues, List<String> expectedValues) {
        Map<String, Symbol> symbols = actualValues.stream().collect(
                Collectors.toMap(s -> s.getName().orElse(""), s -> s));
        assertList(symbols, expectedValues);
    }

    public static void assertList(Map<String, ? extends Symbol> actualValues, List<String> expectedValues) {
        assertEquals(actualValues.size(), expectedValues.size());

        for (String val : expectedValues) {
            assertTrue(actualValues.containsKey(val), "Symbol not found: " + val);
        }
    }

    public static Map<String, Symbol> getSymbolsInFile(SemanticModel model, Document srcFile, int line,
                                                       int column, ModuleID moduleID) {
        List<Symbol> allInScopeSymbols = model.visibleSymbols(srcFile, LinePosition.from(line, column));
        return allInScopeSymbols.stream()
                .filter(s -> s.getModule().get().id().equals(moduleID))
                .collect(Collectors.toMap(s -> s.getName().orElse(""), s -> s));
    }

    public static List<String> getSymbolNames(List<String> mainList, String... args) {
        return Stream.concat(mainList.stream(), Stream.of(args)).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<T> getSymbolNames(List<T>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<String> getSymbolNames(BPackageSymbol pkgSymbol, int symTag) {
        List<String> symbolNames = new ArrayList<>();
        for (Map.Entry<Name, Scope.ScopeEntry> entry : pkgSymbol.scope.entries.entrySet()) {
            Name name = entry.getKey();
            Scope.ScopeEntry value = entry.getValue();

            if (value.symbol != null && (value.symbol.tag & symTag) == symTag
                    && Symbols.isFlagOn(value.symbol.flags, Flags.PUBLIC) && value.symbol.origin == COMPILED_SOURCE) {
                symbolNames.add(name.value);
            }
        }
        return symbolNames;
    }

    public static List<String> getSymbolNames(BPackageSymbol pkgSymbol, int symTag, SymbolOrigin origin) {
        List<String> symbolNames = new ArrayList<>();
        for (Map.Entry<Name, Scope.ScopeEntry> entry : pkgSymbol.scope.entries.entrySet()) {
            Name name = entry.getKey();
            Scope.ScopeEntry value = entry.getValue();

            if (value.symbol != null && (value.symbol.tag & symTag) == symTag && value.symbol.origin == origin) {
                symbolNames.add(name.value);
            }
        }
        return symbolNames;
    }
}
