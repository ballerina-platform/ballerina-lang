/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for class symbols.
 *
 * @since 2.0.0
 */
public class TypeAssignabilityTest {

    private SemanticModel model;
    private final String fileName = "type_assignability_test.bal";

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/type_assignability_test.bal");
    }

    @Test(dataProvider = "ClassPosProvider")
    public void testAssignabilityForClasses(int sourceLine, int sourceCol, int targetLine, int targetCol) {
        ClassSymbol srcSymbol = (ClassSymbol) model.symbol(fileName, from(sourceLine, sourceCol)).get();
        ClassSymbol targetSymbol = (ClassSymbol) model.symbol(fileName, from(targetLine, targetCol)).get();

        assertTrue(srcSymbol.assignableTo(targetSymbol));
    }

    @DataProvider(name = "ClassPosProvider")
    public Object[][] getClassPos() {
        return new Object[][]{
                {16, 6, 30, 6},
                {30, 6, 16, 6},
        };
    }

    @Test
    public void testAssignabilityForClassesAndObjects() {
        ClassSymbol srcSymbol = (ClassSymbol) model.symbol(fileName, from(30, 6)).get();
        TypeDefinitionSymbol targetSymbol = (TypeDefinitionSymbol) model.symbol(fileName, from(39, 5)).get();

        assertTrue(srcSymbol.assignableTo(targetSymbol.typeDescriptor()));
    }

    @Test(dataProvider = "TypeDefPosProvider")
    public void testAssignabilityForTypeDefs(int sourceLine, int sourceCol, int targetLine, int targetCol,
                                             boolean assignable) {
        TypeDefinitionSymbol srcSymbol =
                (TypeDefinitionSymbol) model.symbol(fileName, from(sourceLine, sourceCol)).get();
        TypeDefinitionSymbol targetSymbol =
                (TypeDefinitionSymbol) model.symbol(fileName, from(targetLine, targetCol)).get();

        assertEquals(srcSymbol.typeDescriptor().assignableTo(targetSymbol.typeDescriptor()), assignable);
    }

    @DataProvider(name = "TypeDefPosProvider")
    public Object[][] getTypeDefPos() {
        return new Object[][]{
                {52, 6, 46, 6, true},
                {58, 6, 60, 6, false},
                {60, 6, 58, 6, true},
        };
    }

    @Test(dataProvider = "VarPosProvider")
    public void testAssignabilityForVars(int sourceLine, int sourceCol, int targetLine, int targetCol,
                                         boolean assignable) {
        VariableSymbol srcSymbol = (VariableSymbol) model.symbol(fileName, from(sourceLine, sourceCol)).get();
        VariableSymbol targetSymbol = (VariableSymbol) model.symbol(fileName, from(targetLine, targetCol)).get();

        assertEquals(srcSymbol.typeDescriptor().assignableTo(targetSymbol.typeDescriptor()), assignable);
    }

    @DataProvider(name = "VarPosProvider")
    public Object[][] getVarPos() {
        return new Object[][]{
                {63, 21, 64, 15, false},
                {64, 15, 63, 21, true},
                {66, 20, 67, 20, true},
                {67, 20, 66, 20, false},
                {69, 17, 70, 20, false},
                {70, 20, 69, 17, true},
        };
    }
}
