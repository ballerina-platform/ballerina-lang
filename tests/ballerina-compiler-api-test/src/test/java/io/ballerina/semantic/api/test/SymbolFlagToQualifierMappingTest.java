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
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.compiler.api.symbols.Qualifier.CLIENT;
import static io.ballerina.compiler.api.symbols.Qualifier.FINAL;
import static io.ballerina.compiler.api.symbols.Qualifier.ISOLATED;
import static io.ballerina.compiler.api.symbols.Qualifier.LISTENER;
import static io.ballerina.compiler.api.symbols.Qualifier.PUBLIC;
import static io.ballerina.compiler.api.symbols.Qualifier.READONLY;
import static io.ballerina.compiler.api.symbols.Qualifier.REMOTE;
import static io.ballerina.compiler.api.symbols.Qualifier.RESOURCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.compile;
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
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_flag_to_qualifier_mapping_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        model = new BallerinaSemanticModel(pkg, context);
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
                {18, 27, getQualifiers(LISTENER, FINAL)},
                {20, 13, Collections.EMPTY_SET},
                {23, 25, getQualifiers(PUBLIC)},
                {39, 19, getQualifiers(READONLY)},
                {53, 20, getQualifiers(ISOLATED)},
                {56, 22, getQualifiers(READONLY)},
                {60, 16, getQualifiers(CLIENT)},
                {61, 22, getQualifiers(REMOTE)},
                {70, 24, getQualifiers(RESOURCE)},
//                {75, 13, getQualifiers(DISTINCT)}, // TODO: enable once issue #26212 is fixed
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
                {23, 25, SymbolKind.METHOD},
                {69, 10, SymbolKind.SERVICE},
                {81, 7, SymbolKind.CONSTANT},
        };
    }

    private Set<Qualifier> getQualifiers(Qualifier... quals) {
        return Stream.of(quals).collect(Collectors.toSet());
    }
}
