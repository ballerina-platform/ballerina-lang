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
package org.ballerinax.jdbc.actions;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
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
import java.nio.file.Paths;

/**
 * Test class for JDBC update remote function tests.
 *
 * @since 1.0.0
 */
public class UpdateTest {
    private static final String DB_NAME_HSQL = "JDBC_UPDATE_TEST_HSQLDB";
    private TestDatabase testDatabase;
    private CompileResult result;
    private static final String UPDATE_TEST = "UpdateTest";
    private static final String JDBC_URL = "jdbc:hsqldb:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME_HSQL;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.HSQLDB,
                Paths.get("datafiles", "sql", "actions", "update_test_data.sql").toString(), SQLDBUtils.DB_DIRECTORY,
                DB_NAME_HSQL);
        result = BCompileUtil.compile(Paths.get("test-src", "actions", "update_test.bal").toString());
    }

    @Test(groups = UPDATE_TEST)
    public void testCreateTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTable", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = UPDATE_TEST)
    public void testBasicInsertData() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertData", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BInteger) returns[1]).intValue() >= 0);
    }

    @Test(groups = UPDATE_TEST)
    public void testBasicInsertDataWithoutGeneratedKey() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertDataWithoutGeneratedKey", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = UPDATE_TEST)
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertDataWithGeneratedKey", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BInteger) returns[1]).intValue() > 0);
    }

    @Test(groups = UPDATE_TEST)
    public void testBasicInsertDataWithDatabaseError() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertDataWithDatabaseError", args);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[4]).booleanValue());
        Assert.assertTrue(returns[5].stringValue().contains("failed to execute update query:"));
        Assert.assertEquals(returns[6].stringValue(), "42501");
        Assert.assertEquals(returns[7].stringValue(), "{ballerinax/java.jdbc}DatabaseError");
        Assert.assertEquals(((BInteger) returns[8]).intValue(), -5501);
    }

    @Test(groups = UPDATE_TEST)
    public void testBasicInsertDataWithApplicationError() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicInsertDataWithApplicationError", args);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(), "{ballerinax/java.jdbc}ApplicationError");
    }

    @Test(groups = UPDATE_TEST)
    public void testUpdateTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateTableData", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    //Insert Numeric Values
    @Test(groups = UPDATE_TEST)
    public void testInsertNumericDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertNumericDataWithParameters", args);
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

    @Test(groups = UPDATE_TEST)
    public void testInsertNumericDataWithDirectValues() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertNumericDataWithDirectValues", args);
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

    @Test(groups = UPDATE_TEST)
    public void testInsertNumericDataWithNilValues() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertNumericDataWithNilValues", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
        Assert.assertNull(returns[3]);
        Assert.assertNull(returns[4]);
        Assert.assertNull(returns[5]);
        Assert.assertNull(returns[6]);
        Assert.assertNull(returns[7]);
        Assert.assertNull(returns[8]);
        Assert.assertNull(returns[9]);
    }

    //Insert String Values
    @Test(groups = UPDATE_TEST)
    public void testInsertStringDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertStringDataWithParameters", args);
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

    @Test(groups = UPDATE_TEST)
    public void testInsertStringDataWithDirectParams() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertStringDataWithDirectParams", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "str1");
        Assert.assertEquals(returns[2].stringValue(), "str2      ");
        Assert.assertEquals(returns[3].stringValue(), "A");
        Assert.assertEquals(returns[4].stringValue(), "str3      ");
        Assert.assertEquals(returns[5].stringValue(), "B");
        Assert.assertEquals(returns[6].stringValue(), "str4");
        Assert.assertEquals(returns[7].stringValue(), "str5");
        Assert.assertEquals(returns[8].stringValue(), "hello ballerina code");
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertStringDataWithNilValues() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertStringDataWithNilValues", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
        Assert.assertNull(returns[3]);
        Assert.assertNull(returns[4]);
        Assert.assertNull(returns[5]);
        Assert.assertNull(returns[6]);
        Assert.assertNull(returns[7]);
        Assert.assertNull(returns[8]);
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertStringDataWithEmptyValues() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertStringDataWithEmptyValues", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "");
        Assert.assertEquals(returns[2].stringValue(), "          ");
        Assert.assertEquals(returns[3].stringValue(), " ");
        Assert.assertEquals(returns[4].stringValue(), "          ");
        Assert.assertEquals(returns[5].stringValue(), " ");
        Assert.assertEquals(returns[6].stringValue(), "");
        Assert.assertEquals(returns[7].stringValue(), "");
        Assert.assertEquals(returns[8].stringValue(), "");
    }

    //Insert Boolean values
    @Test(groups = UPDATE_TEST)
    public void testInsertBoolDataAsIntsAndReturnInts() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertBoolDataAsIntsAndReturnInts", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
    }

    @Test(groups = UPDATE_TEST)
    public void testGetEmptyGeneratedKey() {
        BValue[] returns = BRunUtil.invoke(result, "testGetGeneratedKey", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        //Test for getting auto-generated key when getGeneratedKey is true and the table doesn't
        // have auto generated key.
        Assert.assertEquals((returns[1]).stringValue(), "");
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertBoolDataAsBoolAndReturnBool() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertBoolDataAsBoolAndReturnBool", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        // Test for getting the auto generated keys from the ResultSet When the getGeneratedKey is false.
        Assert.assertEquals(returns[3].stringValue(), "");
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertBoolDataAsBoolAndReturnBoolAsDirectParams() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertBoolDataAsBoolAndReturnBoolAsDirectParams", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertBoolDataAsIntsInvalidParams() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertBoolDataAsIntsInvalidParams", args);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerinax/java.jdbc}ApplicationError"));
        Assert.assertTrue(returns[0].stringValue().contains("invalid integer value \"91\" specified for boolean"));
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertBoolDataWithNilValues() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertBoolDataWithNilValues", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
    }

    //Insert binary values
    @Test(groups = UPDATE_TEST)
    public void testInsertBinaryDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertBinaryDataWithParameters", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(new String(((BValueArray) returns[1]).getBytes()), "blob data");
        Assert.assertEquals(new String(((BValueArray) returns[2]).getBytes()), "blob data");
        Assert.assertEquals(new String(((BValueArray) returns[3]).getBytes()), "blob data");
        Assert.assertEquals(new String(((BValueArray) returns[4]).getBytes()), "blob data");
        Assert.assertEquals(new String(((BValueArray) returns[5]).getBytes()), "blob data");
        Assert.assertEquals(new String(((BValueArray) returns[6]).getBytes()), "blob data");
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertBinaryDataWithNilValues() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertBinaryDataWithNilValues", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
        Assert.assertNull(returns[3]);
        Assert.assertNull(returns[4]);
        Assert.assertNull(returns[5]);
        Assert.assertNull(returns[6]);
    }

    //Insert Date time Values
    @Test(groups = UPDATE_TEST)
    public void testInsertTimeDataAsString() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTimeDataAsString", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(returns[1].stringValue().contains("2019-08-09"));
        Assert.assertTrue(returns[2].stringValue().contains("20:08:08"));
        Assert.assertTrue(returns[3].stringValue().contains("2019-08-09T20:08:08"));
        Assert.assertTrue(returns[4].stringValue().contains("2019-08-09T20:08:08"));
        //TODO:#17546
        //Assert.assertEquals(returns[5].stringValue(), "20:08:08.034900-08:00");
        //Assert.assertEquals(returns[6].stringValue(), "2019-08-09");
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertTimeDataAsBallerinaTime() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTimeDataAsBallerinaTime", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        //TODO:#17546
        //Assert.assertTrue(((BBoolean) returns[5]).booleanValue());
        //Assert.assertTrue(((BBoolean) returns[6]).booleanValue());
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertTimeDataAsInt() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTimeDataAsInt", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 72488000);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1565381288);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1565381288);
    }

    @Test(groups = UPDATE_TEST)
    public void testInsertTimeDataAsBallerinaTimeWithNil() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTimeDataAsBallerinaTimeWithNil", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
        Assert.assertNull(returns[3]);
        Assert.assertNull(returns[4]);
        Assert.assertNull(returns[5]);
        Assert.assertNull(returns[6]);
    }

    @Test(groups = UPDATE_TEST)
    public void testUpdateResult() {
        BValue[] returns = BRunUtil.invoke(result, "testInvalidUpdateOnUpdateResultRecord", args);
        Assert.assertTrue(returns[0].stringValue()
                .contains("Invalid update of record field: modification not allowed on readonly value"));
    }

    @Test(groups = UPDATE_TEST)
    public void testStopClient() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testStopClient", args);
        Assert.assertNull(returns[0]);
    }

    @Test(dependsOnGroups = UPDATE_TEST)
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
