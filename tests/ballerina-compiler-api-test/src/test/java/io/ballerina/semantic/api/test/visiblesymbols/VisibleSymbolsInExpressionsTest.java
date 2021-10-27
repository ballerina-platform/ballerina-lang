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

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;

/**
 * Test cases for symbols visible at various points in expressions.
 */
@Test
public class VisibleSymbolsInExpressionsTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/visible_symbols_in_exprs_test.bal";
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expModuleSymbols =
                List.of(
                        from("x", VARIABLE),
                        from("testObjectConstructor", FUNCTION),
                        from("testAnonFuncs", FUNCTION),
                        from("testMiscExprs", FUNCTION),
                        from("Person", TYPE_DEFINITION)
                );
        return new Object[][]{
                // TODO: Will fail once closure support is added
                {22, 22, expModuleSymbols},
                {24, 4, expModuleSymbols},
                {25, 22, concat(expModuleSymbols,
                                from("self", VARIABLE),
                                from("name", CLASS_FIELD),
                                from("age", CLASS_FIELD),
                                from("init", METHOD)
//                                from("getName", METHOD)
                )},
                {26, 25, concat(expModuleSymbols,
                                from("self", VARIABLE),
                                from("name", CLASS_FIELD),
                                from("age", CLASS_FIELD),
                                from("init", METHOD)
//                                from("getName", METHOD)
                )},
                {29, 43, concat(expModuleSymbols,
                                from("self", VARIABLE),
                                from("name", CLASS_FIELD),
                                from("age", CLASS_FIELD),
                                from("init", METHOD),
                                from("getName", METHOD)
                )},
                {32, 6, concat(expModuleSymbols,
                               from("y", VARIABLE),
                               from("obj", VARIABLE)
                )},
//                {38, 24, concat(expModuleSymbols, from("y", VARIABLE))},
                {38, 35, concat(expModuleSymbols,
//                                from("y", VARIABLE),
                                from("s", PARAMETER)
                )},
                {39, 22, concat(expModuleSymbols,
//                                from("y", VARIABLE),
                                from("s", PARAMETER),
                                from("z", VARIABLE)
                )},
                {42, 32, concat(expModuleSymbols,
//                                from("y", VARIABLE),
//                                from("y", VARIABLE),
                                from("s", PARAMETER) // TODO: Check if this is correct
                )},
                {42, 53, concat(expModuleSymbols,
//                                from("y", VARIABLE),
//                                from("fn2", VARIABLE),
                                from("s", PARAMETER)
                )},
                {44, 39, concat(expModuleSymbols,
                                from("y", VARIABLE),
                                from("fn1", VARIABLE),
                                from("fn2", VARIABLE),
                                from("fn3", VARIABLE) // TODO: This shouldn't be visible
                )},
                {44, 48, concat(expModuleSymbols,
                                from("y", VARIABLE),
                                from("fn1", VARIABLE),
                                from("fn2", VARIABLE),
                                from("fn3", VARIABLE) // TODO: This shouldn't be visible
//                                from("p", PARAMETER) // TODO: Should be visible
                )},
                {48, 12, concat(expModuleSymbols, from("b", VARIABLE))},
                {48, 16, concat(expModuleSymbols, from("b", VARIABLE))},
                {48, 27, concat(expModuleSymbols,
                                from("b", VARIABLE),
                                from("p", VARIABLE)
                )},
                {48, 35, concat(expModuleSymbols,
                                from("b", VARIABLE),
                                from("p", VARIABLE),
                                from("z", VARIABLE)
                )},
                {48, 42, concat(expModuleSymbols,
                                from("b", VARIABLE),
                                from("p", VARIABLE),
                                from("z", VARIABLE)
                )},
                {50, 50, concat(expModuleSymbols,
                                from("b", VARIABLE),
                                from("strTemp", VARIABLE)
                )},
                {52, 53, concat(expModuleSymbols,
                                from("b", VARIABLE),
                                from("strTemp", VARIABLE),
                                from("rawTemp", VARIABLE)
                )},
        };
    }
}
