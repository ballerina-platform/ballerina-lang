/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

/**
 * Test cases for verifying that type caching works.
 *
 * @since 2.0.0
 */
public class TypeCacheTest {

    private SemanticModel model;
    private final String fileName = "type_cache_test.bal";

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/type_cache_test.bal");
    }

    @Test
    public void testRecords() {
        TypeDefinitionSymbol personDefSymbol =
                (TypeDefinitionSymbol) model.symbol(fileName, LinePosition.from(30, 5)).get();
        TypeSymbol personTypedesc = personDefSymbol.typeDescriptor();
        List<FieldSymbol> personFields = ((RecordTypeSymbol) personTypedesc).fieldDescriptors();
        FieldSymbol parentField = personFields.stream().filter(field -> "parent".equals(field.name())).findAny().get();
        TypeSymbol type = ((UnionTypeSymbol) parentField.typeDescriptor()).memberTypeDescriptors().get(0);

        assertSame(personTypedesc, ((TypeReferenceTypeSymbol) type).typeDescriptor());

        TypeDefinitionSymbol employeeDefSymbol =
                (TypeDefinitionSymbol) model.symbol(fileName, LinePosition.from(36, 5)).get();
        TypeSymbol typeInclusion = ((RecordTypeSymbol) employeeDefSymbol.typeDescriptor()).typeInclusions().get(0);

        assertSame(personTypedesc, ((TypeReferenceTypeSymbol) typeInclusion).typeDescriptor());

        List<FieldSymbol> empFields = ((RecordTypeSymbol) employeeDefSymbol.typeDescriptor()).fieldDescriptors();
        FieldSymbol designationType =
                empFields.stream().filter(field -> "designation".equals(field.name())).findAny().get();
        FieldSymbol nameType =
                personFields.stream().filter(field -> "name".equals(field.name())).findAny().get();

        assertSame(nameType.typeDescriptor(), designationType.typeDescriptor());
    }

    @Test(enabled = false)
    public void testObjectsAndClasses() {
        ClassSymbol personClzSymbol =
                (ClassSymbol) model.symbol(fileName, LinePosition.from(16, 6)).get();
        VariableSymbol personVar =
                (VariableSymbol) model.symbol(fileName, LinePosition.from(42, 14)).get();
        assertSame(personClzSymbol, ((TypeReferenceTypeSymbol) personVar.typeDescriptor()).typeDescriptor());
    }

    @Test
    public void testFunctionTypes() {
        FunctionSymbol fnAdd =
                (FunctionSymbol) model.symbol(fileName, LinePosition.from(46, 9)).get();
        FunctionSymbol fnAdd2 =
                (FunctionSymbol) model.symbol(fileName, LinePosition.from(43, 14)).get();
        assertSame(fnAdd.typeDescriptor(), fnAdd2.typeDescriptor());

        FunctionSymbol fnSum1 =
                (FunctionSymbol) model.symbol(fileName, LinePosition.from(48, 9)).get();
        FunctionSymbol fnSum2 =
                (FunctionSymbol) model.symbol(fileName, LinePosition.from(48, 9)).get();

        // Due to the issue with overridden equals() in BInvokableType, this equivalent function type to the above
        // one doesn't get cached. Hence not the same typedesc.
        assertNotSame(fnSum1.typeDescriptor(), fnSum2.typeDescriptor());
    }
}
