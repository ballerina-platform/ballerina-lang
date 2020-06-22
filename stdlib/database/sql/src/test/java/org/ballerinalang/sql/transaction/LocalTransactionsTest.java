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
package org.ballerinalang.sql.transaction;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.sql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

/**
 * This test class includes local transactions test cases of sql client.
 *
 * @since 2.0.0
 */
public class LocalTransactionsTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_LOCAL_TRANSACTIONS";
    private static final String URL = SQLDBUtils.URL_PREFIX + DB_NAME;
    private BValue[] args = {new BString(URL), new BString(SQLDBUtils.DB_USER), new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compile(SQLDBUtils.getMockModuleDir(), "transaction");
        SQLDBUtils.initHsqlDatabase(DB_NAME, SQLDBUtils.getSQLResourceDir("transaction",
                "local-transaction-test-data.sql"));
    }

    @Test
    public void testLocalTransaction() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testLocalTransaction", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 0);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 2);
        Assert.assertTrue(((BBoolean) bArray.getBValue(2)).booleanValue());
    }

    @Test
    public void testTransactionRollbackWithCheck() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testTransactionRollbackWithCheck", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 0);
        Assert.assertFalse(((BBoolean) bArray.getBValue(2)).booleanValue());
    }

    @Test
    public void testTransactionRollbackWithRollback() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testTransactionRollbackWithRollback", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 0);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 0);
        Assert.assertTrue(((BBoolean) bArray.getBValue(2)).booleanValue());
    }

    @Test
    public void testLocalTransactionUpdateWithGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result,
                "testLocalTransactionUpdateWithGeneratedKeys", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 0);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 2);
    }

    @Test
    public void testLocalTransactionRollbackWithGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result,
                "testLocalTransactionRollbackWithGeneratedKeys", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 0);
    }

    @Test
    public void testTransactionAbort() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testTransactionAbort", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 0);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), -1);
        Assert.assertEquals(((BInteger) bArray.getBValue(2)).intValue(), 0);
    }

    @Test(enabled = false)
    public void testTransactionErrorPanic() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testTransactionErrorPanic", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), -1);
        Assert.assertEquals(((BInteger) bArray.getBValue(2)).intValue(), 0);
    }

    @Test
    public void testTransactionErrorPanicAndTrap() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testTransactionErrorPanicAndTrap", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 0);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), -1);
        Assert.assertEquals(((BInteger) bArray.getBValue(2)).intValue(), 1);
    }

    @Test
    public void testTwoTransactions() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testTwoTransactions", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 0);
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 0);
        Assert.assertEquals(((BInteger) bArray.getBValue(2)).intValue(), 4);
    }

    @Test
    public void testTransactionWithoutHandlers() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testTransactionWithoutHandlers", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BInteger) bArray.getBValue(0)).intValue(), 2);
    }

    @Test
    public void testLocalTransactionFailed() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testLocalTransactionFailed", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BString) bArray.getBValue(0)).stringValue(),
                "beforetx inTrx trxAborted inTrx trxAborted inTrx trapped afterTrx");
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 0);
    }

    @Test
    public void testLocalTransactionSuccessWithFailed() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testLocalTransactionSuccessWithFailed", args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray bArray = (BValueArray) returnVal[0];
        Assert.assertEquals(((BString) bArray.getBValue(0)).stringValue(),
                "beforetx inTrx inTrx inTrx committed afterTrx");
        Assert.assertEquals(((BInteger) bArray.getBValue(1)).intValue(), 2);
    }

}
