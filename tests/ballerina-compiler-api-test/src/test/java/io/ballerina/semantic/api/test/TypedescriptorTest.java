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
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;

import static io.ballerina.compiler.api.symbols.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.symbols.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.symbols.ParameterKind.REST;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.SINGLETON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
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
        Symbol symbol = getSymbol(22, 37);
        TypeReferenceTypeSymbol type =
                (TypeReferenceTypeSymbol) ((AnnotationSymbol) symbol).typeDescriptor().get();
        assertEquals(type.typeDescriptor().typeKind(), TypeDescKind.RECORD);
    }

    @Test
    public void testConstantType() {
        Symbol symbol = getSymbol(16, 7);
        TypeSymbol type = ((ConstantSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), FLOAT);
    }

    @Test
    public void testFunctionType() {
        Symbol symbol = getSymbol(43, 12);
        FunctionTypeSymbol type = ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.FUNCTION);

        List<ParameterSymbol> parameters = type.parameters();
        assertEquals(parameters.size(), 2);
        validateParam(parameters.get(0), "x", REQUIRED, INT);

        validateParam(parameters.get(1), "y", DEFAULTABLE, FLOAT);

        ParameterSymbol restParam = type.restParam().get();
        validateParam(restParam, "rest", REST, ARRAY);

        TypeSymbol returnType = type.returnTypeDescriptor().get();
        assertEquals(returnType.typeKind(), INT);
    }

    @Test
    public void testFutureType() {
        Symbol symbol = getSymbol(45, 16);
        FutureTypeSymbol type = (FutureTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), FUTURE);
        assertEquals(type.typeParameter().get().typeKind(), INT);
    }

    @Test
    public void testArrayType() {
        Symbol symbol = getSymbol(47, 18);
        ArrayTypeSymbol type = (ArrayTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), ARRAY);
        assertEquals(((TypeReferenceTypeSymbol) type.memberTypeDescriptor()).typeDescriptor().typeKind(), OBJECT);
    }

    @Test
    public void testMapType() {
        Symbol symbol = getSymbol(49, 16);
        MapTypeSymbol type = (MapTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), MAP);
        assertEquals(type.typeParameter().get().typeKind(), STRING);
    }

    @Test
    public void testNilType() {
        Symbol symbol = getSymbol(38, 9);
        FunctionTypeSymbol type = (FunctionTypeSymbol) ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.returnTypeDescriptor().get().typeKind(), NIL);
    }

    @Test
    public void testObjectType() {
        Symbol symbol = getSymbol(28, 6);
        TypeReferenceTypeSymbol typeRef =
                (TypeReferenceTypeSymbol) ((TypeDefinitionSymbol) symbol).typeDescriptor();
        ObjectTypeSymbol type = (ObjectTypeSymbol) typeRef.typeDescriptor();
        assertEquals(type.typeKind(), OBJECT);

        List<FieldSymbol> fields = type.fieldDescriptors();
        FieldSymbol field = fields.get(0);
        assertEquals(fields.size(), 1);
        assertEquals(field.name(), "name");
        assertEquals(field.typeDescriptor().typeKind(), STRING);

        List<MethodSymbol> methods = type.methods();
        MethodSymbol method = methods.get(0);
        assertEquals(fields.size(), 1);
        assertEquals(method.name(), "getName");

        assertEquals(type.initMethod().get().name(), "init");
    }

    @Test
    public void testRecordType() {
        Symbol symbol = getSymbol(18, 5);
        TypeReferenceTypeSymbol typeRef =
                (TypeReferenceTypeSymbol) ((TypeDefinitionSymbol) symbol).typeDescriptor();
        RecordTypeSymbol type = (RecordTypeSymbol) typeRef.typeDescriptor();
        assertEquals(type.typeKind(), RECORD);
        assertFalse(type.inclusive());
        assertFalse(type.restTypeDescriptor().isPresent());

        List<FieldSymbol> fields = type.fieldDescriptors();
        FieldSymbol field = fields.get(0);
        assertEquals(fields.size(), 1);
        assertEquals(field.name(), "path");
        assertEquals(field.typeDescriptor().typeKind(), STRING);
    }

    @Test
    public void testTupleType() {
        Symbol symbol = getSymbol(51, 28);
        TupleTypeSymbol type = (TupleTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TUPLE);

        List<TypeSymbol> members = type.memberTypeDescriptors();
        assertEquals(members.size(), 2);
        assertEquals(members.get(0).typeKind(), INT);
        assertEquals(members.get(1).typeKind(), STRING);

        assertTrue(type.restTypeDescriptor().isPresent());
        assertEquals(type.restTypeDescriptor().get().typeKind(), FLOAT);
    }

    @Test(dataProvider = "TypedescDataProvider")
    public void testTypedescType(int line, int col, TypeDescKind kind) {
        Symbol symbol = getSymbol(line, col);
        TypeDescTypeSymbol type = (TypeDescTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TYPEDESC);
        assertTrue(type.typeParameter().isPresent());
        assertEquals(type.typeParameter().get().typeKind(), kind);
    }

    @DataProvider(name = "TypedescDataProvider")
    public Object[][] getTypedescPositions() {
        return new Object[][]{
                {53, 22, ANYDATA},
                {54, 13, UNION}
        };
    }

    @Test
    public void testUnionType() {
        Symbol symbol = getSymbol(56, 21);
        UnionTypeSymbol type = (UnionTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), UNION);

        List<TypeSymbol> members = type.memberTypeDescriptors();
        assertEquals(members.get(0).typeKind(), INT);
        assertEquals(members.get(1).typeKind(), STRING);
        assertEquals(members.get(2).typeKind(), FLOAT);
    }

    @Test(enabled = false)
    public void testNamedUnion() {
        Symbol symbol = getSymbol(58, 11);
        TypeReferenceTypeSymbol typeRef =
                (TypeReferenceTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeRef.typeKind(), TYPE_REFERENCE);

        UnionTypeSymbol type = (UnionTypeSymbol) typeRef.typeDescriptor();

        List<TypeSymbol> members = type.memberTypeDescriptors();
        assertEquals(members.get(0).typeKind(), INT);
        assertEquals(members.get(1).typeKind(), FLOAT);
        assertEquals(members.get(2).typeKind(), DECIMAL);
    }

    @Test(dataProvider = "FiniteTypeDataProvider")
    public void testFiniteType(int line, int column, List<String> expSignatures) {
        Symbol symbol = getSymbol(line, column);
        UnionTypeSymbol union = (UnionTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(union.typeKind(), UNION);

        List<TypeSymbol> members = union.memberTypeDescriptors();
        for (int i = 0; i < members.size(); i++) {
            TypeSymbol member = members.get(i);
            assertEquals(member.typeKind(), SINGLETON);
            assertEquals(member.signature(), expSignatures.get(i));
        }
    }

    @DataProvider(name = "FiniteTypeDataProvider")
    public Object[][] getFiniteTypePos() {
        return new Object[][]{
                {60, 10, List.of("0", "1", "2", "3")},
                {62, 11, List.of("default", "csv", "tdf")}
        };
    }

    private Symbol getSymbol(int line, int column) {
        return model.symbol("typedesc_test.bal", from(line, column)).get();
    }

    private void validateParam(ParameterSymbol param, String name, ParameterKind kind, TypeDescKind typeKind) {
        assertEquals(param.name().get(), name);
        assertEquals(param.kind(), kind);
        assertEquals(param.typeDescriptor().typeKind(), typeKind);
    }
}
