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

import org.testng.annotations.DataProvider;
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

    @Test
    public void testSimpleMatchStatementWithOnFailClauseWithoutVariable() {
        testFile("match-stmt/match_stmt_source_23.bal", "match-stmt/match_stmt_assert_23.json");
    }

    @Test
    public void testConstPatternWithPreDeclaredPrefix() {
        testFile("match-stmt/match_stmt_source_22.bal", "match-stmt/match_stmt_assert_22.json");
    }

    @Test
    public void testSimpleMatchStatementWithOnFailClauseHavingErrorBPWithVar() {
        testFile("match-stmt/match_stmt_source_24.bal", "match-stmt/match_stmt_assert_24.json");
    }

    @Test
    public void testSimpleMatchStatementWithOnFailClauseHavingErrorBPWithType() {
        testFile("match-stmt/match_stmt_source_25.bal", "match-stmt/match_stmt_assert_25.json");
    }

    @Test
    public void testSimpleMatchStatementWithOnFailClauseHavingErrorBPWithUserDefinedError() {
        testFile("match-stmt/match_stmt_source_26.bal", "match-stmt/match_stmt_assert_26.json");
    }

    @Test
    public void testSimpleMatchStatementWithOnFailClauseHavingErrorBPWithUserDefinedErrorWithVar() {
        testFile("match-stmt/match_stmt_source_27.bal", "match-stmt/match_stmt_assert_27.json");
    }

    @DataProvider(name = "onFailClauseOtherBPTestDataProvider")
    public Object[][] onFailClauseOtherBPTestDataProvider() {
        return new Object[][]{
                {"match-stmt/match_stmt_source_32.bal", "match-stmt/match_stmt_assert_32.json"},
                {"match-stmt/match_stmt_source_33.bal", "match-stmt/match_stmt_assert_33.json"},
                {"match-stmt/match_stmt_source_34.bal", "match-stmt/match_stmt_assert_34.json"},
                {"match-stmt/match_stmt_source_35.bal", "match-stmt/match_stmt_assert_35.json"}
        };
    }

    @Test(dataProvider = "onFailClauseOtherBPTestDataProvider")
    public void testMatchStmtOnFailClauseWithOtherBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "onFailClauseErrorBPWithFieldBPTestDataProvider")
    public Object[][] onFailClauseErrorBPWithFieldBPTestDataProvider() {
        return new Object[][]{
                {"match-stmt/match_stmt_source_36.bal", "match-stmt/match_stmt_assert_36.json"},
                {"match-stmt/match_stmt_source_37.bal", "match-stmt/match_stmt_assert_37.json"},
                {"match-stmt/match_stmt_source_38.bal", "match-stmt/match_stmt_assert_38.json"}
        };
    }

    @Test(dataProvider = "onFailClauseErrorBPWithFieldBPTestDataProvider")
    public void testMatchStmtOnFailClausHavingErrorBPWithFieldBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "onFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public Object[][] onFailClauseErrorBPWithOtherTypeDescTestDataProvider() {
        return new Object[][]{
                {"match-stmt/match_stmt_source_39.bal", "match-stmt/match_stmt_assert_39.json"},
                {"match-stmt/match_stmt_source_40.bal", "match-stmt/match_stmt_assert_40.json"},
                {"match-stmt/match_stmt_source_41.bal", "match-stmt/match_stmt_assert_41.json"}
        };
    }

    @Test(dataProvider = "onFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public void testMatchStmtOnFailClausHavingErrorBPWithOtherTypeDesc(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
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
        testFile("match-stmt/match_stmt_source_19.bal", "match-stmt/match_stmt_assert_19.json");
    }

    @Test
    public void testMatchStmtRecoveryInvalidListMatchPatterns() {
        testFile("match-stmt/match_stmt_source_09.bal", "match-stmt/match_stmt_assert_09.json");
    }

    @Test
    public void testMatchStmtRecoveryInvalidMappingMatchPatterns() {
        testFile("match-stmt/match_stmt_source_11.bal", "match-stmt/match_stmt_assert_11.json");
        testFile("match-stmt/match_stmt_source_21.bal", "match-stmt/match_stmt_assert_21.json");
    }

    @Test
    public void testMatchStmtRecoveryInvalidErrorMatchPatterns() {
        testFile("match-stmt/match_stmt_source_13.bal", "match-stmt/match_stmt_assert_13.json");
    }

    @DataProvider(name = "onFailClauseRecoveryTestDataProvider")
    public Object[][] onFailClauseRecoveryTestDataProvider() {
        return new Object[][]{
                {"match-stmt/match_stmt_source_15.bal", "match-stmt/match_stmt_assert_15.json"},
                {"match-stmt/match_stmt_source_28.bal", "match-stmt/match_stmt_assert_28.json"},
                {"match-stmt/match_stmt_source_29.bal", "match-stmt/match_stmt_assert_29.json"},
                {"match-stmt/match_stmt_source_30.bal", "match-stmt/match_stmt_assert_30.json"},
                {"match-stmt/match_stmt_source_31.bal", "match-stmt/match_stmt_assert_31.json"},
                {"match-stmt/match_stmt_source_42.bal", "match-stmt/match_stmt_assert_42.json"}

        };
    }

    @Test(dataProvider = "onFailClauseRecoveryTestDataProvider")
    public void testMatchStatementOnFailClauseRecovery(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @Test
    public void testMatchStmtRecoveryZeroMatchCluases() {
        testFile("match-stmt/match_stmt_source_16.bal", "match-stmt/match_stmt_assert_16.json");
    }

    @Test
    public void testIncompleteMatchStmtRecovery() {
        testFile("match-stmt/match_stmt_source_17.bal", "match-stmt/match_stmt_assert_17.json");
        testFile("match-stmt/match_stmt_source_18.bal", "match-stmt/match_stmt_assert_18.json");
    }

    @Test
    public void testMissingMatchPattern() {
        testFile("match-stmt/match_stmt_source_20.bal", "match-stmt/match_stmt_assert_20.json");
    }
}
