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
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import io.ballerina.compiler.api.types.TypeBuilder;
import io.ballerina.compiler.api.types.Types;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the builders in Types API
 *
 * @since 2.0.0
 */
public class TypeBuildersTest {

    private Types types;
    private TypeBuilder builder;

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
    }

    @Test(dataProvider = "xmlTypeBuilderProvider")
    public void testXMLTypeBuilder(TypeSymbol typeParam, TypeDescKind typeDescKind, String signature) {
        TypeBuilder builder = types.builder();
        XMLTypeSymbol xmlTypeSymbol = builder.XML.withTypeParam(typeParam).build();
        assertEquals(xmlTypeSymbol.typeKind(), typeDescKind);
        if (typeParam != null) {
            assertTrue(xmlTypeSymbol.typeParameter().isPresent());
            assertEquals(xmlTypeSymbol.typeParameter().get().signature(), typeParam.signature());
        }

        assertEquals(xmlTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "xmlTypeBuilderProvider")
    private Object[][] getXMLTypeBuilders() {
        return new Object[][] {
                {types.STRING, XML, "xml<string>"},
                {types.INT, XML, "xml<int>"},
                {null, XML, "xml"},
                {builder.XML.withTypeParam(types.FLOAT).build(), XML, "xml<xml<float>>"}
        };
    }

    @Test(dataProvider = "mapTypeBuilderProvider")
    public void testMapTypeBuilder(TypeSymbol typeParam, TypeDescKind typeDescKind, String signature) {
        TypeBuilder builder = types.builder();
        MapTypeSymbol mapTypeSymbol = builder.MAP.withTypeParam(typeParam).build();
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
        FutureTypeSymbol futureTypeSymbol = builder.FUTURE.withTypeParam(typeParam).build();
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
//                {types.INT, FUTURE, "future<int>"},
//                {null, FUTURE, "future"},
        };
    }

    @Test(dataProvider = "typedescTypeBuilderProvider")
    public void testTypeDescTypeBuilder(TypeSymbol typeParam, TypeDescKind typeDescKind, String signature) {
        TypeBuilder builder = types.builder();
        TypeDescTypeSymbol typeDescTypeSymbol = builder.TYPEDESC.withTypeParam(typeParam).build();
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
}
