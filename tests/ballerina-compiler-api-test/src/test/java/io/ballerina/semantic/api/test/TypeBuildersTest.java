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
import io.ballerina.compiler.api.symbols.TypeDescKind;
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

import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

}
