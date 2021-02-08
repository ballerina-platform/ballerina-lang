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
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
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

    @Test(dataProvider = "PositionProvider1")
    public void testVarSymbolLookup(int line, int column, int expSymbols, List<String> expSymbolNames) {
        Project project = BCompileUtil.loadProject("test-src/var_symbol_lookup_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, line, column, moduleID);

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
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_workers_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, line, column, moduleID);

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
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_typedefs_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, line, column, moduleID);

        assertEquals(symbolsInFile.size(), expSymbols);
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }
    }

    @DataProvider(name = "PositionProvider3")
    public Object[][] getPositionsForTypedefs() {
        List<String> moduleLevelSymbols = asList("aString", "anInt", "HELLO", "testAnonTypes", "Person", "PersonObj",
                                                 "Colour", "RED", "GREEN", "BLUE");
        return new Object[][]{
                {18, 0, 10, moduleLevelSymbols},
//                {22, 20, 6, moduleLevelSymbols}, // TODO: Filter out field symbols
//                {28, 22, 6, moduleLevelSymbols}, // TODO: Filter out field symbols
                {30, 65, 18, getSymbolNames(moduleLevelSymbols, "parent", "pParent", "name", "pName", "age", "pAge",
                                            "self", "init")},
                {39, 8, 17, getSymbolNames(moduleLevelSymbols, "parent", "name", "age", "self", "getAge", "getName",
                                           "init")},
                {46, 9, 11, getSymbolNames(moduleLevelSymbols, "x")},
                {48, 19, 11, getSymbolNames(moduleLevelSymbols, "x")},
                // TODO: Fix filtering out 'person'
                {50, 15, 12, getSymbolNames(moduleLevelSymbols, "x", "person")},
//                {50, 16, 9, getSymbolNames(moduleLevelSymbols, "x", "person", "name", "age")},
                {51, 0, 12, getSymbolNames(moduleLevelSymbols, "x", "person")},
//                {52, 17, 11, getSymbolNames(moduleLevelSymbols, "x", "person", "p2", "name", "age")},
        };
    }

    @Test(dataProvider = "PositionProvider4")
    public void testSymbolLookupForComplexExpressions(int line, int column, List<String> expSymbolNames) {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_exprs_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, line, column, moduleID);
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
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_in_assignment.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, 18, 9, moduleID);
        assertList(symbolsInFile, Arrays.asList("test", "v1"));
    }

    @Test
    public void testMissingNodeFiltering() {
        Project project = BCompileUtil.loadProject("test-src/missing_node_filtering_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, 19, 4, moduleID);
        assertList(symbolsInFile, Arrays.asList("test", "x"));
    }

    @Test
    public void testSymbolLookupInOnFail() {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_in_onfail.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInDo = getSymbolsInFile(model, srcFile, 20, 21, moduleID);
        assertList(symbolsInDo, Arrays.asList("test", "e1", "str"));

        Map<String, Symbol> symbolsInTrx = getSymbolsInFile(model, srcFile, 24, 18, moduleID);
        assertList(symbolsInTrx, Arrays.asList("test", "res"));

        Map<String, Symbol> symbolsInTrxOnFail = getSymbolsInFile(model, srcFile, 26, 21, moduleID);
        assertList(symbolsInTrxOnFail, Arrays.asList("test", "e2", "str"));
    }
}
