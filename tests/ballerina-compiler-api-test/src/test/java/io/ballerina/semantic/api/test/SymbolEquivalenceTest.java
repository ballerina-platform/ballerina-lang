/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for retrieving annotations from a symbol.
 *
 * @since 2.0.0
 */
public class SymbolEquivalenceTest {

    private SemanticModel model;
    private SemanticModel typesModel;
    private final String fileName = "symbol_at_cursor_basic_test.bal";
    private final String typesFileName = "typedesc_test.bal";

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/symbol_at_cursor_basic_test.bal");
        typesModel = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/typedesc_test.bal");
    }

    @Test(dataProvider = "SymbolPosProvider")
    public void testSymbols(List<LinePosition> positions) {
        List<Symbol> symbols = positions.stream()
                .map(pos -> model.symbol(fileName, pos).get())
                .collect(Collectors.toList());
        assertSymbols(symbols);
    }

    @DataProvider(name = "SymbolPosProvider")
    public Object[][] getSymbolPos() {
        return new Object[][]{
                {List.of(from(16, 7), from(20, 30))},
                {List.of(from(22, 8), from(23, 12))},
                {List.of(from(40, 11), from(43, 11), from(44, 19))},
                {List.of(from(47, 7), from(22, 59))},
                {List.of(from(79, 13), from(76, 25), from(82, 4), from(83, 15))},
        };
    }

    @Test
    public void testSymbolsNegative() {
        List<LinePosition> positions = List.of(from(16, 7), from(17, 4), from(19, 9),
                                               from(20, 11), from(49, 5), from(40, 4));
        List<Symbol> symbols = positions.stream()
                .map(pos -> model.symbol(fileName, pos).get())
                .collect(Collectors.toList());

        for (int i = 0; i < symbols.size(); i++) {
            Symbol symbol = symbols.get(i);

            for (int j = i + 1; j < symbols.size(); j++) {
                assertFalse(symbol.equals(symbols.get(j)),
                            "'" + symbol.name() + "' is equal to '" + symbols.get(j).name() + "'");
                assertNotEquals(symbol.hashCode(), symbols.get(j).hashCode());
            }
        }
    }

    @Test
    public void testTypeRefEquivalence() {
        List<LinePosition> positions = List.of(from(73, 10), from(74, 10), from(75, 10), from(93, 11), from(94, 11),
                                               from(114, 5));
        SemanticModel typeRefModel = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/typedesc_test.bal");
        List<Symbol> symbols = positions.stream()
                .map(pos -> typeRefModel.symbol("typedesc_test.bal", pos).get())
                .collect(Collectors.toList());

        assertSymbols(symbols);
    }

    @Test(dataProvider = "TypeSymbolPosProvider")
    public void testTypedescriptors(List<LinePosition> positions) {
        List<TypeSymbol> types = positions.stream()
                .map(pos -> typesModel.symbol(typesFileName, pos).get())
                .map(s -> ((VariableSymbol) s).typeDescriptor())
                .collect(Collectors.toList());
        assertTypeSymbols(types);
    }

    @DataProvider(name = "TypeSymbolPosProvider")
    public Object[][] getVarPos() {
        return new Object[][]{
                {List.of(from(24, 17), from(43, 8), from(110, 6))},
                {List.of(from(19, 11), from(31, 25))},
                {List.of(from(24, 26), from(128, 10))}
        };
    }

    @Test
    public void testTypedescriptorsNegative() {
        List<LinePosition> positions = List.of(from(24, 17), from(19, 11), from(24, 26));
        List<TypeSymbol> types = positions.stream()
                .map(pos -> typesModel.symbol(typesFileName, pos).get())
                .map(s -> ((VariableSymbol) s).typeDescriptor())
                .collect(Collectors.toList());

        for (int i = 0; i < types.size(); i++) {
            TypeSymbol type = types.get(i);

            for (int j = i + 1; j < types.size(); j++) {
                assertFalse(type.equals(types.get(j)),
                            "'" + type.signature() + "' is equal to '" + types.get(j).signature() + "'");
                assertNotEquals(type.hashCode(), types.get(j).hashCode());
            }
        }
    }

    // utils
    private void assertSymbols(List<Symbol> symbols) {
        for (int i = 0; i < symbols.size(); i++) {
            Symbol symbol = symbols.get(i);

            for (int j = i + 1; j < symbols.size(); j++) {
                assertTrue(symbol.equals(symbols.get(j)),
                           "'" + symbol.name() + "' not equal to '" + symbols.get(j).name() + "'");
                assertEquals(symbol.hashCode(), symbols.get(j).hashCode());
            }
        }
    }

    private void assertTypeSymbols(List<TypeSymbol> types) {
        for (int i = 0; i < types.size(); i++) {
            TypeSymbol type = types.get(i);

            for (int j = i + 1; j < types.size(); j++) {
                assertTrue(type.equals(types.get(j)),
                           "'" + type.signature() + "' not equal to '" + types.get(j).signature() + "'");
                assertEquals(type.hashCode(), types.get(j).hashCode());
            }
        }
    }
}
