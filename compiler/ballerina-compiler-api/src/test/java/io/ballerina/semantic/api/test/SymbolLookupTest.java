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

import org.ballerina.compiler.api.ModuleID;
import org.ballerina.compiler.api.symbols.Symbol;
import org.ballerina.compiler.impl.BallerinaModuleID;
import org.ballerina.compiler.impl.BallerinaSemanticModel;
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

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolNames;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolsInFile;
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
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, "var_symbol_lookup_test.bal", line, column,
                                                             moduleID);

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
//                {21, 31, 4, moduleLevelSymbols}, // TODO: Feature not yet supported - issue #25607
                {21, 39, 5, asList("aString", "anInt", "test", "HELLO", "greet")},
                {22, 1, 5, asList("aString", "anInt", "test", "HELLO", "greet")},
                // TODO: issue #25607
//                {23, 60, 6, asList("aString", "anInt", "test", "HELLO", "greet", "name")},
                {31, 13, 8, getSymbolNames(moduleLevelSymbols, "greet", "a", "x", "greetFn")},
        };
    }

    @Test(dataProvider = "PositionProvider2")
    public void testVarSymbolLookupInWorkers(int line, int column, int expSymbols, List<String> expSymbolNames) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_with_workers_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, "symbol_lookup_with_workers_test.bal", line, column,
                                                             moduleID);

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
                {26, 1, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "i")},
                {24, 15, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "i")},
                {31, 13, 6, getSymbolNames(moduleLevelSymbols, "w1", "w2")},
                {35, 13, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "j")},
                {40, 23, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "ret")},
                {44, 1, 4, moduleLevelSymbols},
        };
    }

    @Test(dataProvider = "PositionProvider3")
    public void testVarSymbolLookupInTypedefs(int line, int column, int expSymbols, List<String> expSymbolNames) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_with_typedefs_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, "symbol_lookup_with_typedefs_test.bal", line,
                                                             column, moduleID);

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

    @Test(dataProvider = "PositionProvider4")
    public void testSymbolLookupForComplexExpressions(int line, int column, List<String> expSymbolNames) {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_with_exprs_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, "symbol_lookup_with_exprs_test.bal", line, column,
                                                             moduleID);
        assertList(symbolsInFile, expSymbolNames);
    }

    @DataProvider(name = "PositionProvider4")
    public Object[][] getPositionsForExprs() {
        List<String> moduleLevelSymbols = asList("aString", "anInt", "test");
        return new Object[][]{
                {21, 13, getSymbolNames(moduleLevelSymbols, "b")},
                {21, 17, getSymbolNames(moduleLevelSymbols, "b")},
                {21, 28, getSymbolNames(moduleLevelSymbols, "b", "x")},
                {21, 36, getSymbolNames(moduleLevelSymbols, "b", "x", "z")},
                {21, 43, getSymbolNames(moduleLevelSymbols, "b", "x", "z")},
                {23, 51, getSymbolNames(moduleLevelSymbols, "b", "strTemp")},
                {25, 54, getSymbolNames(moduleLevelSymbols, "b", "strTemp", "rawTemp")},
        };
    }

    private CompileResult compile(String path, CompilerContext context) {
        Path sourcePath = Paths.get(path);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }
}
