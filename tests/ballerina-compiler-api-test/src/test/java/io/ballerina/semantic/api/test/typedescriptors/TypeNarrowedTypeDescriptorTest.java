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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.typedescriptors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
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
 * Test cases for the type-narrowed type descriptor.
 *
 * @since 2201.0.2
 */
public class TypeNarrowedTypeDescriptorTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/typedescriptors/type_narrrowed_type_desc_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "SingletonTypeSymbolData")
    public void testSingletonType(int line, int col, TypeDescKind typeDescKind, String signature) {
        Optional<Symbol> symbolOptional = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbolOptional.isPresent());

        assertEquals(symbolOptional.get().kind(), SymbolKind.VARIABLE);
        Symbol symbol = symbolOptional.get();

        TypeSymbol typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        assertEquals(typeSymbol.typeKind(), typeDescKind);
        assertEquals(typeSymbol.signature(), signature);
    }

    @DataProvider(name = "SingletonTypeSymbolData")
    public Object[][] getSingletonTypeSymbolData() {
        return new Object[][]{
                {19, 7, TypeDescKind.UNION, "1|2"},
                {19, 22, TypeDescKind.SINGLETON, "2"},
                {24, 7, TypeDescKind.UNION, "\"10\"|\"20\""},
                {25, 17, TypeDescKind.SINGLETON, "\"10\""},
                {27, 17, TypeDescKind.SINGLETON, "\"20\""},
        };
    }
}
