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
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.compiler.api.symbols.Qualifier.FINAL;
import static io.ballerina.compiler.api.symbols.Qualifier.LISTENER;
import static io.ballerina.compiler.api.symbols.Qualifier.PUBLIC;
import static io.ballerina.compiler.api.symbols.Qualifier.RESOURCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for class symbols.
 *
 * @since 2.0.0
 */
public class ServiceSemanticAPITest {

    private SemanticModel model;
    private final String fileName = "service_symbol_test.bal";

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/service_symbol_test.bal");
    }

    @Test
    public void testServiceClass() {
        ClassSymbol symbol = (ClassSymbol) model.symbol(fileName, from(22, 14)).get();

        List<String> expMethods = List.of("foo", "$get$barPath", "$get$foo$path", "$get$.", "$get$foo$baz",
                                          "$get$foo$*", "$get$foo$*$**");
        assertList(symbol.methods(), expMethods);

        MethodSymbol method = symbol.methods().get(0);
        assertEquals(method.qualifiers().size(), 1);
        assertTrue(method.qualifiers().contains(Qualifier.REMOTE));

        method = symbol.methods().get(1);
        assertEquals(method.qualifiers().size(), 1);
        assertTrue(method.qualifiers().contains(Qualifier.RESOURCE));
    }

    @Test
    public void testServiceDeclTypedesc() {
        TypeSymbol symbol = (TypeSymbol) model.symbol(fileName, from(66, 8)).get();
        assertEquals(symbol.typeKind(), TYPE_REFERENCE);
        assertEquals(symbol.name(), "ProcessingService");
        assertEquals(((TypeReferenceTypeSymbol) symbol).typeDescriptor().typeKind(), OBJECT);
    }

    @Test
    public void testServiceDeclListener() {
        VariableSymbol symbol = (VariableSymbol) model.symbol(fileName, from(66, 31)).get();
        assertEquals(symbol.name(), "lsn");
        assertEquals(symbol.typeDescriptor().typeKind(), TYPE_REFERENCE);
        assertEquals(symbol.typeDescriptor().name(), "Listener");
        assertEquals(symbol.qualifiers().size(), 2);
        assertTrue(symbol.qualifiers().contains(LISTENER));
        assertTrue(symbol.qualifiers().contains(FINAL));
    }

    @Test
    public void testServiceDeclField() {
        VariableSymbol symbol = (VariableSymbol) model.symbol(fileName, from(68, 18)).get();
        assertEquals(symbol.name(), "magic");
        assertEquals(symbol.typeDescriptor().typeKind(), STRING);
        assertEquals(symbol.qualifiers().size(), 1);
        assertTrue(symbol.qualifiers().contains(PUBLIC));
    }

    @Test(dataProvider = "ServiceDeclMethodPos")
    public void testServiceDeclMethods(int line, int col, String name, List<Qualifier> quals) {
        MethodSymbol symbol = (MethodSymbol) model.symbol(fileName, from(line, col)).get();
        assertEquals(symbol.name(), name);
        assertEquals(symbol.qualifiers().size(), quals.size());

        for (Qualifier qual : quals) {
            assertTrue(symbol.qualifiers().contains(qual));
        }
    }

    @DataProvider(name = "ServiceDeclMethodPos")
    public Object[][] getMethodPos() {
        return new Object[][]{
                {70, 22, "$get$processRequest", List.of(RESOURCE)},
                {72, 13, "createError", new ArrayList<Qualifier>()}
        };
    }

    @Test(dataProvider = "LookupPosProvider")
    public void testInScopeSymbolLookup(int line, int col, List<String> expSymNames) {
        List<Symbol> inscopeSymbols = model.visibleSymbols("service_symbol_test.bal", from(line, col));
        List<Symbol> sourceFileSymbols = getFilteredSymbolNames(inscopeSymbols);

        assertEquals(sourceFileSymbols.size(), expSymNames.size());
        assertList(sourceFileSymbols, expSymNames);
    }

    @DataProvider(name = "LookupPosProvider")
    public Object[][] getLookupPos() {
        List<String> moduleSymbols = List.of("AServiceType", "Listener", "lsn", "ProcessingService", "AServiceClass");
        return new Object[][]{
                {68, 26, concatSymbols(moduleSymbols, "magic")},
//                {70, 59, concatSymbols(moduleSymbols, "magic", "createError")} // TODO: Fix #27314
        };
    }

    private List<Symbol> getFilteredSymbolNames(List<Symbol> symbols) {
        return symbols.stream()
                .filter(s -> !"ballerina".equals(s.moduleID().orgName()))
                .collect(Collectors.toList());
    }

    private List<String> concatSymbols(List<String> moduleSymbols, String... symbols) {
        return Stream.concat(moduleSymbols.stream(), Arrays.stream(symbols)).collect(Collectors.toList());
    }
}
