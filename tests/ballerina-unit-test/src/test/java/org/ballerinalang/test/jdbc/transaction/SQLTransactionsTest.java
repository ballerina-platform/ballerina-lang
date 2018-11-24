/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jdbc.transaction;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.ballerinalang.test.utils.SQLDBUtils.DBType;
import static org.ballerinalang.test.utils.SQLDBUtils.DB_DIRECTORY;
import static org.ballerinalang.test.utils.SQLDBUtils.FileBasedTestDatabase;

/**
 * Class to test functionality of transactions in SQL.
 */
public class SQLTransactionsTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR_TR";
    private static final String TRANSACTION_TEST_GROUP = "TransactionTest";
    private TestDatabase testDatabase;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/jdbc/transaction/sql_transaction_test.bal");
        testDatabase = new FileBasedTestDatabase(DBType.H2,
                "datafiles/sql/SQLTableCreate.sql", DB_DIRECTORY, DB_NAME);
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransaction() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransaction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionRollback() {
        BValue[] returns = BRunUtil.invoke(result, "testTransactionRollback");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionUpdateWithGeneratedKeys() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionUpdateWithGeneratedKeys");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionRollbackUpdateWithGeneratedKeys() {
        BValue[] returns = BRunUtil.invoke(result, "testTransactionRollbackUpdateWithGeneratedKeys");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionStoredProcedure() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionStoredProcedure");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionRollbackStoredProcedure() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionRollbackStoredProcedure");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0, "Insertion count inside transaction is incorrect");
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionBatchUpdate");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionRollbackBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionRollbackBatchUpdate");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionAbort() {
        BValue[] returns = BRunUtil.invoke(result, "testTransactionAbort");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionPanic() {
        BValue[] returns = BRunUtil.invoke(result, "testTransactionErrorPanic");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionPanicAndTrap() {
        BValue[] returns = BRunUtil.invoke(result, "testTransactionErrorPanicAndTrap");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -1,
                "Exception thrown inside transaction should have been caught");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionCommitted() {
        BValue[] returns = BRunUtil.invoke(result, "testTransactionCommitted");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTwoTransactions() {
        BValue[] returns = BRunUtil.invoke(result, "testTwoTransactions");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 4, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testTransactionWithoutHandlers() {
        BValue[] returns = BRunUtil.invoke(result, "testTransactionWithoutHandlers");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionFailed() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionFailed");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "beforetx inTrx inFld inTrx inFld inTrx inFld inTrx inFld "
                + "afterTrx");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionSuccessWithFailed() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionSuccessWithFailed");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "beforetx inTrx inFld inTrx inFld inTrx afterTrx");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionFailedWithNextupdate() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionFailedWithNextupdate");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1,
                "Update after transaction failure may not have happened");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testNestedTwoLevelTransactionSuccess() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedTwoLevelTransactionSuccess");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2, "Insertion count inside transactions is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testNestedThreeLevelTransactionSuccess() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedThreeLevelTransactionSuccess");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP, enabled = false) //Issue #7706
    public void testNestedThreeLevelTransactionFailed() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedThreeLevelTransactionFailed");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1, "Transaction should have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testNestedThreeLevelTransactionFailedWithRetrySuccess() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedThreeLevelTransactionFailedWithRetrySuccess");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0, "Transaction shouldn't have been retried");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "Insertion count inside transaction is incorrect");
        Assert.assertEquals(returns[2].stringValue(), "start txL1 txL2 txL3 txL3_Else txL3_Failed");
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionWithSelectAndForeachIteration() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionWithSelectAndForeachIteration");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = TRANSACTION_TEST_GROUP)
    public void testLocalTransactionWithSelectAndHasNextIteration() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalTransactionWithSelectAndHasNextIteration");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(dependsOnGroups = TRANSACTION_TEST_GROUP)
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    //Following methods are used as UDFs
    public static void insertPersonDataSuccessful(Connection conn, int regid1, int regid2) throws SQLException {
        conn.createStatement().executeUpdate("INSERT INTO Customers (firstName, lastName, registrationID, creditLimit,"
                + " country)  values ('James', 'Clerk', " + regid1 + ", 5000.75, 'USA')");
        conn.createStatement().executeUpdate("INSERT INTO Customers (firstName, lastName, registrationID, creditLimit,"
                + " country)  values ('James', 'Clerk', " + regid2 + ", 5000.75, 'USA')");
    }

    public static void insertPersonDataFailure(Connection conn, int regid1, int regid2) throws SQLException {
        conn.createStatement().executeUpdate("INSERT INTO Customers (firstName, lastName, registrationID, creditLimit,"
                + " country)  values ('James', 'Clerk', " + regid1 + ", 5000.75, 'USA')");
        conn.createStatement().executeUpdate("INSERT INTO Customers (firstName, lastName, registrationID, creditLimit,"
                + " country)  values ('James', 'Clerk', " + regid2 + ", 'invalid', 'USA')");
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
