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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ANY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for binding patterns.
 *
 * @since 2.0.0
 */
public class BindingPatternTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/binding_patterns_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "CBPPosProvider")
    public void testCaptureBindingPattern(int line, int col, String name, TypeDescKind typeKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().getName().get(), name);
        assertEquals(((VariableSymbol) symbol.get()).typeDescriptor().typeKind(), typeKind);
    }

    @DataProvider(name = "CBPPosProvider")
    public Object[][] getCBPPos() {
        return new Object[][]{
                {18, 8, "cbp1", INT},
                {19, 8, "cbp2", STRING}
        };
    }

    @Test
    public void testWildcardBindingPattern() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(22, 4));
        assertTrue(symbol.isEmpty());

        Optional<TypeSymbol> type = model.type(LineRange.from(srcFile.name(), from(22, 4), from(22, 5)));
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), ANY);
    }

    @Test(dataProvider = "PosProvider")
    public void testListMappingErrorBindingPatterns(int line, int col, String name, TypeDescKind typeKind,
                                                    TypeDescKind constraintKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().getName().get(), name);

        TypeSymbol type = ((VariableSymbol) symbol.get()).typeDescriptor();
        assertEquals(type.typeKind(), typeKind);

        if (constraintKind != null) {
            switch (type.typeKind()) {
                case ARRAY:
                    assertEquals(((ArrayTypeSymbol) type).memberTypeDescriptor().typeKind(), constraintKind);
                    break;
                case MAP:
                    assertEquals(((MapTypeSymbol) type).typeParameter().get().typeKind(), constraintKind);
                    assertEquals(((MapTypeSymbol) type).typeParam().typeKind(), constraintKind);
                    break;
            }
        }
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {28, 5, "lbp1", INT, null},
                {28, 11, "lbp2", FLOAT, null},
                {28, 20, "rbp1", ARRAY, UNION},
                {29, 29, "lbp3", INT, null},
                {29, 35, "lbp4", FLOAT, null},
                {29, 44, "rbp2", ARRAY, STRING},
                {35, 11, "mbp1", STRING, null},
                {35, 17, "mbp2", INT, null},
                {35, 26, "rbp3", MAP, STRING},
                {35, 26, "rbp3", MAP, null},
                {36, 42, "mbp3", STRING, null},
                {36, 48, "age", INT, null},
                {36, 56, "rbp4", RECORD, ANYDATA},
                {44, 16, "msg", STRING, null},
                {44, 21, "cause", ERROR, null},
                {44, 33, "code", INT, null},
                {44, 42, "rbp5", MAP, STRING},
                {45, 22, "msg1", STRING, null},
                {45, 28, "cause1", UNION, null},
                {45, 41, "code1", INT, null},
                {45, 51, "rbp6", MAP, ANYDATA},
        };
    }

    @Test(dataProvider = "ExprPosProvider")
    public void testExprOfBindingPatterns(int line, int col, SymbolKind expKind, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), expKind);
        assertEquals(symbol.get().getName().get(), name);
    }

    @DataProvider(name = "ExprPosProvider")
    public Object[][] getExprPos() {
        return new Object[][]{
                {29, 53, SymbolKind.VARIABLE, "cbp1"},
                {37, 51, SymbolKind.RECORD_FIELD, "name"},
                {45, 65, SymbolKind.TYPE, "Error"},
        };
    }

    @Test
    public void testListBindingPatternPos() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(29, 5));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE);
        assertEquals(((TypeSymbol) symbol.get()).typeKind(), INT);
    }

    @Test(dataProvider = "PosProvider2")
    public void testTypeOfWithinBindingPatterns(int line, int sCol, int eCol, TypeDescKind expTypeKind) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("binding_patterns_test.bal", from(line, sCol), from(line, eCol)));

        if (expTypeKind == null) {
            assertTrue(type.isEmpty());
            return;
        }

        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), expTypeKind);
    }

    @DataProvider(name = "PosProvider2")
    public Object[][] getPos2() {
        return new Object[][]{
                {28, 11, 15, FLOAT},
                {28, 20, 24, ARRAY},
                {29, 29, 33, null},
                {29, 44, 48, null},
                {29, 53, 57, INT},
                {35, 5, 9, null},
                {35, 11, 15, STRING},
                {35, 17, 21, INT},
                {35, 26, 30, MAP},
                {36, 42, 46, null},
                {36, 48, 51, null},
                {36, 56, 60, null},
                {44, 16, 19, STRING},
                {44, 28, 32, null},
                {44, 33, 37, INT},
                {44, 42, 46, MAP},
                {45, 22, 26, null},
                {45, 41, 46, null},
                {45, 51, 55, null},
        };
    }
}
