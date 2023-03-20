/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.semantic.api.test.expectedtype;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;

/**
 * Test cases to find the expected types in function-call expressions.
 *
 * @since 2201.4.0
 */
public class FunctionCallExpressionTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.
                loadProject("test-src/expected-type/function_call_expression_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "LinePosProvider")
    public void testExpectedType(int line, int col, TypeDescKind typeDescKind) {
        Optional<TypeSymbol> typeSymbol = model.
                expectedType(srcFile, LinePosition.from(line, col));
        assertEquals(typeSymbol.get().typeKind(), typeDescKind);
    }

    @DataProvider(name = "LinePosProvider")
    public Object[][] getLinePos() {
        return new Object[][]{
                {24, 88, TypeDescKind.FUNCTION},
                {37, 15, TypeDescKind.TYPE_REFERENCE},
                {42, 13, TypeDescKind.INT},
                {43, 25, TypeDescKind.INT},
                {44, 34, TypeDescKind.INT},
                {52, 36, TypeDescKind.STRING},
                {62, 51, TypeDescKind.TYPE_REFERENCE},
                {78, 63, TypeDescKind.TYPE_REFERENCE},
                {96, 28, TypeDescKind.INT},
                {97, 16, TypeDescKind.INT},
                {98, 16, TypeDescKind.INT},
                {100, 34, TypeDescKind.UNION},
                {101, 24, TypeDescKind.INT},
                {110, 25, TypeDescKind.TYPEDESC},
                {111, 27, TypeDescKind.TYPEDESC},
                {114, 20, TypeDescKind.STRING},
                {115, 21, TypeDescKind.INT},
                {116, 22, TypeDescKind.INT},
                {120, 8, TypeDescKind.INT},
                {121, 12, TypeDescKind.INT},
                {122, 19, TypeDescKind.STRING},
                {123, 20, TypeDescKind.INT},
                {124, 22, TypeDescKind.INT},
                {136, 32, TypeDescKind.STRING},
        };
    }
}
