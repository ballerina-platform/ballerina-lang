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

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
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
 * Test cases for module symbols.
 */
public class ModuleSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/module_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "TypePosProvider")
    public void testBuiltinSubtypeModule(int line, int col, String expName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertTrue(symbol.isPresent());
        assertTrue(symbol.get().getModule().isPresent());

        ModuleSymbol module = symbol.get().getModule().get();

        assertEquals(module.kind(), SymbolKind.MODULE);
        assertEquals(module.getName().get(), expName);
        assertEquals(module.id().moduleName(), expName);
        assertEquals(module.id().orgName(), "ballerina");
        assertEquals(module.id().version(), "0.0.0");
    }

    @DataProvider(name = "TypePosProvider")
    public Object[][] getTypePos() {
        return new Object[][]{
                {17, 8, "lang.int"},
                {18, 8, "lang.int"},
                {19, 8, "lang.int"},
                {20, 8, "lang.int"},
                {21, 8, "lang.int"},
                {22, 8, "lang.int"},
                {23, 11, "lang.string"},
                {24, 8, "lang.xml"},
                {25, 8, "lang.xml"},
                {26, 8, "lang.xml"},
                {27, 8, "lang.xml"},
        };
    }
}
