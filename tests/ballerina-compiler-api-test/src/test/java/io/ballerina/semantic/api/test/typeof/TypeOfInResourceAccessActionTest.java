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

import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests cases for testing typeOf() with resource access action.
 *
 * @since 2201.2.0
 */
public class TypeOfInResourceAccessActionTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel(
                "test-src/actions/resource_access_action.bal");
    }

    @Test(dataProvider = "NamedArgsPos")
    public void testTypeOfInNamedArgs(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "NamedArgsPos")
    public Object[][] getPos() {
        return new Object[][]{
                {40, 23, 40, 54, UNION},
                {40, 23, 40, 32, TYPE_REFERENCE},
        };
    }

    private void assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("resource_access_action.bal", LinePosition.from(sLine, sCol),
                               LinePosition.from(eLine, eCol)));

        if (kind == null) {
            assertTrue(type.isEmpty());
            return;
        }

        assertEquals(type.get().typeKind(), kind);
    }
}
