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
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INTERSECTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.JSON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_ELEMENT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
        TypeSymbol type = getExprType(19, 15, 19, 44);
        assertEquals(type.typeKind(), ARRAY);
        assertEquals(((ArrayTypeSymbol) type).memberTypeDescriptor().typeKind(), BYTE);
    }

    @Test
    public void testStringTemplateExpr() {
        assertType(23, 15, 23, 36, STRING);
    }

    @Test
    public void testXMLTemplateExpr() {
        TypeSymbol type = getExprType(24, 12, 24, 45);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), XML_ELEMENT);
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

        TypeSymbol constraint = ((MapTypeSymbol) type).typeParam();
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

        TypeSymbol constraint = ((MapTypeSymbol) type).typeParam();
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
        assertEquals(((TypeReferenceTypeSymbol) type).getName().get(), "PersonObj");
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
                {73, 12, 73, 19, INT},
                {73, 17, 73, 23, null},
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
        assertType(97, 4, 97, 20, RECORD);
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
                {112, 15, 112, 27, null},
                {112, 15, 112, 26, STRING},
                {127, 4, 127, 35, STRING},
                {127, 4, 127, 34, STRING},
                {129, 12, 129, 36, INT},
                {129, 12, 129, 37, null},
                {130, 4, 130, 36, BOOLEAN}
        };
    }

    @Test
    public void testExpressionsOfIntersectionTypes() {
        assertType(135, 4, 135, 21, INTERSECTION);
        assertType(135, 4, 135, 22, INTERSECTION);
        assertType(137, 4, 137, 24, INTERSECTION);
        assertType(137, 4, 137, 23, INTERSECTION);
        assertType(139, 4, 139, 26, UNION);
        TypeSymbol t1 = getExprType(139, 4, 139, 27);
        assertEquals(t1.typeKind(), UNION);
        assertEquals(t1.signature(), "(Foo & readonly)|int|(string[] & readonly)");
        assertType(141, 4, 141, 26, UNION);
        TypeSymbol t2 = getExprType(141, 4, 141, 27);
        assertEquals(t2.typeKind(), UNION);
        assertEquals(t2.signature(), "(int[] & readonly)?");
        assertType(143, 4, 143, 26, UNION);
        TypeSymbol t3 = getExprType(143, 4, 143, 27);
        assertEquals(t3.typeKind(), UNION);
        assertEquals(t3.signature(), "(int[] & readonly)?");
    }

    @Test
    public void testTypeWithinServiceDecl() {
        assertType(118, 15, 118, 16, RECORD);
    }

    @Test
    public void testTypeWithinDoAndOnFailClause() {
        TypeSymbol exprType = getExprType(164, 16, 164, 23);
        assertEquals(exprType.typeKind(), TYPE_REFERENCE);
        assertEquals(exprType.getName().get(), "Foo");

        exprType = getExprType(166, 12, 166, 42);
        assertEquals(exprType.typeKind(), STRING);
    }

    @Test
    public void testFuncCallForDependentlyTypedSignatures() {
        TypeSymbol exprType = getExprType(172, 12, 172, 35);
        assertEquals(exprType.typeKind(), INT);
    }

    private void assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        Optional<TypeSymbol> type = model.type(
                LineRange.from("expressions_test.bal", LinePosition.from(sLine, sCol), LinePosition.from(eLine, eCol)));

        if (kind == null) {
            assertTrue(type.isEmpty());
            return;
        }

        assertEquals(type.get().typeKind(), kind);
    }

    private TypeSymbol getExprType(int sLine, int sCol, int eLine, int eCol) {
        LinePosition start = LinePosition.from(sLine, sCol);
        LinePosition end = LinePosition.from(eLine, eCol);
        return model.type(LineRange.from("expressions_test.bal", start, end)).get();
    }
}
