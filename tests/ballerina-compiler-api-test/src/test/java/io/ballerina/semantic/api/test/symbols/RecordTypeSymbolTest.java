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
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for record type symbols.
 *
 * @since 2.0.0
 */
public class RecordTypeSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/record_type_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "FieldProvider")
    public void testFields(int line, int col, String expName, String expDoc, TypeDescKind expTypeKind,
                           Qualifier expQual, boolean expDefVal, boolean expOptional) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.RECORD_FIELD);

        RecordFieldSymbol field = (RecordFieldSymbol) symbol.get();

        // check name
        assertEquals(field.getName().get(), expName);

        // check docs (metadata)
        Optional<Documentation> fieldDocs = field.documentation();
        assertTrue(fieldDocs.isPresent());
        assertTrue(fieldDocs.get().description().isPresent());
        assertEquals(fieldDocs.get().description().get(), expDoc);

        // check annotations (metadata)
        List<AnnotationSymbol> fieldAnnots = field.annotations();
        assertEquals(fieldAnnots.size(), 1);
        assertEquals(fieldAnnots.get(0).getName().get(), "v1");

        // check qualifiers
        if (expQual != null) {
            assertEquals(field.qualifiers().size(), 1);
            assertEquals(field.qualifiers().get(0), expQual);
        } else {
            assertTrue(field.qualifiers().isEmpty());
        }

        // check type
        assertEquals(field.typeDescriptor().typeKind(), expTypeKind);

        // check misc info
        assertEquals(field.hasDefaultValue(), expDefVal);
        assertEquals(field.isOptional(), expOptional);
    }

    @DataProvider(name = "FieldProvider")
    public Object[][] getFields() {
        return new Object[][]{
                {22, 15, "designation", "Designation field", TypeDescKind.STRING, null, false, false},
                {29, 20, "name", "Name field", TypeDescKind.STRING, Qualifier.READONLY, false, false},
                {32, 12, "age", "Age field", TypeDescKind.INT, null, false, true},
                {43, 11, "designation", "Designation field", TypeDescKind.STRING, null, true, false},
        };
    }

    @Test(dataProvider = "TypeInclusionProvider")
    public void testTypeInclusion(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE);
        assertEquals(((TypeSymbol) symbol.get()).typeKind(), TypeDescKind.TYPE_REFERENCE);

        TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) symbol.get();
        assertEquals(typeRef.getName().get(), "Person");
        assertEquals(typeRef.typeDescriptor().typeKind(), TypeDescKind.RECORD);
    }

    @DataProvider(name = "TypeInclusionProvider")
    public Object[][] getTypeInclusions() {
        return new Object[][]{
                {18, 9},
                {39, 5}
        };
    }
}
