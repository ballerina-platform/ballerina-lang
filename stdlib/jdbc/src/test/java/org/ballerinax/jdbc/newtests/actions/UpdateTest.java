/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinax.jdbc.newtests.actions;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.ballerinax.jdbc.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Test class for JDBC remote functions test.
 *
 * @since 1.0.0
 */
public class UpdateTest {
    private static final String DB_NAME_HSQL = "JDBC_ACTIONS_TEST_HSQLDB";
    private TestDatabase testDatabase;
    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enableJBallerinaTests", "true");
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.HSQLDB,
                "new/datafiles/sql/update_test_data.sql", SQLDBUtils.DB_DIRECTORY, DB_NAME_HSQL);
        result = BCompileUtil.compile("new/test-src/actions/update_test.bal");
    }

    @Test
    public void testCreateTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test
    public void testBasicInsertData() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertData");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BInteger) returns[1]).intValue() >= 0);
    }

    @Test
    public void testBasicInsertDataWithoutGeneratedKey() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertDataWithoutGeneratedKey");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertDataWithGeneratedKey");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BInteger) returns[1]).intValue() > 0);
    }

    @Test
    public void testBasicInsertDataWithDatabaseError() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertDataWithDatabaseError");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[4]).booleanValue());
        Assert.assertTrue(returns[5].stringValue().contains("Failed to execute update query:"));
        Assert.assertEquals(returns[6].stringValue(), "42501");
        Assert.assertEquals(returns[7].stringValue(),"{ballerinax/java.jdbc}DatabaseError");
        Assert.assertEquals(((BInteger) returns[8]).intValue(), -5501);
    }

    @Test
    public void testBasicInsertDataWithApplicationError() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertDataWithApplicationError");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(),"{ballerinax/java.jdbc}ApplicationError");
    }

    @Test
    public void testUpdateTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateTableData");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    //Insert Numeric Values
    @Test
    public void testInsertNumericDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertNumericDataWithParameters");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2147483647);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 2147483650L);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 3000);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 255);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 1);
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("5000.75"));
        Assert.assertEquals(((BDecimal) returns[7]).decimalValue(), new BigDecimal("5000.76"));
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 5000.77, 0.01);
        Assert.assertEquals(((BFloat) returns[9]).floatValue(), 5000.78, 0.01);

    }

    @Test
    public void testInsertNumericDataWithDirectValues() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertNumericDataWithDirectValues");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -2147483648);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), -2147483650L);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), -32768);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 0);
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("-5000.75"));
        Assert.assertEquals(((BDecimal) returns[7]).decimalValue(), new BigDecimal("-5000.76"));
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), -5000.77, 0.01);
        Assert.assertEquals(((BFloat) returns[9]).floatValue(), -5000.78, 0.01);
    }

    @Test
    public void testInsertStringDataWithParameters1() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertStringDataWithParameters");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "test1");
        Assert.assertEquals(returns[2].stringValue(), "test2     ");
        Assert.assertEquals(returns[3].stringValue(), "c");
        Assert.assertEquals(returns[4].stringValue(), "test3     ");
        Assert.assertEquals(returns[5].stringValue(), "d");
        Assert.assertEquals(returns[6].stringValue(), "test4");
        Assert.assertEquals(returns[7].stringValue(), "test5");
        Assert.assertEquals(returns[8].stringValue(), "hello ballerina code");
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
