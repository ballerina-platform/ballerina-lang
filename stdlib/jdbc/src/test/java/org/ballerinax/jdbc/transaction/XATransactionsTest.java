/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinax.jdbc.transaction;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.ballerinax.jdbc.utils.SQLDBUtils.DBType;
import org.ballerinax.jdbc.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Class to test functionality of distributed transactions in SQL.
 */
public class XATransactionsTest {
    private CompileResult result;
    private static final String DB_NAME1 = "TestDB1";
    private static final String DB_NAME2 = "TestDB2";
    private TestDatabase testDatabase1;
    private TestDatabase testDatabase2;

    private static final String JDBC_URL1 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY_H2_1 + DB_NAME1;
    private static final String JDBC_URL2 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY_H2_2 + DB_NAME2;
    private BValue[] args = { new BString(JDBC_URL1), new BString(JDBC_URL2) };

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(
                Paths.get("test-src", "transaction", "xa_transaction_test.bal").toString());
        testDatabase1 = new SQLDBUtils.FileBasedTestDatabase(DBType.H2,
                Paths.get("datafiles", "sql", "transaction", "xa_transaction_test_data_1.sql").toString(),
                SQLDBUtils.DB_DIRECTORY_H2_1, DB_NAME1);
        testDatabase2 = new SQLDBUtils.FileBasedTestDatabase(DBType.H2,
                Paths.get("datafiles", "sql", "transaction", "xa_transaction_test_data_2.sql").toString(),
                SQLDBUtils.DB_DIRECTORY_H2_2, DB_NAME2);
    }

    @Test
    public void testXATransactionSuccess() {
        BValue[] returns = BRunUtil.invoke(result, "testXATransactionSuccess", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    @Test
    public void testXATransactionSuccessWithDataSource() {
        BValue[] returns = BRunUtil.invoke(result, "testXATransactionSuccessWithDataSource", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    @Test
    public void testXATransactionSuccessWithH2Client() {
        BValue[] returns = BRunUtil.invoke(result, "testXATransactionSuccessWithH2Client", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    @Test
    public void testXATransactionFailed1() {
        BValue[] returns = BRunUtil.invoke(result, "testXATransactionFailed1", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test
    public void testXATransactionFailed2() {
        BValue[] returns = BRunUtil.invoke(result, "testXATransactionFailed2", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test
    public void testXATransactionRetry() {
        BValue[] returns = BRunUtil.invoke(result, "testXATransactionRetry", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase1 != null) {
            testDatabase1.stop();
        }
        if (testDatabase2 != null) {
            testDatabase2.stop();
        }
    }
}
