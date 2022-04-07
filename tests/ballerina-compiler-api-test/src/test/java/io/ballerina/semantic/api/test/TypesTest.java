/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.impl.symbols.BallerinaAnyTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaAnydataTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaBooleanTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaByteTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaCompilationErrorTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaDecimalTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaErrorTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFloatTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFutureTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaHandleTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaIntTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaJSONTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaNeverTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaNilTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaReadonlyTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaStreamTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaStringTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeDescTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaXMLTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM_MEMBER;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.COMPILATION_ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.HANDLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.JSON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NEVER;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.READONLY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.SINGLETON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getModule;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the types() API.
 *
 * @since 2.0.0
 */
public class TypesTest {

    Project project;
    private Types types;

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/typesbir");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }

        project = BCompileUtil.loadProject("test-src/types-project");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        types = model.types();
    }

    @Test(dataProvider = "BuiltInTypesProvider")
    public void testBuiltInTypes(TypeSymbol typeSymbol, TypeDescKind kind, Class<? extends TypeSymbol> typeClass) {
        assertEquals(typeSymbol.typeKind(), kind);
        assertEquals(typeSymbol.getClass(), typeClass);
    }

    @DataProvider(name = "BuiltInTypesProvider")
    private Object[][] getBuiltInTypes() {
        return new Object[][] {
                {types.BOOLEAN, BOOLEAN, BallerinaBooleanTypeSymbol.class},
                {types.INT, INT, BallerinaIntTypeSymbol.class},
                {types.FLOAT, FLOAT, BallerinaFloatTypeSymbol.class},
                {types.DECIMAL, DECIMAL, BallerinaDecimalTypeSymbol.class},
                {types.STRING, STRING, BallerinaStringTypeSymbol.class},
                {types.NIL, NIL, BallerinaNilTypeSymbol.class},
                {types.XML, XML, BallerinaXMLTypeSymbol.class},
                {types.ERROR, ERROR, BallerinaErrorTypeSymbol.class},
                {types.FUTURE, FUTURE, BallerinaFutureTypeSymbol.class},
                {types.FUNCTION, FUNCTION, BallerinaFunctionTypeSymbol.class},
                {types.TYPEDESC, TYPEDESC, BallerinaTypeDescTypeSymbol.class},
                {types.HANDLE, HANDLE, BallerinaHandleTypeSymbol.class},
                {types.STREAM, STREAM, BallerinaStreamTypeSymbol.class},
                {types.ANY, ANY, BallerinaAnyTypeSymbol.class},
                {types.ANYDATA, ANYDATA, BallerinaAnydataTypeSymbol.class},
                {types.NEVER, NEVER, BallerinaNeverTypeSymbol.class},
                {types.READONLY, READONLY, BallerinaReadonlyTypeSymbol.class},
                {types.JSON, JSON, BallerinaJSONTypeSymbol.class},
                {types.BYTE, BYTE, BallerinaByteTypeSymbol.class},
                {types.COMPILATION_ERROR, COMPILATION_ERROR, BallerinaCompilationErrorTypeSymbol.class},
        };
    }

    @Test(dataProvider = "TypesByNameProvider")
    public void testTypeByName(String moduleName, String typeDefName, SymbolKind symKind, TypeDescKind typeDescKind) {
        Module module = getModule(project, moduleName);
        SemanticModel model = project.currentPackage().getCompilation().getSemanticModel(module.moduleId());
        Types types = model.types();
        Optional<Symbol> symbol = types.getTypeByName("testorg", "typesapi." + moduleName, "1.0.0", typeDefName);
        assertTrue(symbol.isPresent());
        assertSymbolTypeDesc(symbol.get(), symKind, typeDescKind);
        Optional<String> symbolName = symbol.get().getName();
        assertTrue(symbolName.isPresent());
        assertEquals(symbolName.get(), typeDefName);
    }

    @DataProvider(name = "TypesByNameProvider")
    private Object[][] getTypesByName() {
        return new Object[][] {
                // `foo` module
                {"foo", "ErrorDetail1", TYPE_DEFINITION, RECORD},
                {"foo", "MyErr", TYPE_DEFINITION, ERROR},
                {"foo", "MyInt", TYPE_DEFINITION, UNION},
                {"foo", "Language", ENUM, UNION},
                {"foo", "SI", ENUM_MEMBER, SINGLETON},
                // `bar` module
                {"bar", "MyBoolean", TYPE_DEFINITION, UNION},
                {"bar", "MyFloat", TYPE_DEFINITION, DECIMAL},
                {"bar", "Student", TYPE_DEFINITION, RECORD},
                {"bar", "Person", TYPE_DEFINITION, RECORD},
                {"bar", "Employee", TYPE_DEFINITION, RECORD},
                {"bar", "Digit", TYPE_DEFINITION, UNION},
                {"bar", "ExampleErr", TYPE_DEFINITION, ERROR},
                {"bar", "Color", ENUM, UNION},
                {"bar", "RED", ENUM_MEMBER, SINGLETON},
                {"bar", "ABC", CONSTANT, SINGLETON},
                {"bar", "PersonClass", CLASS, OBJECT},
        };
    }

    @Test
    public void testGetTypesInFooModule() {
        testTypesInModule("typesapi.foo", getTypesInFooModule());
    }

    @Test
    public void testTypesInBarModule() {
        testTypesInModule("typesapi.bar", getTypesInBarModule());
    }

    private Object[][] getTypesInFooModule() {
        return new Object[][] {
                {"ErrorDetail1", TYPE_DEFINITION, RECORD},
                {"MyErr", TYPE_DEFINITION, ERROR},
                {"MyInt", TYPE_DEFINITION, UNION},
                {"Language", ENUM, UNION},
                {"SI", ENUM_MEMBER, SINGLETON},
                {"TA", ENUM_MEMBER, SINGLETON},
                {"EN", ENUM_MEMBER, SINGLETON},
        };
    }

    private Object[][] getTypesInBarModule() {
        return new Object[][] {
                // types.bal
                {"MyBoolean", TYPE_DEFINITION, UNION},
                {"MyFloat", TYPE_DEFINITION, DECIMAL},
                {"Student", TYPE_DEFINITION, RECORD},
                {"Color", ENUM, UNION},
                {"RED", ENUM_MEMBER, SINGLETON},
                {"GREEN", ENUM_MEMBER, SINGLETON},
                {"BLUE", ENUM_MEMBER, SINGLETON},
                // more_typedefs.bal
                {"Person", TYPE_DEFINITION, RECORD},
                {"Employee", TYPE_DEFINITION, RECORD},
                {"Digit", TYPE_DEFINITION, UNION},
                {"ExampleErr", TYPE_DEFINITION, ERROR},
                {"ABC", CONSTANT, SINGLETON},
                {"PersonClass", CLASS, OBJECT},
                {"ListenerCls", CLASS, OBJECT},
        };
    }

    @Test(dataProvider = "TypeByNameFromBir")
    public void testTypeByNameFromBir(String pkgName, String typeDefName,
                                      SymbolKind symKind, TypeDescKind typeDescKind) {
        Optional<Symbol> symbol = types.getTypeByName("testorg", pkgName, "1.0.0", typeDefName);
        assertTrue(symbol.isPresent());
        assertSymbolTypeDesc(symbol.get(), symKind, typeDescKind);
        Optional<String> symbolName = symbol.get().getName();
        assertTrue(symbolName.isPresent());
        assertEquals(symbolName.get(), typeDefName);
    }

    @DataProvider(name = "TypeByNameFromBir")
    private Object[][] getTypeByNameFromBir() {
        return new  Object[][] {
                {"typesapi", "ExampleDec", TYPE_DEFINITION, DECIMAL},
                {"typesbir", "TestRecord", TYPE_DEFINITION, RECORD},
                {"typesbir", "AnInt", TYPE_DEFINITION, TYPE_REFERENCE},
        };
    }

    @Test
    public void testTypesFromBir() {
        testTypesInModule("typesbir", getTypesFromBir());
    }

    private Object[][] getTypesFromBir() {
        return new Object[][] {
                {"TestRecord", TYPE_DEFINITION, RECORD},
                {"AnInt", TYPE_DEFINITION, TYPE_REFERENCE},
        };
    }

    @Test(dataProvider = "subTypeOfProvider")
    public void testSubTypeOfTypes(String moduleName, String typeDefName, TypeSymbol expType) {
        Module module = getModule(project, moduleName);
        SemanticModel model = project.currentPackage().getCompilation().getSemanticModel(module.moduleId());
        Types types = model.types();
        Optional<Symbol> symbol = types.getTypeByName("testorg", "typesapi." + moduleName, "1.0.0", typeDefName);
        assertTrue(symbol.isPresent());
        Optional<TypeSymbol> typeSymbol = getTypeSymbolOfTypeDef(symbol.get());
        assertTrue(typeSymbol.isPresent());
        assertTrue(typeSymbol.get().subtypeOf(expType));
    }

    @DataProvider(name = "subTypeOfProvider")
    private Object[][] getSubTypeOfTypes() {
        return new Object[][] {
                // foo module
                {"foo", "MyErr", types.ERROR},
                {"foo", "ErrorDetail1", types.ANY},
                {"foo", "MyInt", types.ANY},
                {"foo", "Language", types.ANY},
                {"foo", "SI", types.ANY},
                // bar module
                {"bar", "MyFloat", types.ANY},
                {"bar", "Employee", types.ANY},
                {"bar", "Digit", types.ANY},
                {"bar", "ABC", types.ANY},
                {"bar", "PersonClass", types.ANY},
        };
    }

    private void testTypesInModule(String pkgName, Object[][] expTypes) {
        Optional<Map<String, Symbol>> typesInModule = types.typesInModule("testorg", pkgName, "1.0.0");
        assertTrue(typesInModule.isPresent());
        Map<String, Symbol> typeDefSymbolMap = typesInModule.get();
        assertEquals(typeDefSymbolMap.size(), expTypes.length);

        for (Object[] expType : expTypes) {
            String expTypeName = (String) expType[0];
            Symbol symbol = typeDefSymbolMap.get(expTypeName);
            assertNotNull(symbol);
            assertSymbolTypeDesc(symbol, (SymbolKind) expType[1], (TypeDescKind) expType[2]);
        }
    }

    private void assertSymbolTypeDesc(Symbol symbol, SymbolKind symbolKind, TypeDescKind typeDescKind) {
        assertEquals(symbol.kind(), symbolKind);
        Optional<TypeSymbol> typeSymbol = getTypeSymbolOfTypeDef(symbol);
        assertTrue(typeSymbol.isPresent());
        assertEquals(typeSymbol.get().typeKind(), typeDescKind);
    }

    private Optional<TypeSymbol> getTypeSymbolOfTypeDef(Symbol symbol) {
        switch (symbol.kind()) {
            case TYPE_DEFINITION:
                return Optional.of(((TypeDefinitionSymbol) symbol).typeDescriptor());
            case CONSTANT:
            case ENUM_MEMBER:
                return Optional.of(((ConstantSymbol) symbol).typeDescriptor());
            case ENUM:
                return Optional.of(((EnumSymbol) symbol).typeDescriptor());
            case CLASS:
                return Optional.of((ClassSymbol) symbol);
        }
        return Optional.empty();
    }
}
