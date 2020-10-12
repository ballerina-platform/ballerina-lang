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
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.ArrayTypeDescriptor;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.FutureTypeDescriptor;
import io.ballerina.compiler.api.types.MapTypeDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.Parameter;
import io.ballerina.compiler.api.types.ParameterKind;
import io.ballerina.compiler.api.types.RecordTypeDescriptor;
import io.ballerina.compiler.api.types.TupleTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeDescTypeDescriptor;
import io.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import io.ballerina.compiler.api.types.UnionTypeDescriptor;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;

import static io.ballerina.compiler.api.types.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.types.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.types.ParameterKind.REST;
import static io.ballerina.compiler.api.types.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.types.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.types.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.types.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.types.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.types.TypeDescKind.INT;
import static io.ballerina.compiler.api.types.TypeDescKind.MAP;
import static io.ballerina.compiler.api.types.TypeDescKind.NIL;
import static io.ballerina.compiler.api.types.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.types.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.types.TypeDescKind.STRING;
import static io.ballerina.compiler.api.types.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.types.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.types.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.types.TypeDescKind.UNION;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.compile;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the types returned from the typeDescriptor() method in the API.
 *
 * @since 2.0.0
 */
public class TypedescriptorTest {

    SemanticModel model;

    @BeforeClass
    public void setup() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/typedesc_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        model = new BallerinaSemanticModel(pkg, context);
    }

    @Test
    public void testAnnotationType() {
        Symbol symbol = getSymbol(23, 38);
        TypeReferenceTypeDescriptor type =
                (TypeReferenceTypeDescriptor) ((AnnotationSymbol) symbol).typeDescriptor().get();
        assertEquals(type.typeDescriptor().kind(), TypeDescKind.RECORD);
    }

    @Test
    public void testConstantType() {
        Symbol symbol = getSymbol(17, 8);
        BallerinaTypeDescriptor type = ((ConstantSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), FLOAT);
    }

    @Test
    public void testFunctionType() {
        Symbol symbol = getSymbol(44, 13);
        FunctionTypeDescriptor type = (FunctionTypeDescriptor) ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), TypeDescKind.FUNCTION);

        List<Parameter> reqParams = type.requiredParams();
        assertEquals(reqParams.size(), 1);
        validateParam(reqParams.get(0), "x", REQUIRED, INT);

        List<Parameter> defParams = type.defaultableParams();
        assertEquals(defParams.size(), 1);
        validateParam(defParams.get(0), "y", DEFAULTABLE, FLOAT);

        Parameter restParam = type.restParam().get();
        validateParam(restParam, "rest", REST, ARRAY);

        BallerinaTypeDescriptor returnType = type.returnTypeDescriptor().get();
        assertEquals(returnType.kind(), INT);
    }

    @Test
    public void testFutureType() {
        Symbol symbol = getSymbol(46, 17);
        FutureTypeDescriptor type = (FutureTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), FUTURE);
        assertEquals(type.typeParameter().get().kind(), INT);
    }

    @Test
    public void testArrayType() {
        Symbol symbol = getSymbol(48, 19);
        ArrayTypeDescriptor type = (ArrayTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), ARRAY);
        assertEquals(((TypeReferenceTypeDescriptor) type.memberTypeDescriptor()).typeDescriptor().kind(), OBJECT);
    }

    @Test
    public void testMapType() {
        Symbol symbol = getSymbol(50, 17);
        MapTypeDescriptor type = (MapTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), MAP);
        assertEquals(type.typeParameter().get().kind(), STRING);
    }

    @Test
    public void testNilType() {
        Symbol symbol = getSymbol(39, 10);
        FunctionTypeDescriptor type = (FunctionTypeDescriptor) ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.returnTypeDescriptor().get().kind(), NIL);
    }

    @Test
    public void testObjectType() {
        Symbol symbol = getSymbol(29, 7);
        TypeReferenceTypeDescriptor typeRef =
                (TypeReferenceTypeDescriptor) ((TypeSymbol) symbol).typeDescriptor();
        ObjectTypeDescriptor type = (ObjectTypeDescriptor) typeRef.typeDescriptor();
        assertEquals(type.kind(), OBJECT);

        List<FieldDescriptor> fields = type.fieldDescriptors();
        FieldDescriptor field = fields.get(0);
        assertEquals(fields.size(), 1);
        assertEquals(field.name(), "name");
        assertEquals(field.typeDescriptor().kind(), STRING);

        List<MethodSymbol> methods = type.methods();
        MethodSymbol method = methods.get(0);
        assertEquals(fields.size(), 1);
        assertEquals(method.name(), "getName");

        assertEquals(type.initMethod().get().name(), "init");
    }

    @Test
    public void testRecordType() {
        Symbol symbol = getSymbol(19, 6);
        TypeReferenceTypeDescriptor typeRef =
                (TypeReferenceTypeDescriptor) ((TypeSymbol) symbol).typeDescriptor();
        RecordTypeDescriptor type = (RecordTypeDescriptor) typeRef.typeDescriptor();
        assertEquals(type.kind(), RECORD);
        assertFalse(type.inclusive());
        assertFalse(type.restTypeDescriptor().isPresent());

        List<FieldDescriptor> fields = type.fieldDescriptors();
        FieldDescriptor field = fields.get(0);
        assertEquals(fields.size(), 1);
        assertEquals(field.name(), "path");
        assertEquals(field.typeDescriptor().kind(), STRING);
    }

    @Test
    public void testTupleType() {
        Symbol symbol = getSymbol(52, 29);
        TupleTypeDescriptor type = (TupleTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), TUPLE);

        List<BallerinaTypeDescriptor> members = type.memberTypeDescriptors();
        assertEquals(members.size(), 2);
        assertEquals(members.get(0).kind(), INT);
        assertEquals(members.get(1).kind(), STRING);

        assertTrue(type.restTypeDescriptor().isPresent());
        assertEquals(type.restTypeDescriptor().get().kind(), FLOAT);
    }

    @Test(dataProvider = "TypedescDataProvider")
    public void testTypedescType(int line, int col, TypeDescKind kind) {
        Symbol symbol = getSymbol(line, col);
        TypeDescTypeDescriptor type = (TypeDescTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), TYPEDESC);
        assertTrue(type.typeParameter().isPresent());
        assertEquals(type.typeParameter().get().kind(), kind);
    }

    @DataProvider(name = "TypedescDataProvider")
    public Object[][] getTypedescPositions() {
        return new Object[][]{
                {54, 23, ANYDATA},
                {55, 14, UNION}
        };
    }

    @Test
    public void testUnionType() {
        Symbol symbol = getSymbol(57, 22);
        UnionTypeDescriptor type = (UnionTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.kind(), UNION);

        List<BallerinaTypeDescriptor> members = type.memberTypeDescriptors();
        assertEquals(members.get(0).kind(), INT);
        assertEquals(members.get(1).kind(), STRING);
        assertEquals(members.get(2).kind(), FLOAT);
    }

    @Test(enabled = false)
    public void testNamedUnion() {
        Symbol symbol = getSymbol(59, 12);
        TypeReferenceTypeDescriptor typeRef =
                (TypeReferenceTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeRef.kind(), TYPE_REFERENCE);

        UnionTypeDescriptor type = (UnionTypeDescriptor) typeRef.typeDescriptor();

        List<BallerinaTypeDescriptor> members = type.memberTypeDescriptors();
        assertEquals(members.get(0).kind(), INT);
        assertEquals(members.get(1).kind(), FLOAT);
        assertEquals(members.get(2).kind(), DECIMAL);
    }

    // TODO: issue #26276
    @Test(enabled = false)
    public void testFiniteType() {
        Symbol symbol = getSymbol(61, 11);
        TypeReferenceTypeDescriptor typeRef =
                (TypeReferenceTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeRef.kind(), TYPE_REFERENCE);
    }

    private Symbol getSymbol(int line, int column) {
        return model.symbol("typedesc_test.bal", from(line, column)).get();
    }

    private void validateParam(Parameter param, String name, ParameterKind kind, TypeDescKind typeKind) {
        assertEquals(param.name().get(), name);
        assertEquals(param.kind(), kind);
        assertEquals(param.typeDescriptor().kind(), typeKind);
    }
}
