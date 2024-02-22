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

                // Simple types
                {17, 5, "UserNil", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {18, 5, "UserBoolean", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {19, 5, "UserInt", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {20, 5, "UserFloat", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {21, 5, "UserDecimal", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Sequence types
                {24, 5, "UserString", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {25, 5, "UserXml_1", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {26, 5, "UserXml_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Other types
                {29, 5, "UserAny", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {30, 5, "UserAnydata", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {31, 5, "UserNever", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {32, 5, "UserReadonly", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {33, 5, "UserJson", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {34, 5, "UserByte", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {35, 5, "UserUnion_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {36, 5, "UserUnion_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {37, 5, "UserIntersection_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {38, 5, "UserIntersection_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Builtin object types
                {41, 5, "UserIterable", "main.bal", "ballerina/lang.object:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {42, 5, "UserRawTemplate", "main.bal", "ballerina/lang.object:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Structured types
                {45, 5, "UserArr_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {46, 5, "UserArr_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {47, 5, "UserTuple", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {48, 5, "UserMap_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {49, 5, "UserMap_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {50, 5, "UserTable_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {51, 5, "UserTable_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {52, 5, "UserRecord", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Behavioral types
                {60, 5, "UserError", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {61, 5, "UserFunction", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {62, 5, "UserClient", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {65, 5, "UserService", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {68, 5, "UserFuture", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {69, 5, "UserTypedesc_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {70, 5, "UserTypedesc_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {71, 5, "UserStream", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
        };
    }
}
