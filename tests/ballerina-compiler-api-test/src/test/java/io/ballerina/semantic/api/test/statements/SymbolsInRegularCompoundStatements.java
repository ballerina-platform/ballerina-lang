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

package io.ballerina.semantic.api.test.statements;

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
 * Test cases for use of symbol() within regular compound statements.
 *
 * @since 2.0.0
 */
public class SymbolsInRegularCompoundStatements {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/statements/symbols_in_reg_compound_stmt.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosInDoStatements")
    public void testSymbolsInDoStatements(int line, int col, String expName, SymbolKind expSymbolKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymbolKind);
    }

    @DataProvider(name = "PosInDoStatements")
    public Object[][] getSymbolsPosInDoStatements() {
        return new Object[][]{
                {19, 12, "next_i", SymbolKind.VARIABLE},
                {19, 21, "next", SymbolKind.FUNCTION},
                {19, 26, "i", SymbolKind.VARIABLE},
                {20, 14, "getError", SymbolKind.FUNCTION},
                {22, 18, "e", SymbolKind.VARIABLE},
                {23, 6, "func", SymbolKind.FUNCTION},
        };
    }

    @Test(dataProvider = "PosInWhileStatements")
    public void testSymbolsInWhileStatements(int line, int col, String expName, SymbolKind expSymbolKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymbolKind);
    }

    @DataProvider(name = "PosInWhileStatements")
    public Object[][] getSymbolsPosInWhileStatements() {
        return new Object[][]{
                {29, 10, "next", SymbolKind.FUNCTION},
                {29, 15, "k", SymbolKind.VARIABLE},
                {30, 12, "i", SymbolKind.VARIABLE},
                {30, 16, "next", SymbolKind.FUNCTION},
                {31, 8, "k", SymbolKind.VARIABLE},
        };
    }

    @Test(dataProvider = "PosInForeachStatements")
    public void testSymbolsInForeachStatements(int line, int col, String expName, SymbolKind expSymbolKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymbolKind);
    }

    @DataProvider(name = "PosInForeachStatements")
    public Object[][] getSymbolsPosInForeachStatements() {
        return new Object[][]{
                {36, 16, "i", SymbolKind.VARIABLE},
                {37, 12, "p", SymbolKind.VARIABLE},
                {37, 16, "i", SymbolKind.VARIABLE},
                {38, 15, "str", SymbolKind.VARIABLE},
                {42, 16, "item", SymbolKind.VARIABLE},
                {42, 24, "ints", SymbolKind.VARIABLE},
                {43, 12, "val", SymbolKind.VARIABLE},
                {44, 8, "call", SymbolKind.FUNCTION},
                {47, 19, "a", SymbolKind.VARIABLE},
                {49, 13, "b", SymbolKind.VARIABLE},
        };
    }

    @Test(dataProvider = "PosInLockStatements")
    public void testSymbolsInLockStatements(int line, int col, String expName, SymbolKind expSymbolKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymbolKind);
    }

    @DataProvider(name = "PosInLockStatements")
    public Object[][] getSymbolsPosInLockStatements() {
        return new Object[][]{
                {56, 8, "iVal", SymbolKind.VARIABLE},
                {57, 8, "call", SymbolKind.FUNCTION},
        };
    }
}
