/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.expressions;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolsInFile;
import static io.ballerina.tools.text.LinePosition.from;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the checking the query expression type.
 *
 */
public class QueryExpressionTest {

    private Project project;
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        project = BCompileUtil.loadProject("test-src/expressions/query_expression_tests.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "visibleSymbolPosProvider")
    public void testVisibleSymbol(int line, int col, int expSymbols, List<String> expSymbolNames) {

        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);

        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        ModuleID moduleID = new BallerinaModuleID(pkg.packageID);

        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, line, col, moduleID);

        assertEquals(symbolsInFile.size(), expSymbols);
        for (String symName : expSymbolNames) {
            assertTrue(symbolsInFile.containsKey(symName), "Symbol not found: " + symName);
        }
    }

    @Test(dataProvider = "SymbolPosProvider")
    public void testSymbol(int line, int col, String name) {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(line, col)).get();
        assertEquals(symbol.getName().get(), name);
    }

    @Test(dataProvider = "TypeSymbolPosProvider")
    public void testType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        Optional<TypeSymbol> typeSymbol = model.type(LineRange.from(srcFile.name(),
                from(sLine, sCol), from(eLine, eCol)));
        assertEquals(typeSymbol.get().typeKind(), kind);
    }

    @DataProvider(name = "SymbolPosProvider")
    public Object[][] getSymbolPos() {
        return new Object[][]{
                {28, 30, "s2"},
                {30, 21, "st"},
                {28, 14, "students"},
                {31, 17, "fname"},
                {37, 43, "lname"},
                {41, 43, "students"},
                {44, 67, "st"}
        };
    }

    @DataProvider(name = "TypeSymbolPosProvider")
    public Object[][] getTypeSymbolPos() {
        return new Object[][]{
                {28, 30, 28, 32, TYPE_REFERENCE},
                {30, 21, 30, 23, TYPE_REFERENCE},
                {24, 24, 24, 29, STRING},
                {26, 54, 26, 56, INT},
                {35, 43, 35, 48, STRING},
                {42, 39, 42, 41, TYPE_REFERENCE},
                {44, 14, 44, 18, ARRAY}
        };
    }

    @DataProvider(name = "visibleSymbolPosProvider")
    public Object[][] getVisibleSymbolsAtPos() {
        return new Object[][]{
                {20, 14, 2, asList("testQueryExpression", "Student")},
                {29, 14, 6, asList("testQueryExpression", "Student", "s1", "s2", "s3",
                        "students")},
                {40, 14, 8, asList("testQueryExpression", "Student", "s1", "s2", "s3",
                        "students", "fullName", "x")},
                {46, 14, 10, asList("testQueryExpression", "Student", "s1", "s2", "s3",
                        "students", "fullName", "x", "gpaRanking", "selectedStudents")},
        };
    }
}
