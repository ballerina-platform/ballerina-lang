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
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.MethodSymbol;
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
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for class symbols.
 *
 * @since 2.0.0
 */
public class ClassSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/class_symbols_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testSymbolAtCursor() {
        final List<String> fieldNames = List.of("fname", "lname");
        ClassSymbol symbol = (ClassSymbol) model.symbol(srcFile, LinePosition.from(16, 6)).get();

        assertList(symbol.fieldDescriptors(), fieldNames);
        assertList(symbol.methods(), List.of("getFullName"));

        MethodSymbol initMethod = symbol.initMethod().get();
        assertEquals(initMethod.getName().get(), "init");
        assertEquals(initMethod.typeDescriptor().params().get().stream()
                             .map(p -> p.getName().get())
                             .collect(Collectors.toList()), fieldNames);
    }

    @Test
    public void testTypeReference() {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(40, 6)).get();
        assertEquals(symbol.getName().get(), "Person1");
        assertEquals(symbol.kind(), TYPE);

        TypeReferenceTypeSymbol tSymbol = (TypeReferenceTypeSymbol) symbol;
        assertEquals(tSymbol.typeKind(), TYPE_REFERENCE);

        ClassSymbol clazz = (ClassSymbol) tSymbol.typeDescriptor();
        assertEquals(clazz.getName().get(), "Person1");
        assertEquals(clazz.kind(), CLASS);
        assertEquals(clazz.typeKind(), OBJECT);
        assertEquals(clazz.initMethod().get().getName().get(), "init");
    }

    @Test
    public void testClassWithoutInit() {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(41, 4)).get();
        assertEquals(symbol.getName().get(), "Person2");
        assertEquals(symbol.kind(), TYPE);

        TypeReferenceTypeSymbol tSymbol = (TypeReferenceTypeSymbol) symbol;
        assertEquals(tSymbol.typeKind(), TYPE_REFERENCE);

        ClassSymbol clazz = (ClassSymbol) tSymbol.typeDescriptor();
        assertEquals(clazz.getName().get(), "Person2");
        assertEquals(clazz.kind(), CLASS);
        assertEquals(clazz.typeKind(), OBJECT);
        assertTrue(clazz.initMethod().isEmpty());
    }

    @Test(dataProvider = "TypeInitPosProvider")
    public void testTypeInit(int line, int col, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        if (name == null) {
            assertTrue(symbol.isEmpty());
            return;
        }

        ClassSymbol clazz = (ClassSymbol) ((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor();
        assertEquals(clazz.getName().get(), name);
    }

    @DataProvider(name = "TypeInitPosProvider")
    public Object[][] getTypeInit() {
        return new Object[][]{
                {40, 17, null},
                {40, 21, null},
                {40, 30, null},
                {41, 17, null},
                {42, 9, null},
                {42, 13, "Person2"},
                {42, 21, null},
        };
    }

    @Test(dataProvider = "TypeInitPosProvider2")
    public void testTypeInit2(int line, int sCol, int eCol, TypeDescKind typeKind, String name) {
        Optional<TypeSymbol> type = model.type(
                LineRange.from(srcFile.name(), LinePosition.from(line, sCol), LinePosition.from(line, eCol)));
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        type.get().getName().ifPresent(tName -> assertEquals(tName, name));
    }

    @Test(dataProvider = "TypeInitPosProvider2")
    public void testTypeInit3(int line, int sCol, int eCol, TypeDescKind typeKind, String name) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from(srcFile.name(), LinePosition.from(line, sCol), LinePosition.from(line, eCol)));
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        type.get().getName().ifPresent(tName -> assertEquals(tName, name));
    }

    @DataProvider(name = "TypeInitPosProvider2")
    public Object[][] getTypeInit2() {
        return new Object[][]{
                {40, 17, 42, TYPE_REFERENCE, "Person1"},
                {40, 21, 29, STRING, null},
                {41, 17, 20, TYPE_REFERENCE, "Person2"},
                {42, 9, 22, TYPE_REFERENCE, "Person2"},
        };
    }

    @Test
    public void testDistinctClasses() {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(45, 15)).get();
        ClassSymbol clazz = (ClassSymbol) symbol;
        assertEquals(clazz.typeKind(), OBJECT);
        assertEquals(clazz.kind(), CLASS);
        assertTrue(clazz.qualifiers().contains(Qualifier.DISTINCT));
    }

    @DataProvider(name = "hasDefaultTestProvider")
    public Object[][] hasDefaultTest() {
        return new Object[][]{
                {66, 7, true},
                {67, 7, true},
                {68, 7, false},
                {69, 7, false}
        };
    }

    @Test(dataProvider = "hasDefaultTestProvider")
    public void testHasDefaults(int line, int col, boolean hasDefault) {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(line, col)).get();
        ClassFieldSymbol fieldSymbol = (ClassFieldSymbol) symbol;
        assertEquals(fieldSymbol.hasDefaultValue(), hasDefault);
    }

    @Test
    public void testTypeAlias() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(72, 5));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), TYPE_DEFINITION);
        assertEquals(symbol.get().getName().get(), "PersonType");

        TypeSymbol type = ((TypeDefinitionSymbol) symbol.get()).typeDescriptor();
        assertEquals(type.kind(), CLASS);
        assertEquals(type.typeKind(), OBJECT);
        assertEquals(type.getName().get(), "Person1");
    }

    @Test
    public void testTypeReference2() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(75, 24));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), TYPE);

        TypeSymbol type = (TypeSymbol) symbol.get();
        assertEquals(type.typeKind(), TYPE_REFERENCE);
        assertEquals(type.getName().get(), "PersonType");
    }

    @Test(dataProvider = "ClassFieldProvider")
    public void testClassFields(int line, int col, String expName, String expDoc, TypeDescKind expTypeKind,
                                String expAnnot, List<Qualifier> expQuals, boolean hasDefaultValue) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());

        assertEquals(symbol.get().kind(), CLASS_FIELD);
        ClassFieldSymbol field = (ClassFieldSymbol) symbol.get();

        // Check name
        assertEquals(field.getName().get(), expName);

        // Check docs
        Optional<Documentation> fieldDocs = field.documentation();
        assertTrue(fieldDocs.isPresent());
        assertTrue(fieldDocs.get().description().isPresent());
        assertEquals(fieldDocs.get().description().get(), expDoc);

        // Check annotations
        List<AnnotationSymbol> fieldAnnots = field.annotations();
        assertEquals(fieldAnnots.size(), 1);
        assertEquals(fieldAnnots.get(0).getName().get(), expAnnot);

        // Check Qualifiers
        if (expQuals.size() > 0) {
            List<Qualifier> qualifiers = field.qualifiers();
            expQuals.forEach(qualifiers::contains);
        } else {
            assertTrue(field.qualifiers().isEmpty());
        }

        // Check type
        assertEquals(field.typeDescriptor().typeKind(), expTypeKind);

        // Has default values
        assertEquals(field.hasDefaultValue(), hasDefaultValue);
    }

    @DataProvider(name = "ClassFieldProvider")
    public Object[][] getFields() {
        return new Object[][]{
                {103, 18, "testName", "Name field", STRING, "v1", List.of(Qualifier.PRIVATE), false},
                {107, 14, "x", "X field", ANY, "v2", List.of(Qualifier.FINAL), true},
                {111, 16, "foo", "Foo field", TYPE_REFERENCE, "v1", List.of(Qualifier.PRIVATE), false},
        };
    }

    @Test(dataProvider = "ClassMethodPosProvider")
    public void testClassMethods(int line, int col, String expName, String expDoc, String expAnnot,
                                 List<Qualifier> expQuals, String expSignature) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.METHOD);

        MethodSymbol method = (MethodSymbol) symbol.get();

        // check name
        assertEquals(method.getName().get(), expName);

        // check docs (metadata)
        Optional<Documentation> methodDocs = method.documentation();
        assertTrue(methodDocs.isPresent());
        assertTrue(methodDocs.get().description().isPresent());
        assertEquals(methodDocs.get().description().get(), expDoc);

        // check annotations (metadata)
        List<AnnotationSymbol> methodAnnots = method.annotations();
        assertEquals(methodAnnots.size(), 1);
        assertEquals(methodAnnots.get(0).getName().get(), expAnnot);

        // check qualifiers
        if (expQuals.size() > 0) {
            List<Qualifier> qualifiers = method.qualifiers();
            expQuals.forEach(qualifiers::contains);
        } else {
            assertTrue(method.qualifiers().isEmpty());
        }

        // check signature
        assertEquals(method.signature(), expSignature);
    }

    @DataProvider(name = "ClassMethodPosProvider")
    public Object[][] getMethods() {
        return new Object[][]{
                {115, 13, "init", "Constructor", "v2", List.of(),
                        "function init(string testName, int id)"},
                {123, 20, "getName", "Get name", "v1", List.of(Qualifier.PUBLIC),
                        "public function getName() returns string"},
                {130, 29, "getX", "Get X", "v1", List.of(Qualifier.PUBLIC, Qualifier.ISOLATED),
                        "public isolated function getX() returns any"},
                {134, 13, "hello", "Hello", "v2", List.of(),
                        "function hello() returns string"},
        };
    }

    @Test(dataProvider = "TypeInclusionProvider")
    public void testTypeInclusion(int line, int col, String name, TypeDescKind typeDescKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), TYPE);
        assertEquals(((TypeSymbol) symbol.get()).typeKind(), TypeDescKind.TYPE_REFERENCE);

        TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) symbol.get();
        assertEquals(typeRef.getName().get(), name);
        assertEquals(typeRef.typeDescriptor().typeKind(), typeDescKind);
    }

    @DataProvider(name = "TypeInclusionProvider")
    public Object[][] getTypeInclusions() {
        return new Object[][]{
                {94, 5, "FooObject", OBJECT},
                {138, 5, "FooClass", OBJECT}
        };
    }
}
