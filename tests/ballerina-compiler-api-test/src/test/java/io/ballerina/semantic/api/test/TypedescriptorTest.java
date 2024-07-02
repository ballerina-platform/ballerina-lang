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
import io.ballerina.compiler.api.impl.symbols.BallerinaBooleanTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaByteTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaConstantSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaDecimalTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFloatTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaIntTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaNilTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaStringTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeReferenceTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaUnionTypeSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.symbols.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.symbols.ParameterKind.REST;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.COMPILATION_ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INTERSECTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT_SIGNED16;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT_SIGNED32;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT_SIGNED8;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT_UNSIGNED16;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT_UNSIGNED32;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT_UNSIGNED8;
import static io.ballerina.compiler.api.symbols.TypeDescKind.JSON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NEVER;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.READONLY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.SINGLETON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING_CHAR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_COMMENT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_ELEMENT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_PROCESSING_INSTRUCTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_TEXT;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
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

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/typedesc_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "ParameterizedUnionReturnTypePosProvider")
    public void testParameterizedUnionReturnType(int line, int col, String unionSignature, String memberSignature) {
        Project project = BCompileUtil.loadProject("test-src/parameterized_return_type_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);
        Optional<Symbol> optionalSymbol = model.symbol(srcFile, from(line, col));
        assertTrue(optionalSymbol.isPresent());
        Symbol symbol = optionalSymbol.get();
        Optional<TypeSymbol> returnTypeDescriptor = ((MethodSymbol) symbol).typeDescriptor().returnTypeDescriptor();
        assertTrue(returnTypeDescriptor.isPresent());
        TypeSymbol returnTypeSymbol = returnTypeDescriptor.get();
        assertEquals(returnTypeSymbol.signature(), unionSignature);
        assertEquals(returnTypeSymbol.typeKind(), UNION);
        TypeSymbol firstMember = ((UnionTypeSymbol) returnTypeSymbol).memberTypeDescriptors().get(0);
        assertEquals(firstMember.typeKind(), TYPE_REFERENCE);
        TypeSymbol firstMemberTypeDescriptor = ((TypeReferenceTypeSymbol) firstMember).typeDescriptor();
        assertEquals(firstMemberTypeDescriptor.signature(), memberSignature);
    }

    @DataProvider(name = "ParameterizedUnionReturnTypePosProvider")
    private Object[][] getParameterizedUnionReturnTypePos() {
        return new Object[][] {
                {25, 9, "tdA|error", "anydata"},
                {26, 7, "testType|error", "int|string"},
        };
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
        assertEquals(((ConstantSymbol) symbol).typeKind(), SINGLETON);

        TypeSymbol type = ((ConstantSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), SINGLETON);
    }

    @Test
    public void testFunctionType() {
        Symbol symbol = getSymbol(43, 12);
        FunctionTypeSymbol type = ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.FUNCTION);

        List<ParameterSymbol> parameters = type.params().get();
        assertEquals(parameters.size(), 2);
        validateParam(parameters.get(0), "x", REQUIRED, INT);

        validateParam(parameters.get(1), "y", DEFAULTABLE, FLOAT);

        ParameterSymbol restParam = type.restParam().get();
        validateParam(restParam, "rest", REST, ARRAY);

        TypeSymbol returnType = type.returnTypeDescriptor().get();
        assertEquals(returnType.typeKind(), INT);
    }

    @Test(dataProvider = "paramsInFunctionType")
    public void testFunctionTypeParams(int line, int col, TypeDescKind typeKind, String signature) {
        Symbol symbol = getSymbol(line, col);
        assertEquals(symbol.kind(), TYPE);
        TypeSymbol typeSymbol = (TypeSymbol) symbol;
        assertEquals(typeSymbol.typeKind(), typeKind);
        assertEquals(typeSymbol.signature(), signature);
    }

    @DataProvider(name = "paramsInFunctionType")
    public Object[][] getFunctionTypeParamsPos() {
        return new Object[][]{
                {241, 18, STRING, "string"},
                {241, 26, INT, "int"},
                {244, 13, STRING, "string"},
                {244, 21, STRING, "string"}
        };
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

    @Test(dataProvider = "ArrayVarPosProvider")
    public void testFixedArrayType(int line, int col, String expSignature) {
        Symbol symbol = getSymbol(line, col);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), ARRAY);
        assertEquals(type.signature(), expSignature);
    }

    @DataProvider(name = "ArrayVarPosProvider")
    public Object[][] getArrayVarPos() {
        return new Object[][]{
                {269, 11, "int[3]"},
                {270, 26, "string[1][2][3][4][5]"},
                {271, 13, "int[][2]"},
                {272, 13, "int[2][]"},
                {273, 23, "(int|string)[1][2]"},
                {274, 30, "(Bar & readonly)[1][2][3]"},
        };
    }

    @Test(dataProvider = "InferredArrayVarPosProvider")
    public void testInferredArrayType(int line, int col, String expSignature) {
        Symbol symbol = getSymbol(line, col);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), ARRAY);
        assertEquals(type.signature(), expSignature);
    }

    @DataProvider(name = "InferredArrayVarPosProvider")
    public Object[][] getInferredArrayVarPos() {
        return new Object[][] {
                {292, 11, "int[*]"},
                {293, 26, "string[1][*][2][*][3]"},
                {294, 16, "int[][2][*]"},
                {295, 17, "int[2][*][3]"},
                {296, 17, "string[*][*]"},
                {297, 24, "(int|string)[][*][]"},
                {298, 30, "(Bar & readonly)[*][2][*]"},
        };
    }

    @Test(dataProvider = "IntersectionReadonlyPosProvider")
    public void testIntersectionReadonlyTypes(int line, int col, TypeDescKind expKind, String expSignature) {
        Symbol symbol = getSymbol(line, col);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), expKind);
        assertEquals(type.signature(), expSignature);
    }

    @DataProvider(name = "IntersectionReadonlyPosProvider")
    public Object[][] getIntersectionReadonlyPos() {
        return new Object[][] {
                {304, 19, INTERSECTION, "Bar & readonly"},
                {305, 16, TYPE_REFERENCE, "BarReadonly"},
                {306, 19, INTERSECTION, "Foo & readonly"},
                {307, 16, TYPE_REFERENCE, "FooReadOnly"},
                {308, 35, INTERSECTION, "record {|int a;|} & readonly"},
        };
    }

    @Test(dataProvider = "XmlSubTypePosProvider")
    public void testXmlSubTypes(int line, int col, TypeDescKind expTypeKind, String expSignature) {
        Symbol symbol = getSymbol(line, col);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), expTypeKind);
        assertEquals(type.signature(), expSignature);
    }

    @DataProvider(name = "XmlSubTypePosProvider")
    private Object[][] getXmlSubTypePos() {
        return new Object[][] {
                {324, 11, TYPE_REFERENCE, "XmlEle"},
                {325, 16, XML, "xml<XmlEle>"},
                {326, 15, TYPE_REFERENCE, "New_XmlEle"},
                {327, 20, XML, "xml<New_XmlEle>"},
                {328, 10, TYPE_REFERENCE, "XmlPI"},
                {329, 15, TYPE_REFERENCE, "XmlComment"},
                {330, 19, TYPE_REFERENCE, "New_XmlComment"},
                {331, 24, XML, "xml<New_XmlComment>"},
                {332, 18, TYPE_REFERENCE, "XmlCommentRef"},
                {333, 23, XML, "xml<XmlCommentRef>"},
                {334, 11, TYPE_REFERENCE, "XmlTxt"},
                {335, 21, XML, "xml<xml:Element>"},
                {336, 22, XML, "xml<xml:Comment>"},
                {337, 14, TYPE_REFERENCE, "XmlUnionA"},
                {338, 19, XML, "xml<XmlUnionA>"},
                {339, 14, TYPE_REFERENCE, "XmlUnionB"},
                {340, 14, TYPE_REFERENCE, "XmlUnionC"},
                {341, 19, XML, "xml<XmlUnionC>"},
        };
    }

    @Test
    public void testMapType() {
        Symbol symbol = getSymbol(49, 16);
        MapTypeSymbol type = (MapTypeSymbol) ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), MAP);
        assertEquals(type.typeParam().typeKind(), STRING);
    }

    @Test
    public void testNilType() {
        Symbol symbol = getSymbol(38, 9);
        FunctionTypeSymbol type = ((FunctionSymbol) symbol).typeDescriptor();
        assertEquals(type.returnTypeDescriptor().get().typeKind(), NIL);
    }

    @Test
    public void testObjectType() {
        Symbol symbol = getSymbol(28, 6);
        ClassSymbol clazz = (ClassSymbol) symbol;
        assertEquals(clazz.typeKind(), OBJECT);

        Map<String, ClassFieldSymbol> fields = clazz.fieldDescriptors();
        ObjectFieldSymbol field = fields.get("name");
        assertEquals(fields.size(), 1);
        assertEquals(field.getName().get(), "name");
        assertEquals(field.typeDescriptor().typeKind(), STRING);

        Map<String, MethodSymbol> methods = clazz.methods();
        MethodSymbol method = methods.get("getName");
        assertEquals(fields.size(), 1);
        assertEquals(method.getName().get(), "getName");

        assertEquals(clazz.initMethod().get().getName().get(), "init");
    }

    @Test
    public void testRecordType() {
        Symbol symbol = getSymbol(18, 5);
        RecordTypeSymbol type = (RecordTypeSymbol) ((TypeDefinitionSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), RECORD);
        assertFalse(type.restTypeDescriptor().isPresent());

        Map<String, RecordFieldSymbol> fields = type.fieldDescriptors();
        assertEquals(fields.size(), 1);
        assertTrue(fields.containsKey("path"));

        RecordFieldSymbol field = fields.get("path");
        assertEquals(field.getName().get(), "path");
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

        assertEquals(type.signature(), "int|string|float");
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

    @Test(dataProvider = "UnionWithNilPos")
    public void testUnionTypeWithNil(int line, int col, String signature) {
        Symbol symbol = getSymbol(line, col);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), UNION);
        assertEquals(type.signature(), signature);
    }

    @DataProvider(name = "UnionWithNilPos")
    public Object[][] getUnionWithNilPos() {
        return new Object[][]{
                {204, 11, "int?"},
                {205, 17, "int|float|()"},
                {206, 11, "\"A\"?"},
//                {207, 15, "A|B|()"}, TODO: Disabled due to /ballerina-lang/issues/27957
        };
    }

    @Test(dataProvider = "UnionWithFunctionTypePos")
    public void testUnionTypeWithFunctionType(int line, int col, String signature) {
        Symbol symbol = getSymbol(line, col);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), UNION);
        assertEquals(type.signature(), signature);
    }

    @DataProvider(name = "UnionWithFunctionTypePos")
    public Object[][] getUnionWithFunctionTypePos() {
        return new Object[][]{
                {259, 34, "(function () returns int)|10|20"},
                {260, 37, "(function () returns int)|string"},
                {261, 35, "string|function () returns int"},
                {262, 36, "(function () returns int)|10|20"},
                {263, 37, "(function () returns int|string)|3"},
                {264, 34, "ReturnIntFunctionType?|string"},
                {265, 57, "(function () returns string)|function () returns int"},
        };
    }

    @Test(dataProvider = "FiniteTypeDataProvider")
    public void testFiniteType(int line, int column, String typeName, List<String> expSignatures) {
        Symbol symbol = getSymbol(line, column);

        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), typeName);
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), UNION);

        UnionTypeSymbol union = (UnionTypeSymbol) ((TypeReferenceTypeSymbol) type).typeDescriptor();
        List<TypeSymbol> members = union.memberTypeDescriptors();

        for (int i = 0; i < members.size(); i++) {
            TypeSymbol member = members.get(i);
            assertEquals(member.typeKind(), SINGLETON);
            assertEquals(member.signature(), expSignatures.get(i));
        }

        String expSignature = members.stream().map(TypeSymbol::signature).collect(Collectors.joining("|"));
        assertEquals(union.signature(), expSignature);
    }

    @DataProvider(name = "FiniteTypeDataProvider")
    public Object[][] getFiniteTypePos() {
        return new Object[][]{
                {60, 10, "Digit", List.of("0", "1", "2", "3")},
                {62, 11, "Format", List.of("\"default\"", "\"csv\"", "\"tdf\"")}
        };
    }

    @Test(dataProvider = "CommonTypesPosProvider")
    public void testCommonTypes(int line, int column, TypeDescKind kind) {
        VariableSymbol symbol = (VariableSymbol) getSymbol(line, column);
        assertEquals(symbol.typeDescriptor().typeKind(), kind);
    }

    @DataProvider(name = "CommonTypesPosProvider")
    public Object[][] getTypesPos() {
        return new Object[][]{
                {64, 9, JSON},
                {68, 13, READONLY},
                {70, 8, ANY},
                {71, 12, ANYDATA},
        };
    }

    @Test(dataProvider = "XMLPosProvider")
    public void testXML(int line, int column, TypeDescKind kind, String name) {
        VariableSymbol symbol = (VariableSymbol) getSymbol(line, column);
        TypeSymbol type = symbol.typeDescriptor();
        assertEquals(type.typeKind(), XML);
        assertEquals(((XMLTypeSymbol) type).typeParameter().get().typeKind(), kind);
        assertEquals(type.getName().get(), name);
    }

    @DataProvider(name = "XMLPosProvider")
    public Object[][] getXMLTypePos() {
        return new Object[][]{
                {66, 8, UNION, "xml"},
                {91, 22, TYPE_REFERENCE, "xml<Element>"},
        };
    }

    @Test(dataProvider = "TablePosProvider")
    public void testTable(int line, int column, TypeDescKind rowTypeKind, String rowTypeName,
                          List<String> keySpecifiers, TypeDescKind keyConstraintTypeKind, String signature) {
        VariableSymbol symbol = (VariableSymbol) getSymbol(line, column);
        TypeSymbol type = symbol.typeDescriptor();
        assertEquals(type.typeKind(), TABLE);

        TableTypeSymbol tableType = (TableTypeSymbol) type;
        assertEquals(tableType.rowTypeParameter().typeKind(), rowTypeKind);
        assertEquals(tableType.rowTypeParameter().getName().get(), rowTypeName);
        assertEquals(tableType.keySpecifiers(), keySpecifiers);
        tableType.keyConstraintTypeParameter().ifPresent(t -> assertEquals(t.typeKind(), keyConstraintTypeKind));
        assertEquals(type.signature(), signature);
    }

    @DataProvider(name = "TablePosProvider")
    public Object[][] getTableTypePos() {
        return new Object[][]{
                {73, 28, TYPE_REFERENCE, "Person", List.of("name"), null, "table<Person> key(name)"},
                {74, 18, TYPE_REFERENCE, "Person", Collections.emptyList(), null, "table<Person>"},
                {75, 30, TYPE_REFERENCE, "Person", Collections.emptyList(), STRING, "table<Person> key<string>"}
        };
    }

    @Test(dataProvider = "BuiltinTypePosProvider")
    public void testBuiltinSubtypes(int line, int column, String name, TypeDescKind kind) {
        Symbol symbol = getSymbol(line, column);
        TypeSymbol typeRef = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeRef.typeKind(), TYPE_REFERENCE);

        TypeSymbol type = ((TypeReferenceTypeSymbol) typeRef).typeDescriptor();
        assertEquals(type.typeKind(), kind);
        assertEquals(type.getName().get(), name);
    }

    @DataProvider(name = "BuiltinTypePosProvider")
    public Object[][] getBuiltinTypePos() {
        return new Object[][]{
                {77, 20, "Unsigned32", INT_UNSIGNED32},
                {78, 18, "Signed32", INT_SIGNED32},
                {79, 19, "Unsigned8", INT_UNSIGNED8},
                {80, 17, "Signed8", INT_SIGNED8},
                {81, 20, "Unsigned16", INT_UNSIGNED16},
                {82, 18, "Signed16", INT_SIGNED16},
                {84, 17, "Char", STRING_CHAR},
                {86, 17, "Element", XML_ELEMENT},
                {87, 31, "ProcessingInstruction", XML_PROCESSING_INSTRUCTION},
                {88, 17, "Comment", XML_COMMENT},
                {89, 14, "Text", XML_TEXT},
        };
    }

    @Test(dataProvider = "StreamTypePosProvider")
    public void testStreamType(int line, int column, TypeDescKind typeParamKind, TypeDescKind completionValueTypeKind) {
        Symbol symbol = getSymbol(line, column);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), STREAM);

        StreamTypeSymbol streamType = (StreamTypeSymbol) type;
        assertEquals(streamType.typeParameter().typeKind(), typeParamKind);
        assertEquals(streamType.completionValueTypeParameter().typeKind(), completionValueTypeKind);
    }

    @DataProvider(name = "StreamTypePosProvider")
    public Object[][] getStreamTypePos() {
        return new Object[][]{
                {93, 19, TYPE_REFERENCE, NIL},
                {94, 23, TYPE_REFERENCE, NIL},
                {95, 45, RECORD, ERROR}
        };
    }

    @Test(dataProvider = "RecordTypeInclusionPosProvider")
    public void testRecordTypeInclusion(int line, int col, TypeDescKind typeKind, Set<String> expTypes) {
        Symbol symbol = getSymbol(line, col);
        RecordTypeSymbol type = (RecordTypeSymbol) ((TypeDefinitionSymbol) symbol).typeDescriptor();
        List<TypeSymbol> typeInclusions = type.typeInclusions();

        for (TypeSymbol inclusion : typeInclusions) {
            assertEquals(((TypeReferenceTypeSymbol) inclusion).typeDescriptor().typeKind(), typeKind);
            assertTrue(expTypes.contains(inclusion.getName().get()));
        }
    }

    @DataProvider(name = "RecordTypeInclusionPosProvider")
    public Object[][] getRecordTypeInclusionPos() {
        return new Object[][]{
                {113, 6, RECORD, Set.of("Person")},
                {126, 6, RECORD, Set.of("Foo", "Bar")},
        };
    }

    @Test
    public void testObjectTypeInclusion() {
        Set<String> expTypes = Set.of("FooObj", "BarObj");
        Symbol symbol = getSymbol(144, 6);
        ObjectTypeSymbol type = (ObjectTypeSymbol) ((TypeDefinitionSymbol) symbol).typeDescriptor();
        List<TypeSymbol> typeInclusions = type.typeInclusions();

        for (TypeSymbol inclusion : typeInclusions) {
            assertEquals(((TypeReferenceTypeSymbol) inclusion).typeDescriptor().typeKind(), OBJECT);
            assertTrue(expTypes.contains(inclusion.getName().get()));
        }
    }

    @Test
    public void testObjectTypeInclusionInClasses() {
        Set<String> expTypes = Set.of("PersonObj");
        Symbol symbol = getSymbol(149, 6);
        ClassSymbol clazz = (ClassSymbol) symbol;
        List<TypeSymbol> typeInclusions = clazz.typeInclusions();

        for (TypeSymbol inclusion : typeInclusions) {
            assertEquals(((TypeReferenceTypeSymbol) inclusion).typeDescriptor().typeKind(), OBJECT);
            assertTrue(expTypes.contains(inclusion.getName().get()));
        }
    }

    @Test
    public void testEnumsAsTypes() {
        Optional<TypeSymbol> type =
                model.type(LineRange.from("typedesc_test.bal", from(160, 37), from(160, 38)));
        assertEquals(type.get().typeKind(), TYPE_REFERENCE);
        assertEquals(type.get().getName().get(), "Colour");

        TypeSymbol enumType = ((TypeReferenceTypeSymbol) type.get()).typeDescriptor();
        assertEquals(enumType.typeKind(), UNION);

        for (TypeSymbol memberType : ((UnionTypeSymbol) enumType).memberTypeDescriptors()) {
            assertEquals(memberType.typeKind(), SINGLETON);
        }
    }

    @Test
    public void testNeverType() {
        Symbol symbol = getSymbol(163, 9);
        TypeSymbol type = ((FunctionSymbol) symbol).typeDescriptor().returnTypeDescriptor().get();
        assertEquals(type.typeKind(), NEVER);
    }

    @Test
    public void testIntersectionType1() {
        Symbol symbol = getSymbol(167, 6);
        TypeSymbol type = ((TypeDefinitionSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), INTERSECTION);
        assertEquals(type.signature(), "Foo & readonly");

        assertEquals(((IntersectionTypeSymbol) type).effectiveTypeDescriptor().typeKind(), TYPE_REFERENCE);
        TypeReferenceTypeSymbol typeRefType =
                (TypeReferenceTypeSymbol) ((IntersectionTypeSymbol) type).effectiveTypeDescriptor();
        assertEquals(typeRefType.typeDescriptor().typeKind(), RECORD);

        List<TypeSymbol> members = ((IntersectionTypeSymbol) type).memberTypeDescriptors();

        TypeSymbol mem1 = members.get(0);
        assertEquals(mem1.getName().get(), "Foo");
        assertEquals(((TypeReferenceTypeSymbol) mem1).typeDescriptor().typeKind(), RECORD);

        assertEquals(members.get(1).typeKind(), READONLY);
    }

    @Test
    public void testIntersectionType2() {
        Symbol symbol = getSymbol(170, 25);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), INTERSECTION);
        assertEquals(type.signature(), "map<json> & readonly");

        assertEquals(((IntersectionTypeSymbol) type).effectiveTypeDescriptor().typeKind(), MAP);

        List<TypeSymbol> members = ((IntersectionTypeSymbol) type).memberTypeDescriptors();
        assertEquals(members.get(0).typeKind(), MAP);
        assertEquals(members.get(1).typeKind(), READONLY);
    }

    @Test
    public void testIntersectionType3() {
        Symbol symbol = getSymbol(171, 16);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();

        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), "ReadonlyFoo");
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), INTERSECTION);

        IntersectionTypeSymbol intrType = (IntersectionTypeSymbol) ((TypeReferenceTypeSymbol) type).typeDescriptor();

        TypeReferenceTypeSymbol typeRefType = (TypeReferenceTypeSymbol) intrType.effectiveTypeDescriptor();
        assertEquals(typeRefType.typeKind(), TYPE_REFERENCE);
        assertEquals(typeRefType.typeDescriptor().typeKind(), RECORD);

        List<TypeSymbol> members = intrType.memberTypeDescriptors();

        assertEquals(members.get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(members.get(0).getName().get(), "Foo");
        assertEquals(members.get(1).typeKind(), READONLY);
    }

    @Test
    public void testIntersectionType4() {
        Symbol symbol = getSymbol(238, 17);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();

        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), "FooReadOnly");
        assertEquals(((TypeReferenceTypeSymbol) type).typeDescriptor().typeKind(), INTERSECTION);

        IntersectionTypeSymbol intrType = (IntersectionTypeSymbol) ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(intrType.effectiveTypeDescriptor().typeKind(), RECORD);

        List<TypeSymbol> members = intrType.memberTypeDescriptors();

        assertEquals(members.get(0).typeKind(), READONLY);
        assertEquals(members.get(1).typeKind(), RECORD);
    }

    @Test
    public void testIntersectionType5() {
        Symbol symbol = getSymbol(237, 51);
        TypeSymbol type = ((ParameterSymbol) symbol).typeDescriptor();

        assertEquals(type.typeKind(), INTERSECTION);
        IntersectionTypeSymbol interType = (IntersectionTypeSymbol) type;
        assertEquals(interType.effectiveTypeDescriptor().typeKind(), TYPE_REFERENCE);

        TypeReferenceTypeSymbol typeRefType =
                (TypeReferenceTypeSymbol) interType.effectiveTypeDescriptor();
        assertEquals(typeRefType.typeDescriptor().typeKind(), OBJECT);

        List<TypeSymbol> members = interType.memberTypeDescriptors();

        assertEquals(members.get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(members.get(0).getName().get(), "FooObj");
        assertEquals(members.get(1).typeKind(), READONLY);
    }

    @Test(dataProvider = "DistinctObjectTypeProvider")
    public void testDistinctObjects(int line, int column, List<Qualifier> typeDefQuals, List<Qualifier> objectQuals) {
        Symbol symbol = getSymbol(line, column);
        assertEquals(symbol.kind(), TYPE_DEFINITION);
        TypeDefinitionSymbol typeDefinitionSymbol = (TypeDefinitionSymbol) symbol;
        TypeSymbol typeSymbol = (typeDefinitionSymbol).typeDescriptor();
        assertEquals(typeSymbol.typeKind(), OBJECT);
        assertQualifiers(typeDefinitionSymbol.qualifiers(), typeDefQuals);
        assertQualifiers(((ObjectTypeSymbol) typeSymbol).qualifiers(), objectQuals);
    }

    @DataProvider(name = "DistinctObjectTypeProvider")
    private Object[][] getDistinctObjectTypes() {
        return new Object[][] {
                {174, 12, List.of(Qualifier.PUBLIC), List.of(Qualifier.DISTINCT)},
                {375, 12, List.of(Qualifier.PUBLIC), List.of(Qualifier.DISTINCT, Qualifier.SERVICE)},
        };
    }

    @Test
    public void testCompileErrorType1() {
        LineRange range = LineRange.from("typedesc_test.bal", from(181, 12), from(181, 17));
        Optional<TypeSymbol> type = model.type(range);
        assertEquals(type.get().typeKind(), COMPILATION_ERROR);
    }

    @Test
    public void testCompileErrorType2() {
        Symbol symbol = getSymbol(182, 18);
        assertEquals(((VariableSymbol) symbol).typeDescriptor().typeKind(), COMPILATION_ERROR);
    }

    @Test(dataProvider = "BasicTestPosProvider")
    public void testTypeBasics(int line, int col, TypeDescKind typeKind, Class<? extends TypeSymbol> clazz) {
        VariableSymbol symbol = (VariableSymbol) getSymbol(line, col);
        assertEquals(symbol.typeDescriptor().typeKind(), typeKind);
        assertEquals(symbol.typeDescriptor().getClass(), clazz);
    }

    @DataProvider(name = "BasicTestPosProvider")
    public Object[][] getBasicTestPos() {
        return new Object[][]{
                {186, 8, INT, BallerinaIntTypeSymbol.class},
                {187, 10, FLOAT, BallerinaFloatTypeSymbol.class},
                {188, 12, DECIMAL, BallerinaDecimalTypeSymbol.class},
                {189, 12, BOOLEAN, BallerinaBooleanTypeSymbol.class},
                {190, 7, NIL, BallerinaNilTypeSymbol.class},
                {191, 11, STRING, BallerinaStringTypeSymbol.class},
                {192, 9, BYTE, BallerinaByteTypeSymbol.class},
        };
    }

    @Test
    public void testMultilineTypedefs() {
        Symbol symbol = getSymbol(198, 18);
        assertEquals(symbol.kind(), TYPE);
        assertEquals(((TypeSymbol) symbol).typeKind(), TYPE_REFERENCE);
        assertEquals(symbol.getName().get(), "CancelledError");
    }

    @Test(dataProvider = "ConstantPosProvider")
    public void testConstantTypeSignature(int line, int col, String signature) {
        Symbol symbol = getSymbol(line, col);
        assertEquals(symbol.kind(), CONSTANT);
        assertEquals(((ConstantSymbol) symbol).typeKind(), SINGLETON);
        assertEquals(((ConstantSymbol) symbol).signature(), signature);
    }

    @DataProvider(name = "ConstantPosProvider")
    public Object[][] getConstPos() {
        return new Object[][]{
                {16, 6, "PI"},
                {210, 6, "NIL"},
        };
    }

    @Test(dataProvider = "SingletonPosProvider")
    public void testSingletonTypeSignature(int line, int col, String signature) {
        Symbol symbol = getSymbol(line, col);
        assertEquals(symbol.kind(), TYPE_DEFINITION);
        assertEquals(((TypeDefinitionSymbol) symbol).typeDescriptor().typeKind(), SINGLETON);
        assertEquals(((TypeDefinitionSymbol) symbol).typeDescriptor().signature(), signature);
    }

    @DataProvider(name = "SingletonPosProvider")
    public Object[][] getSingletonTypePos() {
        return new Object[][]{
                {211, 5, "()"},
                {213, 5, "3.14"},
        };
    }

    @Test
    public void testFunctionTypedesc() {
        VariableSymbol symbol = (VariableSymbol) getSymbol(216, 13);
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.FUNCTION);

        FunctionTypeSymbol type = (FunctionTypeSymbol) symbol.typeDescriptor();
        assertTrue(type.params().isEmpty());
        assertTrue(type.restParam().isEmpty());
        assertTrue(type.returnTypeDescriptor().isEmpty());
        assertEquals(type.signature(), "function");
    }

    @Test
    public void testParameterizedType() {
        Symbol symbol = getSymbol(219, 9);
        FunctionTypeSymbol type = ((FunctionSymbol) symbol).typeDescriptor();
        TypeSymbol returnTypeSymbol = type.returnTypeDescriptor().get();
        assertEquals(returnTypeSymbol.signature(), "td");

        symbol = getSymbol(221, 9);
        type = ((FunctionSymbol) symbol).typeDescriptor();
        returnTypeSymbol = type.returnTypeDescriptor().get();
        assertEquals(returnTypeSymbol.typeKind(), UNION);
        assertEquals(returnTypeSymbol.signature(), "error|td");
        List<TypeSymbol> members = ((UnionTypeSymbol) returnTypeSymbol).memberTypeDescriptors();
        assertEquals(members.size(), 2);
        assertEquals(members.get(0).typeKind(), ERROR);
        assertEquals(members.get(1).signature(), "td");
    }

    @Test(dataProvider = "CursorPosProvider")
    public void testSymbolAtCursor(int line, int col, TypeDescKind typeKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));

        if (typeKind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        assertEquals(symbol.get().kind(), TYPE);
        assertEquals(((TypeSymbol) symbol.get()).typeKind(), typeKind);
    }

    @DataProvider(name = "CursorPosProvider")
    public Object[][] getCursorPos() {
        return new Object[][]{
                {24, 58, INT},
                {29, 4, STRING},
                {45, 4, FUTURE},
                {49, 4, MAP},
                {51, 4, null},
                {54, 4, TYPEDESC},
                {64, 4, JSON},
                {66, 4, XML},
                {68, 4, READONLY},
                {70, 4, ANY},
                {71, 4, ANYDATA},
                {73, 4, TABLE},
                {93, 4, STREAM},

        };
    }

    @Test(dataProvider = "TypeRefPosProvider")
    public void testSymbolAtCursorForTypeRefs(int line, int col, String name, TypeDescKind typeKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));

        assertEquals(symbol.get().kind(), TYPE);
        assertEquals(((TypeSymbol) symbol.get()).typeKind(), TYPE_REFERENCE);

        TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) symbol.get();
        assertEquals(type.getName().get(), name);
        assertEquals(type.typeDescriptor().typeKind(), typeKind);
    }

    @DataProvider(name = "TypeRefPosProvider")
    public Object[][] getTypeRefPos() {
        return new Object[][]{
                {47, 4, "PersonObj", OBJECT},
                {58, 4, "Number", UNION},
                {60, 4, "Digit", UNION},
                {62, 4, "Format", UNION},
                {77, 9, "Unsigned32", INT_UNSIGNED32},
                {84, 12, "Char", STRING_CHAR},
                {86, 9, "Element", XML_ELEMENT},
        };
    }

    @Test
    public void testUserSpecifiedUnionMembers() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(229, 12));
        TypeSymbol type = ((VariableSymbol) symbol.get()).typeDescriptor();

        assertEquals(type.typeKind(), UNION);
        assertEquals(type.signature(), "Colour?");

        List<TypeSymbol> userSpecifiedMembers = ((UnionTypeSymbol) type).userSpecifiedMemberTypes();
        assertEquals(userSpecifiedMembers.size(), 2);
        assertEquals(userSpecifiedMembers.get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(userSpecifiedMembers.get(0).getName().get(), "Colour");
        assertEquals(userSpecifiedMembers.get(1).typeKind(), NIL);

        symbol = model.symbol(srcFile, from(230, 13));
        type = ((VariableSymbol) symbol.get()).typeDescriptor();

        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.signature(), "FooUnion");

        type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(type.typeKind(), UNION);

        userSpecifiedMembers = ((UnionTypeSymbol) type).userSpecifiedMemberTypes();
        assertEquals(userSpecifiedMembers.get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(userSpecifiedMembers.get(0).getName().get(), "IntString");
        assertEquals(userSpecifiedMembers.get(1).typeKind(), TYPE_REFERENCE);
        assertEquals(userSpecifiedMembers.get(1).getName().get(), "FloatBoolean");
    }

    @Test
    public void testFlattenedUnionMembers() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(229, 12));
        TypeSymbol type = ((VariableSymbol) symbol.get()).typeDescriptor();

        assertEquals(type.typeKind(), UNION);
        assertEquals(type.signature(), "Colour?");

        List<TypeSymbol> expandedMembers = ((UnionTypeSymbol) type).memberTypeDescriptors();
        assertEquals(expandedMembers.size(), 4);
        assertEquals(expandedMembers.get(0).typeKind(), SINGLETON);
        assertEquals(expandedMembers.get(1).typeKind(), SINGLETON);
        assertEquals(expandedMembers.get(2).typeKind(), SINGLETON);
        assertEquals(expandedMembers.get(3).typeKind(), NIL);

        symbol = model.symbol(srcFile, from(230, 13));
        type = ((VariableSymbol) symbol.get()).typeDescriptor();

        assertEquals(type.typeKind(), TYPE_REFERENCE);

        type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(type.typeKind(), UNION);

        expandedMembers = ((UnionTypeSymbol) type).memberTypeDescriptors();
        assertEquals(expandedMembers.get(0).typeKind(), INT);
        assertEquals(expandedMembers.get(1).typeKind(), STRING);
        assertEquals(expandedMembers.get(2).typeKind(), FLOAT);
        assertEquals(expandedMembers.get(3).typeKind(), BOOLEAN);
    }

    @Test
    public void testSymbolModuleInfo() {
        Project project = BCompileUtil.loadProject("test-src/testprojmodules");
        Package currentPackage = project.currentPackage();

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        List<Symbol> symbolList = new ArrayList<>();
        moduleIds.forEach(moduleId -> symbolList.addAll(packageCompilation.getSemanticModel(moduleId).moduleSymbols()));

        List<SymbolInfo> expectedSymbolList = createSymbolInfoList(getSymbolModuleInfo());
        assertList(symbolList, expectedSymbolList);
    }

    @Test
    public void testObjectTypeSignature() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(255, 6));
        assertEquals(((VariableSymbol) symbol.get()).typeDescriptor().signature(),
                     "client object {int a; int b; function testFunc(); remote function testRFunc(); " +
                             "function getA() returns int;}");
    }

    public Object[][] getSymbolModuleInfo() {
        return new Object[][]{
                {0, 16, "main.bal", SymbolKind.FUNCTION, "main",
                        "symbolowner/testprojmodules:0.1.0"},
                {5, 12, "module1.bal", SymbolKind.TYPE_DEFINITION, "Int",
                        "symbolowner/testprojmodules.module1:0.1.0"},
                {9, 12, "module1.bal", SymbolKind.TYPE_DEFINITION, "StreamType1",
                        "symbolowner/testprojmodules.module1:0.1.0"},
                {11, 12, "module1.bal", SymbolKind.TYPE_DEFINITION, "StreamType2",
                        "symbolowner/testprojmodules.module1:0.1.0"},
                {13, 12, "module1.bal", SymbolKind.TYPE_DEFINITION, "TestRecord",
                        "symbolowner/testprojmodules.module1:0.1.0"},
                {1, 6, "module1.bal", SymbolKind.CLASS, "Class1",
                        "symbolowner/testprojmodules.module1:0.1.0"},
                {7, 12, "module1.bal", SymbolKind.TYPE_DEFINITION, "ClassType",
                        "symbolowner/testprojmodules.module1:0.1.0"},
        };
    }

    @Test
    public void testIncompleteConstDef() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(277, 13));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), CONSTANT);
        BallerinaConstantSymbol constSymbol = (BallerinaConstantSymbol) symbol.get();
        assertEquals(constSymbol.getName().get(), "greeting");
        assertEquals(constSymbol.typeDescriptor().typeKind(), COMPILATION_ERROR);
        assertEquals(constSymbol.typeDescriptor().signature(), "$CompilationError$");
    }

    @Test(dataProvider = "UnionTypeSymbolPos")
    public void testUnionTypeSymbolSignature(int line, int col, String signature) {
        TypeReferenceTypeSymbol symbol = (TypeReferenceTypeSymbol) getSymbol(line, col);
        assertEquals(symbol.typeDescriptor().typeKind(), UNION);
        UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) symbol.typeDescriptor();
        assertEquals(unionTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "UnionTypeSymbolPos")
    public Object[][] getUnionTypeSymbolPos() {
        return new Object[][]{
                {280, 0, "\"foo1\"|\"foo2\""},
                {282, 0, "\"parent\"|\"any\""},
        };
    }

    @Test
    public void testSingletonTypeSignatureInUnionType() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(282, 7));
        assertTrue(symbol.isPresent());
        List<TypeSymbol> memberSymbols = ((BallerinaUnionTypeSymbol) ((BallerinaTypeReferenceTypeSymbol)
                ((VariableSymbol) symbol.get()).typeDescriptor()).typeDescriptor()).memberTypeDescriptors();
        assertEquals(memberSymbols.get(0).signature(), "\"parent\"");
        assertEquals(memberSymbols.get(1).signature(), "\"any\"");
    }

    @Test(dataProvider = "SingletonTypePos")
    public void testSingletonType(int line, int col, TypeDescKind expKind, String expString) {
        Symbol symbol = getSymbol(line, col);
        TypeSymbol typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeSymbol.typeKind(), SINGLETON);
        assertEquals(typeSymbol.signature(), expString);
        TypeSymbol originalType = ((SingletonTypeSymbol) typeSymbol).originalType();
        assertEquals(originalType.typeKind(), expKind);
    }

    @DataProvider(name = "SingletonTypePos")
    private Object[][] getSingletonType() {
        return new Object[][] {
                {351, 6, INT, "5"},
                {352, 8, STRING, "\"6\""},
                {353, 10, STRING, "\"abc\""},
                {354, 8, FLOAT, "1.2"},
                {355, 9, FLOAT, "3.4"},
                {356, 8, BYTE, "10"},
                {357, 11, INT, "46575"},
                {358, 12, FLOAT, "0xA1.B5p0"},
                {359, 14, FLOAT, "0xB2.8Fp1"},
                {360, 8, STRING, "\"a\""},
                {361, 8, STRING, "\"RED\""},
        };
    }

    @Test(dataProvider = "ClassAndObjectTypePosProvider")
    public void testClassAndObjectType(int line, int column, String expectedSignature, TypeDescKind expTypeKind) {
        Symbol varSymbol = getSymbol(line, column);
        assertEquals(varSymbol.kind(), VARIABLE);
        TypeSymbol typeSymbol = ((VariableSymbol) varSymbol).typeDescriptor();
        assertEquals(typeSymbol.signature(), expectedSignature);
        assertEquals(typeSymbol.typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor().typeKind(), expTypeKind);
    }

    @DataProvider(name = "ClassAndObjectTypePosProvider")
    private Object[][] getClassAndObjectTypePos() {
        return new Object[][] {
                {371, 12, "'client", OBJECT},
                {372, 11, "'class", OBJECT},
        };
    }

    private List<SymbolInfo> createSymbolInfoList(Object[][] infoArr) {
        List<SymbolInfo> symInfo = new ArrayList<>();
        for (Object[] objects : infoArr) {
            symInfo.add(new SymbolInfo((int) objects[0], (int) objects[1], (String) objects[2],
                    (SymbolKind) objects[3], (String) objects[4], (String) objects[5]));
        }
        return symInfo;
    }

    static class SymbolInfo {
        int line;
        int column;
        String srcFile;
        SymbolKind symbolKind;
        String name;
        String moduleOwner;

        SymbolInfo(int line, int column, String srcFile, SymbolKind symbolKind, String name, String moduleOwner) {
            this.line = line;
            this.column = column;
            this.srcFile = srcFile;
            this.symbolKind = symbolKind;
            this.name = name;
            this.moduleOwner = moduleOwner;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SymbolInfo that)) {
                return false;
            }

            return line == that.line &&
                   column == that.column &&
                   srcFile.equals(that.srcFile) &&
                   symbolKind == that.symbolKind &&
                   name.equals(that.name) &&
                   moduleOwner.equals(that.moduleOwner);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.line,
                                this.column,
                                this.srcFile,
                                this.symbolKind,
                                this.name,
                                this.moduleOwner);
        }

        @Override
        public String toString() {
            return "SymbolInfo{" +
                    "location=" + srcFile +
                    " (" + line +
                    "," + column +
                    "), symbolKind=" + symbolKind +
                    ", name='" + name + '\'' +
                    ", moduleOwner='" + moduleOwner + '\'' +
                    '}';
        }
    }

    @Test
    public void testImportSymbolTypeDesc() {
        Project project = BCompileUtil.loadProject("test-src/imported_typedesc_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, from(19, 33));
        TypeSymbol type = (TypeSymbol) symbol.get();
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), "StackFrame");

        TypeSymbol typeDescriptorSym = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(typeDescriptorSym.typeKind(), INTERSECTION);
        IntersectionTypeSymbol intersectionTypeSymbol = (IntersectionTypeSymbol) typeDescriptorSym;
        assertEquals(intersectionTypeSymbol.effectiveTypeDescriptor().typeKind(), OBJECT);

        ObjectTypeSymbol effectiveType = (ObjectTypeSymbol) intersectionTypeSymbol.effectiveTypeDescriptor();
        MethodSymbol methodSymbol = effectiveType.methods().get("toString");

        FunctionTypeSymbol functionSymbol = methodSymbol.typeDescriptor();
        assertTrue(functionSymbol.params().get().isEmpty());
        assertEquals(functionSymbol.returnTypeDescriptor().get().typeKind(), STRING);
    }

    private static void assertList(List<Symbol> actualValues, List<SymbolInfo> expectedValues) {
        assertEquals(actualValues.size(), expectedValues.size());

        for (SymbolInfo val : expectedValues) {
            assertTrue(actualValues.stream()
                            .anyMatch(sym -> {
                                LineRange lineRange = sym.getLocation().get().lineRange();
                                LinePosition linePosition = lineRange.startLine();
                                return val.equals(
                                        new SymbolInfo(linePosition.line(), linePosition.offset(),
                                                lineRange.fileName(), sym.kind(), sym.getName().get(),
                                                ((BallerinaSymbol) sym).getInternalSymbol().owner.toString()));
                            }),
                    "Symbol not found: " + val);

        }
    }

    private void assertQualifiers(List<Qualifier> qualifiers, List<Qualifier> expectedQualifiers) {
        assertEquals(qualifiers.size(), expectedQualifiers.size());
        for (Qualifier expectedQualifier : expectedQualifiers) {
            assertTrue(qualifiers.contains(expectedQualifier));
        }
    }

    private Symbol getSymbol(int line, int column) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, column));
        assertTrue(symbol.isPresent());
        return symbol.get();
    }

    private void validateParam(ParameterSymbol param, String name, ParameterKind kind, TypeDescKind typeKind) {
        assertEquals(param.getName().get(), name);
        assertEquals(param.paramKind(), kind);
        assertEquals(param.typeDescriptor().typeKind(), typeKind);
    }
}
