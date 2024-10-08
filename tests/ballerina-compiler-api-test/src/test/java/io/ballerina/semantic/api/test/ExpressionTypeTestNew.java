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
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
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
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INTERSECTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.JSON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.REGEXP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_ELEMENT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the checking the types of expressions. This test is for the new API, typeOf().
 *
 * @since 2.0.0
 */
public class ExpressionTypeTestNew {

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
                {19, 21, 19, 22, INT},
                {19, 24, 19, 29, FLOAT},
                {19, 31, 19, 36, DECIMAL},
                {19, 38, 19, 42, BOOLEAN},
                {19, 44, 19, 46, NIL},
                {19, 48, 19, 53, STRING},
                {20, 13, 20, 17, NIL},
        };
    }

    @Test
    public void testByteLiteral() {
        TypeSymbol type = getExprType(21, 15, 21, 44);
        assertEquals(type.typeKind(), ARRAY);
        assertEquals(((ArrayTypeSymbol) type).memberTypeDescriptor().typeKind(), BYTE);
    }

    @Test
    public void testStringTemplateExpr() {
        assertType(25, 15, 25, 36, STRING);
    }

    @Test
    public void testXMLTemplateExpr() {
        TypeSymbol type = getExprType(26, 12, 26, 45);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), XML_ELEMENT);

        assertType(371, 8, 371, 14, XML);
    }

    @Test
    public void testRawTemplate() {
        TypeSymbol type = getExprType(27, 29, 27, 50);
        assertEquals(type.typeKind(), TYPE_REFERENCE);

        TypeSymbol objType = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(objType.typeKind(), OBJECT);

        type = getExprType(25, 32, 25, 33);
        assertEquals(type.typeKind(), STRING);
    }

    @Test
    public void testArrayLiteral() {
        TypeSymbol type = getExprType(31, 20, 31, 34);
        assertEquals(type.typeKind(), ARRAY);

        TypeSymbol memberType = ((ArrayTypeSymbol) type).memberTypeDescriptor();
        assertEquals(memberType.typeKind(), STRING);
    }

    @Test(dataProvider = "RegexpTemplateLiteralPosProvider")
    public void testRegexpTemplateLiteralExpr(int sLine, int sCol, int eLine, int eCol) {
        TypeSymbol type = getExprType(sLine, sCol, eLine, eCol);
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), REGEXP);
        assertEquals(type.signature(), "ballerina/lang.regexp:0.0.0:RegExp");
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().signature(), "regexp:RegExp");
    }

    @DataProvider(name = "RegexpTemplateLiteralPosProvider")
    private Object[][] getRegexpTemplateLiteralPos() {
        return new Object[][] {
                {362, 8, 362, 22},
                {363, 8, 363, 13},
                {364, 8, 364, 14},
                {365, 8, 365, 20},
                {366, 25, 366, 33},
                {367, 25, 367, 36},
        };
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
                {32, 15, 32, 27, List.of(INT, INT, INT)},
                {33, 31, 33, 49, List.of(INT, STRING, FLOAT)},
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

        assertType(40, 17, 21, null);
        assertType(40, 23, 40, 33, STRING);
        assertType(40, 35, 38, null);
        assertType(40, 40, 40, 42, INT);
    }

    @Test
    public void testJSONObject() {
        TypeSymbol type = getExprType(42, 13, 42, 40);
        assertEquals(type.typeKind(), MAP);

        TypeSymbol constraint = ((MapTypeSymbol) type).typeParam();
        assertEquals(constraint.typeKind(), JSON);

        assertType(42, 14, 18, null);
        assertType(42, 20, 30, STRING);
        assertType(42, 32, 35, null);
        assertType(42, 37, 39, INT);
    }

    @Test(dataProvider = "AccessPosProvider")
    public void testAccessExpr(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "AccessPosProvider")
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
        assertEquals(type.getName().get(), "PersonObj");
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
    public void testArgsInNewExpr() {
        assertType(57, 24, 57, 32, STRING);
        assertType(58, 33, 58, 41, STRING);
    }

    @Test
    public void testObjectConstructorExpr() {
        assertType(64, 15, 68, 5, OBJECT);
        assertType(65, 15, 65, 19, null);
        assertType(65, 22, 65, 28, STRING);
        assertType(67, 45, 67, 54, STRING);
        assertType(67, 45, 67, 49, OBJECT);
        assertType(67, 50, 67, 54, STRING);
    }

    @Test(dataProvider = "MiscExprPosProvider")
    public void testMiscExprs(int sLine, int sCol, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eCol, kind);
    }

    @DataProvider(name = "MiscExprPosProvider")
    public Object[][] getExprPos() {
        return new Object[][]{
                {72, 12, 15, INT},
                {73, 12, 23, INT},
                {73, 12, 19, INT},
                {73, 17, 23, null},
                {74, 12, 23, INT},
                {75, 16, 22, BOOLEAN},
                {76, 17, 22, STRING},
                {78, 8, 20, BOOLEAN},
                {78, 8, 10, ANYDATA},
                {78, 14, 20, null},
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
        assertType(101, 10, 101, 21, NIL);
    }

    @Test
    public void testFutureResultType() {
        TypeSymbol type = getExprType(350, 31, 350, 38);
        assertEquals(type.typeKind(), FUTURE);
        Optional<TypeSymbol> typeParameter = ((FutureTypeSymbol) type).typeParameter();
        assertTrue(typeParameter.isPresent());
        assertEquals(typeParameter.get().typeKind(), INT);
        assertType(354, 17, 354, 24, INT);
    }

    @Test(dataProvider = "CallExprPosProvider")
    public void testFunctionCall(int sLine, int sCol, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eCol, kind);
    }

    @DataProvider(name = "CallExprPosProvider")
    public Object[][] getCallExprPos() {
        return new Object[][]{
                {109, 4, 10, null},
                {109, 4, 9, UNION},
                {112, 15, 16, TYPE_REFERENCE},
                {112, 15, 27, null},
                {112, 15, 26, STRING},
                {127, 4, 35, null},
                {127, 4, 34, STRING},
                {129, 12, 36, INT},
                {129, 12, 37, null},
                {130, 4, 35, BOOLEAN}
        };
    }

    @Test
    public void testExpressionsOfIntersectionTypes() {
        assertType(135, 4, 21, INTERSECTION);
        assertType(135, 4, 22, null);
        assertType(137, 4, 24, null);
        assertType(137, 4, 23, INTERSECTION);
        assertType(139, 4, 26, UNION);

        TypeSymbol t1 = getExprType(139, 4, 139, 26);
        assertEquals(t1.typeKind(), UNION);
        assertEquals(t1.signature(), "(Foo & readonly)|int|(string[] & readonly)");

        assertType(141, 4, 27, null);
        TypeSymbol t2 = getExprType(141, 4, 141, 26);
        assertEquals(t2.typeKind(), UNION);
        assertEquals(t2.signature(), "(int[] & readonly)?");

        assertType(143, 4, 27, null);
        TypeSymbol t3 = getExprType(143, 4, 143, 26);
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

    @Test
    public void testTypeOfExprInErroredStmt() {
        TypeSymbol type = getExprType(177, 12, 177, 23);
        assertEquals(type.typeKind(), UNION);

        UnionTypeSymbol union = (UnionTypeSymbol) type;
        assertEquals(union.memberTypeDescriptors().get(0).typeKind(), INT);
        assertEquals(union.memberTypeDescriptors().get(1).typeKind(), ERROR);
    }

    @Test(dataProvider = "TableCtrPosProvider")
    public void testTableConstructor(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        assertType(sLine, sCol, eLine, eCol, expKind);
    }

    @DataProvider(name = "TableCtrPosProvider")
    public Object[][] getTableCtrPos() {
        return new Object[][]{
                {181, 33, 183, 5, TABLE},
                {181, 43, 181, 45, null},
                {182, 25, 182, 35, STRING},
                {181, 10, 181, 16, null},
        };
    }

    @Test(dataProvider = "XMLAttribAccessPos")
    public void testXMLAttribAccess(int sLine, int sCol, int eLine, int eCol, List<TypeDescKind> memKinds) {
        TypeSymbol type = assertType(sLine, sCol, eLine, eCol, UNION);

        UnionTypeSymbol union = (UnionTypeSymbol) type;
        List<TypeSymbol> userSpecifiedMemberTypes = union.userSpecifiedMemberTypes();

        for (int i = 0; i < userSpecifiedMemberTypes.size(); i++) {
            TypeSymbol memType = userSpecifiedMemberTypes.get(i);
            assertEquals(memType.typeKind(), memKinds.get(i));
        }
    }

    @DataProvider(name = "XMLAttribAccessPos")
    public Object[][] getXMLAttrib() {
        return new Object[][]{
                {188, 23, 188, 29, List.of(STRING, ERROR)},
                {192, 10, 192, 19, List.of(STRING, ERROR)},
//                TODO: https://github.com/ballerina-platform/ballerina-lang/issues/33018
//                {194, 27, 194, 34, List.of(STRING, ERROR, NIL)},
        };
    }

    @Test
    public void testAnnotAccess() {
        UnionTypeSymbol type = (UnionTypeSymbol) assertType(198, 19, 198, 29, UNION);

        assertEquals(type.userSpecifiedMemberTypes().get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(type.userSpecifiedMemberTypes().get(1).typeKind(), NIL);
        assertType(198, 19, 198, 25, TYPEDESC);
    }

    @Test(dataProvider = "ErrorCtrPos")
    public void testErrorConstructor(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        assertType(sLine, sCol, eLine, eCol, expKind);
    }

    @DataProvider(name = "ErrorCtrPos")
    public Object[][] getErrorCtrPos() {
        return new Object[][]{
                {203, 17, 203, 38, ERROR},
                {203, 23, 203, 26, STRING},
//                {203, 28, 203, 29, null}, TODO: https://github.com/ballerina-platform/ballerina-lang/issues/32994
                {203, 32, 203, 37, STRING},
                {204, 23, 204, 28, null},
                {204, 34, 204, 38, ERROR},
                {204, 44, 204, 46, INT},
        };
    }

    @Test
    public void testErrorCtr2() {
        TypeSymbol type = assertType(204, 17, 204, 47, TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), ERROR);
    }

    @Test(dataProvider = "AnonFuncPos")
    public void testAnonFuncs(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        assertType(sLine, sCol, eLine, eCol, expKind);
    }

    @DataProvider(name = "AnonFuncPos")
    public Object[][] getAnonFnPos() {
        return new Object[][]{
                {209, 16, 209, 68, FUNCTION},
                {209, 46, 209, 68, STRING},
                {211, 14, 213, 5, FUNCTION},
                {211, 27, 213, 5, null},
                {211, 51, 211, 52, INT},
                {216, 42, 216, 61, FUNCTION},
                {216, 43, 216, 44, null},
                {216, 56, 216, 57, INT},
        };
    }

    @Test(dataProvider = "LetExprPos")
    public void testLetExpr(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        assertType(sLine, sCol, eLine, eCol, expKind);
    }

    @DataProvider(name = "LetExprPos")
    public Object[][] getLetExprPos() {
        return new Object[][]{
                {220, 12, 220, 88, INT},
                {220, 24, 220, 26, INT},
                {220, 37, 220, 42, STRING},
                {220, 70, 220, 78, STRING},
                {220, 83, 220, 88, INT},
        };
    }

    @Test(dataProvider = "TypeOfPos")
    public void testTypeOfExpr(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        assertType(sLine, sCol, eLine, eCol, expKind);
    }

    @DataProvider(name = "TypeOfPos")
    public Object[][] getTypeOfExprPos() {
        return new Object[][]{
                {225, 27, 225, 42, TYPEDESC},
                {225, 34, 225, 42, INT},
        };
    }

    @Test(dataProvider = "BitwiseExprPos")
    public void testBitwiseExpr(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        assertType(sLine, sCol, eLine, eCol, expKind);
    }

    @DataProvider(name = "BitwiseExprPos")
    public Object[][] getBitwiseExprPos() {
        return new Object[][]{
                {230, 14, 230, 15, INT},
                {230, 14, 230, 20, INT},
                {231, 10, 231, 18, INT},
                {232, 10, 232, 19, INT},
        };
    }

    @Test(dataProvider = "LogicalExprPos")
    public void testLogicalExpr(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        assertType(sLine, sCol, eLine, eCol, expKind);
    }

    @DataProvider(name = "LogicalExprPos")
    public Object[][] getLogicalExprPos() {
        return new Object[][]{
                // Logical expr
                {237, 18, 237, 30, BOOLEAN},
                {237, 18, 237, 22, BOOLEAN},
                {238, 10, 238, 27, BOOLEAN},
                {238, 11, 238, 13, INT},
//                // Conditional expr
                {241, 31, 241, 44, STRING},
                {241, 31, 241, 35, UNION},
                {241, 39, 241, 44, STRING},
                // Shift expr
                {246, 14, 246, 25, INT},
                {246, 20, 246, 25, BYTE},
                {247, 10, 247, 17, INT},
                {248, 10, 248, 21, BYTE},
                // Range expr
                {255, 13, 255, 21, OBJECT},
                {256, 13, 256, 18, OBJECT},
                {257, 13, 257, 18, OBJECT},
                // XML navigation
                {266, 13, 266, 22, XML},
                {267, 9, 267, 22, XML},
                {272, 13, 272, 26, XML},
                {273, 9, 273, 13, XML},
                {274, 9, 274, 15, XML},
                {275, 9, 275, 25, XML},
                {276, 9, 276, 25, XML},
                {276, 23, 276, 24, INT},
                {277, 9, 277, 33, XML},
                // Group expr
                {282, 12, 282, 34, INT},
                {282, 14, 282, 28, INT},
                // XML step expression with extension
                {376, 17, 376, 18, INT},
                {377, 13, 377, 19, UNION},
                {377, 17, 377, 18, INT},
                {378, 9, 378, 31, XML},
                {378, 16, 378, 23, XML},
        };
    }

    @Test
    public void testTrapExpr() {
        UnionTypeSymbol type = (UnionTypeSymbol) assertType(261, 20, 261, 33, UNION);
        assertEquals(type.userSpecifiedMemberTypes().get(0).typeKind(), INT);
        assertEquals(type.userSpecifiedMemberTypes().get(1).typeKind(), ERROR);
        assertType(261, 25, 261, 33, INT);
    }


    // utils

    private void assertType(int line, int sCol, int eCol, TypeDescKind kind) {
        assertType(line, sCol, line, eCol, kind);
    }

    private TypeSymbol assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("expressions_test.bal", LinePosition.from(sLine, sCol), LinePosition.from(eLine, eCol)));

        if (kind == null) {
            assertTrue(type.isEmpty());
            return null;
        }

        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), kind);

        return type.get();
    }

    private TypeSymbol getExprType(int sLine, int sCol, int eLine, int eCol) {
        LinePosition start = LinePosition.from(sLine, sCol);
        LinePosition end = LinePosition.from(eLine, eCol);
        Optional<TypeSymbol> typeSymbol = model.typeOf(LineRange.from("expressions_test.bal", start, end));
        assertTrue(typeSymbol.isPresent());
        return typeSymbol.get();
    }
}
