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
 * Test parsing match statement.
 * 
 * @since 2.0.0
 */
public class MatchStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testSimpleMatchStatement() {
        testFile("match-stmt/match_stmt_source_01.bal", "match-stmt/match_stmt_assert_01.json");
    }

    @Test
    public void testMatchStmtWithMatchClauseBody() {
        testFile("match-stmt/match_stmt_source_02.bal", "match-stmt/match_stmt_assert_02.json");
    }

    @Test
    public void testSimpleMatchStatementWithVar() {
        testFile("match-stmt/match_stmt_source_05.bal", "match-stmt/match_stmt_assert_05.json");
    }

    @Test
    public void testMatchStatementWithListMatchPattern() {
        testFile("match-stmt/match_stmt_source_08.bal", "match-stmt/match_stmt_assert_08.json");
    }

    @Test
    public void testMatchStatementWithMappingMatchPattern() {
        testFile("match-stmt/match_stmt_source_10.bal", "match-stmt/match_stmt_assert_10.json");
    }

    @Test
    public void testMatchStatementWithErrorMatchPattern() {
        testFile("match-stmt/match_stmt_source_12.bal", "match-stmt/match_stmt_assert_12.json");
    }

    @Test
    public void testSimpleMatchStatementWithOnFailClause() {
        testFile("match-stmt/match_stmt_source_14.bal", "match-stmt/match_stmt_assert_14.json");
    }

    // Recovery tests

    @Test
    public void testMatchStmtBodyRecovery() {
        testFile("match-stmt/match_stmt_source_03.bal", "match-stmt/match_stmt_assert_03.json");
    }

    @Test
    public void testMatchStmtRecovery() {
        testFile("match-stmt/match_stmt_source_04.bal", "match-stmt/match_stmt_assert_04.json");
    }

    @Test
    public void testMatchStmtRecoveryMissingVarKeyword() {
        testFile("match-stmt/match_stmt_source_06.bal", "match-stmt/match_stmt_assert_06.json");
    }

    @Test
    public void testMatchStmtRecoveryMissingSymbolsAdditionalSymbols() {
        testFile("match-stmt/match_stmt_source_07.bal", "match-stmt/match_stmt_assert_07.json");
    }

    @Test(enabled = false)
    public void testMatchStmtRecoveryInvalidListMatchPatterns() {
        testFile("match-stmt/match_stmt_source_09.bal", "match-stmt/match_stmt_assert_09.json");
    }

    @Test
    public void testMatchStmtRecoveryInvalidMappingMatchPatterns() {
        testFile("match-stmt/match_stmt_source_11.bal", "match-stmt/match_stmt_assert_11.json");
    }

    @Test(enabled = false)
    public void testMatchStmtRecoveryInvalidErrorMatchPatterns() {
        testFile("match-stmt/match_stmt_source_13.bal", "match-stmt/match_stmt_assert_13.json");
    }

    @Test
    public void testMatchStatementOnFailClauseRecovery() {
        testFile("match-stmt/match_stmt_source_15.bal", "match-stmt/match_stmt_assert_15.json");
    }

    @Test
    public void testMatchStmtRecoveryZeroMatchCluases() {
        testFile("match-stmt/match_stmt_source_16.bal", "match-stmt/match_stmt_assert_16.json");
    }
}
