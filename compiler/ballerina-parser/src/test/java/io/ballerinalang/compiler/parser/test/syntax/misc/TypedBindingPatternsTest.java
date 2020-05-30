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
package io.ballerinalang.compiler.parser.test.syntax.misc;

import org.testng.annotations.Test;

/**
 * Test parsing typed binding patterns.
 * 
 * @since 2.0.0
 */
public class TypedBindingPatternsTest extends AbstractMiscTest {

    @Test
    public void testListBindingPatternWithBuiltinType() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_01.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_01.json");
    }

    @Test
    public void testListBindingPatternWithCustomType() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_02.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_02.json");
    }

    @Test
    public void testListBindingPatternWithBuiltinArrayType() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_03.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_03.json");
    }

    @Test
    public void testListBindingPatternWithCustomArrayType() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_04.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_04.json");
    }

    @Test
    public void testBindingPatternWithMultiDimentionalArrays() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_05.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_05.json");
    }

    @Test
    public void testBindingPatternWithUnionsAndIntersectionOfArrays() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_06.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_06.json");
    }

    @Test
    public void testListBindingPatternInLetVarDeclInQuery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_10.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_10.json");
    }

    @Test
    public void testListBindingPatternInLetVarDeclInLetExpr() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_11.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_11.json");
    }

    @Test
    public void testMappingBindingPattern() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_14.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_14.json");
    }

    @Test
    public void testMappingBindingPatternWithListsInside() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_15.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_15.json");
    }

    @Test
    public void testComplexTypedBindingPatternWith() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_16.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_16.json");
    }

    @Test
    public void testMappingBindingPatternInLetVarDeclInQuery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_17.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_17.json");
    }

    // Recovery tests

    @Test
    public void testListBindingPatternWithBuiltinTypeRecovery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_07.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_07.json");
    }

    @Test
    public void testListBindingPatternWithCustomTypeRecovery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_08.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_08.json");
    }

    @Test
    public void testListBindingPatternWithArrayTypeRecovery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_09.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_09.json");
    }

    @Test
    public void testBindingPatternInLetVarDeclInQueryRecovery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_12.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_12.json");
    }

    @Test
    public void testBindingPatternInLetVarDeclInLetExprRecovery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_13.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_13.json");
    }

    @Test
    public void testMappingBindingPatternWithListsInsideRecovery() {
        testFile("typed-binding-patterns/typed_binding_patterns_source_18.bal",
                "typed-binding-patterns/typed_binding_patterns_assert_18.json");
    }

}
