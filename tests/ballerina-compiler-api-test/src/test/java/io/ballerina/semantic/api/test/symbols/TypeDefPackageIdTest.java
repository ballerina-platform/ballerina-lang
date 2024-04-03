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
import org.testng.annotations.AfterClass;
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
 * Test cases for user defined type packageIDs.
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
                {19, 5, "UserNil", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {20, 5, "UserBoolean", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {21, 5, "UserInt", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {22, 5, "UserFloat", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {23, 5, "UserDecimal", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {24, 5, "UserSingleton", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {25, 5, "UserIntSubtype_1", "main.bal", "ballerina/lang.int:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {26, 5, "UserIntSubtype_2", "main.bal", "ballerina/lang.int:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Sequence types
                {29, 5, "UserString", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {30, 5, "UserXml_1", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {31, 5, "UserXml_2", "main.bal", "ballerina/lang.xml:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {32, 5, "UserXml_3", "main.bal", "ballerina/lang.xml:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {33, 5, "UserXml_4", "main.bal", "ballerina/lang.xml:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {34, 5, "UserXml_5", "main.bal", "ballerina/lang.xml:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Other types
                {37, 5, "UserAny", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {38, 5, "UserAnydata", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {39, 5, "UserNever", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {40, 5, "UserReadonly", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {41, 5, "UserJson", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {42, 5, "UserByte", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {43, 5, "UserUnion_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {44, 5, "UserUnion_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {45, 5, "UserIntersection_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {46, 5, "UserIntersection_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {47, 5, "UserRegexp", "main.bal", "ballerina/lang.regexp:0.0.0", "sample_org/symbol_package_id:0.1.0"},

                // Builtin object types
                {50, 5, "UserIterable", "main.bal", "ballerina/lang.object:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {51, 5, "UserRawTemplate", "main.bal", "ballerina/lang.object:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Structured types
                {54, 5, "UserArr_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {55, 5, "UserArr_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {56, 5, "UserArr_3", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {57, 5, "UserArr_4", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {58, 5, "UserTuple", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {59, 5, "UserMap_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {60, 5, "UserMap_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {61, 5, "UserMap_3", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {62, 5, "UserMap_4", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {63, 5, "UserTable_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {64, 5, "UserTable_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {65, 5, "UserTable_3", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {66, 5, "UserRecord", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},

                // Behavioral types
                {74, 5, "UserError", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {75, 5, "UserFunction", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {76, 5, "UserClient", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {79, 5, "UserService", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {82, 5, "UserFuture_1", "main.bal", "ballerina/lang.annotations:0.0.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {83, 5, "UserFuture_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {84, 5, "UserTypedesc_1", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {85, 5, "UserTypedesc_2", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
                {86, 5, "UserStream", "main.bal", "sample_org/symbol_package_id:0.1.0",
                        "sample_org/symbol_package_id:0.1.0"},
        };
    }

    @AfterClass
    public void tearDown() {
        this.model = null;
        this.project = null;
    }
}
