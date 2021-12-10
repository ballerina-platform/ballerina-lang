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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.SymbolKind.XMLNS;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;

/**
 * Test cases for symbols visible at various points in statements.
 */
@Test
public class VisibleSymbolsInStatementsTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/var_symbol_lookup_test.bal";
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expModuleSymbols =
                List.of(
                        from("aString", VARIABLE),
                        from("anInt", VARIABLE),
                        from("test", FUNCTION),
                        from("test2", FUNCTION),
                        from("HELLO", CONSTANT),
                        from("testLetExp1", FUNCTION),
                        from("testLetExp2", FUNCTION),
                        from("testLetExp3", FUNCTION)
                );
        return new Object[][]{
                {2, 13, expModuleSymbols},
                {19, 17, expModuleSymbols},
//                {20, 30, expModuleSymbols}, TODO: issue #25607
                {20, 38, concat(expModuleSymbols, from("greet", VARIABLE))},
                {21, 0, concat(expModuleSymbols, from("greet", VARIABLE))},
                // TODO: issue #25607
//                {22, 59, concat(expModuleSymbols,
//                                from("greet", VARIABLE),
//                                from("name", VARIABLE)
//                )},
                {30, 12, concat(expModuleSymbols,
                                from("greet", VARIABLE),
                                from("a", VARIABLE),
                                from("x", VARIABLE),
                                from("greetFn", VARIABLE)
                )},
                {35, 19, concat(expModuleSymbols,
                                from("b", VARIABLE),
                                from("greet", VARIABLE),
                                from("greetFn", VARIABLE)
                )},
                {39, 0, concat(expModuleSymbols,
                               from("greet", VARIABLE),
                               from("greetFn", VARIABLE),
                               from("z", VARIABLE)
                )},
                {41, 18, expModuleSymbols},
                {43, 4, concat(expModuleSymbols, from("b7a", XMLNS))},
                {45, 8, concat(expModuleSymbols,
                               from("b7a", XMLNS),
                               from("x", VARIABLE)
                )},
                {46, 9, concat(expModuleSymbols,
                               from("b7a", XMLNS),
                               from("x", VARIABLE)
                )},
                {48, 9, concat(expModuleSymbols,
                               from("b7a", XMLNS),
                               from("x", VARIABLE)
                )},
                {54, 51, concat(expModuleSymbols,
                        from("x", PARAMETER),
                        from("x1", VARIABLE),
                        from("x2", VARIABLE)
                )},
                {58, 42, concat(expModuleSymbols,
                        from("y", PARAMETER),
                        from("x3", VARIABLE),
                        from("x4", VARIABLE)
                )},
                {61, 44, concat(expModuleSymbols,
                        from("x", PARAMETER),
                        from("x1", VARIABLE),
                        from("x2", VARIABLE)
                )},
        };
    }
}
