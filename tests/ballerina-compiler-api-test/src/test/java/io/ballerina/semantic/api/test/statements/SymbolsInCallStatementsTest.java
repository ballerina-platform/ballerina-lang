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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() within call statements.
 *
 * @since 2.0.0
 */
public class SymbolsInCallStatementsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/statements/symbols_in_call_stmt.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "FunctionsPosProvider")
    public void testFunctions(int line, int col, String expName, SymbolKind expSymbolKind, List<String> expParams) {
        FunctionSymbol functionSymbol =
                (FunctionSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymbolKind);

        FunctionTypeSymbol fnType = functionSymbol.typeDescriptor();

        Optional<List<ParameterSymbol>> params = fnType.params();

        assertTrue(params.isPresent());
        List<ParameterSymbol> paramSymbols = params.get();
        assertList(paramSymbols, expParams);
    }

    @DataProvider(name = "FunctionsPosProvider")
    public Object[][] getFunctionPos() {
        return new Object[][]{
                {22, 4, "foo", SymbolKind.FUNCTION, List.of("name", "age")},
                {23, 4, "foo", SymbolKind.FUNCTION, List.of("name", "age")},
                {25, 10, "bar", SymbolKind.FUNCTION, List.of("name", "age")},
                {26, 15, "bar", SymbolKind.FUNCTION, List.of("name", "age")},
                {28, 8, "eat", SymbolKind.METHOD, List.of()},
                {30, 8, "bark", SymbolKind.METHOD, List.of("i", "j")},
                {31, 14, "walk", SymbolKind.METHOD, List.of()},
                {32, 19, "walk", SymbolKind.METHOD, List.of()},
        };
    }

    @Test(dataProvider = "PosProvider")
    public void testSymbols(int line, int col, String expName, SymbolKind expSymbolKind) {
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymbolKind);
        symbol.getName();
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {22, 16, "val", SymbolKind.VARIABLE},
                {23, 14, "age", SymbolKind.PARAMETER},
                {23, 20, "val", SymbolKind.VARIABLE},
                {24, 22, "ints", SymbolKind.VARIABLE},
                {25, 14, "name", SymbolKind.VARIABLE},
                {26, 25, "val", SymbolKind.VARIABLE},
                {28, 4, "dog", SymbolKind.VARIABLE},
                {29, 13, "val", SymbolKind.VARIABLE},
                {29, 18, "j", SymbolKind.PARAMETER},
                {29, 22, "name", SymbolKind.VARIABLE},
                {30, 27, "ints", SymbolKind.VARIABLE},
                {31, 10, "dog", SymbolKind.VARIABLE},
        };
    }
}
