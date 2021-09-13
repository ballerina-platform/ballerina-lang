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
import io.ballerina.compiler.api.impl.symbols.BallerinaModule;
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
 * Test cases for module-level symbols.
 *
 * @since 2.0.0
 */
public class ImportDeclSymbolTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/module_level");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "ImportDeclarationPosProvider")
    public void testImports(int line, int offset, String name, String org, String prefix) {
        BallerinaModule symbol = (BallerinaModule) assertBasicsAndGetSymbol(model, srcFile, line, offset,
                                                                            name, SymbolKind.MODULE);
        assertEquals(symbol.id().orgName(), org);
        assertEquals(symbol.id().version(), "1.0.0");
        // TODO: Fix https://github.com/ballerina-platform/ballerina-lang/issues/32660
        // assertEquals(symbol.id().modulePrefix(), prefix);
    }

    @DataProvider(name = "ImportDeclarationPosProvider")
    public Object[][] getImportDeclPos() {
        return new Object[][]{
                {16, 20, "module_level.foo", "testorg", "foo"},
                {17, 27, "module_level.bar", "testorg", "barPrefix"},
                {18, 27, "module_level.baz", "testorg", "_"},
        };
    }
}
