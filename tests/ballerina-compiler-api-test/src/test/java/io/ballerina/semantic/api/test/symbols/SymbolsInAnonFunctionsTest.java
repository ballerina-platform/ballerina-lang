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

import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() with anonymous functions.
 *
 * @since 2.0.0
 */
public class SymbolsInAnonFunctionsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_anon_funcs_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void testFuncs(int line, int col, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        Assert.assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().getName().get(), name);
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {18, 62, VARIABLE, "name"},
//                {20, 15, ANNOTATION, "v1"}, TODO: https://github.com/ballerina-platform/ballerina-lang/issues/32809
                {20, 31, PARAMETER, "x"},
                {20, 38, PARAMETER, "y"},
                {20, 55, PARAMETER, "z"},
                {21, 12, VARIABLE, "total"},
                {21, 20, PARAMETER, "x"},
                {25, 43, PARAMETER, "a"},
                {25, 56, PARAMETER, "b"},
                {25, 60, VARIABLE, "c"},
        };
    }

    @Test
    public void testClosureVars() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(17, 11));
        Optional<Symbol> closureVar = model.symbol(srcFile, LinePosition.from(18, 62));

        assertTrue(symbol.isPresent());
        assertTrue(closureVar.isPresent());
        assertEquals(symbol.get(), closureVar.get());
    }
}
