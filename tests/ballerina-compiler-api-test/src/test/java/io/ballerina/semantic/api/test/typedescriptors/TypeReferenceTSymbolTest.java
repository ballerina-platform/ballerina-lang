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

package io.ballerina.semantic.api.test.typedescriptors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Test cases for the type reference type descriptor.
 *
 * @since 2.0.0
 */
public class TypeReferenceTSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/typedescriptors/typeref_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testTypeDef() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(34, 11));
        TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) ((VariableSymbol) symbol.get()).typeDescriptor();
        Symbol definition = type.definition();
        assertEquals(definition.kind(), TYPE_DEFINITION);
        assertEquals(definition.getName().get(), type.getName().get());
        assertEquals(((TypeDefinitionSymbol) definition).documentation().get().description().get(),
                "Represents a person.");
        assertSame(type.definition(), definition);
    }

    @Test
    public void testClass() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(35, 13));
        TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) ((VariableSymbol) symbol.get()).typeDescriptor();
        Symbol clazz = type.definition();
        assertEquals(clazz.kind(), CLASS);
        assertEquals(clazz.getName().get(), type.getName().get());
        assertEquals(((ClassSymbol) clazz).documentation().get().description().get(), "Represents an employee.");
        assertSame(type.definition(), clazz);
    }

    @Test
    public void testEnum() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(36, 11));
        TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) ((VariableSymbol) symbol.get()).typeDescriptor();
        Symbol enm = type.definition();
        assertEquals(enm.kind(), ENUM);
        assertEquals(enm.getName().get(), type.getName().get());
        assertEquals(((EnumSymbol) enm).documentation().get().description().get(), "An enumeration of colours.");
        assertSame(type.definition(), enm);
    }

    @Test
    public void testRecordField() {
        Optional<Symbol> fieldSymbol = model.symbol(srcFile, from(44, 7));
        Optional<Symbol> typeSymbol = model.symbol(srcFile, from(39, 6));

        TypeSymbol variableSymbol = ((VariableSymbol) fieldSymbol.get()).typeDescriptor();
        assertEquals(variableSymbol.typeKind(), TypeDescKind.RECORD);
        Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) (variableSymbol)).fieldDescriptors();
        assertEquals(recordFields.size(), 1);
        RecordFieldSymbol recordFieldSymbol = recordFields.get("age");
        assertEquals(recordFieldSymbol.getName().get(), "age");
        assertEquals(((TypeReferenceTypeSymbol) (recordFieldSymbol).typeDescriptor()).definition(), typeSymbol.get());
        assertEquals(((recordFieldSymbol).typeDescriptor()).typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(((recordFieldSymbol).typeDescriptor()).getName().get().toString(), "Age");
    }

    @Test
    public void testReferringATypeRef1() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(46, 8));
        TypeSymbol type = ((VariableSymbol) symbol.get()).typeDescriptor();

        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), "Foo");

        type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), "Person");

        type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.RECORD);
    }

    @Test
    public void testReferringATypeRef2() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(47, 8));
        TypeSymbol type = ((VariableSymbol) symbol.get()).typeDescriptor();

        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), "Bar");

        type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), "Foo");

        type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), "Person");

        type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        assertEquals(type.typeKind(), TypeDescKind.RECORD);
    }
}
