/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.semantic.api.test.typeof;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Negative test cases for testing typeOf() with expressions.
 */
public class TypeOfNegativeTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel("test-src/type_of_negative_test.bal");
    }

    @Test(dataProvider = "EmptyMappingConstructorPos")
    public void testTypeOfEmptyMappingConstruct(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "EmptyMappingConstructorPos")
    public Object[][] getPos() {
        return new Object[][]{
                {17, 21, 17, 23, TypeDescKind.COMPILATION_ERROR},
                {18, 38, 18, 40, TypeDescKind.COMPILATION_ERROR},
                {24, 78, 24, 80, TypeDescKind.COMPILATION_ERROR},
        };
    }

    private void assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("type_of_negative_test.bal", LinePosition.from(sLine, sCol),
                               LinePosition.from(eLine, eCol)));

        if (kind == null) {
            assertTrue(type.isEmpty());
            return;
        }

        assertEquals(type.get().typeKind(), kind);
    }
}
