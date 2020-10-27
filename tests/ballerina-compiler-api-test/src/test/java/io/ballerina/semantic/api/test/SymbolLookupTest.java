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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
                {2, 13, 4, moduleLevelSymbols},
                {19, 17, 4, moduleLevelSymbols},
//                {20, 30, 4, moduleLevelSymbols}, // TODO: Feature not yet supported - issue #25607
                {20, 38, 5, asList("aString", "anInt", "test", "HELLO", "greet")},
                {21, 0, 5, asList("aString", "anInt", "test", "HELLO", "greet")},
                // TODO: issue #25607
//                {22, 59, 6, asList("aString", "anInt", "test", "HELLO", "greet", "name")},
                {30, 12, 8, getSymbolNames(moduleLevelSymbols, "greet", "a", "x", "greetFn")},
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
                {19, 50, 6, getSymbolNames(moduleLevelSymbols, "w1", "w2")},
                {21, 15, 6, getSymbolNames(moduleLevelSymbols, "w1", "w2")},
                {25, 0, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "i")},
                {23, 14, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "i")},
                {30, 12, 6, getSymbolNames(moduleLevelSymbols, "w1", "w2")},
                {34, 12, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "j")},
                {39, 22, 7, getSymbolNames(moduleLevelSymbols, "w1", "w2", "ret")},
                {43, 0, 4, moduleLevelSymbols},
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
                {18, 0, 6, moduleLevelSymbols},
//                {22, 20, 6, moduleLevelSymbols}, // TODO: Filter out field symbols
//                {28, 22, 6, moduleLevelSymbols}, // TODO: Filter out field symbols
                {30, 65, 14, getSymbolNames(moduleLevelSymbols, "parent", "pParent", "name", "pName", "age", "pAge",
                                            "self", "PersonObj.init")},
                {39, 8, 13, getSymbolNames(moduleLevelSymbols, "parent", "name", "age", "self",
                                            "PersonObj.getAge", "PersonObj.getName", "PersonObj.init")},
                {46, 9, 7, getSymbolNames(moduleLevelSymbols, "x")},
                {48, 19, 7, getSymbolNames(moduleLevelSymbols, "x")},
                // TODO: Fix filtering out 'person'
                {50, 15, 8, getSymbolNames(moduleLevelSymbols, "x", "person")},
//                {50, 16, 9, getSymbolNames(moduleLevelSymbols, "x", "person", "name", "age")},
                {51, 0, 8, getSymbolNames(moduleLevelSymbols, "x", "person")},
//                {52, 17, 11, getSymbolNames(moduleLevelSymbols, "x", "person", "p2", "name", "age")},
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
                {20, 12, getSymbolNames(moduleLevelSymbols, "b")},
                {20, 16, getSymbolNames(moduleLevelSymbols, "b")},
                {20, 27, getSymbolNames(moduleLevelSymbols, "b", "x")},
                {20, 35, getSymbolNames(moduleLevelSymbols, "b", "x", "z")},
                {20, 42, getSymbolNames(moduleLevelSymbols, "b", "x", "z")},
                {22, 50, getSymbolNames(moduleLevelSymbols, "b", "strTemp")},
                {24, 53, getSymbolNames(moduleLevelSymbols, "b", "strTemp", "rawTemp")},
        };
    }

    @Test
    public void testSymbolLookupInFollowingLine() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_lookup_in_assignment.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, "symbol_lookup_in_assignment.bal", 18, 9,
                                                             moduleID);
        assertList(symbolsInFile, Arrays.asList("test", "v1"));
    }

    @Test
    public void testMissingNodeFiltering() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/missing_node_filtering_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, "missing_node_filtering_test.bal", 19, 4,
                                                             moduleID);
        assertList(symbolsInFile, Arrays.asList("test", "x"));
    }

    private CompileResult compile(String path, CompilerContext context) {
        Path sourcePath = Paths.get(path);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return BCompileUtil.compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, false);
    }
}
