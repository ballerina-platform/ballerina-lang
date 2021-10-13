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

package io.ballerina.semantic.api.test.visiblesymbols;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolsInFile;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for symbols visible at various points in regular compound statements.
 */
@Test
public class VisibleSymbolsInCompoundStatementsTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/symbol_lookup_in_compound_stmts.bal";
    }

    @Test
    public void testSameNameSymbols() {
        Map<String, Symbol> symbolsInFile = getSymbolsInFile(model, srcFile, 55, 8, moduleID);
        assertTrue(symbolsInFile.containsKey("z"));

        Symbol sym = symbolsInFile.get("z");
        assertEquals(sym.kind(), VARIABLE);
        assertEquals(((VariableSymbol) sym).typeDescriptor().typeKind(), TypeDescKind.FLOAT);
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expCommonSymbols =
                List.of(
                        from("c1", CONSTANT),
                        from("c2", CONSTANT),
                        from("testRegularCompoundStmts", FUNCTION),
                        from("v", VARIABLE)
                );
        return new Object[][]{
                {24, 26, concat(expCommonSymbols,
                                from("x1", VARIABLE),
                                from("x2", VARIABLE),
                                from("a", VARIABLE) // TODO: due to issue #25607
                )},
                {28, 26, concat(expCommonSymbols,
                                from("x3", VARIABLE),
                                from("x4", VARIABLE),
                                from("b", VARIABLE) // TODO: due to issue #25607
                )},
                {32, 26, concat(expCommonSymbols,
                                from("x5", VARIABLE),
                                from("x6", VARIABLE),
                                from("c", VARIABLE) // TODO: due to issue #25607
                )},
                {35, 18, concat(expCommonSymbols, from("x7", VARIABLE))},
                {36, 26, concat(expCommonSymbols,
                                from("x7", VARIABLE),
                                from("d", VARIABLE) // TODO: due to issue #25607
                )},
                {39, 9, expCommonSymbols},
                {44, 21, expCommonSymbols},
                {46, 9, concat(expCommonSymbols,
                               from("i", VARIABLE),
                               from("y", VARIABLE)
                )},
                {49, 12, concat(expCommonSymbols,
                                from("i", VARIABLE),
                                from("y", VARIABLE),
                                from("z", VARIABLE)
                )},
                {51, 16, concat(expCommonSymbols,
                                from("i", VARIABLE),
                                from("y", VARIABLE),
                                from("z", VARIABLE)
                )},
                {55, 8, concat(expCommonSymbols,
                               from("i", VARIABLE),
                               from("y", VARIABLE),
                               from("z", VARIABLE)
                )},
                {59, 7, expCommonSymbols},
        };
    }
}
