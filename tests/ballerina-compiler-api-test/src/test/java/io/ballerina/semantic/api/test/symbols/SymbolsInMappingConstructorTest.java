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
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for use of symbol() with mapping constructors.
 *
 * @since 2.0.0
 */
public class SymbolsInMappingConstructorTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_in_mapping_constructor_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "FieldPosProvider")
    public void testFields(int line, int col, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        Assert.assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().getName().get(), name);
    }

    @DataProvider(name = "FieldPosProvider")
    public Object[][] getSpecificFieldPos() {
        return new Object[][]{
                {17, 20, RECORD_FIELD, "city"},
                {17, 39, RECORD_FIELD, "country"},
                {20, 8, RECORD_FIELD, "name"},
                {21, 8, PARAMETER, "age"},
                {22, 10, FUNCTION, "foo"},
                {23, 11, VARIABLE, "adrs"},
                {27, 8, RECORD_FIELD, "name"},
                {29, 18, RECORD_FIELD, "city"},
        };
    }
}
