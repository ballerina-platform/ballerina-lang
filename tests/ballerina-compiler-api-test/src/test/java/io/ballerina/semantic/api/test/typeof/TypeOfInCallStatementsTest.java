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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests cases for testing typeOf() in call statements.
 */
public class TypeOfInCallStatementsTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel(
                "test-src/statements/symbols_in_call_stmt.bal");
    }

    @Test(dataProvider = "NamedArgsPos")
    public void testTypeOfInNamedArgs(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "NamedArgsPos")
    public Object[][] getPos() {
        return new Object[][]{
                {23, 14, 23, 17, null},
                {23, 14, 23, 23, null},
                {23, 20, 23, 23, INT},
                {29, 22, 29, 29, STRING},
                {44, 28, 44, 29, null},
                {44, 28, 44, 37, null},
                {44, 32, 44, 37, STRING},
        };
    }

    private void assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("symbols_in_call_stmt.bal", LinePosition.from(sLine, sCol),
                               LinePosition.from(eLine, eCol)));

        if (kind == null) {
            assertTrue(type.isEmpty());
            return;
        }

        assertEquals(type.get().typeKind(), kind);
    }
}
