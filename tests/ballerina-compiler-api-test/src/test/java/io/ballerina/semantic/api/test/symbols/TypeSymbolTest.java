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
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ANY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.HANDLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.JSON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NEVER;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.READONLY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.SINGLETON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for type symbols.
 *
 * @since 2.0.0
 */
public class TypeSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/type_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void testTypeSymbolLookup(int line, int col, TypeDescKind typeKind, String signature) {
        TypeSymbol type = assertBasicsAndGetType(line, col, typeKind, signature);
        assertTrue(type.getName().isEmpty());
        assertTrue(type.getLocation().isEmpty());
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getTypePosition() {
        return new Object[][]{
                {17, 4, INT, "int"},
                {18, 4, BOOLEAN, "boolean"},
                {19, 4, FLOAT, "float"},
                {20, 4, DECIMAL, "decimal"},
                {21, 4, NIL, "()"},
                {22, 4, STRING, "string"},
                {23, 4, INT, "int"},
//                {26, 4, TypeDescKind.XML, "xml"},
//                {27, 4, TypeDescKind.XML, "xml<xml>"},
//                {28, 4, TypeDescKind.XML, "xml<ballerina/lang.xml:0.8.0:Element>"},
                {31, 4, TYPEDESC, "typedesc<anydata>"},
                {32, 4, TYPEDESC, "typedesc<any|error>"},
                {35, 4, FUTURE, "future<string>"},
                {36, 4, FUTURE, "future<any|error>"},
                {40, 4, INT, "int"},
                {46, 4, ERROR, "error<ErrorData>"},
                {47, 4, HANDLE, "handle"},
                {48, 4, STREAM, "stream<Person, error>"},
                {52, 4, SINGLETON, "10"},
                {54, 4, ANY, "any"},
                {55, 4, NEVER, "never"},
                {56, 4, READONLY, "readonly"},
                {57, 4, INT, "int"},
                {57, 8, STRING, "string"},
                {58, 13, READONLY, "readonly"},
                {59, 4, INT, "int"},
                {60, 4, ANYDATA, "anydata"},
                {61, 4, JSON, "json"},
                {62, 4, BYTE, "byte"},
                {63, 13, ERROR, "error"},
        };
    }

    @Test(dataProvider = "StructuredPosProvider")
    public void testStructuredTypeSymbolLookup(int line, int sCol, int eCol, TypeDescKind typeKind, String signature) {
        TypeSymbol type = assertBasicsAndGetType(line, sCol, typeKind, signature);
        assertTrue(type.getName().isEmpty());

        Optional<Location> location = type.getLocation();
        assertTrue(location.isPresent());
        assertLocationPosition(location.get().lineRange(), line, sCol, eCol);
    }

    @DataProvider(name = "StructuredPosProvider")
    public Object[][] getStructuredTypePosition() {
        return new Object[][]{
                {30, 4, 15, MAP, "map<string>"},
                {33, 4, 17, TABLE, "table<Person>"},
                {34, 4, 26, TABLE, "table<Person> key<int>"}
        };
    }

    @Test(dataProvider = "ConstrainedTypePosProvider")
    public void testConstrainedTypes(int line, int col, TypeDescKind typeKind, String signature) {
        assertBasicsAndGetType(line, col, typeKind, signature);
    }

    @DataProvider(name = "ConstrainedTypePosProvider")
    public Object[][] getConstrainedTypePos() {
        return new Object[][]{
//                {26, 4, XML, "xml"},
//                {27, 4, XML, "xml<xml>"},
//                {28, 4, XML, "xml<ballerina/lang.xml:0.8.0:Element>"},
                {30, 9, STRING, "string"},
                {31, 13, ANYDATA, "anydata"},
                {34, 22, INT, "int"},
                {35, 11, STRING, "string"},
                {48, 19, ERROR, "error"},
        };
    }

    @Test(dataProvider = "TypeRefPosProvider")
    public void testTypeRefs(int line, int col, TypeDescKind typeKind, String name) {
        TypeSymbol type = assertBasicsAndGetType(line, col, typeKind, name);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), name);
    }

    @DataProvider(name = "TypeRefPosProvider")
    public Object[][] getTypeRefPos() {
        return new Object[][]{
                {33, 10, TYPE_REFERENCE, "Person"},
                {34, 10, TYPE_REFERENCE, "Person"},
                {46, 10, TYPE_REFERENCE, "ErrorData"},
                {48, 11, TYPE_REFERENCE, "Person"},
                {58, 4, TYPE_REFERENCE, "Person"},
        };
    }

    @Test(dataProvider = "ConstRefPosProvider")
    public void testConstRef(int line, int col, String expName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.CONSTANT);
        assertEquals(symbol.get().getName().get(), expName);
    }

    @DataProvider(name = "ConstRefPosProvider")
    public Object[][] getConstRefPos() {
        return new Object[][]{
                {41, 8, "TEN"},
                {53, 4, "FOO"},
        };
    }

    @Test
    public void testConstAsAType() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(53, 4));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.CONSTANT);

        ConstantSymbol constant = (ConstantSymbol) symbol.get();
        assertEquals(constant.getName().get(), "FOO");
        assertEquals(constant.typeKind(), SINGLETON);
        assertEquals(constant.typeDescriptor().typeKind(), SINGLETON);
        assertEquals(constant.broaderTypeDescriptor().typeKind(), STRING);
        assertEquals(constant.signature(), "FOO");
    }

    @Test(dataProvider = "TupleTypeMemberPosProvider")
    public void testTupleMemberTypes(int line, int col, TypeDescKind typeKind, String signature) {
        assertBasicsAndGetType(line, col, typeKind, signature);
    }

    @DataProvider(name = "TupleTypeMemberPosProvider")
    public Object[][] getTupleTypePosition() {
        return new Object[][]{
                {42, 5, INT, "int"},
                {42, 10, STRING, "string"}
        };
    }

    @Test(dataProvider = "StructuredTypeSymbolLocationPos")
    public void testStructuredTypeSymbolLocation(int line, int col, int expLine, int expSCol, int expECol) {
        Optional<TypeSymbol> typeSymbol = model.typeOf(
                LineRange.from(srcFile.name(), LinePosition.from(line, 8), LinePosition.from(line, col)));
        assertTrue(typeSymbol.isPresent());

        Optional<Location> location = typeSymbol.get().getLocation();
        assertTrue(location.isPresent());
        assertLocationPosition(location.get().lineRange(), expLine, expSCol, expECol);
    }

    @DataProvider(name = "StructuredTypeSymbolLocationPos")
    public Object[][] getStructuredTypeSymbolLocationPos() {
        return new Object[][]{
                {67, 11, 92, 0, 5},
                {68, 10, 93, 0, 11},
                {69, 13, 94, 0, 13},
                {70, 11, 95, 0, 30},
                {71, 11, 96, 5, 8},
                {72, 11, 98, 0, 10},
                {73, 14, 99, 0, 17},
                {74, 14, 100, 0, 25}
        };
    }

    // private utils
    private TypeSymbol assertBasicsAndGetType(int line, int col, TypeDescKind typeKind, String signature) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE);

        TypeSymbol type = (TypeSymbol) symbol.get();
        assertEquals(type.typeKind(), typeKind);
        assertEquals(type.signature(), signature);
        return (TypeSymbol) symbol.get();
    }

    private void assertLocationPosition(LineRange actualLineRange, int line, int sCol, int eCol) {
        LinePosition startLine = actualLineRange.startLine();
        LinePosition endLine = actualLineRange.endLine();
        assertEquals(startLine.line(), line);
        assertEquals(endLine.line(), line);
        assertEquals(startLine.offset(), sCol);
        assertEquals(endLine.offset(), eCol);
    }
}
