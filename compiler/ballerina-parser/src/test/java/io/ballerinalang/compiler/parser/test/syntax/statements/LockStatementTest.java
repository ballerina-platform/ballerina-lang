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
 * Test parsing Lock statements.
 */
public class LockStatementTest extends AbstractStatementTest {

    // Valid syntax tests

   @Test
    public void testComplexLockStmt() {
        testFile("lock-stmt/lock_stmt_source_01.bal",
        "lock-stmt/lock_stmt_assert_01.json");
    }

    @Test
    public void testEmptyLockStmt() {
        testFile("lock-stmt/lock_stmt_source_02.bal",
        "lock-stmt/lock_stmt_assert_02.json");
    }

    @Test
    public void testLockStmtWithFieldsOnly() {
        testFile("lock-stmt/lock_stmt_source_03.bal",
        "lock-stmt/lock_stmt_assert_03.json");
    }

    @Test
    public void testLockStmtWithOnFailClause() {
        testFile("lock-stmt/lock_stmt_source_08.bal",
                "lock-stmt/lock_stmt_assert_08.json");
    }

    @Test
    public void testLockStmtWithOnFailClauseWithoutVariable() {
        testFile("lock-stmt/lock_stmt_source_10.bal",
                "lock-stmt/lock_stmt_assert_10.json");
    }

    public void testLockWithOnFailClauseHavingErrorBPWithVar() {
        testFile("lock-stmt/lock_stmt_source_11.bal",
                "lock-stmt/lock_stmt_assert_11.json");
    }

    public void testLockWithOnFailClauseHavingErrorBPWithType() {
        testFile("lock-stmt/lock_stmt_source_12.bal",
                "lock-stmt/lock_stmt_assert_12.json");
    }

    public void testLockWithOnFailClauseHavingErrorBPWithUserDefinedError() {
        testFile("lock-stmt/lock_stmt_source_13.bal",
                "lock-stmt/lock_stmt_assert_13.json");
    }

    public void testLockWithOnFailClauseHavingErrorBPWithUserDefinedErrorWithVar() {
        testFile("lock-stmt/lock_stmt_source_14.bal",
                "lock-stmt/lock_stmt_assert_14.json");
    }

    @DataProvider(name = "onFailClauseOtherBPTestDataProvider")
    public Object[][] onFailClauseOtherBPTestDataProvider() {
        return new Object[][]{
                {"lock-stmt/lock_stmt_source_19.bal", "lock-stmt/lock_stmt_assert_19.json"},
                {"lock-stmt/lock_stmt_source_20.bal", "lock-stmt/lock_stmt_assert_20.json"},
                {"lock-stmt/lock_stmt_source_21.bal", "lock-stmt/lock_stmt_assert_21.json"},
                {"lock-stmt/lock_stmt_source_22.bal", "lock-stmt/lock_stmt_assert_22.json"}
        };
    }

    @Test(dataProvider = "onFailClauseOtherBPTestDataProvider")
    public void testLockOnFailClauseWithOtherBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "onFailClauseErrorBPWithFieldBPTestDataProvider")
    public Object[][] onFailClauseErrorBPWithFieldBPTestDataProvider() {
        return new Object[][]{
                {"lock-stmt/lock_stmt_source_23.bal", "lock-stmt/lock_stmt_assert_23.json"},
                {"lock-stmt/lock_stmt_source_24.bal", "lock-stmt/lock_stmt_assert_24.json"},
                {"lock-stmt/lock_stmt_source_25.bal", "lock-stmt/lock_stmt_assert_25.json"}
        };
    }

    @Test(dataProvider = "onFailClauseErrorBPWithFieldBPTestDataProvider")
    public void testLockOnFailClausHavingErrorBPWithFieldBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "onFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public Object[][] onFailClauseErrorBPWithOtherTypeDescTestDataProvider() {
        return new Object[][]{
                {"lock-stmt/lock_stmt_source_26.bal", "lock-stmt/lock_stmt_assert_26.json"},
                {"lock-stmt/lock_stmt_source_27.bal", "lock-stmt/lock_stmt_assert_27.json"},
                {"lock-stmt/lock_stmt_source_28.bal", "lock-stmt/lock_stmt_assert_38.json"}
        };
    }

    @Test(dataProvider = "onFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public void testLockOnFailClausHavingErrorBPWithOtherTypeDesc(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    // Recovery tests

    @Test
    public void testLockStmtWithExtraTokens() {
        testFile("lock-stmt/lock_stmt_source_04.bal",
        "lock-stmt/lock_stmt_assert_04.json");
    }

    @Test
    public void testLockStmtWithMissingEqual() {
        testFile("lock-stmt/lock_stmt_source_05.bal",
        "lock-stmt/lock_stmt_assert_05.json");
    }

    @Test
    public void testNestedObjectRecovery() {
        testFile("lock-stmt/lock_stmt_source_06.bal",
        "lock-stmt/lock_stmt_assert_06.json");
    }

    @Test
    public void testMissingCloseBrace() {
        testFile("lock-stmt/lock_stmt_source_07.bal",
        "lock-stmt/lock_stmt_assert_07.json");
    }

    @DataProvider(name = "onFailClauseRecoveryTestDataProvider")
    public Object[][] onFailClauseRecoveryTestDataProvider() {
        return new Object[][]{
                {"lock-stmt/lock_stmt_source_09.bal", "lock-stmt/lock_stmt_assert_09.json"},
                {"lock-stmt/lock_stmt_source_15.bal", "lock-stmt/lock_stmt_assert_15.json"},
                {"lock-stmt/lock_stmt_source_16.bal", "lock-stmt/lock_stmt_assert_16.json"},
                {"lock-stmt/lock_stmt_source_17.bal", "lock-stmt/lock_stmt_assert_17.json"},
                {"lock-stmt/lock_stmt_source_18.bal", "lock-stmt/lock_stmt_assert_18.json"},
                {"lock-stmt/lock_stmt_source_19.bal", "lock-stmt/lock_stmt_assert_19.json"}
        };
    }

    @Test(dataProvider = "onFailClauseRecoveryTestDataProvider")
    public void testLockOnFailClauseRecovery(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }
}
