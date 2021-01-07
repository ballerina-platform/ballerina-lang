/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.api.symbols.Qualifier.CLIENT;
import static io.ballerina.compiler.api.symbols.Qualifier.CONFIGURABLE;
import static io.ballerina.compiler.api.symbols.Qualifier.FINAL;
import static io.ballerina.compiler.api.symbols.Qualifier.ISOLATED;
import static io.ballerina.compiler.api.symbols.Qualifier.LISTENER;
import static io.ballerina.compiler.api.symbols.Qualifier.PUBLIC;
import static io.ballerina.compiler.api.symbols.Qualifier.READONLY;
import static io.ballerina.compiler.api.symbols.Qualifier.REMOTE;
import static io.ballerina.compiler.api.symbols.Qualifier.RESOURCE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for validating the availability of the information provided in the internal symbol through flags in the
 * public API through qualifiers.
 *
 * @since 2.0.0
 */
public class SymbolFlagToQualifierMappingTest {

    SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel(
                "test-src/symbol_flag_to_qualifier_mapping_test.bal");
    }

    @Test(dataProvider = "QualifierProvider")
    public void testFlagToQualifierMapping(int line, int column, Set<Qualifier> expQuals) {
        Optional<Symbol> optionalSymbol = model.symbol("symbol_flag_to_qualifier_mapping_test.bal",
                                                       LinePosition.from(line, column));
        Qualifiable symbol = (Qualifiable) optionalSymbol.get();
        assertTrue(symbol.qualifiers().containsAll(expQuals) && symbol.qualifiers().size() == expQuals.size());
    }

    @DataProvider(name = "QualifierProvider")
    public Object[][] getPositionsAndQualifiers() {
        return new Object[][]{
                {16, 27, Set.of(LISTENER, FINAL)},
                {18, 13, new HashSet<Qualifier>()},
                {19, 20, Set.of(PUBLIC)},
                {35, 19, Set.of(READONLY)},
                {49, 20, Set.of(ISOLATED)},
                {52, 22, Set.of(READONLY, PUBLIC)},
                {56, 16, Set.of(CLIENT)},
                {57, 22, Set.of(REMOTE)},
                {65, 17, Set.of(LISTENER, FINAL)},
                {66, 24, Set.of(RESOURCE)},
                {77, 19, Set.of(CONFIGURABLE)},
                {78, 20, Set.of(CONFIGURABLE)}
        };
    }

    @Test(dataProvider = "SymbolKindProvider")
    public void testFlagToSymbolKindMapping(int line, int column, SymbolKind kind) {
        Optional<Symbol> optionalSymbol = model.symbol("symbol_flag_to_qualifier_mapping_test.bal",
                                                       LinePosition.from(line, column));
        Symbol symbol = optionalSymbol.get();
        assertEquals(symbol.kind(), kind);
    }

    @DataProvider(name = "SymbolKindProvider")
    public Object[][] getSymbolKinds() {
        return new Object[][]{
                {19, 20, SymbolKind.METHOD},
                {75, 7, SymbolKind.CONSTANT},
        };
    }
}
