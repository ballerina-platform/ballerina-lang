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
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.types.ArrayTypeDescriptor;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.MapTypeDescriptor;
import io.ballerina.compiler.api.types.TupleTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;

import static io.ballerina.compiler.api.types.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.types.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.types.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.types.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.types.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.types.TypeDescKind.INT;
import static io.ballerina.compiler.api.types.TypeDescKind.JSON;
import static io.ballerina.compiler.api.types.TypeDescKind.MAP;
import static io.ballerina.compiler.api.types.TypeDescKind.NIL;
import static io.ballerina.compiler.api.types.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.types.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.types.TypeDescKind.STRING;
import static io.ballerina.compiler.api.types.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.types.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.types.TypeDescKind.UNION;
import static io.ballerina.compiler.api.types.TypeDescKind.XML;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.compile;
import static org.testng.Assert.assertEquals;

/**
 * Tests for the checking the types of expressions.
 *
 * @since 2.0.0
 */
public class ExpressionTypeTest {

    SemanticModel model;

    @BeforeClass
    public void setup() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/expressions_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        model = new BallerinaSemanticModel(pkg, context);
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
        BallerinaTypeDescriptor type = getExprType(19, 13, 19, 42);
        assertEquals(type.kind(), ARRAY);
        assertEquals(((ArrayTypeDescriptor) type).memberTypeDescriptor().kind(), BYTE);
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
        BallerinaTypeDescriptor type = getExprType(25, 29, 25, 50);
        assertEquals(type.kind(), TYPE_REFERENCE);

        BallerinaTypeDescriptor objType = ((TypeReferenceTypeDescriptor) type).typeDescriptor();
        assertEquals(objType.kind(), OBJECT);

        type = getExprType(25, 32, 25, 33);
        assertEquals(type.kind(), STRING);
    }

    @Test
    public void testArrayLiteral() {
        BallerinaTypeDescriptor type = getExprType(29, 20, 29, 34);
        assertEquals(type.kind(), ARRAY);

        BallerinaTypeDescriptor memberType = ((ArrayTypeDescriptor) type).memberTypeDescriptor();
        assertEquals(memberType.kind(), STRING);
    }

    @Test(dataProvider = "TupleLiteralPosProvider")
    public void testTupleLiteral(int sLine, int sCol, int eLine, int eCol, List<TypeDescKind> memberKinds) {
        BallerinaTypeDescriptor type = getExprType(sLine, sCol, eLine, eCol);
        assertEquals(type.kind(), TUPLE);

        List<BallerinaTypeDescriptor> memberTypes = ((TupleTypeDescriptor) type).memberTypeDescriptors();

        assertEquals(memberTypes.size(), memberKinds.size());
        for (int i = 0; i < memberTypes.size(); i++) {
            BallerinaTypeDescriptor memberType = memberTypes.get(i);
            assertEquals(memberType.kind(), memberKinds.get(i));
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
        BallerinaTypeDescriptor type = getExprType(34, 20, 34, 34);
        assertEquals(type.kind(), MAP);

        BallerinaTypeDescriptor constraint = ((MapTypeDescriptor) type).typeParameter().get();
        assertEquals(constraint.kind(), STRING);

        assertType(34, 28, 34, 33, STRING);
    }

    @Test
    public void testInferredMappingConstructorType() {
        BallerinaTypeDescriptor type = getExprType(35, 13, 35, 43);
        assertEquals(type.kind(), TYPE_REFERENCE);

        BallerinaTypeDescriptor referredType = ((TypeReferenceTypeDescriptor) type).typeDescriptor();
        assertEquals(referredType.kind(), RECORD);

        assertType(35, 14, 35, 20, STRING);
        assertType(35, 22, 35, 31, STRING);
        assertType(35, 33, 35, 39, STRING);
        assertType(35, 41, 35, 42, INT);
    }

    @Test
    public void testRecordLiteral() {
        BallerinaTypeDescriptor type = getExprType(40, 16, 40, 43);
        assertEquals(type.kind(), RECORD);

        assertType(40, 17, 40, 21, STRING);
        assertType(40, 23, 40, 33, STRING);
        assertType(40, 35, 40, 38, STRING);
        assertType(40, 40, 40, 42, INT);
    }

    @Test
    public void testJSONObject() {
        BallerinaTypeDescriptor type = getExprType(42, 13, 42, 40);
        assertEquals(type.kind(), MAP);

        BallerinaTypeDescriptor constraint = ((MapTypeDescriptor) type).typeParameter().get();
        assertEquals(constraint.kind(), JSON);

        assertType(42, 14, 42, 18, STRING);
        assertType(42, 20, 42, 30, STRING);
        assertType(42, 32, 42, 35, STRING);
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

    private void assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        BallerinaTypeDescriptor type = getExprType(sLine, sCol, eLine, eCol);
        assertEquals(type.kind(), kind);
    }

    private BallerinaTypeDescriptor getExprType(int sLine, int sCol, int eLine, int eCol) {
        LinePosition start = LinePosition.from(sLine, sCol);
        LinePosition end = LinePosition.from(eLine, eCol);
        return model.getType("expressions_test.bal", LineRange.from("expressions_test.bal", start, end)).get();
    }
}
