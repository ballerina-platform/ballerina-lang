/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;

/**
 * Test cases for ensuring that the visitor and transformer covers all the symbols.
 */
public class VisitorAPITest {

    private final SymbolVisitor visitor = new SymbolVisitor() {
        @Override
        public void visitSymbol(Symbol symbol) {
            symbol.accept(this);
        }
    };

    private final SymbolTransformer<Integer> transformer = new SymbolTransformer<>() {
        private final Set<Symbol> visited = new HashSet<>();

        @Override
        public Integer transformSymbol(Symbol symbol) {
            if (visited.contains(symbol)) {
                return symbol.hashCode();
            }
            visited.add(symbol);
            return symbol.apply(this);
        }
    };

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbol_visitor_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "SymbolPos")
    public void testSymbolVisitor(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        Assert.assertTrue(symbol.isPresent());
        visitor.visitSymbol(symbol.get());
    }

    @Test(dataProvider = "TypeSymbolPos")
    public void testTypeSymbolVisitor(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        Assert.assertTrue(symbol.isPresent());
        Assert.assertEquals(symbol.get().kind(), SymbolKind.VARIABLE);
        visitor.visitSymbol(((VariableSymbol) symbol.get()).typeDescriptor());
    }

    @Test(dataProvider = "SymbolPos")
    public void testSymbolTransformer(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        Assert.assertTrue(symbol.isPresent());
        Assert.assertEquals((int) transformer.transformSymbol(symbol.get()), symbol.get().hashCode());
    }

    @Test(dataProvider = "TypeSymbolPos")
    public void testTypeSymbolTransformer(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        Assert.assertTrue(symbol.isPresent());
        Assert.assertEquals(symbol.get().kind(), SymbolKind.VARIABLE);

        TypeSymbol type = ((VariableSymbol) symbol.get()).typeDescriptor();
        Assert.assertEquals((int) transformer.transformSymbol(type), type.hashCode());
    }

    @DataProvider(name = "SymbolPos")
    public Object[][] getSymbolPos() {
        return new Object[][]{
                {16, 22},
                {18, 11},
                {20, 6},
                {22, 5},
                {26, 5},
                {28, 6},
                {29, 11},
                {31, 13},
                {34, 31},
                {36, 9},
                {36, 21},
                {61, 15},
                {62, 17},
                {65, 20},
                {83, 11},
                {87, 0},
                {89, 22},
                {89, 37},
        };
    }

    @DataProvider(name = "TypeSymbolPos")
    public Object[][] getTypeSymbolPos() {
        return new Object[][]{
                {37, 12},
                {38, 8},
                {39, 10},
                {40, 12},
                {41, 9},
                {42, 12},
                {43, 10},
                {44, 10},
                {45, 13},
                {46, 11},
                {47, 11},
                {48, 21},
                {49, 16},
                {50, 17},
                {51, 17},
                {52, 8},
                {53, 18},
                {54, 19},
                {55, 19},
                {56, 9},
                {57, 16},
                {58, 10},
                {59, 7},
                {63, 6},
                {64, 13},
                {65, 28},
                {66, 7},
                {67, 16},
                {68, 16},
                {69, 11},
                {70, 32},
                {71, 18},
                {72, 18},
                {73, 8},
                {74, 15},
                {75, 16},
                {76, 16},
                {77, 30},
                {78, 13},
                {79, 8},
        };
    }
}
