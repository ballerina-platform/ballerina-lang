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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for looking up symbols within an expression.
 */
public class SymbolsWithinExprTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_within_expr_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "LiteralPosProvider")
    public void testLiterals(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isEmpty());
    }

    @DataProvider(name = "LiteralPosProvider")
    public Object[][] getLiteralPos() {
        return new Object[][]{
                {17, 13},
                {18, 16},
                {19, 12},
                {20, 14},
                {21, 15},
                {22, 17},
        };
    }

    @Test(dataProvider = "TemplateExprPosProvider")
    public void testTemplateExprs(int line, int col, boolean symbolPresent) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertEquals(symbol.isPresent(), symbolPresent);

        if (!symbolPresent) {
            return;
        }

        assertEquals(symbol.get().kind(), SymbolKind.VARIABLE);
        assertEquals(symbol.get().getName().get(), "i");
        assertEquals(((VariableSymbol) symbol.get()).typeDescriptor().typeKind(), TypeDescKind.INT);
    }

    @DataProvider(name = "TemplateExprPosProvider")
    public Object[][] getTemplateExprPos() {
        return new Object[][]{
                {27, 14, false},
                {27, 25, false},
                {27, 29, true},
                {28, 12, false},
                {28, 20, false},
                {28, 24, true},
                {29, 35, false},
                {29, 36, true},
        };
    }
}
