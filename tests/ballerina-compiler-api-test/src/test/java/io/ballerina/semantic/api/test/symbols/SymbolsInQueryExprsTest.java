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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() with query expressions.
 *
 * @since 2.0.0
 */
public class SymbolsInQueryExprsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_query_exprs_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "QueryExprPosProvider")
    public void testQueryExprs(int line, int col, SymbolKind kind, String name) {
        assertSymbol(line, col, kind, name);
    }

    @DataProvider(name = "QueryExprPosProvider")
    public Object[][] getQueryExprPos() {
        return new Object[][]{
                {21, 18, null, null},
                {21, 27, VARIABLE, "st"},
                {21, 33, VARIABLE, "students"},
                {21, 50, RECORD_FIELD, "id"},
                {21, 54, VARIABLE, "st"},
                {21, 57, RECORD_FIELD, "id"},
                {22, 45, TYPE, "Person"},
                {26, 22, VARIABLE, "st"},
                {26, 25, RECORD_FIELD, "fname"},
                {31, 27, VARIABLE, "name"},
                {31, 34, VARIABLE, "st"},
                {31, 54, RECORD_FIELD, "lname"},
                {36, 26, VARIABLE, "id"},
                {36, 39, VARIABLE, "people"},
                {36, 49, VARIABLE, "st"},
                {36, 62, VARIABLE, "id"},
                {41, 25, VARIABLE, "st"},
                {42, 20, VARIABLE, "st"},
                {46, 22, CONSTANT, "LIMIT"},
                {50, 75, FUNCTION, "toStream"},
                {52, 74, VARIABLE, "st"},
        };
    }

    // utils

    private void assertSymbol(int line, int col, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (kind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        Assert.assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().getName().get(), name);
    }
}
