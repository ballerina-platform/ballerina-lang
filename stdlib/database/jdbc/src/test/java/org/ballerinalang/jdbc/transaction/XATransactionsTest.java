/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jdbc.transaction;

import org.ballerinalang.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.sql.SQLException;

/**
 * This test class includes distributed transactions test cases of sql client.
 *
 * @since 2.0.0
 */
public class XATransactionsTest {
    private CompileResult result;
    private static final String DB_NAME1 = "TEST_SQL_XA_TRANSACTIONS_1";
    private static final String DB_NAME2 = "TEST_SQL_XA_TRANSACTIONS_2";
    private static final String JDBC_URL1 = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME1;
    private static final String JDBC_URL2 = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME2;
    private BValue[] args = {new BString(JDBC_URL1), new BString(JDBC_URL2), new BString(SQLDBUtils.DB_USER),
            new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compile(SQLDBUtils.getBalFilesDir("transaction",
                "xa-transactions-test.bal"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME1);
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME2);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME1, SQLDBUtils.getSQLResourceDir("transaction",
                "xa-transaction-test-data-1.sql"));
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME2, SQLDBUtils.getSQLResourceDir("transaction",
                "xa-transaction-test-data-2.sql"));
    }

    @Test
    public void testXATransactionSuccess() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testXATransactionSuccess", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 1);
    }

    @Test
    public void testXATransactionSuccessWithDataSource() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testXATransactionSuccessWithDataSource", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 1);
    }
}
