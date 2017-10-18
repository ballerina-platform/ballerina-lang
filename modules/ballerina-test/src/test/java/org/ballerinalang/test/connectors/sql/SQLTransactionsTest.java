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
        result = BTestUtils.compile("test-src/connectors/sql/sql-transactions.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/SQLConnectorDataFile.sql");
    }

    @Test
    public void testLocalTransacton() {
        BValue[] returns = BTestUtils.invoke(result, "testLocalTransacton");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test
    public void testTransactonRollback() {
        BValue[] returns = BTestUtils.invoke(result, "testTransactonRollback");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test
    public void testTransactonAbort() {
        BValue[] returns = BTestUtils.invoke(result, "testTransactonAbort");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test
    public void testTransactonThrow() {
        BValue[] returns = BTestUtils.invoke(result, "testTransactonErrorThrow");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
    }

    @Test
    public void testTransactonThrowAndCatch() {
        BValue[] returns = BTestUtils.invoke(result, "testTransactionErrorThrowAndCatch");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
    }

    @Test
    public void testTransactonCommitted() {
        BValue[] returns = BTestUtils.invoke(result, "testTransactonCommitted");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test
    public void testTransactonHandlerOrder() {
        BValue[] returns = BTestUtils.invoke(result, "testTransactonHandlerOrder");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 4);
    }

    @Test
    public void testTransactonWithoutHandlers() {
        BValue[] returns = BTestUtils.invoke(result, "testTransactonWithoutHandlers");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test
    public void testLocalTransactonFailed() {
        BValue[] returns = BTestUtils.invoke(result, "testLocalTransactionFailed");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "beforetx inTrx inFld inTrx inFld inTrx inFld inTrx inFld inAbrt "
                + "inCatch afterTrx");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test
    public void testLocalTransactonSuccessWithFailed() {
        BValue[] returns = BTestUtils.invoke(result, "testLocalTransactonSuccessWithFailed");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "beforetx inTrx inFld inTrx inFld inTrx inCmt afterTrx");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
