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
 * Test parsing transaction statement.
 * 
 * @since 2.0.0
 */
public class TransactionStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testBasicTransactionStatement() {
        testFile("transaction-stmt/transaction_stmt_source_01.bal", "transaction-stmt/transaction_stmt_assert_01.json");
    }

    @Test
    public void testRollbackStatement() {
        testFile("transaction-stmt/rollback_stmt_source_01.bal", "transaction-stmt/rollback_stmt_assert_01.json");
    }

    @Test
    public void testRetryStatement() {
        testFile("transaction-stmt/retry_stmt_source_01.bal", "transaction-stmt/retry_stmt_assert_01.json");
    }

    @Test
    public void testRetryTransactionStatement() {
        testFile("transaction-stmt/retry_stmt_source_02.bal", "transaction-stmt/retry_stmt_assert_02.json");
    }

    @Test
    public void testTransactionStatementWithOnFailClause() {
        testFile("transaction-stmt/transaction_stmt_source_03.bal", "transaction-stmt/transaction_stmt_assert_03.json");
    }

    @Test
    public void testRetryStatementWithOnFailClause() {
        testFile("transaction-stmt/retry_stmt_source_05.bal", "transaction-stmt/retry_stmt_assert_05.json");
    }

    @Test
    public void testRetryStatementWithOnFailClauseWithoutVariable() {
        testFile("transaction-stmt/retry_stmt_source_07.bal", "transaction-stmt/retry_stmt_assert_07.json");
    }

    @Test
    public void testRetryWithOnFailClauseHavingErrorBPWithVar() {
        testFile("transaction-stmt/retry_stmt_source_08.bal",
                "transaction-stmt/retry_stmt_assert_08.json");
    }

    @Test
    public void testRetryWithOnFailClauseHavingErrorBPWithType() {
        testFile("transaction-stmt/retry_stmt_source_09.bal",
                "transaction-stmt/retry_stmt_assert_09.json");
    }

    @Test
    public void testRetryWithOnFailClauseHavingErrorBPWithUserDefinedError() {
        testFile("transaction-stmt/retry_stmt_source_10.bal",
                "transaction-stmt/retry_stmt_assert_10.json");
    }

    @Test
    public void testRetryWithOnFailClauseHavingErrorBPWithUserDefinedErrorWithVar() {
        testFile("transaction-stmt/retry_stmt_source_11.bal",
                "transaction-stmt/retry_stmt_assert_11.json");
    }

    @Test
    public void testTransactionWithOnFailClauseHavingErrorBPWithVar() {
        testFile("transaction-stmt/transaction_stmt_source_05.bal",
                "transaction-stmt/transaction_stmt_assert_05.json");
    }

    @Test
    public void testTransactionWithOnFailClauseHavingErrorBPWithType() {
        testFile("transaction-stmt/transaction_stmt_source_06.bal",
                "transaction-stmt/transaction_stmt_assert_06.json");
    }

    @Test
    public void testTransactionWithOnFailClauseHavingErrorBPWithUserDefinedError() {
        testFile("transaction-stmt/transaction_stmt_source_07.bal",
                "transaction-stmt/transaction_stmt_assert_07.json");
    }

    @Test
    public void testTransactionWithOnFailClauseHavingErrorBPWithUserDefinedErrorWithVar() {
        testFile("transaction-stmt/transaction_stmt_source_08.bal",
                "transaction-stmt/transaction_stmt_assert_08.json");
    }

    @Test
    public void testTransactionalWorker() {
        testFile("transaction-stmt/transactional_worker_source_01.bal",
                "transaction-stmt/transactional_worker_assert_01.json");
    }

    @DataProvider(name = "retryOnFailClauseOtherBPTestDataProvider")
    public Object[][] retryOnFailClauseOtherBPTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/retry_stmt_source_16.bal",
                        "transaction-stmt/retry_stmt_assert_16.json"},
                {"transaction-stmt/retry_stmt_source_17.bal",
                        "transaction-stmt/retry_stmt_assert_17.json"},
                {"transaction-stmt/retry_stmt_source_18.bal",
                        "transaction-stmt/retry_stmt_assert_18.json"},
                {"transaction-stmt/retry_stmt_source_19.bal",
                        "transaction-stmt/retry_stmt_assert_19.json"}
        };
    }

    @Test(dataProvider = "retryOnFailClauseOtherBPTestDataProvider")
    public void testRetryOnFailClauseWithOtherBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "retryOnFailClauseErrorBPWithFieldBPTestDataProvider")
    public Object[][] retryOnFailClauseErrorBPWithFieldBPTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/retry_stmt_source_20.bal",
                        "transaction-stmt/retry_stmt_assert_20.json"},
                {"transaction-stmt/retry_stmt_source_21.bal",
                        "transaction-stmt/retry_stmt_assert_21json"},
                {"transaction-stmt/retry_stmt_source_22.bal",
                        "transaction-stmt/retry_stmt_assert_22.json"}
        };
    }

    @Test(dataProvider = "retryOnFailClauseErrorBPWithFieldBPTestDataProvider")
    public void testRetryOnFailClausHavingErrorBPWithFieldBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "retryOnFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public Object[][] retryOnFailClauseErrorBPWithOtherTypeDescTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/retry_stmt_source_23.bal",
                        "transaction-stmt/retry_stmt_assert_23.json"},
                {"transaction-stmt/retry_stmt_source_24.bal",
                        "transaction-stmt/retry_stmt_assert_24.json"},
                {"transaction-stmt/retry_stmt_source_25.bal",
                        "transaction-stmt/retry_stmt_assert_25.json"}
        };
    }

    @Test(dataProvider = "retryOnFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public void testRetryOnFailClausHavingErrorBPWithOtherTypeDesc(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "transactionOnFailClauseOtherBPTestDataProvider")
    public Object[][] transactionOnFailClauseOtherBPTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/transaction_stmt_source_13.bal",
                        "transaction-stmt/transaction_stmt_assert_13.json"},
                {"transaction-stmt/transaction_stmt_source_14.bal",
                        "transaction-stmt/transaction_stmt_assert_14.json"},
                {"transaction-stmt/transaction_stmt_source_15.bal",
                        "transaction-stmt/transaction_stmt_assert_15.json"},
                {"transaction-stmt/transaction_stmt_source_16.bal",
                        "transaction-stmt/transaction_stmt_assert_16.json"}
        };
    }

    @Test(dataProvider = "transactionOnFailClauseOtherBPTestDataProvider")
    public void testTransactionOnFailClauseWithOtherBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "transactionOnFailClauseErrorBPWithFieldBPTestDataProvider")
    public Object[][] transactionOnFailClauseErrorBPWithFieldBPTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/transaction_stmt_source_17.bal",
                        "transaction-stmt/transaction_stmt_assert_17.json"},
                {"transaction-stmt/transaction_stmt_source_18.bal",
                        "transaction-stmt/transaction_stmt_assert_18.json"},
                {"transaction-stmt/transaction_stmt_source_19.bal",
                        "transaction-stmt/transaction_stmt_assert_19.json"}
        };
    }

    @Test(dataProvider = "transactionOnFailClauseErrorBPWithFieldBPTestDataProvider")
    public void testTransactionOnFailClausHavingErrorBPWithFieldBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "transactionOnFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public Object[][] transactionOnFailClauseErrorBPWithOtherTypeDescTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/transaction_stmt_source_20.bal",
                        "transaction-stmt/transaction_stmt_assert_20.json"},
                {"transaction-stmt/transaction_stmt_source_21.bal",
                        "transaction-stmt/transaction_stmt_assert_21.json"},
                {"transaction-stmt/transaction_stmt_source_22.bal",
                        "transaction-stmt/transaction_stmt_assert_22.json"}
        };
    }

    @Test(dataProvider = "transactionOnFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public void testTransactionOnFailClausHavingErrorBPWithOtherTypeDesc(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    // Recovery tests

    @Test
    public void testTransactionStatementRecovery() {
        testFile("transaction-stmt/transaction_stmt_source_02.bal", "transaction-stmt/transaction_stmt_assert_02.json");
    }

    @Test
    public void testRollbackStatementRecovery() {
        testFile("transaction-stmt/rollback_stmt_source_02.bal", "transaction-stmt/rollback_stmt_assert_02.json");
    }

    @Test
    public void testRetryStatementRecovery() {
        testFile("transaction-stmt/retry_stmt_source_03.bal", "transaction-stmt/retry_stmt_assert_03.json");
    }

    @Test
    public void testRetryTransactionStatementRecovery() {
        testFile("transaction-stmt/retry_stmt_source_04.bal", "transaction-stmt/retry_stmt_assert_04.json");
    }

    @DataProvider(name = "transactionOnFailClauseRecoveryTestDataProvider")
    public Object[][] transactionOnFailClauseRecoveryTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/transaction_stmt_source_04.bal", "transaction-stmt/transaction_stmt_assert_04.json"},
                {"transaction-stmt/transaction_stmt_source_09.bal", "transaction-stmt/transaction_stmt_assert_09.json"},
                {"transaction-stmt/transaction_stmt_source_10.bal", "transaction-stmt/transaction_stmt_assert_10.json"},
                {"transaction-stmt/transaction_stmt_source_11.bal", "transaction-stmt/transaction_stmt_assert_11.json"},
                {"transaction-stmt/transaction_stmt_source_12.bal", "transaction-stmt/transaction_stmt_assert_12.json"},
                {"transaction-stmt/transaction_stmt_source_23.bal", "transaction-stmt/transaction_stmt_assert_23.json"}
        };
    }

    @Test(dataProvider = "transactionOnFailClauseRecoveryTestDataProvider")
    public void testTransactionOnFailClauseRecovery(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "retryOnFailClauseRecoveryTestDataProvider")
    public Object[][] retryOnFailClauseRecoveryTestDataProvider() {
        return new Object[][]{
                {"transaction-stmt/retry_stmt_source_06.bal", "transaction-stmt/retry_stmt_assert_06.json"},
                {"transaction-stmt/retry_stmt_source_12.bal", "transaction-stmt/retry_stmt_assert_12.json"},
                {"transaction-stmt/retry_stmt_source_13.bal", "transaction-stmt/retry_stmt_assert_13.json"},
                {"transaction-stmt/retry_stmt_source_14.bal", "transaction-stmt/retry_stmt_assert_14.json"},
                {"transaction-stmt/retry_stmt_source_15.bal", "transaction-stmt/retry_stmt_assert_15.json"},
                {"transaction-stmt/retry_stmt_source_26.bal", "transaction-stmt/retry_stmt_assert_26.json"}
        };
    }

    @Test(dataProvider = "retryOnFailClauseRecoveryTestDataProvider")
    public void testRetryOnFailClauseRecovery(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }
}
