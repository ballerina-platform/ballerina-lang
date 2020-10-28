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

import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaModule;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/test-project", "testorg", "foo");
    }

    @Test
    public void testSymbolLookupInBIR() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_with_imports_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BPackageSymbol fooPkgSymbol = pkg.imports.get(0).symbol;
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        List<String> annotationModuleSymbols = asList("deprecated", "untainted", "tainted", "icon", "strand",
                                                      "StrandData", "typeParam", "Thread", "builtinSubtype",
                                                      "isolatedParam");
        List<String> moduleLevelSymbols = asList("aString", "anInt", "HELLO", "testAnonTypes");
        List<String> moduleSymbols = asList("xml", "foo", "object", "error", "boolean", "decimal", "typedesc", "float",
                                            "future", "int", "map", "stream", "string", "table");
        List<String> expSymbolNames = getSymbolNames(annotationModuleSymbols, moduleLevelSymbols, moduleSymbols);

        Map<String, Symbol> symbolsInScope =
                model.visibleSymbols("symbol_lookup_with_imports_test.bal", LinePosition.from(18, 0))
                        .stream().collect(Collectors.toMap(Symbol::name, s -> s));
        assertList(symbolsInScope, expSymbolNames);

        BallerinaModule fooModule = (BallerinaModule) symbolsInScope.get("foo");
        List<String> fooFunctions = getSymbolNames(fooPkgSymbol, SymTag.FUNCTION);
        assertList(fooModule.functions(), fooFunctions);

        List<String> fooConstants = getSymbolNames(fooPkgSymbol, SymTag.CONSTANT);
        assertList(fooModule.constants(), fooConstants);

        List<String> fooTypeDefs = getSymbolNames(getSymbolNames(fooPkgSymbol, SymTag.TYPE_DEF), "FileNotFoundError",
                                                  "EofError", "Digit");
        assertList(fooModule.typeDefinitions(), fooTypeDefs);

        List<String> allSymbols = getSymbolNames(fooPkgSymbol, 0);
        assertList(fooModule.allSymbols(), allSymbols);
    }

    @Test(dataProvider = "ImportSymbolPosProvider")
    public void testImportSymbols(int line, int column, String expSymbolName) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_at_cursor_import_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Optional<Symbol> symbol = model.symbol("symbol_at_cursor_import_test.bal", LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.name(), expSymbolName));

        if (!symbol.isPresent()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "ImportSymbolPosProvider")
    public Object[][] getImportSymbolPos() {
        return new Object[][]{
                {16, 6, null},
                {16, 10, "foo"},
                {16, 16, "foo"},
                {16, 18, null},
//                {19, 17, "foo"}, // TODO: issue #25841
                {20, 13, "foo"},
                {22, 5, "foo"},
//                {26, 12, "foo"}, // TODO: issue #25841
                {31, 20, "PersonObj.getName"},
        };
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/test-project", "testorg", "foo");
    }

    private CompileResult compile(String path, CompilerContext context) {
        Path sourcePath = Paths.get(path);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }
}
