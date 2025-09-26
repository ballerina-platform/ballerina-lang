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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.Names;

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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

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
        Optional<Symbol> symbol = model.symbol(srcFile, from(43, 11));
        TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) ((VariableSymbol) symbol.get()).typeDescriptor();
        Symbol enm = type.definition();
        assertEquals(enm.kind(), ENUM);
        assertEquals(enm.getName().get(), type.getName().get());
        assertEquals(((EnumSymbol) enm).documentation().get().description().get(), "An enumeration of colours.");
        assertSame(type.definition(), enm);
    }

    @Test 
    public void testEnumFieldInRecord() {
        // Test the fix for issue #44238: enum fields in records should return TYPE_REFERENCE, not UNION
        // Get the Department type reference symbol
        Optional<Symbol> symbol = model.symbol(srcFile, from(43, 12)); // Department type
        assertTrue(symbol.isPresent(), "Could not find Department symbol");
        
        TypeReferenceTypeSymbol deptType = (TypeReferenceTypeSymbol) symbol.get();
        assertEquals(deptType.getName().get(), "Department");
        
        // Get the record definition and find the enum field 'code'
        TypeDefinitionSymbol typeDef = (TypeDefinitionSymbol) deptType.definition();
        RecordTypeSymbol recordType = (RecordTypeSymbol) typeDef.typeDescriptor();
        RecordFieldSymbol codeField = recordType.fieldDescriptors().get("code");
        
        assertTrue(codeField != null, "Could not find field 'code' in Department record");
        
        // Verify the field type is a type reference to the enum
        TypeSymbol fieldType = codeField.typeDescriptor();
        assertEquals(fieldType.typeKind(), TypeDescKind.TYPE_REFERENCE, "Field 'code' should be TYPE_REFERENCE");
        assertEquals(fieldType.getName().get(), "Colour", "Field 'code' should reference Colour enum");
        
        // This is the key test: enum type references should return themselves (TYPE_REFERENCE)
        // instead of the underlying union when typeDescriptor() is called
        TypeReferenceTypeSymbol enumTypeRef = (TypeReferenceTypeSymbol) fieldType;
        TypeSymbol enumTypeDescriptor = enumTypeRef.typeDescriptor();
        
        // Before fix: This would return UNION 
        // After fix: This should return TYPE_REFERENCE (self-reference)
        assertEquals(enumTypeDescriptor.typeKind(), TypeDescKind.TYPE_REFERENCE, 
                     "Enum type reference should return TYPE_REFERENCE, not the underlying UNION");
        assertEquals(enumTypeDescriptor.getName().get(), "Colour", "Returned type should be named Colour");
        
        // Verify the definition is actually an enum
        assertEquals(enumTypeRef.definition().kind(), ENUM, "Type reference should point to an ENUM");
        assertEquals(enumTypeRef.definition().getName().get(), "Colour", "Enum should be named Colour");
        
        // The returned type descriptor should be the same instance (self-reference)
        assertSame(enumTypeRef, enumTypeDescriptor, "Enum type reference should return itself");
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

    @Test(dataProvider = "TypeRefPos")
    public void testTypeRefLookup(int line, int col, String expName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE);

        TypeSymbol type = (TypeSymbol) symbol.get();
        assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
        assertEquals(type.getName().get(), expName);
        assertEquals(type.getModule().get().id().orgName(), Names.ANON_ORG.toString());
        assertEquals(type.getModule().get().id().moduleName(), PackageID.DEFAULT.toString());
    }

    @DataProvider(name = "TypeRefPos")
    public Object[][] getTypeRefPos() {
        return new Object[][]{
                {46, 4, "Foo"},
                {48, 4, "Baz"},
        };
    }

    @Test(dataProvider = "TypeRefPosForTypeNarrowing")
    public void testTypeRefLookupForTypeNarrowing(int line, int start, int end) {
        Optional<TypeSymbol> typeSymbol = model.typeOf(LineRange.from(srcFile.name(), LinePosition.from(line, start),
                LinePosition.from(line, end)));

        assertTrue(typeSymbol.isPresent());
        assertEquals(typeSymbol.get().typeKind(), TypeDescKind.TYPE_REFERENCE);

        TypeReferenceTypeSymbol typeRefTSymbol = (TypeReferenceTypeSymbol) typeSymbol.get();
        Symbol definition = typeRefTSymbol.definition();
        assertEquals(definition.kind(), TYPE_DEFINITION);

        Optional<String> definitionName = definition.getName();
        assertTrue(definitionName.isPresent());
        assertEquals(definitionName.get(), "(Person & readonly)");
    }

    @DataProvider(name = "TypeRefPosForTypeNarrowing")
    public Object[][] getTypeRefPosForTypeNarrowing() {
        return new Object[][]{
                {56, 8, 14},
                {62, 12, 18},
                {68, 8, 14},
                {76, 8, 11},
        };
    }
}
