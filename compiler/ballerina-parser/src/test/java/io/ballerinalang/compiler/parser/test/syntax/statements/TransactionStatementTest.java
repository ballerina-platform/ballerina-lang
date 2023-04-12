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

    @Test
    public void testTransactionOnFailClauseRecovery() {
        testFile("transaction-stmt/transaction_stmt_source_04.bal", "transaction-stmt/transaction_stmt_assert_04.json");
        testFile("transaction-stmt/transaction_stmt_source_09.bal", "transaction-stmt/transaction_stmt_assert_09.json");
        testFile("transaction-stmt/transaction_stmt_source_10.bal", "transaction-stmt/transaction_stmt_assert_10.json");
        testFile("transaction-stmt/transaction_stmt_source_11.bal", "transaction-stmt/transaction_stmt_assert_11.json");
        testFile("transaction-stmt/transaction_stmt_source_12.bal", "transaction-stmt/transaction_stmt_assert_12.json");
    }

    @Test
    public void testRetryOnFailClauseRecovery() {
        testFile("transaction-stmt/retry_stmt_source_06.bal", "transaction-stmt/retry_stmt_assert_06.json");
        testFile("transaction-stmt/retry_stmt_source_12.bal", "transaction-stmt/retry_stmt_assert_12.json");
        testFile("transaction-stmt/retry_stmt_source_13.bal", "transaction-stmt/retry_stmt_assert_13.json");
        testFile("transaction-stmt/retry_stmt_source_14.bal", "transaction-stmt/retry_stmt_assert_14.json");
        testFile("transaction-stmt/retry_stmt_source_15.bal", "transaction-stmt/retry_stmt_assert_15.json");
    }
}
