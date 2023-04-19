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
 * Test parsing while-statements.
 */
public class WhileStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testEmptyWhile() {
        testFile("while-stmt/while_stmt_source_01.bal", "while-stmt/while_stmt_assert_01.json");
    }

    @Test
    public void testWhileWithBody() {
        testFile("while-stmt/while_stmt_source_02.bal", "while-stmt/while_stmt_assert_02.json");
    }

    @Test
    public void testWhileWithOnFailClause() {
        testFile("while-stmt/while_stmt_source_04.bal", "while-stmt/while_stmt_assert_04.json");
    }

    @Test
    public void testWhileWithOnFailClauseWithoutVariable() {
        testFile("while-stmt/while_stmt_source_06.bal", "while-stmt/while_stmt_assert_06.json");
    }

    @Test
    public void testWhileWithOnFailClauseHavingErrorBPWithVar() {
        testFile("while-stmt/while_stmt_source_07.bal", "while-stmt/while_stmt_assert_07.json");
    }

    @Test
    public void testWhileWithOnFailClauseHavingErrorBPWithType() {
        testFile("while-stmt/while_stmt_source_08.bal", "while-stmt/while_stmt_assert_08.json");
    }

    @Test
    public void testWhileWithOnFailClauseHavingErrorBPWithUserDefinedError() {
        testFile("while-stmt/while_stmt_source_09.bal", "while-stmt/while_stmt_assert_09.json");
    }

    @Test
    public void testWhileWithOnFailClauseHavingErrorBPWithUserDefinedErrorWithVar() {
        testFile("while-stmt/while_stmt_source_10.bal", "while-stmt/while_stmt_assert_10.json");
    }

    // Recovery tests

    @Test
    public void testWhileStmtRecovery() {
        testFile("while-stmt/while_stmt_source_03.bal", "while-stmt/while_stmt_assert_03.json");
    }

    @Test
    public void testWhileOnFailClauseRecovery() {
        testFile("while-stmt/while_stmt_source_05.bal", "while-stmt/while_stmt_assert_05.json");
        testFile("while-stmt/while_stmt_source_11.bal", "while-stmt/while_stmt_assert_11.json");
        testFile("while-stmt/while_stmt_source_12.bal", "while-stmt/while_stmt_assert_12.json");
        testFile("while-stmt/while_stmt_source_13.bal", "while-stmt/while_stmt_assert_13.json");
        testFile("while-stmt/while_stmt_source_14.bal", "while-stmt/while_stmt_assert_14.json");
    }
}
