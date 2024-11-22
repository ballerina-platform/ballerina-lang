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
import io.ballerina.compiler.api.impl.symbols.BallerinaErrorTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaIntersectionTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaRecordTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.List;
import java.util.Map;
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
    public void testErrorTypeSymbols(int line, int col, String description) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE_DEFINITION);
        assertEquals(((TypeDefinitionSymbol) symbol.get()).typeDescriptor().kind(), SymbolKind.TYPE);
        Optional<Documentation> documentation = ((Documentable) symbol.get()).documentation();
        assertTrue(documentation.isPresent());
        assertEquals(documentation.get().description().get(), description);
    }

    @DataProvider(name = "ErrorTypeProvider")
    public Object[][] getErrorType() {
        return new Object[][]{
                {26, 13, "Represents email related errors."},
                {29, 13, "Represents the operation canceled(typically by the caller) error."}
        };
    }

    @Test(dependsOnMethods = {"testErrorTypeSymbols"})
    public void testErrorTypeModules() {
        Optional<Symbol> unionErrorSymbol = model.symbol(srcFile, LinePosition.from(26, 13));
        Optional<Symbol> distinctErrorSymbol = model.symbol(srcFile, LinePosition.from(29, 13));

        assertTrue(unionErrorSymbol.isPresent());
        TypeDefinitionSymbol unionTypeDefinitionSymbol = (TypeDefinitionSymbol) unionErrorSymbol.get();
        assertEquals(unionTypeDefinitionSymbol.typeDescriptor().typeKind(), TypeDescKind.UNION);
        assertEquals(((unionTypeDefinitionSymbol)).getName().get(), "EmailError");

        UnionTypeSymbol unionTypeSymbol =
                (UnionTypeSymbol) ((TypeDefinitionSymbol) unionErrorSymbol.get()).typeDescriptor();
        List<TypeSymbol> memberTypeSymbols = unionTypeSymbol.memberTypeDescriptors();

        for (TypeSymbol typeSymbol : memberTypeSymbols) {
            assertEquals(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor().typeKind(), TypeDescKind.ERROR);
            assertModuleInfo(typeSymbol.getModule().get());
        }

        assertTrue(distinctErrorSymbol.isPresent());
        ErrorTypeSymbol errorTypeSymbol =
                (ErrorTypeSymbol) ((TypeDefinitionSymbol) distinctErrorSymbol.get()).typeDescriptor();
        assertEquals(errorTypeSymbol.typeKind(), TypeDescKind.ERROR);
        assertModuleInfo(errorTypeSymbol.getModule().get());
    }

    @Test(dataProvider = "TypeRefErrorTypeProvider")
    public void testErrorTypeRef(int line, int offset, String expectedErrorType) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, offset));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE);

        TypeSymbol type = (TypeSymbol) symbol.get();
        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), expectedErrorType);
        assertModuleInfo(type.getModule().get());
    }

    @DataProvider(name = "TypeRefErrorTypeProvider")
    public Object[][] getTypeRefErrorType() {
        return new Object[][]{
                {40, 4, "SendError"},
                {41, 4, "EmailError"},
                {42, 4, "CancelledError"},
                {43, 4, "IntersectionError"}
        };
    }

    @Test(dataProvider = "IntersectionErrorTypeProvider")
    public void testFieldDescriptorsOfErrorIntersection(int line, int offset, String expectedErrorType,
                                                        List<String> expectedFieldNames) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, offset));
        assertTrue(symbol.isPresent());

        Symbol ballerinaTypeDefinitionSymbol = symbol.get();
        assertEquals(ballerinaTypeDefinitionSymbol.kind(), SymbolKind.TYPE);
        assertTrue(ballerinaTypeDefinitionSymbol.getName().isPresent());
        assertEquals(ballerinaTypeDefinitionSymbol.getName().get(), expectedErrorType);

        TypeSymbol ballerinaIntersectionTypeSymbol =
                ((BallerinaTypeReferenceTypeSymbol) ballerinaTypeDefinitionSymbol).typeDescriptor();
        assertEquals(ballerinaIntersectionTypeSymbol.kind(), SymbolKind.TYPE);

        TypeSymbol ballerinaErrorTypeSymbol =
                ((BallerinaIntersectionTypeSymbol) ballerinaIntersectionTypeSymbol).effectiveTypeDescriptor();
        assertEquals(ballerinaErrorTypeSymbol.kind(), SymbolKind.TYPE);

        TypeSymbol ballerinaRecordTypeSymbol =
                ((BallerinaErrorTypeSymbol) ballerinaErrorTypeSymbol).detailTypeDescriptor();
        assertEquals(ballerinaRecordTypeSymbol.kind(), SymbolKind.TYPE);

        Map<String, RecordFieldSymbol> stringRecordFieldSymbolMap =
                ((BallerinaRecordTypeSymbol) ballerinaRecordTypeSymbol).fieldDescriptors();
        List<String> actualFieldNames = stringRecordFieldSymbolMap.keySet().stream().toList();
        assertEquals(actualFieldNames.size(), expectedFieldNames.size());
        for (int i = 0; i < actualFieldNames.size(); i++) {
            assertEquals(actualFieldNames.get(i), expectedFieldNames.get(i));
        }
    }

    @DataProvider(name = "IntersectionErrorTypeProvider")
    public Object[][] getIntersectionErrorType() {
        return new Object[][]{
                {62, 4, "SimpleIntersectionError", List.of("msg", "value")},
                {63, 4, "MultipleIntersectionError", List.of("flag", "msg", "value")}
        };
    }

    private void assertModuleInfo(ModuleSymbol module) {
        assertEquals(module.id().orgName(), Names.ANON_ORG.toString());
        assertEquals(module.id().moduleName(), PackageID.DEFAULT.toString());
    }
}
