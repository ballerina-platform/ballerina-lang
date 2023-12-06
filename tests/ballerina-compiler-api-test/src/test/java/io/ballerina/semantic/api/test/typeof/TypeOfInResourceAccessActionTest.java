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

import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
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
                {42, 23, 42, 54, UNION},
                {42, 23, 42, 32, TYPE_REFERENCE},
                {110, 9, 110, 12, STRING},
                {111, 9, 111, 14, STRING},
                {111, 17, 111, 25, STRING},
                {112, 17, 111, 19, STRING},
                {113, 15, 113, 19, INT},
                {114, 15, 114, 16, INT},
                {115, 9, 115, 14, STRING},
                {115, 17, 115, 24, STRING},
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
