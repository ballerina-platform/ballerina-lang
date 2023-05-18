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
package io.ballerinalang.compiler.parser.test.syntax.statements;

import org.testng.annotations.Test;

/**
 * Test parsing assignment statements.
 */
public class DestructuringAssignmentStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testListBindingPattern() {
        testFile("destructuring-assignment/list_binding_pattern_source_01.bal",
                "destructuring-assignment/list_binding_pattern_assert_01.json");
    }

    @Test
    public void testMappingBindingPattern() {
        testFile("destructuring-assignment/mapping_binding_pattern_source_01.bal",
                "destructuring-assignment/mapping_binding_pattern_assert_01.json");
    }

    @Test
    public void testErrorBindingPattern() {
        testFile("destructuring-assignment/error_binding_pattern_source_01.bal",
                "destructuring-assignment/error_binding_pattern_assert_01.json");
    }

    @Test
    public void testErrorBindingPatternAsMember() {
        testFile("destructuring-assignment/error_binding_pattern_source_02.bal",
                "destructuring-assignment/error_binding_pattern_assert_02.json");
    }

    @Test
    public void testWildcardBindingPattern() {
        testFile("destructuring-assignment/wildcard_binding_pattern_source_01.bal",
                "destructuring-assignment/wildcard_binding_pattern_assert_01.json");
    }

    // Recovery tests

    @Test
    public void testListBindingPatternRecovery() {
        testFile("destructuring-assignment/list_binding_pattern_source_02.bal",
                "destructuring-assignment/list_binding_pattern_assert_02.json");
    }

    @Test
    public void testErrorBindingPatternRecovery() {
        testFile("destructuring-assignment/error_binding_pattern_source_03.bal",
                "destructuring-assignment/error_binding_pattern_assert_03.json");
    }

    @Test
    public void testMappingBindingPatternWithSingleVarNameField() {
        testFile("destructuring-assignment/mapping_binding_pattern_source_02.bal",
                "destructuring-assignment/mapping_binding_pattern_assert_02.json");
    }
}
