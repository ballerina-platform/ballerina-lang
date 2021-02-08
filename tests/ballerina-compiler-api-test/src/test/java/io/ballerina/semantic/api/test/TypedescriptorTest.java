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
import io.ballerina.compiler.api.impl.symbols.BallerinaDecimalTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFloatTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaIntTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaNilTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaStringTypeSymbol;
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
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
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
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.symbols.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.symbols.ParameterKind.REST;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
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
        assertEquals(field.name(), "name");
        assertEquals(field.typeDescriptor().typeKind(), STRING);

        Map<String, MethodSymbol> methods = clazz.methods();
        MethodSymbol method = methods.get("getName");
        assertEquals(fields.size(), 1);
        assertEquals(method.name(), "getName");

        assertEquals(clazz.initMethod().get().name(), "init");
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
                {206, 11, "A?"},
//                {207, 15, "A|B|()"}, TODO: Disabled due to /ballerina-lang/issues/27957
        };
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

        String expSignature = members.stream().map(TypeSymbol::signature).collect(Collectors.joining("|"));
        assertEquals(union.signature(), expSignature);
    }

    @DataProvider(name = "FiniteTypeDataProvider")
    public Object[][] getFiniteTypePos() {
        return new Object[][]{
                {60, 10, List.of("0", "1", "2", "3")},
                {62, 11, List.of("default", "csv", "tdf")}
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
        assertEquals(type.name(), name);
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
        assertEquals(tableType.rowTypeParameter().name(), rowTypeName);
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
        assertEquals(type.name(), name);
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
                {93, 19, TYPE_REFERENCE, NEVER},
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
            assertTrue(expTypes.contains(inclusion.name()));
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
            assertTrue(expTypes.contains(inclusion.name()));
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
            assertTrue(expTypes.contains(inclusion.name()));
        }
    }

    @Test
    public void testEnumsAsTypes() {
        Optional<TypeSymbol> type =
                model.type(LineRange.from("typedesc_test.bal", from(160, 37), from(160, 38)));
        assertEquals(type.get().typeKind(), TYPE_REFERENCE);
        assertEquals(type.get().name(), "Colour");

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

        List<TypeSymbol> members = ((IntersectionTypeSymbol) type).memberTypeDescriptors();

        TypeSymbol mem1 = members.get(0);
        assertEquals(mem1.name(), "Foo");
        assertEquals(((TypeReferenceTypeSymbol) mem1).typeDescriptor().typeKind(), RECORD);

        assertEquals(members.get(1).typeKind(), READONLY);
    }

    @Test
    public void testIntersectionType2() {
        Symbol symbol = getSymbol(170, 25);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), INTERSECTION);

        List<TypeSymbol> members = ((IntersectionTypeSymbol) type).memberTypeDescriptors();
        assertEquals(members.get(0).typeKind(), MAP);
        assertEquals(members.get(1).typeKind(), READONLY);
    }

    @Test
    public void testIntersectionType3() {
        Symbol symbol = getSymbol(171, 16);
        TypeSymbol type = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(type.typeKind(), INTERSECTION);
    }

    @Test
    public void testDistinctObjects() {
        Symbol symbol = getSymbol(174, 12);
        TypeSymbol type = ((TypeDefinitionSymbol) symbol).typeDescriptor();

        assertTrue(((TypeDefinitionSymbol) symbol).qualifiers().contains(Qualifier.PUBLIC));
        assertEquals(type.typeKind(), OBJECT);
        // disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/27279
//        assertTrue(((ObjectTypeSymbol) type).qualifiers().contains(Qualifier.DISTINCT));
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
        assertEquals(((TypeReferenceTypeSymbol) symbol).name(), "CancelledError");
    }

    private Symbol getSymbol(int line, int column) {
        return model.symbol(srcFile, from(line, column)).get();
    }

    private void validateParam(ParameterSymbol param, String name, ParameterKind kind, TypeDescKind typeKind) {
        assertEquals(param.name().get(), name);
        assertEquals(param.kind(), kind);
        assertEquals(param.typeDescriptor().typeKind(), typeKind);
    }
}
