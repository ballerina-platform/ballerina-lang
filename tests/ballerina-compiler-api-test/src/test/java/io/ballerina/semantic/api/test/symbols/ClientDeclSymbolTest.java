/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import io.ballerina.compiler.api.symbols.ClientDeclSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
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
 * Test cases for client declaration symbols.
 *
 * @since 2201.3.0
 */
public class ClientDeclSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/clients_decl_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "ClientDeclarationSymbolInfoProvider")
    public void testClientDeclarations(int line, int col, String modPrefix, String serviceUri) {
        ClientDeclSymbol symbol = (ClientDeclSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col,
                                                                              modPrefix, SymbolKind.CLIENT_DECLARATION);
        assertEquals(symbol.serviceUri(), serviceUri);
    }

    @DataProvider(name = "ClientDeclarationSymbolInfoProvider")
    public Object[][] getClientDeclInfo() {
        return new Object[][]{
                {16, 31, "cl1", "http://example.com"},
                {19, 53, "bar", "http://www.example.com/apis/one.yaml"},
        };
    }

    // TODO: Add test with constant references
//    @Test(dataProvider = "ClientDeclarationWithConstRefProvider")
//    public void testConstRef(int line, int col, String expName) {
//        ConstantSymbol symbol = (ConstantSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, expName,
//                                                                          SymbolKind.CONSTANT);
//        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.SINGLETON);
//    }
//
//    @DataProvider(name = "ClientDeclarationWithConstRefProvider")
//    public Object[][] getConstRefInfo() {
//        return new Object[][]{
//        };
//    }
}
