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
 * Test parsing ForEach statements.
 */
public class ForEachStatementTest extends AbstractStatementTest {

    // Valid syntax tests

   @Test
    public void testForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_01.bal",
        "forEach-stmt/forEach_stmt_assert_01.json");
    }

    @Test
    public void testEmptyForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_02.bal",
        "forEach-stmt/forEach_stmt_assert_02.json");
    }

    @Test
    public void testMultipleForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_03.bal",
        "forEach-stmt/forEach_stmt_assert_03.json");
    }

    @Test
    public void testListBindingPatternForm1ForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_10.bal",
        "forEach-stmt/forEach_stmt_assert_10.json");
    }

    @Test
    public void testListBindingPatternForm2ForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_11.bal",
        "forEach-stmt/forEach_stmt_assert_11.json");
    }

    @Test
    public void testListBindingPatternForm3ForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_12.bal",
        "forEach-stmt/forEach_stmt_assert_12.json");
    }

    @Test
    public void testListBindingPatternForm4ForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_13.bal",
        "forEach-stmt/forEach_stmt_assert_13.json");
    }

    @Test
    public void testComplexTypedBindingPatternForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_18.bal",
                "forEach-stmt/forEach_stmt_assert_18.json");
    }

    @Test
    public void testMappingBindingPatternForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_19.bal",
                "forEach-stmt/forEach_stmt_assert_19.json");
    }
    // Recovery tests

    @Test
    public void testForEachStmtWithMissingClosingBraces() {
        testFile("forEach-stmt/forEach_stmt_source_04.bal",
        "forEach-stmt/forEach_stmt_assert_04.json");
    }

    @Test
    public void testForEachStmtwithMissingInKeyword() {
        testFile("forEach-stmt/forEach_stmt_source_05.bal",
        "forEach-stmt/forEach_stmt_assert_05.json");
    }

    @Test
    public void testNestedObjectRecovery() {
        testFile("forEach-stmt/forEach_stmt_source_06.bal",
        "forEach-stmt/forEach_stmt_assert_06.json");
    }

    @Test
    public void testForEachStmtwithMissingExpression() {
        testFile("forEach-stmt/forEach_stmt_source_07.bal",
        "forEach-stmt/forEach_stmt_assert_07.json");
    }

    @Test
    public void testForEachStmtwithMissingTypeDescriptor() {
        testFile("forEach-stmt/forEach_stmt_source_08.bal",
        "forEach-stmt/forEach_stmt_assert_08.json");
    }

    @Test
    public void testForEachStmtwithMissingTypedBindingPattern() {
        testFile("forEach-stmt/forEach_stmt_source_09.bal",
        "forEach-stmt/forEach_stmt_assert_09.json");
    }

    @Test
    public void testListBindingPatternMissingCommaForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_14.bal",
        "forEach-stmt/forEach_stmt_assert_14.json");
    }

    @Test
    public void testListBindingPatternMissingBindingPatternForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_15.bal",
        "forEach-stmt/forEach_stmt_assert_15.json");
    }

    @Test
    public void testListBindingPatternInvalidComponentsForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_16.bal",
                "forEach-stmt/forEach_stmt_assert_16.json");
    }

    @Test
    public void testListBindingPatternInvalidComponents2ForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_17.bal",
                "forEach-stmt/forEach_stmt_assert_17.json");
    }

    @Test
    public void testMappingBindingPatternMissingCommaForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_20.bal",
                "forEach-stmt/forEach_stmt_assert_20.json");
    }

    @Test
    public void testMappingBindingPatternMissingComma2ForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_21.bal",
                "forEach-stmt/forEach_stmt_assert_21.json");
    }

    @Test
    public void testMappingBindingPatternInvalidTokenForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_22.bal",
                "forEach-stmt/forEach_stmt_assert_22.json");
    }

    @Test
    public void testMappingBindingPatternMissingCloseBraceForEachStmt() {
        testFile("forEach-stmt/forEach_stmt_source_23.bal",
                "forEach-stmt/forEach_stmt_assert_23.json");
    }
}
