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
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for object type symbols.
 *
 * @since 2.0.0
 */
public class ObjectTypeSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/object_type_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testObjectTypeQualifiers() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(42, 5));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE_DEFINITION);

        TypeDefinitionSymbol typeDef = (TypeDefinitionSymbol) symbol.get();
        TypeSymbol type = typeDef.typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.OBJECT);

        ObjectTypeSymbol object = (ObjectTypeSymbol) type;
        List<Qualifier> qualifiers = object.qualifiers();

        assertEquals(qualifiers.size(), 2);
        assertTrue(qualifiers.contains(Qualifier.CLIENT));
        assertTrue(qualifiers.contains(Qualifier.ISOLATED));
    }

    @Test
    public void testFields() {
        assertField(20, 22);
        assertField(45, 18);
    }

    @Test
    public void testMethods() {
        assertMethod(24, 54);
        assertMethod(49, 50);
    }

    @Test
    public void testTypeInclusion() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(38, 5));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE);
        assertEquals(((TypeSymbol) symbol.get()).typeKind(), TypeDescKind.TYPE_REFERENCE);

        TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) symbol.get();
        assertEquals(typeRef.getName().get(), "Person");
        assertEquals(typeRef.typeDescriptor().typeKind(), TypeDescKind.OBJECT);
    }

    // utils
    private void assertField(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.OBJECT_FIELD);

        ObjectFieldSymbol nameField = (ObjectFieldSymbol) symbol.get();

        // check name
        assertEquals(nameField.getName().get(), "name");

        // check docs (metadata)
        Optional<Documentation> fieldDocs = nameField.documentation();
        assertTrue(fieldDocs.isPresent());
        assertTrue(fieldDocs.get().description().isPresent());
        assertEquals(fieldDocs.get().description().get(), "An object field");

        // check annotations (metadata)
        List<AnnotationSymbol> fieldAnnots = nameField.annotations();
        assertEquals(fieldAnnots.size(), 1);
        assertEquals(fieldAnnots.get(0).getName().get(), "v1");

        // check qualifiers
        assertEquals(nameField.qualifiers().size(), 1);
        assertEquals(nameField.qualifiers().get(0), Qualifier.PUBLIC);

        // check type
        assertEquals(nameField.typeDescriptor().typeKind(), TypeDescKind.STRING);
    }

    private void assertMethod(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.METHOD);

        MethodSymbol barMethod = (MethodSymbol) symbol.get();

        // check name
        assertEquals(barMethod.getName().get(), "bar");

        // check docs (metadata)
        Optional<Documentation> methodDocs = barMethod.documentation();
        assertTrue(methodDocs.isPresent());
        assertTrue(methodDocs.get().description().isPresent());
        assertEquals(methodDocs.get().description().get(), "An object method");

        // check annotations (metadata)
        List<AnnotationSymbol> methodAnnots = barMethod.annotations();
        assertEquals(methodAnnots.size(), 1);
        assertEquals(methodAnnots.get(0).getName().get(), "v1");

        // check qualifiers
        assertEquals(barMethod.qualifiers().size(), 3);
        assertTrue(barMethod.qualifiers().contains(Qualifier.ISOLATED));
        assertTrue(barMethod.qualifiers().contains(Qualifier.REMOTE));
        assertTrue(barMethod.qualifiers().contains(Qualifier.TRANSACTIONAL));
    }
}
