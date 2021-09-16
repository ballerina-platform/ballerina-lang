/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semantic.api.test.symbols;

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

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.OBJECT_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() with field access expressions.
 *
 * @since 2.0.0
 */
public class SymbolsInFieldAccessTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_field_access_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void testFields(int line, int col, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (kind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().getName().get(), name);
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {18, 15, VARIABLE, "p"},
                {18, 17, RECORD_FIELD, "name"},
                {20, 14, FUNCTION, "getPerson"},
                {20, 26, RECORD_FIELD, "age"},
                {23, 19, VARIABLE, "j1"},
                {23, 22, null, null},
                {23, 24, null, null},
                {26, 9, VARIABLE, "j2"},
                {26, 11, null, null},
                {26, 13, null, null},
                {29, 14, VARIABLE, "p2"},
                {29, 17, RECORD_FIELD, "address"},
                {29, 25, RECORD_FIELD, "city"},
                {31, 4, VARIABLE, "p2"},
                {31, 7, RECORD_FIELD, "address"},
                {34, 8, VARIABLE, "p3"},
                {34, 11, OBJECT_FIELD, "name"},
                {37, 8, VARIABLE, "p4"},
                {37, 11, CLASS_FIELD, "name"},
        };
    }
}
