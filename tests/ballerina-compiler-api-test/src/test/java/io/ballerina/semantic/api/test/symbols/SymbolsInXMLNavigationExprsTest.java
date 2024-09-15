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

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.SymbolKind.XMLNS;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() with XML navigation expressions.
 *
 * @since 2.0.0
 */
public class SymbolsInXMLNavigationExprsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_xml_navigation_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "FilterExprPosProvider")
    public void testXMLFilterExprs(int line, int col, SymbolKind kind, String name) {
        assertSymbol(line, col, kind, name);
    }

    @DataProvider(name = "FilterExprPosProvider")
    public Object[][] getFilterExprPos() {
        return new Object[][]{
                {21, 13, VARIABLE, "x1"},
                {21, 17, null, null},
                {22, 13, XMLNS, "ns"},
                {23, 9, FUNCTION, "getXML"},
                {23, 19, XMLNS, "ns"},
                {23, 22, null, null},
                {24, 16, null, null},
                {25, 13, null, null},
                {26, 13, XMLNS, "k"},
                {27, 9, FUNCTION, "getXML"},
                {27, 19, XMLNS, "ns"},
                {27, 24, XMLNS, "k"},
        };
    }

    @Test(dataProvider = "StepExprPosProvider", enabled = false)
    public void testXMLStepExprs(int line, int col, SymbolKind kind, String name) {
        assertSymbol(line, col, kind, name);
    }

    @DataProvider(name = "StepExprPosProvider")
    public Object[][] getStepExprPos() {
        return new Object[][]{
                {32, 13, VARIABLE, "x1"},
                {32, 17, XMLNS, "ns"},
                {32, 20, null, null},
                {33, 12, null, null},
                {34, 13, null, null},
                {35, 12, null, null},
                {35, 16, XMLNS, "ns"},
                {36, 23, VARIABLE, "indx"},
                {37, 16, XMLNS, "ns"},
                {37, 25, XMLNS, "k"},
                {38, 32, FUNCTION, "toString"},
        };
    }

    @Test(dataProvider = "StepExprWithExtensionPosProvider")
    public void testXmlStepExprWithExtension(int line, int col, SymbolKind kind, String name) {
        assertSymbol(line, col, kind, name);
    }

    @DataProvider(name = "StepExprWithExtensionPosProvider")
    public Object[][] getStepExprWithExtensionPos() {
        return new Object[][]{
                {49, 13, VARIABLE, "x1"},
                {49, 18, FUNCTION, "get"},
                {49, 22, VARIABLE, "indx"},
                {50, 23, XMLNS, "ns"},
                {51, 31, null, null},
                {51, 37, VARIABLE, "indx"}
        };
    }

    // utils

    private void assertSymbol(int line, int col, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (kind == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        Assert.assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().getName().get(), name);
    }
}
