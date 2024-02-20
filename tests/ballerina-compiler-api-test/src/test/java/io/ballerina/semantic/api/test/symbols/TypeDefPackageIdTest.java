/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocument;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for user defined builtinType type definition packageIDs.
 *
 * @since 2201.9.0
 */
public class TypeDefPackageIdTest {

    protected Project project;
    private SemanticModel model;

    @BeforeClass
    public void setup() {
        project = BCompileUtil.loadProject("test-src/symbols/symbol-package-id");
        model = getDefaultModulesSemanticModel(project);
    }

    @Test(dataProvider = "TypeDefPackageIDPosProvider")
    public void testTypeDefPackageId(int line, int offset, String name, String srcFile, String typeDescPackageID,
                                     String internalPackageID) {
        Optional<Document> srcDocument = getDocument(project, null, srcFile);
        assertTrue(srcDocument.isPresent());

        BallerinaTypeDefinitionSymbol symbol =
                (BallerinaTypeDefinitionSymbol) assertBasicsAndGetSymbol(model, srcDocument.get(), line, offset, name,
                        SymbolKind.TYPE_DEFINITION);
        PackageID actualTypeDescPackageID = ((AbstractTypeSymbol) (symbol).typeDescriptor()).getBType().tsymbol.pkgID;
        PackageID actualInternalPackageID = symbol.getInternalSymbol().pkgID;
        assertEquals(actualTypeDescPackageID.toString(), typeDescPackageID);
        assertEquals(actualInternalPackageID.toString(), internalPackageID);
    }

    @DataProvider(name = "TypeDefPackageIDPosProvider")
    public Object[][] getTypeDefPackageIDPos() {
        return new Object[][]{
                {24, 5, "UserTable", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {27, 5, "UserFuture", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {30, 5, "UserXml_1", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {31, 5, "UserXml_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {34, 5, "UserStream", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {37, 5, "UserTypedesc_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {38, 5, "UserTypedesc_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"}
        };
    }
}
