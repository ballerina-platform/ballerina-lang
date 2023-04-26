/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaModule;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.SERVICE_DECLARATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolNames;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the looking up symbols from the semantic API.
 *
 * @since 2.0.0
 */
public class SymbolBIRTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/testproject");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }

        Project project = BCompileUtil.loadProject("test-src/symbol_at_cursor_import_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "FunctionTypePosProvider")
    public void test(int line, int col, String signature) {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_function_types_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), FUNCTION);

        FunctionTypeSymbol functionTypeSymbol = ((FunctionSymbol) symbol.get()).typeDescriptor();
        assertEquals(functionTypeSymbol.signature(), signature);
    }

    @DataProvider(name = "FunctionTypePosProvider")
    private Object[][] getFunctionTypePos() {
        return new Object[][] {
                {19, 24, "function (function (int x, float... y) returns string fn) returns int"},
                {23, 16, "function (function (int x, function (string s, int... t) returns float fA)" +
                        " returns string fn) returns ()"},
        };
    }

    // Tests if the field symbols of a record type defined in testorg/testproject are accessible
    @Test(dataProvider = "RecordFieldImports")
    public void testRecordFieldSymbol(int line, int col, String fieldName, TypeDescKind typeKind, boolean isOptional,
                                      boolean hasDefaultValue, String signature) {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_record_field_imports_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Symbol symbol = model.symbol(srcFile, from(line, col))
                .orElseThrow(() -> new AssertionError("Expected a symbol at: (" + line + ", " + col + ")"));

        assertEquals(symbol.kind(), SymbolKind.RECORD_FIELD);

        RecordFieldSymbol fieldSymbol = (RecordFieldSymbol) symbol;
        assertEquals(fieldSymbol.getName().get(), fieldName);
        assertEquals(fieldSymbol.typeDescriptor().typeKind(), typeKind);
        assertEquals(fieldSymbol.isOptional(), isOptional);
        assertEquals(fieldSymbol.hasDefaultValue(), hasDefaultValue);
        assertEquals(fieldSymbol.signature(), signature);
    }

    @DataProvider(name = "RecordFieldImports")
    public Object[][] getRecordFieldPos() {
        return new Object[][]{
                {21, 17, "name", STRING, false, false, "string name"},
                {22, 14, "age", INT, false, false, "int age"},
        };
    }

    @Test
    public void testSymbolLookupInBIR() {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_imports_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        Document srcFile = getDocumentForSingleSource(project);

        PackageCompilation packageCompilation = currentPackage.getCompilation();
        SemanticModel model = packageCompilation.getSemanticModel(defaultModuleId);
        BLangPackage pkg = packageCompilation.defaultModuleBLangPackage();
        BPackageSymbol fooPkgSymbol = pkg.imports.get(0).symbol;

        List<SymbolInfo> annotationModuleSymbols = getAnnotationModuleSymbolInfo();
        List<SymbolInfo> moduleLevelSymbols = getModuleLevelSymbolInfo();
        List<SymbolInfo> moduleSymbols = getModuleSymbolInfo();
        List<SymbolInfo> expSymbolNames = getSymbolNames(annotationModuleSymbols, moduleLevelSymbols, moduleSymbols);

        List<Symbol> symbolsInScope = model.visibleSymbols(srcFile, from(18, 0));
        assertList(symbolsInScope, expSymbolNames);

        BallerinaModule fooModule = (BallerinaModule) symbolsInScope.stream()
                .filter(sym -> sym.getName().get().equals("testproject")).findAny().get();
        SemanticAPITestUtils.assertList(fooModule.functions(), List.of("loadHuman", 
                "testAnonTypeDefSymbolsIsNotVisible", "add", "testFnA", "testFnB"));
        SemanticAPITestUtils.assertList(fooModule.constants(), List.of("RED", "GREEN", "BLUE", "PI", "TRUE", "FALSE"));
        
        SemanticAPITestUtils.assertList(fooModule.typeDefinitions(), List.of("HumanObj", "ApplicationResponseError", 
                "Person", "BasicType", "Digit", "FileNotFoundError", "EofError", "Error", "Pet", "Student", "Cat", 
                "Annot", "Detail", "Service", "FnTypeA", "FnTypeB"));

        
        SemanticAPITestUtils.assertList(fooModule.classes(), List.of("PersonObj", "Dog", "EmployeeObj", "Human"));
        SemanticAPITestUtils.assertList(fooModule.enums(), List.of("Colour"));

        List<String> allSymbols = getSymbolNames(fooPkgSymbol, 0);
        SemanticAPITestUtils.assertList(fooModule.allSymbols(), allSymbols);
    }

    @Test(dataProvider = "ImportSymbolPosProvider")
    public void testImportSymbols(int line, int column, String expSymbolName) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, column));
        symbol.ifPresent(value -> assertEquals(value.getName().get(), expSymbolName));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "ImportSymbolPosProvider")
    public Object[][] getImportSymbolPos() {
        return new Object[][]{
                {16, 6, null},
                {16, 10, "testproject"},
                {16, 16, "testproject"},
                {16, 26, null},
                {19, 17, "testproject"},
                {20, 13, "testproject"},
                {22, 5, "testproject"},
                {26, 12, "testproject"},
                {31, 20, "getName"},
        };
    }

    @Test
    public void testTypeInclusions() {
        ModuleSymbol module = (ModuleSymbol) model.symbol(srcFile, from(16, 15)).get();

        Optional<ClassSymbol> dog = module.classes().stream()
                .filter(symbol -> "Dog".equals(symbol.getName().get())).findFirst();
        List<TypeSymbol> inclusions = dog.get().typeInclusions();
        assertEquals(inclusions.size(), 1);
        assertEquals(inclusions.get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) inclusions.get(0)).typeDescriptor().typeKind(), OBJECT);
        assertEquals(inclusions.get(0).getName().get(), "Pet");

        Optional<TypeDefinitionSymbol> typeDef = module.typeDefinitions().stream()
                .filter(symbol -> "Cat".equals(symbol.getName().get())).findFirst();
        ObjectTypeSymbol cat = (ObjectTypeSymbol) typeDef.get().typeDescriptor();
        inclusions = cat.typeInclusions();
        assertEquals(inclusions.size(), 1);
        assertEquals(inclusions.get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) inclusions.get(0)).typeDescriptor().typeKind(), OBJECT);
        assertEquals(inclusions.get(0).getName().get(), "Pet");

        typeDef = module.typeDefinitions().stream()
                .filter(symbol -> "Student".equals(symbol.getName().get())).findFirst();
        RecordTypeSymbol student = (RecordTypeSymbol) typeDef.get().typeDescriptor();
        inclusions = student.typeInclusions();
        assertEquals(inclusions.size(), 1);
        assertEquals(inclusions.get(0).typeKind(), TYPE_REFERENCE);
        assertEquals(((TypeReferenceTypeSymbol) inclusions.get(0)).typeDescriptor().typeKind(), RECORD);
        assertEquals(inclusions.get(0).getName().get(), "Person");
    }

    @Test(dataProvider = "MethodsInAbstractObject")
    public void testMethodsInAbstractObject(int line, int col, String name, SymbolKind kind) {
        Optional<Symbol> optionalSymbol = model.symbol(srcFile, from(line, col));
        assertTrue(optionalSymbol.isPresent());
        Symbol symbol = optionalSymbol.get();
        assertEquals(symbol.getName().get(), name);
        assertEquals(symbol.kind(), kind);
    }

    @DataProvider(name = "MethodsInAbstractObject")
    public Object[][] getMethodSymbolsInAbstractObject() {
        return new Object[][]{
                {36, 26, "eatFunction", SymbolKind.METHOD},
                {37, 24, "walkFunction", SymbolKind.METHOD},
                {38, 17, "age", SymbolKind.OBJECT_FIELD},
        };
    }

    @Test
    public void testServiceDecl() {
        Optional<Symbol> optionalSymbol = model.symbol(srcFile, from(46, 0));
        assertTrue(optionalSymbol.isPresent());
        assertEquals(optionalSymbol.get().kind(), SERVICE_DECLARATION);
        ServiceDeclarationSymbol symbol = (ServiceDeclarationSymbol) optionalSymbol.get();

        // Annotations
        assertEquals(symbol.annotations().size(), 1);
        AnnotationSymbol annotationSymbol = symbol.annotations().get(0);
        assertEquals(annotationSymbol.getName().get(), "ServiceAnnot");
        assertTrue(annotationSymbol.typeDescriptor().isPresent());
        assertEquals(annotationSymbol.typeDescriptor().get().typeKind(), TYPE_REFERENCE);
        TypeReferenceTypeSymbol typeRefSymbol = (TypeReferenceTypeSymbol) annotationSymbol.typeDescriptor().get();
        assertEquals(typeRefSymbol.typeDescriptor().typeKind(), RECORD);
        assertTrue(((RecordTypeSymbol) typeRefSymbol.typeDescriptor()).fieldDescriptors().containsKey("serviceName"));

        // Fields and methods
        assertTrue(symbol.fieldDescriptors().containsKey("count"));
        assertTrue(symbol.methods().isEmpty());
    }

    // util methods

    public static void assertList(List<Symbol> actualValues, List<SymbolInfo> expectedValues) {
        assertEquals(actualValues.size(), expectedValues.size());

        for (SymbolInfo val : expectedValues) {
            assertTrue(actualValues.stream()
                               .anyMatch(sym -> val.equals(new SymbolInfo(sym.getName().get(), sym.kind()))),
                       "Symbol not found: " + val);

        }
    }

    private List<SymbolInfo> getAnnotationModuleSymbolInfo() {
        return createSymbolInfoList(
                new Object[][]{
                        {"deprecated", ANNOTATION}, {"untainted", ANNOTATION}, {"tainted", ANNOTATION},
                        {"display", ANNOTATION}, {"strand", ANNOTATION}, {"StrandData", TYPE_DEFINITION},
                        {"typeParam", ANNOTATION}, {"Thread", TYPE_DEFINITION}, {"builtinSubtype", ANNOTATION},
                        {"isolatedParam", ANNOTATION}, {"error", TYPE_DEFINITION}
                });
    }

    private List<SymbolInfo> getModuleLevelSymbolInfo() {
        return createSymbolInfoList(
                new Object[][]{
                        {"aString", VARIABLE}, {"anInt", VARIABLE}, {"HELLO", CONSTANT}, {"testAnonTypes", FUNCTION}
                });
    }

    private List<SymbolInfo> getModuleSymbolInfo() {
        return createSymbolInfoList(
                new Object[][]{
                        {"xml", MODULE}, {"testproject", MODULE}, {"object", MODULE}, {"error", MODULE},
                        {"boolean", MODULE}, {"decimal", MODULE}, {"typedesc", MODULE}, {"float", MODULE},
                        {"function", MODULE}, {"future", MODULE}, {"int", MODULE}, {"map", MODULE}, {"stream", MODULE},
                        {"string", MODULE}, {"table", MODULE}, {"transaction", MODULE}
                });
    }

    private List<SymbolInfo> createSymbolInfoList(Object[][] infoArr) {
        List<SymbolInfo> symInfo = new ArrayList<>();
        for (int i = 0; i < infoArr.length; i++) {
            symInfo.add(new SymbolInfo((String) infoArr[i][0], (SymbolKind) infoArr[i][1]));
        }
        return symInfo;
    }

    static class SymbolInfo {
        private String name;
        private SymbolKind kind;

        SymbolInfo(String name, SymbolKind kind) {
            this.name = name;
            this.kind = kind;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof SymbolInfo)) {
                return false;
            }

            SymbolInfo info = (SymbolInfo) obj;
            return this.name.equals(info.name) && this.kind == info.kind;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.kind);
        }

        @Override
        public String toString() {
            return "Symbol {name='" + name + '\'' + ", kind=" + kind + '}';
        }
    }

    @DataProvider(name = "hasDefaultTestProvider")
    public Object[][] hasDefaultTest() {
        return new Object[][]{
                {21, 6, null},
                {22, 6, true},
                {23, 6, false}
        };
    }

    @Test(dataProvider = "hasDefaultTestProvider")
    public void testHasDefaultValue(int line, int col, Boolean hasDefault) {
        Project projectDefaultValue = BCompileUtil.loadProject("test-src/symbol_lookup_with_class_field_test.bal");
        SemanticModel modelDefaultValue = getDefaultModulesSemanticModel(projectDefaultValue);
        Document srcFileDefaultValue = getDocumentForSingleSource(projectDefaultValue);
        Optional<Symbol> symbol = modelDefaultValue.symbol(srcFileDefaultValue, from(line, col));

        if (symbol.isEmpty()) {
            assertNull(hasDefault);
        } else {
            ClassFieldSymbol fieldSymbol = (ClassFieldSymbol) symbol.get();
            assertEquals((Boolean) fieldSymbol.hasDefaultValue(), hasDefault);
        }
    }
}
