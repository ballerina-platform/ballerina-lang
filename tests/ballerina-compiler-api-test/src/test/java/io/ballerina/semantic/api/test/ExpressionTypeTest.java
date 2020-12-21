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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.JSON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static org.testng.Assert.assertEquals;

/**
 * Tests for the checking the types of expressions.
 *
 * @since 2.0.0
 */
public class ExpressionTypeTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/expressions_test.bal");
    }

    @Test(dataProvider = "LiteralPosProvider")
    public void testLiterals(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "LiteralPosProvider")
    public Object[][] getLiteralPos() {
        return new Object[][]{
                {17, 21, 17, 22, INT},
                {17, 24, 17, 29, FLOAT},
                {17, 31, 17, 36, DECIMAL},
                {17, 38, 17, 42, BOOLEAN},
                {17, 44, 17, 46, NIL},
                {17, 48, 17, 53, STRING},
                {18, 13, 18, 17, NIL},
        };
    }

    @Test
    public void testByteLiteral() {
        TypeSymbol type = getExprType(19, 13, 19, 42);
        assertEquals(type.typeKind(), ARRAY);
        assertEquals(((ArrayTypeSymbol) type).memberTypeDescriptor().typeKind(), BYTE);
    }

    @Test(dataProvider = "TemplateExprProvider")
    public void testTemplateExprs(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "TemplateExprProvider")
    public Object[][] getTemplateExprPos() {
        return new Object[][]{
                {23, 15, 23, 36, STRING},
                {24, 12, 24, 45, XML},
        };
    }

    @Test
    public void testRawTemplate() {
        TypeSymbol type = getExprType(25, 29, 25, 50);
        assertEquals(type.typeKind(), TYPE_REFERENCE);

        TypeSymbol objType = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(objType.typeKind(), OBJECT);

        type = getExprType(25, 32, 25, 33);
        assertEquals(type.typeKind(), STRING);
    }

    @Test
    public void testArrayLiteral() {
        TypeSymbol type = getExprType(29, 20, 29, 34);
        assertEquals(type.typeKind(), ARRAY);

        TypeSymbol memberType = ((ArrayTypeSymbol) type).memberTypeDescriptor();
        assertEquals(memberType.typeKind(), STRING);
    }

    @Test(dataProvider = "TupleLiteralPosProvider")
    public void testTupleLiteral(int sLine, int sCol, int eLine, int eCol, List<TypeDescKind> memberKinds) {
        TypeSymbol type = getExprType(sLine, sCol, eLine, eCol);
        assertEquals(type.typeKind(), TUPLE);

        List<TypeSymbol> memberTypes = ((TupleTypeSymbol) type).memberTypeDescriptors();

        assertEquals(memberTypes.size(), memberKinds.size());
        for (int i = 0; i < memberTypes.size(); i++) {
            TypeSymbol memberType = memberTypes.get(i);
            assertEquals(memberType.typeKind(), memberKinds.get(i));
        }
    }

    @DataProvider(name = "TupleLiteralPosProvider")
    public Object[][] getTuplePos() {
        return new Object[][]{
                {30, 15, 30, 27, List.of(INT, INT, INT)},
                {32, 31, 32, 49, List.of(INT, STRING, FLOAT)},
        };
    }

    @Test
    public void testMapLiteral() {
        TypeSymbol type = getExprType(34, 20, 34, 34);
        assertEquals(type.typeKind(), MAP);

        TypeSymbol constraint = ((MapTypeSymbol) type).typeParameter().get();
        assertEquals(constraint.typeKind(), STRING);

        assertType(34, 28, 34, 33, STRING);
    }

    @Test
    public void testInferredMappingConstructorType() {
        TypeSymbol type = getExprType(35, 13, 35, 43);
        assertEquals(type.typeKind(), RECORD);

        assertType(35, 14, 35, 20, STRING);
        assertType(35, 22, 35, 31, STRING);
        assertType(35, 33, 35, 39, STRING);
        assertType(35, 41, 35, 42, INT);
    }

    @Test
    public void testRecordLiteral() {
        TypeSymbol type = getExprType(40, 16, 40, 43);
        assertEquals(type.typeKind(), RECORD);

        // Disabled ones due to #26628
//        assertType(40, 17, 40, 21, STRING);
        assertType(40, 23, 40, 33, STRING);
//        assertType(40, 35, 40, 38, STRING);
        assertType(40, 40, 40, 42, INT);
    }

    @Test
    public void testJSONObject() {
        TypeSymbol type = getExprType(42, 13, 42, 40);
        assertEquals(type.typeKind(), MAP);

        TypeSymbol constraint = ((MapTypeSymbol) type).typeParameter().get();
        assertEquals(constraint.typeKind(), JSON);

        // Disabled ones due to #26628
//        assertType(42, 14, 42, 18, STRING);
        assertType(42, 20, 42, 30, STRING);
//        assertType(42, 32, 42, 35, STRING);
        assertType(42, 37, 42, 39, INT);
    }

    @Test(dataProvider = "FieldAccessPosProvider")
    public void testFieldAccessExpr(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "FieldAccessPosProvider")
    public Object[][] getFieldAccessPos() {
        return new Object[][]{
                // Field access
                {51, 18, 51, 29, STRING},
                {51, 18, 51, 24, RECORD},
                // Optional Field access
                {52, 15, 52, 26, UNION},
                {52, 15, 52, 21, RECORD},
                // Member access
                {53, 21, 53, 35, STRING},
                {53, 21, 53, 27, RECORD},
                {53, 28, 53, 34, STRING},
        };
    }

    @Test(dataProvider = "TypeInitPosProvider")
    public void testObjecTypeInit(int sLine, int sCol, int eLine, int eCol) {
        TypeSymbol type = getExprType(sLine, sCol, eLine, eCol);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) type).name(), "PersonObj");
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), OBJECT);
    }

    @DataProvider(name = "TypeInitPosProvider")
    public Object[][] getTypeInitPos() {
        return new Object[][]{
                {57, 19, 57, 33},
                {58, 19, 58, 42}
        };
    }

    @Test
    public void testObjectConstructorExpr() {
        assertType(64, 15, 68, 5, OBJECT);
        assertType(65, 22, 65, 28, STRING);
        assertType(67, 45, 67, 54, STRING);
        assertType(67, 45, 67, 49, OBJECT);
        assertType(67, 50, 67, 54, STRING);
    }

    @Test(dataProvider = "MiscExprPosProvider")
    public void testMiscExprs(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "MiscExprPosProvider")
    public Object[][] getExprPos() {
        return new Object[][]{
                {72, 12, 72, 15, INT},
                {73, 12, 73, 23, INT},
                {73, 17, 73, 23, INT},
                {74, 12, 74, 23, INT},
                {75, 16, 75, 22, BOOLEAN},
                {76, 17, 76, 22, STRING},
                {78, 8, 78, 20, BOOLEAN},
                {78, 8, 78, 10, ANYDATA},
                {78, 14, 78, 20, STRING},
        };
    }

    @Test(dataProvider = "CheckingExprPosProvider")
    public void testCheckingExprs(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "CheckingExprPosProvider")
    public Object[][] getCheckingExprPos() {
        return new Object[][]{
                {86, 16, 86, 27, STRING},
                {86, 22, 86, 27, UNION},
                {87, 16, 87, 32, STRING},
                {87, 27, 87, 32, UNION},
        };
    }

    @Test(dataProvider = "CastingExprPosProvider")
    public void testCastingExprs(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "CastingExprPosProvider")
    public Object[][] getCastingExprPos() {
        return new Object[][]{
                {92, 15, 92, 25, STRING},
                {92, 23, 92, 25, ANYDATA},
                {93, 12, 93, 30, INT},
                {93, 28, 93, 30, ANYDATA},
        };
    }

    @Test
    public void testInferredRecordTypeForInvalidExprs() {
        assertType(97, 5, 97, 20, RECORD);
    }

    @Test
    public void testStartAction() {
        TypeSymbol type = getExprType(101, 4, 101, 21);
        assertEquals(type.typeKind(), FUTURE);
        assertEquals(((FutureTypeSymbol) type).typeParameter().get().typeKind(), NIL);
    }

    @Test(dataProvider = "CallExprPosProvider")
    public void testFunctionCall(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "CallExprPosProvider")
    public Object[][] getCallExprPos() {
        return new Object[][]{
                {109, 4, 109, 10, UNION},
                {109, 4, 109, 9, UNION},
                {112, 15, 112, 27, STRING},
                {112, 15, 112, 26, STRING}
        };
    }

    private void assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        TypeSymbol type = getExprType(sLine, sCol, eLine, eCol);
        assertEquals(type.typeKind(), kind);
    }

    private TypeSymbol getExprType(int sLine, int sCol, int eLine, int eCol) {
        LinePosition start = LinePosition.from(sLine, sCol);
        LinePosition end = LinePosition.from(eLine, eCol);
        return model.type("expressions_test.bal", LineRange.from("expressions_test.bal", start, end)).get();
    }
}
