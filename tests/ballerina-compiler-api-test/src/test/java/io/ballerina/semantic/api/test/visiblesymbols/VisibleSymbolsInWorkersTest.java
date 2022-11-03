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
import static io.ballerina.compiler.api.symbols.SymbolKind.WORKER;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;

/**
 * Test cases for symbols visible at various points in workers and fork statement.
 */
@Test
public class VisibleSymbolsInWorkersTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/symbol_lookup_with_workers_test.bal";
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expModuleSymbols =
                List.of(
                        from("aString", VARIABLE),
                        from("workerSendToWorker", FUNCTION),
                        from("testFork", FUNCTION),
                        from("testWorkerSendReceive", FUNCTION),
                        from("testFlushWithoutWorkerName", FUNCTION)
                );
        List<ExpectedSymbolInfo> commonSyms = concat(expModuleSymbols,
                                                     from("w1", WORKER),
                                                     from("w2", WORKER));
        return new Object[][]{
                {18, 50, commonSyms},
                {20, 15, concat(expModuleSymbols, from("w2", WORKER))},
                {22, 14, concat(expModuleSymbols,
                        from("w2", WORKER),
                        from("i", VARIABLE)
                )},
                {24, 0, concat(expModuleSymbols,
                        from("w2", WORKER),
                        from("i", VARIABLE)
                )},
                {29, 12, commonSyms},
                {33, 12, concat(expModuleSymbols,
                        from("w1", WORKER),
                        from("j", VARIABLE)
                )},
                {38, 22, concat(commonSyms, from("ret", VARIABLE))},
                {45, 4, concat(expModuleSymbols,
                               from("x", VARIABLE),
                               from("w1", WORKER)
                )},
                {46, 10, concat(expModuleSymbols,
                                from("x", VARIABLE),
                                from("w1", WORKER)
                )},
                {47, 19, concat(expModuleSymbols, from("x", VARIABLE))},
                {49, 10, concat(expModuleSymbols,
                                from("x", VARIABLE),
                                from("i", VARIABLE)
                )},
                {57, 12, concat(expModuleSymbols,
                        from("a", VARIABLE),
                        from("wrk2", WORKER),
                        from("wrk3", WORKER)
                )},
                {64, 18, concat(expModuleSymbols,
                        from("b", VARIABLE),
                        from("wrk1", WORKER),
                        from("wrk2", WORKER)
                )},
                {71, 26, concat(expModuleSymbols,
                        from("err", VARIABLE),
                        from("w2", WORKER)
                )},
        };
    }
}
