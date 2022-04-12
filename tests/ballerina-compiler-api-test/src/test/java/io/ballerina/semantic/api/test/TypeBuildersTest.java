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
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
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

import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
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

    @Test(dataProvider = "streamTypeBuilderProvider")
    public void testStreamTypeBuilder(TypeSymbol vType, TypeSymbol cType, TypeDescKind typeDescKind,String signature) {
        TypeBuilder builder = types.builder();
        StreamTypeSymbol streamTypeSymbol = builder.STREAM_TYPE.withValueType(vType).withCompletionType(cType).build();
        assertEquals(streamTypeSymbol.typeKind(), typeDescKind);
        assertEquals(streamTypeSymbol.typeParameter(), vType);

        if (cType.typeKind() != NIL) {
            assertEquals(streamTypeSymbol.completionValueTypeParameter().signature(), cType.signature());
        }

        assertEquals(streamTypeSymbol.signature(), signature);

    }

    @DataProvider(name = "streamTypeBuilderProvider")
    private Object[][] getStreamTypeBuilder() {
        return new Object[][] {
                {types.FLOAT, types.INT, STREAM, "stream<float, int>"},
                {types.BYTE, types.STRING, STREAM, "stream<byte, string>"},
                {types.ANY, types.NIL, STREAM, "stream<any>"},
        };
    }
}
