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

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() with binary expressions.
 *
 * @since 2.0.0
 */
public class SymbolsInBinaryExprsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_binary_exprs_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void testBinaryExprs(int line, int col, SymbolKind expKind, String expName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (expKind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        Assert.assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), expKind);
        assertEquals(symbol.get().getName().get(), expName);
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                // Multiplicative exprs
                {19, 14, VARIABLE, "a"},
                {19, 18, CONSTANT, "b"},
                {20, 11, VARIABLE, "a"},
                {20, 15, FUNCTION, "getOp"},
                {21, 10, CONSTANT, "b"},
                {21, 14, VARIABLE, "a"},
                {22, 10, FUNCTION, "getOp"},
                {22, 20, CONSTANT, "b"},
                // Additive exprs
                {26, 14, VARIABLE, "a"},
                {26, 18, CONSTANT, "b"},
                {27, 10, FUNCTION, "getOp"},
                {27, 20, CONSTANT, "b"},
                {28, 10, VARIABLE, "a"},
                {28, 14, CONSTANT, "b"},
                // Shift exprs
                {33, 14, VARIABLE, "a"},
                {33, 19, VARIABLE, "shift"},
                {34, 10, VARIABLE, "a"},
                {34, 15, null, null},
                {35, 10, VARIABLE, "shift"},
                {35, 20, null, null},
                // Range exprs
                {40, 13, VARIABLE, "x"},
                {40, 17, null, null},
                {41, 13, CONSTANT, "b"},
                {41, 17, VARIABLE, "x"},
                {41, 16, null, null},
                {42, 13, VARIABLE, "a"},
                {42, 17, CONSTANT, "b"},
                // Relational exprs
                {46, 18, VARIABLE, "a"},
                {46, 22, CONSTANT, "b"},
                {47, 10, VARIABLE, "a"},
                {47, 14, null, null},
                {48, 10, CONSTANT, "b"},
                {48, 15, VARIABLE, "a"},
                {49, 10, CONSTANT, "b"},
                {49, 15, VARIABLE, "a"},
                // Equality exprs
                {53, 18, VARIABLE, "a"},
                {53, 23, CONSTANT, "b"},
                {54, 10, VARIABLE, "a"},
                {54, 15, null, null},
                {55, 10, VARIABLE, "a"},
                {55, 16, CONSTANT, "b"},
                {56, 10, FUNCTION, "getOp"},
                {56, 23, CONSTANT, "b"},
                // Bitwise exprs
                {61, 14, VARIABLE, "x"},
                {61, 18, FUNCTION, "getOp"},
                {62, 10, VARIABLE, "x"},
                {62, 14, null, null},
                {63, 10, null, null},
                {63, 18, VARIABLE, "x"},
                // Logical exprs
                {68, 18, VARIABLE, "expr"},
                {68, 26, null, null},
                {69, 11, FUNCTION, "getOp"},
                {69, 28, VARIABLE, "expr"},
        };
    }
}
