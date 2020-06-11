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

    // Recovery tests

    @Test
    public void testMatchStmtBodyRecovery() {
        testFile("match-stmt/match_stmt_source_03.bal", "match-stmt/match_stmt_assert_03.json");
    }

    @Test
    public void testMatchStmtRecovery() {
        testFile("match-stmt/match_stmt_source_04.bal", "match-stmt/match_stmt_assert_04.json");
    }
}
