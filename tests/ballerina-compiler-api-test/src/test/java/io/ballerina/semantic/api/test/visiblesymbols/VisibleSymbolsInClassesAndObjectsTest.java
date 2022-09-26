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

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.visiblesymbols.BaseVisibleSymbolsTest.ExpectedSymbolInfo.from;

/**
 * Test cases for symbols visible at various points in classes and objects.
 */
@Test
public class VisibleSymbolsInClassesAndObjectsTest extends BaseVisibleSymbolsTest {

    @Override
    String getTestSourcePath() {
        return "test-src/visiblesymbols/visible_symbols_in_classes_and_objects.bal";
    }

    @DataProvider(name = "PositionProvider")
    @Override
    public Object[][] getLookupPositions() {
        List<ExpectedSymbolInfo> expModuleSymbols =
                List.of(
                        from("Vehicle", CLASS),
                        from("testObjectConstructor", FUNCTION),
                        from("testObjectConstructor", FUNCTION)
                );

        return new Object[][]{
                {20, 33, concat(expModuleSymbols,
                        from("vehicleType", CLASS_FIELD),
                        from("getType", METHOD),
                        from("vType", PARAMETER),
                        from("init", METHOD),
                        from("self", VARIABLE))},
                {29, 29, concat(expModuleSymbols,
                        from("name", CLASS_FIELD),
                        from("generateName", METHOD),
                        from("age", CLASS_FIELD),
                        from("init", METHOD),
                        from("obj", VARIABLE),
                        from("self", VARIABLE))},
                {49, 25, concat(expModuleSymbols,
                        from("y", VARIABLE),
                        from("name", CLASS_FIELD),
                        from("getName", METHOD),
                        from("age", CLASS_FIELD),
                        from("init", METHOD),
                        from("obj", VARIABLE),
                        from("self", VARIABLE))},
                {62, 23, concat(expModuleSymbols,
                        from("objectVar6", VARIABLE))},
                {63, 39, concat(expModuleSymbols,
                        from("objectVar6", VARIABLE),
                        from("self", VARIABLE))}
        };
    }
}
