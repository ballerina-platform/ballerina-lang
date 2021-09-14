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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for looking up symbols within an expression.
 */
public class SymbolsWithinExprTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_within_expr_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "LiteralPosProvider")
    public void testLiterals(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isEmpty());
    }

    @DataProvider(name = "LiteralPosProvider")
    public Object[][] getLiteralPos() {
        return new Object[][]{
                {17, 13},
                {18, 16},
                {19, 12},
                {20, 14},
                {21, 15},
                {22, 17},
        };
    }

    @Test(dataProvider = "TemplateExprPosProvider")
    public void testTemplateExprs(int line, int col, boolean symbolPresent) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertEquals(symbol.isPresent(), symbolPresent);

        if (!symbolPresent) {
            return;
        }

        assertEquals(symbol.get().kind(), VARIABLE);
        assertEquals(symbol.get().getName().get(), "i");
        assertEquals(((VariableSymbol) symbol.get()).typeDescriptor().typeKind(), TypeDescKind.INT);
    }

    @DataProvider(name = "TemplateExprPosProvider")
    public Object[][] getTemplateExprPos() {
        return new Object[][]{
                {27, 14, false},
                {27, 25, false},
                {27, 29, true},
                {28, 12, false},
                {28, 20, false},
                {28, 24, true},
                {29, 35, false},
                {29, 36, true},
        };
    }

    @Test(dataProvider = "StructuralConstructorPosProvider")
    public void testStructuralConstructors(int line, int col, SymbolKind expKind, String expName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (expKind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        assertEquals(symbol.get().kind(), expKind);
        assertEquals(symbol.get().getName().get(), expName);
    }

    @DataProvider(name = "StructuralConstructorPosProvider")
    public Object[][] getStructuralConstructorPos() {
        return new Object[][]{
                // List constructor
                {33, 21, null, null},
                {33, 22, VARIABLE, "hundred"},
                {33, 31, CONSTANT, "PI"},
                {34, 12, null, null},
                {35, 15, VARIABLE, "greet"},
                {35, 22, CONSTANT, "PI"},
                {35, 26, null, null},

                // Mapping constructor
                {37, 23, null, null},
                {37, 26, null, null},
                {37, 30, null, null},
                {37, 35, CONSTANT, "PI"},
                {38, 19, RECORD_FIELD, "x"},
                {38, 22, VARIABLE, "hundred"},
                {38, 31, RECORD_FIELD, "y"},
                {38, 36, VARIABLE, "greet"},
                {38, 44, VARIABLE, "greet"},
                {38, 52, null, null},
                {38, 65, VARIABLE, "greet"},
                {38, 72, null, null},
                {38, 78, CONSTANT, "PI"},
                {40, 19, VARIABLE, "y"},
                {40, 25, VARIABLE, "m1"},

                // Table constructor
                {42, 33, null, null},
//                {42, 43, RECORD_FIELD, "id"}, TODO: https://github.com/ballerina-platform/ballerina-lang/issues/32705
                {43, 9, RECORD_FIELD, "id"},
                {48, 25, null, null},
                {49, 19, RECORD_FIELD, "name"},
//                {50, 12, VARIABLE, "m"}, TODO: https://github.com/ballerina-platform/ballerina-lang/issues/32707
        };
    }

    @Test(dataProvider = "NewExprPosProvider")
    public void testNewExprs(int line, int col, SymbolKind expKind, String expName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (expKind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        assertEquals(symbol.get().kind(), expKind);

        if (expName == null) {
            assertTrue(symbol.get().getName().isEmpty());
            return;
        }
        
        assertEquals(symbol.get().getName().get(), expName);
    }

    @DataProvider
    public static Object[][] NewExprPosProvider() {
        return new Object[][]{
                {57, 18, null, null},
                {57, 22, VARIABLE, "name"},
                {58, 8, null, null},
                {58, 12, TYPE, "PersonClz"},
                {58, 22, VARIABLE, "name"},
                {65, 13, null, null},
                {65, 17, TYPE, null},
                {65, 24, TYPE, "FooRec"},
                {65, 32, VARIABLE, "si"},
        };
    }
}
