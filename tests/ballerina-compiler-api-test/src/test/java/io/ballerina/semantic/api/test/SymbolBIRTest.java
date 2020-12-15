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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolNames;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test cases for the looking up symbols from the semantic API.
 *
 * @since 2.0.0
 */
public class SymbolBIRTest {

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBalo("test-src/testproject");
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

        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        BPackageSymbol fooPkgSymbol = pkg.imports.get(0).symbol;

        List<String> annotationModuleSymbols = asList("deprecated", "untainted", "tainted", "icon", "strand",
                "StrandData", "typeParam", "Thread", "builtinSubtype",
                "isolatedParam");
        List<String> moduleLevelSymbols = asList("aString", "anInt", "HELLO", "testAnonTypes");
        List<String> moduleSymbols = asList("xml", "testproject", "object", "error", "boolean", "decimal", "typedesc",
                "float", "future", "int", "map", "stream", "string", "table");
        List<String> expSymbolNames = getSymbolNames(annotationModuleSymbols, moduleLevelSymbols, moduleSymbols);

        Map<String, Symbol> symbolsInScope =
                model.visibleSymbols("symbol_lookup_with_imports_test.bal", LinePosition.from(18, 0))
                        .stream().collect(Collectors.toMap(Symbol::name, s -> s));
        assertList(symbolsInScope, expSymbolNames);

        BallerinaModule fooModule = (BallerinaModule) symbolsInScope.get("testproject");
        List<String> fooFunctions = getSymbolNames(fooPkgSymbol, SymTag.FUNCTION);
        assertList(fooModule.functions(), fooFunctions);

        List<String> fooConstants = getSymbolNames(fooPkgSymbol, SymTag.CONSTANT);
        assertList(fooModule.constants(), fooConstants);

        List<String> fooTypeDefs = getSymbolNames(getSymbolNames(fooPkgSymbol, SymTag.TYPE_DEF), "FileNotFoundError",
                                                  "EofError", "Digit");
        fooTypeDefs.remove("PersonObj");
        assertList(fooModule.typeDefinitions(), fooTypeDefs);

        assertList(fooModule.classes(), List.of("PersonObj"));

        List<String> allSymbols = getSymbolNames(fooPkgSymbol, 0);
        assertList(fooModule.allSymbols(), allSymbols);
    }

    @Test(dataProvider = "ImportSymbolPosProvider")
    public void testImportSymbols(int line, int column, String expSymbolName) {
        SemanticModel model = SemanticAPITestUtils.getDefaultModulesSemanticModel(
                "test-src/symbol_at_cursor_import_test.bal");

        Optional<Symbol> symbol = model.symbol("symbol_at_cursor_import_test.bal", LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.name(), expSymbolName));

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
}
