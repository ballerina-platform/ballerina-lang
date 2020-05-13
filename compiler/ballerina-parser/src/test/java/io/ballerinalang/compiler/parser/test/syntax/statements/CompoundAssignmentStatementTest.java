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
public class CompoundAssignmentStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testSimpleCompoundAssignment() {
        test("a += 5;", "compound-assignment-stmt/compound_assignment_stmt_assert_1.json");
    }

    @Test
    public void testVarRefCompoundAssignment() {
        test("a += b;", "compound-assignment-stmt/compound_assignment_stmt_assert_2.json");
    }

    @Test
    public void testQualifiedVarRefCompoundAssignment() {
        test("pkg:a += b;", "compound-assignment-stmt/compound_assignment_stmt_assert_7.json");
    }

    @Test
    public void testExpressionsInLHS() {
        testFile("compound-assignment-stmt/compound_assignment_stmt_source_9.bal",
                 "compound-assignment-stmt/compound_assignment_stmt_assert_9.json");
    }

    // Recovery tests

    @Test
    public void testCompoundAssignmentWithMissingSemicolon() {
        test("a += 5", "compound-assignment-stmt/compound_assignment_stmt_assert_3.json");
    }

    @Test
    public void testCompoundAssignmentWithMissingRhsExpr() {
        test("a +=;", "compound-assignment-stmt/compound_assignment_stmt_assert_4.json");
    }

    @Test
    public void testCompoundAssignmentWithMissingLhs() {
        test("+= 5;", "compound-assignment-stmt/compound_assignment_stmt_assert_5.json");
    }

    @Test
    public void testCompoundAssignmentWithMissingLhsAndRhs() {
        test("+=;", "compound-assignment-stmt/compound_assignment_stmt_assert_6.json");
    }

    @Test
    public void testQualifiedVarRefWithAdditionalColons() {
        test("pkg::a += b;", "compound-assignment-stmt/compound_assignment_stmt_assert_8.json");
    }
    
    @Test
    public void testMissingRHS() {
        testFile("compound-assignment-stmt/compound_assignment_stmt_source_10.bal",
                 "compound-assignment-stmt/compound_assignment_stmt_assert_10.json");
    }
}
