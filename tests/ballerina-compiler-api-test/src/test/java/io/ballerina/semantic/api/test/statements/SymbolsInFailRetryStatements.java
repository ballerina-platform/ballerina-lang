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
 * Test cases for use of symbol() within fail and retry statements.
 *
 * @since 2.0.0
 */
public class SymbolsInFailRetryStatements {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/statements/symbols_in_fail_retry_stmt.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosSymbols")
    public void testSymbols(int line, int col, String expName, SymbolKind expSymbolKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymbolKind);
    }

    @DataProvider(name = "PosSymbols")
    public Object[][] getSymbolsPos() {
        return new Object[][]{
                {21, 8, "count", SymbolKind.VARIABLE},
                {23, 12, "str", SymbolKind.VARIABLE},
                {24, 12, "func", SymbolKind.FUNCTION},
                {27, 13, "er", SymbolKind.VARIABLE},
                {33, 10, "RetryMgr", SymbolKind.TYPE},
                {35, 8, "func", SymbolKind.FUNCTION},
                {36, 13, "func2", SymbolKind.FUNCTION},
                {42, 12, "value", SymbolKind.VARIABLE},
                {44, 20, "e", SymbolKind.VARIABLE},
                {45, 13, "func2", SymbolKind.FUNCTION},
        };
    }
}
