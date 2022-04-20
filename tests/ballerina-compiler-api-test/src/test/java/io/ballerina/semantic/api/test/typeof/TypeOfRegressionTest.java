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

package io.ballerina.semantic.api.test.typeof;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Regression tests for typeOf().
 */
public class TypeOfRegressionTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel(
                "test-src/regression-tests/typeof_listener_test.bal");
    }

    @Test
    public void testListenerType() {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("typeof_listener_test.bal",
                               LinePosition.from(16, 19), LinePosition.from(16, 35)));

        assertTrue(type.isPresent());
        assertEquals(type.get().kind(), SymbolKind.TYPE);
        assertEquals(type.get().typeKind(), TypeDescKind.TYPE_REFERENCE);

        ClassSymbol clazz = (ClassSymbol) ((TypeReferenceTypeSymbol) type.get()).definition();
        assertEquals(clazz.kind(), SymbolKind.CLASS);
        assertEquals(clazz.typeKind(), TypeDescKind.OBJECT);
        assertEquals(clazz.getName().get(), "Listener");
    }
}
