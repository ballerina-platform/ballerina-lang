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

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM_MEMBER;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.RESOURCE_METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.SymbolKind.XMLNS;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;

/**
 * Test cases for symbols visible at various points in module level decls.
 */
@Test
public class VisibleSymbolsInModuleLevelDeclsTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/visible_symbols_in_module_level_decls_test.bal";
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expModuleSymbols =
                List.of(
//                        from("val", SymbolKind.MODULE),
                        from("b7a", XMLNS),
                        from("GR", CONSTANT),
                        from("lsn", VARIABLE),
                        from("gFoo", VARIABLE),
                        from("Error", TYPE_DEFINITION),
                        from("Person", TYPE_DEFINITION),
                        from("Colour", ENUM),
                        from("v1", ANNOTATION),
                        from("foo", FUNCTION),
                        from("PersonClz", CLASS),
                        from("Listener", CLASS),
                        from("RED", ENUM_MEMBER),
                        from("GREEN", ENUM_MEMBER),
                        from("BLUE", ENUM_MEMBER),
                        from("exprBodyScope", FUNCTION)
                );
        return new Object[][]{
                {15, 0, expModuleSymbols},
                {20, 11, expModuleSymbols},
                {22, 24, expModuleSymbols},
                {23, 11, expModuleSymbols},
                {26, 5, expModuleSymbols},
                {26, 36, expModuleSymbols},
                {30, 13, expModuleSymbols},
                {33, 5, expModuleSymbols},
                {34, 17, expModuleSymbols},
                {37, 18, expModuleSymbols},
                {39, 9, expModuleSymbols},
                {39, 13, expModuleSymbols},
                {39, 28, concat(expModuleSymbols,
                                from("a", PARAMETER),
                                from("b", PARAMETER) // TODO: This is incorrect
                )},
                {40, 5, concat(expModuleSymbols,
                               from("a", PARAMETER),
                               from("b", PARAMETER),
                               from("c", PARAMETER)
                )},
                {44, 18, expModuleSymbols},
                {47, 21, concat(expModuleSymbols,
                                from("self", VARIABLE),
                                from("name", CLASS_FIELD),
                                from("age", CLASS_FIELD),
                                from("getName", METHOD)
                )},
                {47, 39, concat(expModuleSymbols,
                                from("self", VARIABLE),
                                from("name", CLASS_FIELD),
                                from("age", CLASS_FIELD),
                                from("getName", METHOD)
                )},
                {53, 8, expModuleSymbols},
                {55, 19, expModuleSymbols},
                {58, 32, concat(expModuleSymbols,
                                from("self", VARIABLE),
                                from("greet", CLASS_FIELD),
                                from("x", CLASS_FIELD),
                                from("get", RESOURCE_METHOD)
                )},
                {61, 56, concat(expModuleSymbols, from("myStr", PARAMETER))},
        };
    }
}
