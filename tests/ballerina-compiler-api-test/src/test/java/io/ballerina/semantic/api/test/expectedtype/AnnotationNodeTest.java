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
    private final String INFO_TYPE_SIGNATURE = "Info";
    private final String DOC_TYPE_SIGNATURE = "int|record {|string description; int id; string name?; anydata...;|}?";

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/expected-type/annotation_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "LinePosProvider")
    public void testExpectedType(int infoLine, int infoCol, int docLine, int docCol) {
        assertTypeSymbol(infoLine, infoCol, TypeDescKind.TYPE_REFERENCE, INFO_TYPE_SIGNATURE);
            assertTypeSymbol(docLine, docCol, TypeDescKind.UNION, DOC_TYPE_SIGNATURE);
    }

    @DataProvider(name = "LinePosProvider")
    public Object[][] getLinePos() {
        return new Object[][]{
                {19, 11, 20, 10},
                {25, 11, 26, 10},
                {33, 11, 34, 10},
                {39, 11, 40, 10},
                {47, 11, 48, 10},
                {52, 15, 53, 14},
                {58, 15, 59, 14},
                {85, 11, 86, 10},
                {90, 15, 91, 14},
                {96, 15, 97, 14},
                {103, 11, 104, 10},
                {108, 15, 109, 14},
                {116, 15, 117, 14},
                {125, 11, 126, 10},
                {130, 15, 131, 14},
                {136, 15, 137, 14},
                {144, 15, 145, 14},
                {150, 15, 151, 14},
                {158, 15, 159, 14},
                {166, 68, 166, 77},
                {168, 58, 168, 67},
                {169, 34, 169, 43},
                {170, 34, 170, 43},
                {171, 34, 171, 43},
                {177, 15, 178, 14},
                {184, 11, 185, 10},
                {190, 11, 191, 10},
                {197, 15, 198, 14},
                {202, 38, 202, 47},
                {208, 11, 209, 10},
                {214, 34, 214, 43},
//                {217, 15, 218, 14}, Blocked by #41805
                {226, 11, 227, 10},
                {231, 15, 232, 14},
                {237, 15, 238, 14},
                {245, 15, 246, 14},
                {255, 11, 256, 10},
                {262, 15, 263, 14},
//                {268, 27, 268, 36}, Blocked by #41787
//                {274, 15, 275, 14}, Blocked by #41803
                {281, 75, 281, 84},
                {285, 71, 285, 80},
                {291, 11, 292, 10},
                {296, 46, 296, 54},
        };
    }

    private void assertTypeSymbol(int line, int col, TypeDescKind expectedKind, String expectedSignature) {
        Optional<TypeSymbol> typeSymbol = model.expectedType(srcFile, LinePosition.from(line, col));
        assertTrue(typeSymbol.isPresent());
        assertEquals(typeSymbol.get().typeKind(), expectedKind);
        assertEquals(typeSymbol.get().signature(), expectedSignature);
    }
}
