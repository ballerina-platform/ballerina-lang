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

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;

/**
 * Test cases for symbols visible at various points in query expressions.
 */
@Test
public class VisibleSymbolsInQueryExprsTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/symbol_lookup_in_query.bal";
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expModuleSymbols =
                List.of(
                        from("arr1", VARIABLE),
                        from("arr2", VARIABLE),
                        from("test", FUNCTION),
                        from("Person", TYPE_DEFINITION)
                );
        return new Object[][]{
                {22, 21, concat(expModuleSymbols,
                                from("i", VARIABLE),
                                from("j", VARIABLE),
                                from("res1", VARIABLE)
                )},
                {28, 21, concat(expModuleSymbols,
                                from("i", VARIABLE),
                                from("j", VARIABLE),
                                from("res1", VARIABLE),
                                from("intVal", VARIABLE),
                                from("stringVal", VARIABLE),
                                from("res2", VARIABLE)
                )},
                {31, 20, concat(expModuleSymbols,
                                from("ii", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE)
                )},
                {36, 20, concat(expModuleSymbols,
                                from("i", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE)
                )},
                {36, 28, concat(expModuleSymbols,
                                from("j", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE)
                )},
                {44, 25, concat(expModuleSymbols,
                                from("p", VARIABLE),
                                from("p1", VARIABLE),
                                from("p2", VARIABLE),
                                from("personList", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE),
                                from("res4", VARIABLE),
                                from("res5", VARIABLE)
                )},
                {48, 42, concat(expModuleSymbols,
                                from("k", VARIABLE),
                                from("p1", VARIABLE),
                                from("p2", VARIABLE),
                                from("personList", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE),
                                from("res4", VARIABLE),
                                from("res5", VARIABLE),
                                from("res6", VARIABLE)
                )},
                {49, 23, concat(expModuleSymbols,
                                from("i", VARIABLE),
                                from("p1", VARIABLE),
                                from("p2", VARIABLE),
                                from("personList", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE),
                                from("res4", VARIABLE),
                                from("res5", VARIABLE),
                                from("res6", VARIABLE)
                )},
                {53, 58, concat(expModuleSymbols,
                                from("m", VARIABLE),
                                from("p1", VARIABLE),
                                from("p2", VARIABLE),
                                from("personList", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE),
                                from("res4", VARIABLE),
                                from("res5", VARIABLE),
                                from("res6", VARIABLE),
                                from("res7", VARIABLE)
                )},
                {53, 64, concat(expModuleSymbols,
                                from("ii", VARIABLE),
                                from("p1", VARIABLE),
                                from("p2", VARIABLE),
                                from("personList", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE),
                                from("res3", VARIABLE),
                                from("res4", VARIABLE),
                                from("res5", VARIABLE),
                                from("res6", VARIABLE)
                )},

        };
    }
}
