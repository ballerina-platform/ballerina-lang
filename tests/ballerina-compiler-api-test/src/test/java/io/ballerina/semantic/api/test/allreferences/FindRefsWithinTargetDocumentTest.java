/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.allreferences;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.allreferences.FindAllReferencesTest.assertLocations;
import static io.ballerina.semantic.api.test.allreferences.FindAllReferencesTest.location;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocument;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSemanticModelOf;

/**
 * Test cases for the finding all references within a given document.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsWithinTargetDocumentTest {

    protected Project project;
    protected SemanticModel model;

    @BeforeClass
    public void setup() {
        project = BCompileUtil.loadProject("test-src/find-all-ref/findRefProj");
        model = getDefaultModulesSemanticModel(project);
    }

    @Test(dataProvider = "PositionProvider")
    public void testFindReferencesOfTargetDocument(String moduleName,
                                                   String srcFile,
                                                   String targetFile,
                                                   LinePosition linePosition,
                                                   List<Location> expLocations) {
        SemanticModel refModel = getSemanticModelOf(project, moduleName);

        Optional<Document> srcDocument = getDocument(project, null, srcFile);
        Optional<Document> targetDocument = getDocument(project, moduleName, targetFile);

        Optional<Symbol> symbol = model.symbol(srcDocument.get(), linePosition);

        List<Location> references = refModel.references(symbol.get(), targetDocument.get(), true);
        assertLocations(references, expLocations);
    }


    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {null, "main.bal", "ref.bal", LinePosition.from(4, 4),
                        List.of(location(0, 16, 21, "ref.bal"))},
                {"refModule", "main.bal", "ref1.bal", LinePosition.from(5, 33),
                        List.of(location(0, 16, 25, "ref1.bal"))},
                {"refModule", "main.bal", "ref2.bal", LinePosition.from(3, 27),
                        List.of(location(0, 11, 15, "ref2.bal"),
                                location(8, 24, 28, "ref2.bal"),
                                location(9, 29, 33, "ref2.bal"))},
                {"refModule", "main.bal", "ref2.bal", LinePosition.from(6, 14),
                        List.of(location(2, 12, 24, "ref2.bal"),
                                location(9, 4, 16, "ref2.bal"))},
        };
    }
}
