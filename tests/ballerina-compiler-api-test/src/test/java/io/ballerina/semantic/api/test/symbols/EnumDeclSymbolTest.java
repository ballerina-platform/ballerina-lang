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
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for enum declaration symbols.
 *
 * @since 2.0.0
 */
public class EnumDeclSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/enum_decl_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "EnumDeclarationProvider")
    public void testEnumDeclarations(int line, int col, String expName,
                                     String expDoc, String expAnnot, List<Qualifier> expQuals, List<String> members) {

        EnumSymbol symbol = (EnumSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col,
                                                                  expName, SymbolKind.ENUM);
        // check docs (metadata)
        assertTrue(symbol.documentation().isPresent());
        Documentation fieldDocs = symbol.documentation().get();
        if (expDoc != null) {
            assertTrue(fieldDocs.description().isPresent());
            assertEquals(fieldDocs.description().get(), expDoc);
        } else {
            assertFalse(fieldDocs.description().isPresent());
        }

        // check annotations (metadata)
        List<AnnotationSymbol> fieldAnnots = symbol.annotations();
        if (expAnnot != null) {
            assertEquals(fieldAnnots.size(), 1);
            assertEquals(fieldAnnots.get(0).getName().get(), expAnnot);
        } else {
            assertEquals(fieldAnnots.size(), 0);
        }

        // check qualifiers
        List<Qualifier> qualifiers = symbol.qualifiers();
        if (expQuals.size() > 0) {
            expQuals.forEach(expQual -> assertTrue(qualifiers.contains(expQual)));
        } else {
            assertTrue(qualifiers.isEmpty());
        }

        // check type
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.UNION);

        // check enum members
        assertList(symbol.members(), members);
    }

    @DataProvider(name = "EnumDeclarationProvider")
    public Object[][] getEnumDeclInfo() {
        return new Object[][]{
                {17, 5, "Color", "Color Enum", null, List.of(),
                        List.of("RED", "BLUE", "GREEN")},
                {25, 12, "Kind", "Kind Enum", "enumdDecl", List.of(Qualifier.PUBLIC),
                        List.of("ONE", "TWO", "THREE")},
                {35, 12, "Continents", "All Continents", "enumdDecl", List.of(Qualifier.PUBLIC),
                        List.of("ASIA", "AMERICA", "AUSTRALIA", "EUROPE", "OTHER")},
        };
    }

    @Test(dataProvider = "EnumMemberDeclarationProvider")
    public void testEnumMemberDeclarations(int line, int col, String expName, String expDoc,
                                           String expAnnot, List<Qualifier> expQuals) {

        ConstantSymbol symbol = (ConstantSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col,
                                                                          expName, SymbolKind.ENUM_MEMBER);
        // check docs (metadata)
        assertTrue(symbol.documentation().isPresent());
        Documentation fieldDocs = symbol.documentation().get();
        if (expDoc != null) {
            assertTrue(fieldDocs.description().isPresent());
            assertEquals(fieldDocs.description().get(), expDoc);
        } else {
            assertFalse(fieldDocs.description().isPresent());
        }

        // check annotations (metadata)
        List<AnnotationSymbol> fieldAnnots = symbol.annotations();
        if (expAnnot != null) {
            assertEquals(fieldAnnots.size(), 1);
            assertEquals(fieldAnnots.get(0).getName().get(), expAnnot);
        } else {
            assertEquals(fieldAnnots.size(), 0);
        }

        // check qualifiers
        List<Qualifier> qualifiers = symbol.qualifiers();
        if (expQuals.size() > 0) {
            expQuals.forEach(qualifiers::contains);
        } else {
            assertTrue(qualifiers.isEmpty());
        }

        // check type
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.SINGLETON);
    }

    @DataProvider(name = "EnumMemberDeclarationProvider")
    public Object[][] getEnumMemberDeclInfo() {
        return new Object[][]{
                {18, 4, "RED", null, null, List.of()},
                {19, 4, "BLUE", null, null, List.of()},
                {28, 4, "TWO", "This kind two", null, List.of(Qualifier.PUBLIC)},
                {30, 4, "THREE", null, "enumMemberDecl", List.of(Qualifier.PUBLIC)},
                {38, 4, "AMERICA", null, "enumMemberDecl", List.of(Qualifier.PUBLIC)},
                {41, 4, "AUSTRALIA", "Australia", "enumMemberDecl", List.of(Qualifier.PUBLIC)},
                {44, 4, "OTHER", "Other", null, List.of(Qualifier.PUBLIC)},
        };
    }
}
