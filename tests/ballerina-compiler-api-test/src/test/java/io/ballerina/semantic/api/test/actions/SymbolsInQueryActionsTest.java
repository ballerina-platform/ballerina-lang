// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.semantic.api.test.actions;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;

/**
 * Test cases for use of symbol() with query actions.
 *
 * @since 2.0.0
 */
public class SymbolsInQueryActionsTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/actions/symbols_in_query_actions_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "SymbolPos")
    public void testSymbols(int line, int col, String name, SymbolKind symbolKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, name, symbolKind);
    }

    @DataProvider(name = "SymbolPos")
    public Object[][] getSymbolPos() {
        return new Object[][]{
                {25, 25, "st", SymbolKind.VARIABLE},
                {25, 31, "students", SymbolKind.VARIABLE},
                {27, 25, "name", SymbolKind.RECORD_FIELD},
                {27, 31, "st", SymbolKind.VARIABLE},
                {27, 34, "fname", SymbolKind.RECORD_FIELD},
                {28, 12, "person_list", SymbolKind.VARIABLE},
        };
    }
}
