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

import io.ballerina.tools.text.LinePosition;
import org.ballerina.compiler.api.element.ModuleID;
import org.ballerina.compiler.api.symbol.BCompiledSymbol;
import org.ballerina.compiler.impl.semantic.SemanticModel;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the looking up symbols from the semantic API.
 *
 * @since 2.0.0
 */
public class SymbolLookupTest {

    private final Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    @Test(dataProvider = "PositionProvider1")
    public void testVarSymbolLookup(int line, int column, int expSymbols, List<String> expSymbolNames) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/var_symbol_lookup_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new ModuleID(pkg.packageID);
        SemanticModel model = new SemanticModel(pkg.compUnits.get(0), pkg, context);

        Map<String, BCompiledSymbol> symbolsInFile = getSymbolsInFile(model, line, column, moduleID);

        assertEquals(symbolsInFile.size(), expSymbols);
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }
    }

    @DataProvider(name = "PositionProvider1")
    public Object[][] getPositions() {
        List<String> moduleLevelSymbols = asList("aString", "anInt", "test", "HELLO");
        return new Object[][]{
                {3, 14, 4, moduleLevelSymbols},
                {20, 18, 4, moduleLevelSymbols},
//                {21, 31, 4, moduleLevelSymbols}, // TODO: Feature not yet supported
                {21, 39, 5, asList("aString", "anInt", "test", "HELLO", "greet")},
                {22, 1, 5, asList("aString", "anInt", "test", "HELLO", "greet")},
//                {23, 60, 6, asList("aString", "anInt", "test", "HELLO", "greet", "name")}, // TODO: Not yet supported
                {31, 13, 8, getSymbolNames(moduleLevelSymbols, "greet", "a", "x", "greetFn")},
        };
    }

    @Test(dataProvider = "PositionProvider2")
    public void testVarSymbolLookupInWorkers(int line, int column, int expSymbols, List<String> expSymbolNames) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_with_workers_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new ModuleID(pkg.packageID);
        SemanticModel model = new SemanticModel(pkg.compUnits.get(0), pkg, context);

        Map<String, BCompiledSymbol> symbolsInFile = getSymbolsInFile(model, line, column, moduleID);

        assertEquals(symbolsInFile.size(), expSymbols);
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }
    }

    @DataProvider(name = "PositionProvider2")
    public Object[][] getPositionsForWorkers() {
        List<String> moduleLevelSymbols = asList("aString", "anInt", "workerSendToWorker", "HELLO");
        return new Object[][]{
                {20, 51, 6, getSymbolNames(moduleLevelSymbols, "w1", "w2")},
                {22, 16, 6, getSymbolNames(moduleLevelSymbols, "w1", "w2")},
                {26, 1, 6, getSymbolNames(moduleLevelSymbols, "w1", "w2")},
                {24, 15, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "i")},
                {31, 13, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "j")},
                {34, 23, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "ret")},
                {38, 1, 4, moduleLevelSymbols},
        };
    }

    @Test(dataProvider = "PositionProvider3")
    public void testVarSymbolLookupInTypedefs(int line, int column, int expSymbols, List<String> expSymbolNames) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_with_typedefs_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new ModuleID(pkg.packageID);
        SemanticModel model = new SemanticModel(pkg.compUnits.get(0), pkg, context);

        Map<String, BCompiledSymbol> symbolsInFile = getSymbolsInFile(model, line, column, moduleID);

        assertEquals(symbolsInFile.size(), expSymbols);
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }
    }

    @DataProvider(name = "PositionProvider3")
    public Object[][] getPositionsForTypedefs() {
        List<String> moduleLevelSymbols = asList("aString", "anInt", "HELLO", "testAnonTypes", "Person", "PersonObj");
        return new Object[][]{
                {19, 1, 6, moduleLevelSymbols},
//                {23, 21, 6, moduleLevelSymbols}, // TODO: Filter out field symbols
//                {29, 23, 6, moduleLevelSymbols}, // TODO: Filter out field symbols
                {31, 66, 13, getSymbolNames(moduleLevelSymbols, "parent", "pParent", "name", "pName", "age", "pAge",
                                            "self")},
                {40, 9, 10, getSymbolNames(moduleLevelSymbols, "parent", "name", "age", "self")},
                {47, 10, 7, getSymbolNames(moduleLevelSymbols, "x")},
                {49, 20, 7, getSymbolNames(moduleLevelSymbols, "x")},
                // TODO: Fix filtering out 'person'
                {51, 16, 8, getSymbolNames(moduleLevelSymbols, "x", "person")},
//                {51, 17, 9, getSymbolNames(moduleLevelSymbols, "x", "person", "name", "age")},
                {52, 1, 8, getSymbolNames(moduleLevelSymbols, "x", "person")},
//                {53, 18, 11, getSymbolNames(moduleLevelSymbols, "x", "person", "p2", "name", "age")},
        };
    }

    private Map<String, BCompiledSymbol> getSymbolsInFile(SemanticModel model, int line, int column,
                                                          ModuleID moduleID) {
        List<BCompiledSymbol> allInScopeSymbols = model.lookupSymbols(LinePosition.from(line, column));
        return allInScopeSymbols.stream()
                .filter(s -> s.moduleID().equals(moduleID))
                .collect(Collectors.toMap(BCompiledSymbol::name, s -> s));
    }

    private CompileResult compile(String path, CompilerContext context) {
        Path sourcePath = Paths.get(path);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }

    private List<String> getSymbolNames(List<String> mainList, String... args) {
        return Stream.concat(mainList.stream(), Stream.of(args)).collect(Collectors.toList());
    }
}
