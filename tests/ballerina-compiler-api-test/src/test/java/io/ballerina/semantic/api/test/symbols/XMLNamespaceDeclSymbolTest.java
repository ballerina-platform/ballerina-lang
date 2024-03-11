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
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for XML namespace declaration symbols.
 *
 * @since 2.0.0
 */
public class XMLNamespaceDeclSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/xmlns_decl_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "XMLNameSpaceDeclarationProvider")
    public void testXMLNameSpaceDeclarations(int line, int col, String expName, String nameSpaceUri) {
        XMLNamespaceSymbol symbol = (XMLNamespaceSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col,
                                                                                  expName, SymbolKind.XMLNS);
        assertEquals(symbol.namespaceUri(), nameSpaceUri);
    }

    @DataProvider(name = "XMLNameSpaceDeclarationProvider")
    public Object[][] getXMLNameSpaceDeclInfo() {
        return new Object[][]{
                {16, 31, "b7a", "http://ballerina.io"},
                {18, 16, "b7a", "http://ballerina.io"},
                {20, 31, "blang", "http://ballerina.io"},
                {26, 13, "ex", "http://example.com"},
        };
    }

    @Test(dataProvider = "XMLNameSpaceDeclarationWithConstRefProvider")
    public void testConstRef(int line, int col, String expName) {
        ConstantSymbol symbol = (ConstantSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, expName,
                                                                          SymbolKind.CONSTANT);
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.SINGLETON);
    }

    @DataProvider(name = "XMLNameSpaceDeclarationWithConstRefProvider")
    public Object[][] getConstRefInfo() {
        return new Object[][]{
                {26, 6, "uri"},
                {30, 6, "intVal"},
        };
    }
}
