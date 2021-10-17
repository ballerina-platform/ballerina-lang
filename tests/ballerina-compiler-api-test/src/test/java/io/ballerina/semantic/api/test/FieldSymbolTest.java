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
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for record/object/class field symbols.
 *
 * @since 2.0.0
 */
public class FieldSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/field_symbols_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "RecordFieldPos")
    public void testRecordFields(int line, int col, String fieldName, TypeDescKind typeKind, boolean isOptional,
                                 boolean hasDefaultValue, String signature) {
        Symbol symbol = getSymbol(line, col);
        assertEquals(symbol.kind(), SymbolKind.RECORD_FIELD);

        RecordFieldSymbol fieldSymbol = (RecordFieldSymbol) symbol;
        assertEquals(fieldSymbol.getName().get(), fieldName);
        assertEquals(fieldSymbol.typeDescriptor().typeKind(), typeKind);
        assertEquals(fieldSymbol.isOptional(), isOptional);
        assertEquals(fieldSymbol.hasDefaultValue(), hasDefaultValue);
        assertEquals(fieldSymbol.signature(), signature);
    }

    @DataProvider(name = "RecordFieldPos")
    public Object[][] getRecordFieldPos() {
        return new Object[][]{
                {17, 20, "name", STRING, false, false, "readonly string name"},
                {18, 8, "age", INT, false, true, "int age"},
                {19, 11, "location", STRING, true, false, "string location?"},
        };
    }

    @Test(dataProvider = "ObjectFieldPos")
    public void testObjectFields(int line, int col, String fieldName, TypeDescKind typeKind, String signature) {
        Symbol symbol = getSymbol(line, col);
        assertEquals(symbol.kind(), SymbolKind.OBJECT_FIELD);

        ObjectFieldSymbol fieldSymbol = (ObjectFieldSymbol) symbol;
        assertEquals(fieldSymbol.getName().get(), fieldName);
        assertEquals(fieldSymbol.typeDescriptor().typeKind(), typeKind);
        assertEquals(fieldSymbol.signature(), signature);
    }

    @DataProvider(name = "ObjectFieldPos")
    public Object[][] getObjectFieldPos() {
        return new Object[][]{
                {23, 18, "fname", STRING, "public string fname"},
                {24, 11, "lname", STRING, "string lname"},
        };
    }

    @Test(dataProvider = "ClassFieldPos")
    public void testClassFields(int line, int col, String fieldName, TypeDescKind typeKind, boolean hasDefaultValue,
                                String signature) {
        Symbol symbol = getSymbol(line, col);
        assertEquals(symbol.kind(), SymbolKind.CLASS_FIELD);

        ClassFieldSymbol fieldSymbol = (ClassFieldSymbol) symbol;
        assertEquals(fieldSymbol.getName().get(), fieldName);
        assertEquals(fieldSymbol.typeDescriptor().typeKind(), typeKind);
//        assertEquals(fieldSymbol.hasDefaultValue(), hasDefaultValue);
        assertEquals(fieldSymbol.signature(), signature);
    }

    @DataProvider(name = "ClassFieldPos")
    public Object[][] getClassFieldPos() {
        return new Object[][]{
                {30, 24, "fName", STRING, true, "public final string fName"},
                {31, 19, "lName", STRING, false, "private string lName"},
                {32, 8, "age", INT, true, "int age"},
        };
    }

    @Test
    public void testRecordFieldSymbolPos() {
        Symbol symbol = getSymbol(51, 14);
        assertEquals(symbol.kind(), SymbolKind.RECORD_FIELD);

        RecordFieldSymbol fieldSymbol = (RecordFieldSymbol) symbol;
        assertTrue(fieldSymbol.getLocation().isPresent());

        Location symbolPos = fieldSymbol.getLocation().get();
        assertEquals(symbolPos.lineRange().startLine().line(), 45);
        assertEquals(symbolPos.lineRange().startLine().offset(), 12);
        assertEquals(symbolPos.lineRange().endLine().line(), 45);
        assertEquals(symbolPos.lineRange().endLine().offset(), 17);
    }

    private Symbol getSymbol(int line, int col) {
        return model.symbol(srcFile, from(line, col))
                .orElseThrow(() -> new AssertionError("Expected a symbol at: (" + line + ", " + col + ")"));
    }
}
