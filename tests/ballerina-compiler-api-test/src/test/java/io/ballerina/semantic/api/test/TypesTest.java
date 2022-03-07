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
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ANY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ANYDATA;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.COMPILATION_ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.HANDLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.JSON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NEVER;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.READONLY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
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
    private SemanticModel model;
    private Types types;

    @BeforeClass
    public void setup() {
        project = BCompileUtil.loadProject("test-src/types-project");
        model = getDefaultModulesSemanticModel(project);
        types = model.types();
    }

    @Test(dataProvider = "BuiltInTypesProvider")
    public void testBuiltInTypes(TypeSymbol typeSymbol, TypeDescKind kind, Class<? extends TypeSymbol> typeClass) {
        assertEquals(typeSymbol.typeKind(), kind);
        assertEquals(typeSymbol.getClass(), typeClass);
    }

    @DataProvider(name = "BuiltInTypesProvider")
    public Object[][] getBuiltInTypes() {
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
    public void testTypeByName(String moduleName, String typeDefName, TypeDescKind typeDescKind) {
        Module module = getModule(project, moduleName);
        model = project.currentPackage().getCompilation().getSemanticModel(module.moduleId());
        types = model.types();
        String org = module.descriptor().org().value();
        String pkgName = module.descriptor().name().toString();
        String version = module.descriptor().version().toString();

        Optional<TypeDefinitionSymbol> typeDefSymbol = types.getByName(org, pkgName, version, typeDefName);

        assertTrue(typeDefSymbol.isPresent());
        assertEquals(typeDefSymbol.get().typeDescriptor().typeKind(), typeDescKind);

        Optional<String> symbolName = typeDefSymbol.get().getName();
        assertTrue(symbolName.isPresent());
        assertEquals(symbolName.get(), typeDefName);
    }

    @DataProvider(name = "TypesByNameProvider")
    public Object[][] getTypesByName() {
        return new Object[][] {
                {"foo", "ErrorDetail1", RECORD},
                {"foo", "MyErr", ERROR},
                {"foo", "MyInt", UNION},

                {"bar", "MyBoolean", UNION},
                {"bar", "MyFloat", DECIMAL},
                {"bar", "Student", RECORD},

                {"bar", "Person", RECORD},
                {"bar", "Employee", RECORD},
                {"bar", "Digit", UNION},
                {"bar", "ExampleErr", ERROR},
        };
    }


    @Test
    public void testGetTypesInFooModule() {
        testTypesInModule("foo", getTypesInFooModule());
    }

    @Test
    public void testTypesInBarModule() {
        testTypesInModule("bar", getTypesInBarModule());
    }

    private Object[][] getTypesInFooModule() {
        return new Object[][] {
                {"ErrorDetail1", RECORD},
                {"MyErr", ERROR},
                {"MyInt", UNION},
        };
    }

    private Object[][] getTypesInBarModule() {
        return new Object[][] {
                // types.bal
                {"MyBoolean", UNION},
                {"MyFloat", DECIMAL},
                {"Student", RECORD},

                // more_typedefs.bal
                {"Person", RECORD},
                {"Employee", RECORD},
                {"Digit", UNION},
                {"ExampleErr", ERROR},
        };
    }

    private void testTypesInModule(String moduleName, Object[][] expTypes) {
        Module module = getModule(project, moduleName);
        model = project.currentPackage().getCompilation().getSemanticModel(module.moduleId());
        types = model.types();
        String org = module.descriptor().org().value();
        String pkgName = module.descriptor().name().toString();
        String version = module.descriptor().version().toString();

        Optional<Map<String, TypeDefinitionSymbol>> typesInModule = types.typesInModule(org, pkgName, version);
        assertTrue(typesInModule.isPresent());

        Map<String, TypeDefinitionSymbol> typeDefSymbolMap = typesInModule.get();
        assertEquals(typeDefSymbolMap.size(), expTypes.length);

        for (Object[] expType : expTypes) {
            String expTypeName = String.valueOf(expType[0]);
            TypeDefinitionSymbol typeDefSymbol = typeDefSymbolMap.get(expTypeName);
            assertNotNull(typeDefSymbol);
            assertEquals(typeDefSymbol.typeDescriptor().typeKind(), expType[1]);
        }
    }

}
