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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
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
 * Test cases for use of symbol() with wait action.
 *
 * @since 2.0.0
 */
public class SymbolsInWaitActionsTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/actions/symbols_in_wait_actions_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosInWaitAction")
    public void testSymbolsInWaitAction(int line, int col, String name, SymbolKind symbolKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (name == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), symbolKind);
        assertTrue(symbol.get().getName().isPresent());
        assertEquals(symbol.get().getName().get(), name);
    }

    @DataProvider(name = "PosInWaitAction")
    public Object[][] getPosSymbolsInWaitAction() {
        return new Object[][]{
                // Single Wait Action
                {24, 28, "foo", SymbolKind.FUNCTION},
                {24, 32, "name", SymbolKind.VARIABLE},
                {24, 38, "b", SymbolKind.PARAMETER},
                {24, 42, "age", SymbolKind.VARIABLE},
                {25, 25, "W1", SymbolKind.WORKER},

                // Multiple Wait Action
                {43, 51, null, null},
                {43, 54, "WA", SymbolKind.WORKER},
                {43, 58, null, null},
                {43, 61, "WB", SymbolKind.WORKER},
                {44, 51, "WC", SymbolKind.WORKER},
                {44, 55, "WD", SymbolKind.WORKER},
                {45, 63, "a", SymbolKind.RECORD_FIELD},
                {45, 66, "WA", SymbolKind.WORKER},
                {45, 70, "b", SymbolKind.RECORD_FIELD},
                {45, 73, "WB", SymbolKind.WORKER},

                // Alternate Wait Action
                {57, 29, "WA", SymbolKind.WORKER},
                {57, 34, "WB", SymbolKind.WORKER},
        };
    }
}
