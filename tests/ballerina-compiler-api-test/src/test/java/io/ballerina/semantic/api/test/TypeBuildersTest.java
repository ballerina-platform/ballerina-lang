/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.impl.types.TypeBuilder;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_COMMENT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_ELEMENT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_PROCESSING_INSTRUCTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_TEXT;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the builders in Types API.
 *
 * @since 2.0.0
 */
public class TypeBuildersTest {
    private Types types;
    private TypeBuilder builder;
    private final List<XMLTypeSymbol> xmlSubTypes = new ArrayList<>();

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/typesbir");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }

        Project project = BCompileUtil.loadProject("test-src/types-project");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        types = model.types();
        builder = types.builder();

        // Extracting the XML subtypes
        List<TypeSymbol> xmlSubTypeMembers =
                ((UnionTypeSymbol) ((XMLTypeSymbol) types.XML).typeParameter().get()).memberTypeDescriptors();
        for (TypeSymbol xmlSubTypeMember : xmlSubTypeMembers) {
            xmlSubTypes.add(((XMLTypeSymbol) ((TypeReferenceTypeSymbol) xmlSubTypeMember).typeDescriptor()));
        }
    }

    @Test(dataProvider = "xmlTypeBuilderProvider")
    public void testXMLTypeBuilder(TypeSymbol typeParam, TypeDescKind typeDescKind, TypeDescKind typeParamDescKind,
                                   String signature) {
        XMLTypeSymbol xmlTypeSymbol = builder.XML_TYPE.withTypeParam(typeParam).build();
        assertEquals(xmlTypeSymbol.typeKind(), typeDescKind);
        if (typeParam != null) {
            assertTrue(xmlTypeSymbol.typeParameter().isPresent());
            assertEquals(xmlTypeSymbol.typeParameter().get().typeKind(), typeParamDescKind);
            assertEquals(xmlTypeSymbol.typeParameter().get().signature(), typeParam.signature());
        }

        assertEquals(xmlTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "xmlTypeBuilderProvider")
    private Object[][] getXMLTypeBuilders() {
        return new Object[][] {
                {null, XML, null, "xml"},
                {types.XML, XML, XML, "xml<xml>"},
                {xmlSubTypes.get(0), XML, XML_ELEMENT, "xml<xml:Element>"},
                {xmlSubTypes.get(1), XML, XML_COMMENT, "xml<xml:Comment>"},
                {xmlSubTypes.get(2), XML, XML_PROCESSING_INSTRUCTION, "xml<xml:ProcessingInstruction>"},
                {xmlSubTypes.get(3), XML, XML_TEXT, "xml<xml:Text>"},
        };
    }

    @Test(dataProvider = "xmlTypeParamsFromSourceProvider")
    public void testXmlTypeParamsFromSource(String typeDef, TypeDescKind typeDescKind, String signature) {
        Optional<Symbol> typeSymbol = types.getTypeByName("testorg", "typesapi.builder", "1.0.0", typeDef);
        assertTrue(typeSymbol.isPresent());
        assertEquals(typeSymbol.get().kind(), SymbolKind.TYPE_DEFINITION);
        TypeSymbol typeParam = ((TypeDefinitionSymbol) typeSymbol.get()).typeDescriptor();
        XMLTypeSymbol xmlTypeSymbol = builder.XML_TYPE.withTypeParam(typeParam).build();
        assertTrue(xmlTypeSymbol.typeParameter().isPresent());
        assertEquals(xmlTypeSymbol.typeParameter().get().typeKind(), typeDescKind);
        assertEquals(xmlTypeSymbol.signature(), signature);
    }

    // TODO: Check and enable after validating #35882
    @DataProvider(name = "xmlTypeParamsFromSourceProvider")
    private Object[][] getXmlTypeParamsFromSource() {
        return new Object[][] {
//                {"XmlEle", TYPE_REFERENCE, "xml<ballerina/lang.xml:0.0.0:Element>"},
//                {"XmlPi", TYPE_REFERENCE, "xml<ballerina/lang.xml:0.0.0:ProcessingInstruction>"},
//                {"XmlCmnt", TYPE_REFERENCE, "xml<ballerina/lang.xml:0.0.0:Comment>"},
//                {"XmlTxt", TYPE_REFERENCE, "xml<ballerina/lang.xml:0.0.0:Text>"},
//                {"XmlUnionA", UNION, "xml<ballerina/lang.xml:0.0.0:Element|ballerina/lang.xml:0.0.0:ProcessingInstruction|ballerina/lang.xml:0.0.0:Text>"},
//                {"XmlUnionB", UNION, "xml<testorg/typesapi.builder:1.0.0:XmlEle|testorg/typesapi.builder:1.0.0:XmlTxt|testorg/typesapi.builder:1.0.0:XmlCmnt>"},
//                {"MixXmlA", UNION, "xml<testorg/typesapi.builder:1.0.0:XmlUnionA|testorg/typesapi.builder:1.0.0:XmlUnionB>"},
//                {"MixXmlB", UNION, "xml<testorg/typesapi.builder:1.0.0:XmlPi|testorg/typesapi.builder:1.0.0:MixXmlC>"},
//                {"MixXmlC", UNION, "xml<testorg/typesapi.builder:1.0.0:XmlUnionA|testorg/typesapi.builder:1.0.0:XmlTxt|testorg/typesapi.builder:1.0.0:MixXmlA>"},
//                {"NewEle", TYPE_REFERENCE, "xml<testorg/typesapi.builder:1.0.0:XmlEle>"},
//                {"EleTxtCmnt", UNION, "xml<testorg/typesapi.builder:1.0.0:XmlCmnt|ballerina/lang.xml:0.0.0:Text|testorg/typesapi.builder:1.0.0:NewEle>"},
        };
    }

    @Test(dataProvider = "mapTypeBuilderProvider")
    public void testMapTypeBuilder(TypeSymbol typeParam, TypeDescKind typeDescKind, String signature) {
        TypeBuilder builder = types.builder();
        MapTypeSymbol mapTypeSymbol = builder.MAP_TYPE.withTypeParam(typeParam).build();
        assertEquals(mapTypeSymbol.typeKind(), typeDescKind);
        assertEquals(mapTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "mapTypeBuilderProvider")
    private Object[][] getMapTypeBuilders() {
        return new Object[][] {
                {types.ANY, MAP, "map<any>"},
                {types.INT, MAP, "map<int>"},
        };
    }

    @Test(dataProvider = "futureTypeBuilderProvider")
    public void testFutureTypeBuilder(TypeSymbol typeParam, TypeDescKind typeDescKind, String signature) {
        TypeBuilder builder = types.builder();
        FutureTypeSymbol futureTypeSymbol = builder.FUTURE_TYPE.withTypeParam(typeParam).build();
        assertEquals(futureTypeSymbol.typeKind(), typeDescKind);
        if (typeParam != null) {
            assertTrue(futureTypeSymbol.typeParameter().isPresent());
            assertEquals(futureTypeSymbol.typeParameter().get().signature(), typeParam.signature());
        }

        assertEquals(futureTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "futureTypeBuilderProvider")
    private Object[][] getFutureTypeBuilders() {
        return new Object[][] {
                {types.STRING, FUTURE, "future<string>"},
                {types.INT, FUTURE, "future<int>"},
                {null, FUTURE, "future<()>"},
        };
    }

    @Test(dataProvider = "typedescTypeBuilderProvider")
    public void testTypeDescTypeBuilder(TypeSymbol typeParam, TypeDescKind typeDescKind, String signature) {
        TypeBuilder builder = types.builder();
        TypeDescTypeSymbol typeDescTypeSymbol = builder.TYPEDESC_TYPE.withTypeParam(typeParam).build();
        assertEquals(typeDescTypeSymbol.typeKind(), typeDescKind);
        if (typeParam != null) {
            assertTrue(typeDescTypeSymbol.typeParameter().isPresent());
            assertEquals(typeDescTypeSymbol.typeParameter().get().signature(), typeParam.signature());
        }

        assertEquals(typeDescTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "typedescTypeBuilderProvider")
    private Object[][] getTypedescTypeBuilders() {
        return new Object[][] {
                {types.FLOAT, TYPEDESC, "typedesc<float>"},
                {types.BOOLEAN, TYPEDESC, "typedesc<boolean>"},
                {null, TYPEDESC, "TYPEDESC"},
        };
    }

    @Test(dataProvider = "streamTypeBuilderProvider")
    public void testStreamTypeBuilder(TypeSymbol vType, TypeSymbol cType, String signature) {
        TypeBuilder builder = types.builder();
        StreamTypeSymbol streamTypeSymbol = builder.STREAM_TYPE.withValueType(vType).withCompletionType(cType).build();
        assertEquals(streamTypeSymbol.typeKind(), STREAM);
        assertEquals(streamTypeSymbol.typeParameter(), vType);
        if (cType != null && cType.typeKind() != NIL) {
            assertEquals(streamTypeSymbol.completionValueTypeParameter().signature(), cType.signature());
        }

        assertEquals(streamTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "streamTypeBuilderProvider")
    private Object[][] getStreamTypeBuilders() {
        return new Object[][] {
                {types.FLOAT, types.INT, "stream<float, int>"},
                {types.BYTE, types.STRING,"stream<byte, string>"},
                {types.ANY, types.NIL, "stream<any>"},
                {types.STRING, null, "stream<string>"},
        };
    }

    @Test(dataProvider = "tupleTypeBuilderProvider")
    public void testTupleTypeBuilder(List<TypeSymbol> memberTypes, TypeSymbol restType, String signature) {

        TypeBuilder builder = types.builder();
        TypeBuilder.TUPLE tupleType = builder.TUPLE_TYPE.withRestType(restType);
        for (TypeSymbol memberType : memberTypes) {
            tupleType.withMemberType(memberType);
        }

        TupleTypeSymbol tupleTypeSymbol = tupleType.build();
        assertEquals(tupleTypeSymbol.typeKind(), TUPLE);
        if (restType != null) {
            assertTrue(tupleTypeSymbol.restTypeDescriptor().isPresent());
        }

        assertEquals(tupleTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "tupleTypeBuilderProvider")
    private Object[][] getTupleTypeBuilders() {
        return new Object[][] {
                {Arrays.asList(types.STRING, types.INT, types.FLOAT), null, "[string, int, float]"},
                {Arrays.asList(types.STRING, types.BOOLEAN), types.INT, "[string, boolean, int...]"},
        };
    }
}
