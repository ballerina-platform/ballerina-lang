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

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.OBJECT_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() with field access expressions.
 *
 * @since 2.0.0
 */
public class SymbolsInAccessExprsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_access_exprs_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void testFields(int line, int col, SymbolKind expKind, String expName) {
        assertSymbol(line, col, expKind, expName);
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {18, 15, VARIABLE, "p"},
                {18, 17, RECORD_FIELD, "name"},
                {20, 14, FUNCTION, "getPerson"},
                {20, 26, RECORD_FIELD, "age"},
                {23, 19, VARIABLE, "j1"},
                {23, 22, null, null},
                {23, 24, null, null},
                {26, 9, VARIABLE, "j2"},
                {26, 11, null, null},
                {26, 13, null, null},
                {29, 14, VARIABLE, "p2"},
                {29, 17, RECORD_FIELD, "address"},
                {29, 25, RECORD_FIELD, "city"},
                {31, 4, VARIABLE, "p2"},
                {31, 7, RECORD_FIELD, "address"},
                {34, 8, VARIABLE, "p3"},
                {34, 11, OBJECT_FIELD, "name"},
                {37, 8, VARIABLE, "p4"},
                {37, 11, CLASS_FIELD, "name"},
                {42, 12, VARIABLE, "emp"},
        };
    }

    @Test
    public void testOptionalFieldAccess() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(42, 17));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), RECORD_FIELD);
        assertEquals(symbol.get().getName().get(), "designation");
        assertTrue(((RecordFieldSymbol) symbol.get()).isOptional());

        TypeSymbol type = ((RecordFieldSymbol) symbol.get()).typeDescriptor();
        assertEquals(type.typeKind(), STRING);
    }

    @Test(dataProvider = "XMLAccessPosProvider")
    public void testXMLAttributeAccess(int line, int col, SymbolKind expKind, String expName) {
        assertSymbol(line, col, expKind, expName);
    }

    @DataProvider(name = "XMLAccessPosProvider")
    public Object[][] getXMLAccessPos() {
        return new Object[][]{
                {47, 23, VARIABLE, "x"},
                {47, 26, null, null},
//                {51, 12, XMLNS, "ns"}, TODO: https://github.com/ballerina-platform/ballerina-lang/issues/32800
                {51, 15, null, null},
                {53, 27, VARIABLE, "x"},
                {53, 30, null, null},
        };
    }

    @Test(dataProvider = "MemberAccessPosProvider")
    public void testMemberAccess(int line, int col, SymbolKind expKind, String expName) {
        assertSymbol(line, col, expKind, expName);
    }

    @DataProvider(name = "MemberAccessPosProvider")
    public Object[][] getMemberAccessPos() {
        return new Object[][]{
                {58, 12, VARIABLE, "s"},
                {58, 14, CONSTANT, "indx"},
                {59, 10, null, null},
                {62, 8, VARIABLE, "xm"},
                {62, 11, null, null},
                {63, 11, CONSTANT, "indx"},
                {66, 8, VARIABLE, "arr"},
                {66, 12, CONSTANT, "indx"},
                {69, 14, VARIABLE, "p"},
                {69, 17, RECORD_FIELD, "age"},
        };
    }

    // utils
    private void assertSymbol(int line, int col, SymbolKind expKind, String expName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (expKind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), expKind);
        assertEquals(symbol.get().getName().get(), expName);
    }
}
