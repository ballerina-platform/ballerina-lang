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
import static org.testng.Assert.assertTrue;

/**
 * Test cases to find the expected types in annotations.
 *
 * @since 2201.9.0
 */
public class AnnotationNodeTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/expected-type/annotation_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "LinePosProvider")
    public void testExpectedType(int infoLine, int infoCol, int docLine, int docCol) {
        assertTypeSymbol(infoLine, infoCol, TypeDescKind.TYPE_REFERENCE, "Info");
        assertTypeSymbol(docLine, docCol, TypeDescKind.UNION,
                "int|record {|string description; int id; string name?; anydata...;|}?");
    }

    @DataProvider(name = "LinePosProvider")
    public Object[][] getLinePos() {
        return new Object[][]{
                {18, 11, 19, 10},
                {24, 11, 25, 10},
                {32, 11, 33, 10},
                {38, 11, 39, 10},
                {45, 11, 46, 10},
                {50, 15, 51, 14},
                {56, 15, 57, 14},
                {82, 11, 83, 10},
                {87, 15, 88, 14},
                {93, 15, 94, 14},
                {100, 11, 101, 10},
                {105, 15, 106, 14},
                {112, 15, 113, 14},
                {120, 11, 121, 10},
                {125, 15, 126, 14},
                {131, 15, 132, 14},
                {139, 15, 140, 14},
                {145, 15, 146, 14},
                {152, 15, 153, 14},
                {159, 68, 159, 77},
                {161, 58, 161, 67},
                {162, 34, 162, 43},
                {163, 34, 163, 43},
                {164, 34, 164, 43},
                {169, 15, 170, 14},
                {176, 11, 177, 10},
                {182, 11, 183, 10},
                {189, 15, 190, 14},
                {194, 38, 194, 47},
                {200, 11, 201, 10},
                {206, 34, 206, 43},
//                {209, 15, 210, 14}, Blocked by #41805
                {217, 11, 218, 10},
                {222, 15, 223, 14},
                {228, 15, 229, 14},
                {235, 15, 236, 14},
                {243, 11, 244, 10},
                {250, 15, 251, 14},
//                {256, 27, 256, 36}, Blocked by #41787
//                {262, 15, 263, 14}, Blocked by #41803
                {269, 75, 269, 84},
                {272, 71, 272, 80},
                {277, 11, 278, 10},
                {282, 46, 282, 54},
        };
    }

    private void assertTypeSymbol(int line, int col, TypeDescKind expectedKind, String expectedSignature) {
        Optional<TypeSymbol> typeSymbol = model.expectedType(srcFile, LinePosition.from(line, col));
        assertTrue(typeSymbol.isPresent());
        assertEquals(typeSymbol.get().typeKind(), expectedKind);
        assertEquals(typeSymbol.get().signature(), expectedSignature);
    }
}
