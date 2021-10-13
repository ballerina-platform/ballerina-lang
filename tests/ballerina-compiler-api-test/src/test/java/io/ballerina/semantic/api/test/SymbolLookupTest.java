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
import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.compiler.api.symbols.DiagnosticState.REDECLARED;
import static io.ballerina.compiler.api.symbols.DiagnosticState.UNKNOWN_TYPE;
import static io.ballerina.compiler.api.symbols.DiagnosticState.VALID;
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
        List<String> moduleLevelSymbols = asList("aString", "anInt", "test", "exprBodyScope");
        return new Object[][]{
                {20, 12, getSymbolNames(moduleLevelSymbols, "b")},
                {20, 16, getSymbolNames(moduleLevelSymbols, "b")},
                {20, 27, getSymbolNames(moduleLevelSymbols, "b", "x")},
                {20, 35, getSymbolNames(moduleLevelSymbols, "b", "x", "z")},
                {20, 42, getSymbolNames(moduleLevelSymbols, "b", "x", "z")},
                {22, 50, getSymbolNames(moduleLevelSymbols, "b", "strTemp")},
                {24, 53, getSymbolNames(moduleLevelSymbols, "b", "strTemp", "rawTemp")},
                {27, 56, getSymbolNames(moduleLevelSymbols, "myStr")},
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

    @Test
    public void testSymbolLookupInQuery() {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_in_query.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsForWhere = getSymbolsInFile(model, srcFile, 22, 21, moduleID);
        assertList(symbolsForWhere, Arrays.asList("test", "arr1", "arr2", "i", "j", "res1", "Person"));

        Map<String, Symbol> symbolsForSelect = getSymbolsInFile(model, srcFile, 28, 21, moduleID);
        assertList(symbolsForSelect, Arrays.asList("intVal", "test", "res1", "stringVal", "res2", "i", "j", "arr2",
                "arr1", "Person"));

        Map<String, Symbol> symbolsForQueryAct = getSymbolsInFile(model, srcFile, 31, 20, moduleID);
        assertList(symbolsForQueryAct, Arrays.asList("ii", "test", "res1", "res3", "res2", "arr2", "arr1", "Person"));

        Map<String, Symbol> symbolsForJoinLHS = getSymbolsInFile(model, srcFile, 36, 20, moduleID);
        assertList(symbolsForJoinLHS, Arrays.asList("test", "res1", "res3", "res2", "i", "arr2", "arr1", "Person"));
 
        Map<String, Symbol> symbolsForJoinRHS = getSymbolsInFile(model, srcFile, 36, 28, moduleID);
        assertList(symbolsForJoinRHS, Arrays.asList("test", "res1", "res3", "res2", "j", "arr2", "arr1", "Person"));

        Map<String, Symbol> symbolsForOrderBy = getSymbolsInFile(model, srcFile, 44, 25, moduleID);
        assertList(symbolsForOrderBy, Arrays.asList("res5", "p", "res4", "p1", "p2", "personList", "test", "res1",
                "res3", "res2", "arr2", "arr1", "Person"));

        Map<String, Symbol> symbolsForNestedFrom1 = getSymbolsInFile(model, srcFile, 48, 42, moduleID);
        assertList(symbolsForNestedFrom1, Arrays.asList("res5", "res4", "p1", "p2", "personList", "res6", "test",
                "res1", "res3", "res2", "k", "arr2", "arr1", "Person"));
 
        Map<String, Symbol> symbolsForNestedFrom2 = getSymbolsInFile(model, srcFile, 49, 23, moduleID);
        assertList(symbolsForNestedFrom2, Arrays.asList("res5", "res4", "p1", "p2", "personList", "res6", "test",
                "res1", "res3", "res2", "i", "arr2", "arr1", "Person"));
 
        Map<String, Symbol> symbolsForNestedFrom3 = getSymbolsInFile(model, srcFile, 53, 58, moduleID);
        assertList(symbolsForNestedFrom3, Arrays.asList("res5", "res4", "p1", "res7", "p2", "personList", "res6",
                "test", "res1", "res3", "res2", "m", "arr2", "arr1", "Person"));

        Map<String, Symbol> symbolsForNestedFrom4 = getSymbolsInFile(model, srcFile, 53, 64, moduleID);
        assertList(symbolsForNestedFrom4, Arrays.asList("res5", "ii", "res4", "p1", "p2", "personList", "res6",
                "test", "res1", "res3", "res2", "arr2", "arr1", "Person"));
    }

    @Test
    public void testSymbolLookupWithAnnotationOnFunction() {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_annotation_on_function.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        List<Symbol> symbolList = model.visibleSymbols(srcFile, LinePosition.from(24, 5)).stream()
                .filter(s -> s.getModule().get().id().equals(moduleID)).collect(Collectors.toList());
        List<String> symbolStringList = symbolList.stream().map(this::createSymbolString).collect(Collectors.toList());

        List<String> expectedNameList = asList("SimpleRecordTYPE_DEFINITION", "func1ANNOTATION", "func1FUNCTION");

        assert symbolStringList.containsAll(expectedNameList);
    }

    @Test
    public void testRedeclaredSymbolLookup() {
        Project project = BCompileUtil.loadProject("test-src/errored_symbol_lookup_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        List<Symbol> allInScopeSymbols = model.visibleSymbols(srcFile, LinePosition.from(23, 8));
        List<Symbol> symbols = new ArrayList<>();
        for (Symbol symbol : allInScopeSymbols) {
            if (symbol.getModule().get().id().equals(moduleID)) {
                symbols.add(symbol);
                switch (symbol.kind()) {
                    case VARIABLE:
                        assertErroredSymbols(symbol);
                        break;
                    case PARAMETER:
                        assertEquals(symbol.getName().get(), "b");
                        assertEquals(((ParameterSymbol) symbol).typeDescriptor().typeKind(), TypeDescKind.INT);
                        break;
                    case FUNCTION:
                        assertEquals(symbol.getName().get(), "test");
                        break;
                    default:
                        throw new AssertionError("Unexpected symbol kind: " + symbol.kind());

                }
            }
        }
    }

    private void assertErroredSymbols(Symbol symbol) {
        assertEquals(symbol.kind(), SymbolKind.VARIABLE);
        assertEquals(symbol.getName().get(), "b");
        assertEquals(((VariableSymbol) symbol).typeDescriptor().typeKind(), TypeDescKind.COMPILATION_ERROR);
        assertEquals(((VariableSymbol) symbol).diagnosticState(), REDECLARED);
    }

    @Test
    public void testRedeclaredSymbolLookup2() {
        Project project = BCompileUtil.loadProject("test-src/errored_symbol_lookup_test_2.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        List<Symbol> allInScopeSymbols = model.visibleSymbols(srcFile, LinePosition.from(25, 8), VALID, REDECLARED,
                                                              UNKNOWN_TYPE);
        List<Symbol> symbols = new ArrayList<>();
        for (Symbol symbol : allInScopeSymbols) {
            if (symbol.getModule().get().id().equals(moduleID) && symbol.kind() == SymbolKind.VARIABLE) {
                symbols.add(symbol);
            }
        }

        assertEquals(symbols.size(), 4);

        for (Symbol symbol : symbols) {
            switch (symbol.getName().get()) {
                case "b":
                    assertVarSymbol(symbol, "b", REDECLARED);
                    break;
                case "p":
                    assertVarSymbol(symbol, "p", UNKNOWN_TYPE);
                    break;
                case "x":
                    assertVarSymbol(symbol, "x", UNKNOWN_TYPE);
                    break;
                default:
                    throw new AssertionError("Unexpected symbol: " + symbol.getName().get());
            }
        }
    }

    private void assertVarSymbol(Symbol symbol, String name, DiagnosticState state) {
        assertEquals(symbol.kind(), SymbolKind.VARIABLE);
        assertEquals(symbol.getName().get(), name);
        assertEquals(((VariableSymbol) symbol).typeDescriptor().typeKind(), TypeDescKind.COMPILATION_ERROR);
        assertEquals(((VariableSymbol) symbol).diagnosticState(), state);
    }

    @Test
    public void testDestructureStmts() {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_destructure_var_exclusion_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);
        List<String> expSymbolNames = List.of("personName", "personAge", "BasicErrorDetail", "rest", "s", "test", "f",
                                              "detail1", "i", "Person", "message1", "UserDefinedError");

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, 22, 4, moduleID);

        assertEquals(symbolsInFile.size(), expSymbolNames.size());
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }
    }

    @Test
    public void testObjectConstructorExpr() {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_in_object_constructor.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);
        List<String> expSymbolNames = List.of("test", "f1", "foo", "self", "a");

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, 21, 20, moduleID);

        assertEquals(symbolsInFile.size(), expSymbolNames.size());
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }
    }

    @Test(dataProvider = "FieldSymbolPosProvider")
    public void testSymbolLookupInFields(int line, int column, int expSymbols, List<String> expSymbolNames) {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_in_fields.bal");
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

    @DataProvider(name = "FieldSymbolPosProvider")
    public Object[][] getFieldSymbolPositions() {
        List<String> moduleSymbols = List.of("Foo", "Bar", "Person", "PersonObj");
        return new Object[][]{
                {18, 4, 4, moduleSymbols},
                {24, 4, 4, moduleSymbols},
                {32, 8, 10, concatSymbols(moduleSymbols, "init", "inc", "self", "x")},
                {38, 4, 4, moduleSymbols},
                {43, 4, 4, moduleSymbols},
        };
    }

    @Test(dataProvider = "PositionProvider6")
    public void testTypeTest(int line, int col, List<String> expSymbolNames, TypeDescKind expVarType) {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_type_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        Document srcFile = getDocumentForSingleSource(project);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, line, col, moduleID);

        assertEquals(symbolsInFile.size(), expSymbolNames.size());
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }

        Symbol getResultSym = symbolsInFile.get("getResult");

        assertEquals(getResultSym.kind(), SymbolKind.VARIABLE);
        assertEquals(((VariableSymbol) getResultSym).typeDescriptor().typeKind(), expVarType);
    }

    @DataProvider(name = "PositionProvider6")
    public Object[][] getPosForTypeTest() {
        List<String> expSymbolNames = List.of("getValue", "testTypeTest", "getResult");
        return new Object[][]{
                {18, 20, expSymbolNames, TypeDescKind.INT},
                {19, 8, expSymbolNames, TypeDescKind.COMPILATION_ERROR},
                {22, 24, expSymbolNames, TypeDescKind.INT},
                {23, 8, expSymbolNames, TypeDescKind.COMPILATION_ERROR},
        };
    }

    private String createSymbolString(Symbol symbol) {
        return (symbol.getName().isPresent() ? symbol.getName().get() : "") + symbol.kind();
    }

    private List<String> concatSymbols(List<String> moduleSymbols, String... symbols) {
        return Stream.concat(moduleSymbols.stream(), Arrays.stream(symbols)).collect(Collectors.toList());
    }
}
