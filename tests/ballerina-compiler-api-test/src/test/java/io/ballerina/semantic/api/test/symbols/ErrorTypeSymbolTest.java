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
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for error type symbols.
 *
 * @since 2.0.0
 */
public class ErrorTypeSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/error_type_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "ErrorTypeProvider")
    public void testErrorTypeSymbols(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE_DEFINITION);
        assertEquals(((TypeDefinitionSymbol) symbol.get()).typeDescriptor().kind(), SymbolKind.TYPE);
    }

    @DataProvider(name = "ErrorTypeProvider")
    public Object[][] getErrorType() {
        return new Object[][]{
                {26, 13},
                {29, 13}
        };
    }

    @Test(dependsOnMethods = {"testErrorTypeSymbols"})
    public void testErrorTypeModules() {
        Optional<Symbol> unionErrorSymbol = model.symbol(srcFile, LinePosition.from(26, 13));
        Optional<Symbol> distinctErrorSymbol = model.symbol(srcFile, LinePosition.from(29, 13));

        assertTrue(unionErrorSymbol.isPresent());
        assertEquals(((TypeDefinitionSymbol) unionErrorSymbol.get()).typeDescriptor().typeKind(),
                TypeDescKind.UNION);

        BUnionType unionType =
                (BUnionType) ((AbstractTypeSymbol) ((TypeDefinitionSymbol) unionErrorSymbol.get()).
                        typeDescriptor()).getBType();
        assertEquals(unionType.toString(), "EmailError");

        for (BType type : unionType.getMemberTypes()) {
            assertEquals(((BTypeReferenceType) type).referredType.getKind(), TypeKind.ERROR);
            assertEquals(type.tsymbol.pkgID, PackageID.DEFAULT);
            assertEquals(type.tsymbol.pkgID.getOrgName(), Names.ANON_ORG);
        }

        assertTrue(distinctErrorSymbol.isPresent());
        ErrorTypeSymbol errorTypeSymbol =
                (ErrorTypeSymbol) ((TypeDefinitionSymbol) distinctErrorSymbol.get()).
                        typeDescriptor();
        assertEquals(errorTypeSymbol.typeKind(),
                TypeDescKind.ERROR);
        assertEquals(((AbstractTypeSymbol) errorTypeSymbol).getBType().tsymbol.pkgID, PackageID.DEFAULT);
        assertEquals(((AbstractTypeSymbol) errorTypeSymbol).getBType().tsymbol.pkgID.getOrgName(), Names.ANON_ORG);
    }
}
