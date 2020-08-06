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
public class AssignmentStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testSimpleAssignment() {
        test("a = 5;", "assignment-stmt/assignment_stmt_assert_1.json");
    }

    @Test
    public void testVarRefAssignment() {
        test("a = b;", "assignment-stmt/assignment_stmt_assert_2.json");
    }

    @Test
    public void testQualifiedVarRefAssignment() {
        test("pkg:a = b;", "assignment-stmt/assignment_stmt_assert_8.json");
    }

    @Test
    public void testExpressionsInLHS() {
        testFile("assignment-stmt/assignment_stmt_source_10.bal", "assignment-stmt/assignment_stmt_assert_10.json");
    }

    // Recovery tests

    @Test
    public void testAssignmentWithMissingSemicolon() {
        test("a = 5", "assignment-stmt/assignment_stmt_assert_3.json");
    }

    @Test
    public void testAssignmentWithMissingEqual() {
        test("a 5;", "assignment-stmt/assignment_stmt_assert_4.json");
    }

    @Test
    public void testAssignmentWithMissingRhsExpr() {
        test("a =;", "assignment-stmt/assignment_stmt_assert_5.json");
    }

    @Test
    public void testAssignmentWithMissingLhs() {
        test("= 5;", "assignment-stmt/assignment_stmt_assert_6.json");
    }

    @Test
    public void testAssignmentWithMissingLhsAndRhs() {
        test("=;", "assignment-stmt/assignment_stmt_assert_7.json");
    }

    @Test
    public void testQualifiedVarRefWithAdditionalColons() {
        test("pkg::a = b;", "assignment-stmt/assignment_stmt_assert_9.json");
    }

    @Test
    public void testExpressionsInLHSWithMissingEqual() {
        testFile("assignment-stmt/assignment_stmt_source_11.bal", "assignment-stmt/assignment_stmt_assert_11.json");
    }

    @Test
    public void testMissingRHS() {
        testFile("assignment-stmt/assignment_stmt_source_12.bal", "assignment-stmt/assignment_stmt_assert_12.json");
        testFile("assignment-stmt/assignment_stmt_source_14.bal", "assignment-stmt/assignment_stmt_assert_14.json");
    }

    @Test
    public void testComplexExprInLHSWithMissingEqual() {
        test("a + b + c  d;", "assignment-stmt/assignment_stmt_assert_13.json");
    }
}
