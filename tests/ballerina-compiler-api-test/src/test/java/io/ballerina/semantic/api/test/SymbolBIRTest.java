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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaModule;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolNames;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the looking up symbols from the semantic API.
 *
 * @since 2.0.0
 */
public class SymbolBIRTest {

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/testproject");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }
    }

    @Test
    public void testSymbolLookupInBIR() {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_imports_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        Document srcFile = getDocumentForSingleSource(project);

        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        BPackageSymbol fooPkgSymbol = pkg.imports.get(0).symbol;

        List<SymbolInfo> annotationModuleSymbols = getAnnotationModuleSymbolInfo();
        List<SymbolInfo> moduleLevelSymbols = getModuleLevelSymbolInfo();
        List<SymbolInfo> moduleSymbols = getModuleSymbolInfo();
        List<SymbolInfo> expSymbolNames = getSymbolNames(annotationModuleSymbols, moduleLevelSymbols, moduleSymbols);

        List<Symbol> symbolsInScope = model.visibleSymbols(srcFile, LinePosition.from(18, 0));
        assertList(symbolsInScope, expSymbolNames);

        BallerinaModule fooModule = (BallerinaModule) symbolsInScope.stream()
                .filter(sym -> sym.getName().get().equals("testproject")).findAny().get();
        List<String> fooFunctions = getSymbolNames(fooPkgSymbol, SymTag.FUNCTION);
        SemanticAPITestUtils.assertList(fooModule.functions(), fooFunctions);

        List<String> fooConstants = getSymbolNames(fooPkgSymbol, SymTag.CONSTANT);
        SemanticAPITestUtils.assertList(fooModule.constants(), fooConstants);

        List<String> fooTypeDefs = getSymbolNames(getSymbolNames(fooPkgSymbol, SymTag.TYPE_DEF), "Digit");
        fooTypeDefs.remove("PersonObj");
        fooTypeDefs.remove("Colour");
        SemanticAPITestUtils.assertList(fooModule.typeDefinitions(), fooTypeDefs);

        SemanticAPITestUtils.assertList(fooModule.classes(), List.of("PersonObj"));
        SemanticAPITestUtils.assertList(fooModule.enums(), List.of("Colour"));

        List<String> allSymbols = getSymbolNames(fooPkgSymbol, 0);
        SemanticAPITestUtils.assertList(fooModule.allSymbols(), allSymbols);
    }

    @Test(dataProvider = "ImportSymbolPosProvider")
    public void testImportSymbols(int line, int column, String expSymbolName) {
        Project project = BCompileUtil.loadProject("test-src/symbol_at_cursor_import_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.getName().get(), expSymbolName));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "ImportSymbolPosProvider")
    public Object[][] getImportSymbolPos() {
        return new Object[][]{
                {16, 6, null},
                {16, 10, "testproject"},
                {16, 16, "testproject"},
                {16, 26, null},
//                {19, 17, "foo"}, // TODO: issue #25841
                {20, 13, "testproject"},
                {22, 5, "testproject"},
//                {26, 12, "foo"}, // TODO: issue #25841
                {31, 20, "getName"},
        };
    }

    // util methods

    public static void assertList(List<Symbol> actualValues, List<SymbolInfo> expectedValues) {
        assertEquals(actualValues.size(), expectedValues.size());

        for (SymbolInfo val : expectedValues) {
            assertTrue(actualValues.stream()
                               .anyMatch(sym -> val.equals(new SymbolInfo(sym.getName().get(), sym.kind()))),
                       "Symbol not found: " + val);

        }
    }

    private List<SymbolInfo> getAnnotationModuleSymbolInfo() {
        return createSymbolInfoList(
                new Object[][]{
                        {"deprecated", ANNOTATION}, {"untainted", ANNOTATION}, {"tainted", ANNOTATION},
                        {"display", ANNOTATION}, {"strand", ANNOTATION}, {"StrandData", TYPE_DEFINITION},
                        {"typeParam", ANNOTATION}, {"Thread", TYPE_DEFINITION}, {"builtinSubtype", ANNOTATION},
                        {"isolatedParam", ANNOTATION}, {"error", TYPE_DEFINITION}
                });
    }

    private List<SymbolInfo> getModuleLevelSymbolInfo() {
        return createSymbolInfoList(
                new Object[][]{
                        {"aString", VARIABLE}, {"anInt", VARIABLE}, {"HELLO", CONSTANT}, {"testAnonTypes", FUNCTION}
                });
    }

    private List<SymbolInfo> getModuleSymbolInfo() {
        return createSymbolInfoList(
                new Object[][]{
                        {"xml", MODULE}, {"testproject", MODULE}, {"object", MODULE}, {"error", MODULE},
                        {"boolean", MODULE}, {"decimal", MODULE}, {"typedesc", MODULE}, {"float", MODULE},
                        {"future", MODULE}, {"int", MODULE}, {"map", MODULE}, {"stream", MODULE},
                        {"string", MODULE}, {"table", MODULE}, {"transaction", MODULE}
                });
    }

    private List<SymbolInfo> createSymbolInfoList(Object[][] infoArr) {
        List<SymbolInfo> symInfo = new ArrayList<>();
        for (int i = 0; i < infoArr.length; i++) {
            symInfo.add(new SymbolInfo((String) infoArr[i][0], (SymbolKind) infoArr[i][1]));
        }
        return symInfo;
    }

    static class SymbolInfo {
        private String name;
        private SymbolKind kind;

        SymbolInfo(String name, SymbolKind kind) {
            this.name = name;
            this.kind = kind;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof SymbolInfo)) {
                return false;
            }

            SymbolInfo info = (SymbolInfo) obj;
            return this.name.equals(info.name) && this.kind == info.kind;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.kind);
        }
    }
}
