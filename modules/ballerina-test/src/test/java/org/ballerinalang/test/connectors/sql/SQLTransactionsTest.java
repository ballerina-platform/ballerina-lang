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
package org.ballerinalang.test.connectors.sql;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public class SQLTransactionsTest {

    CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/connectors/sql-transactions.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/SQLConnectorDataFile.sql");
    }

    @Test(enabled = false)
    public void testLocalTransacton() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testLocalTransacton", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(enabled = false)
    public void testTransactonRollback() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testTransactonRollback", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(enabled = false)
    public void testTransactonAbort() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testTransactonAbort", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(enabled = false)
    public void testTransactonThrow() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testTransactonErrorThrow", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
    }

    @Test(enabled = false)
    public void testTransactonThrowAndCatch() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testTransactionErrorThrowAndCatch", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
    }

    @Test(enabled = false)
    public void testTransactonCommitted() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testTransactonCommitted", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(enabled = false)
    public void testTransactonHandlerOrder() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testTransactonHandlerOrder", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 4);
    }

    @Test(enabled = false)
    public void testTransactonWithoutHandlers() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testTransactonWithoutHandlers", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(enabled = false)
    public void testLocalTransactonFailed() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testLocalTransactionFailed", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "beforetx inTrx inFld inTrx inFld inTrx inFld inTrx inFld inAbrt "
                + "inCatch afterTrx");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(enabled = false)
    public void testLocalTransactonSuccessWithFailed() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testLocalTransactonSuccessWithFailed", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "beforetx inTrx inFld inTrx inFld inTrx inCmt afterTrx");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
