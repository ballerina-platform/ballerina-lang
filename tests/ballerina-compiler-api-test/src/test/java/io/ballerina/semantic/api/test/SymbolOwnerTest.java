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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
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
 * Test cases for validating the module symbol info of a symbol.
 */
public class SymbolOwnerTest {

    private Project project;
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        project = BCompileUtil.loadProject("test-src/symbol_owner_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test (dataProvider = "LangLibSymbolProvider")
    public void testLangLibSymbols(int line, int column, String orgName, String moduleName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, column));
        assertTrue(symbol.isPresent());

        Optional<ModuleSymbol> module = symbol.get().getModule();

        assertTrue(module.isPresent());
        assertEquals(module.get().id().orgName(), orgName);
        assertEquals(module.get().id().moduleName(), moduleName);
    }

    @DataProvider(name = "LangLibSymbolProvider")
    public Object[][] getLangLibSymbols() {
        return new Object[][]{
                {20, 10, "ballerina", "lang.value"},
                {22, 12, "ballerina", "lang.runtime"},
        };
    }
}
