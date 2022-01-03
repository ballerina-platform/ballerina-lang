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
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;

/**
 * Test cases for symbols visible at various points in query actions.
 */
@Test
public class VisibleSymbolsInQueryActionsTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/symbol_lookup_in_query_actions.bal";
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expCommonSymbols =
                List.of(
                        from("arr1", VARIABLE),
                        from("arr2", VARIABLE),
                        from("test", FUNCTION),
                        from("result", VARIABLE)
                );
        return new Object[][]{
                {25, 33, concat(expCommonSymbols,
                                from("i", VARIABLE),
                                from("j", VARIABLE),
                                from("res1", VARIABLE)
                )},
                {30, 43, concat(expCommonSymbols,
                                from("i", VARIABLE),
                                from("j", VARIABLE),
                                from("stringVal", VARIABLE),
                                from("intVal", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE)
                )},
                {35, 20, concat(expCommonSymbols,
                                from("i", VARIABLE),
                                from("j", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE)
                )},
                {38, 20, concat(expCommonSymbols,
                                from("i", VARIABLE),
                                from("res1", VARIABLE),
                                from("res2", VARIABLE)
                )},
        };
    }
}
