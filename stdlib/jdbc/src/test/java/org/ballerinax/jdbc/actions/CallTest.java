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
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.nio.file.Paths;

/**
 * Test class for JDBC call remote function tests.
 *
 * @since 1.1.0
 */
public class CallTest {
    private static final String DB_NAME_HSQL = "JDBC_CALL_TEST_HSQLDB";
    private SQLDBUtils.TestDatabase testDatabase;
    private CompileResult result;
    private static final String CALL_TEST = "CallTest";
    private static final String JDBC_URL = "jdbc:hsqldb:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME_HSQL;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.HSQLDB,
                Paths.get("datafiles", "sql", "actions", "call_test_data.sql").toString(),
                SQLDBUtils.DB_DIRECTORY, DB_NAME_HSQL);
        result = BCompileUtil.compile(Paths.get("test-src", "actions", "call_test.bal").toString());
    }

    //Test String Types
    @Test(groups = CALL_TEST)
    public void testCallWithStringTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithStringTypes", args);
        Assert.assertEquals(returns[0].stringValue(), "test1");
        Assert.assertEquals(returns[1].stringValue(), "test2     ");
        Assert.assertEquals(returns[2].stringValue(), "c");
        Assert.assertEquals(returns[3].stringValue(), "test3     ");
        Assert.assertEquals(returns[4].stringValue(), "d");
        Assert.assertEquals(returns[5].stringValue(), "test4");
        Assert.assertEquals(returns[6].stringValue(), "test5");
        Assert.assertEquals(returns[7].stringValue(), "hello ballerina code");
        Assert.assertNull(returns[8]);
    }

    @Test(groups = CALL_TEST)
    public void testCallWithStringTypesInParams() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithStringTypesInParams", args);
        Assert.assertEquals(returns[0].stringValue(), "test1");
        Assert.assertEquals(returns[1].stringValue(), "test2     ");
        Assert.assertEquals(returns[2].stringValue(), "c");
        Assert.assertEquals(returns[3].stringValue(), "test3     ");
        Assert.assertEquals(returns[4].stringValue(), "d");
        Assert.assertEquals(returns[5].stringValue(), "test4");
        Assert.assertEquals(returns[6].stringValue(), "test5");
        Assert.assertEquals(returns[7].stringValue(), "hello ballerina code");
        Assert.assertNull(returns[8]);
    }

    @Test(groups = CALL_TEST)
    public void testCallWithStringTypesReturnsData() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithStringTypesReturnsData", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(returns[2].stringValue(), "test0");
        Assert.assertEquals(returns[3].stringValue(), "test1     ");
        Assert.assertEquals(returns[4].stringValue(), "a");
        Assert.assertEquals(returns[5].stringValue(), "test2     ");
        Assert.assertEquals(returns[6].stringValue(), "b");
        Assert.assertEquals(returns[7].stringValue(), "test3");
        Assert.assertEquals(returns[8].stringValue(), "test4");
        Assert.assertEquals(returns[9].stringValue(), "hello ballerina");
    }

    @Test(groups = CALL_TEST)
    public void testCallWithStringTypesReturnsDataMultiple() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithStringTypesReturnsDataMultiple", args);
        Assert.assertEquals(returns[0].stringValue(), "test0");
        Assert.assertEquals(returns[1].stringValue(), "test1     ");
        Assert.assertEquals(returns[2].stringValue(), "a");
        Assert.assertEquals(returns[3].stringValue(), "test2     ");
        Assert.assertEquals(returns[4].stringValue(), "b");
        Assert.assertEquals(returns[5].stringValue(), "test3");
        Assert.assertEquals(returns[6].stringValue(), "test4");
        Assert.assertEquals(returns[7].stringValue(), "hello ballerina");
        Assert.assertEquals(returns[8].stringValue(), "test0");
    }

    @Test(groups = CALL_TEST)
    public void testCallWithStringTypesOutParams() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithStringTypesOutParams", args);
        Assert.assertEquals(returns[0].stringValue(), "test0");
        Assert.assertEquals(returns[1].stringValue(), "test1     ");
        Assert.assertEquals(returns[2].stringValue(), "a");
        Assert.assertEquals(returns[3].stringValue(), "test2     ");
        Assert.assertEquals(returns[4].stringValue(), "b");
        Assert.assertEquals(returns[5].stringValue(), "test3");
        Assert.assertEquals(returns[6].stringValue(), "test4");
        Assert.assertEquals(returns[7].stringValue(), "hello ballerina");
        Assert.assertNull(returns[8]);
    }

    //Test Numeric Typs
    @Test(groups = CALL_TEST)
    public void testCallWithNumericTypesOutParams() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithNumericTypesOutParams", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2147483647);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 32767);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 127);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(((BDecimal) returns[5]).decimalValue(), new BigDecimal("1234.56"));
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("1234.56"));
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.56, 0.01);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.56, 0.01);
        Assert.assertEquals(((BFloat) returns[9]).floatValue(), 1234.56, 0.01);
        Assert.assertNull(returns[10]);
    }

    //Test Errors
    @Test(groups = CALL_TEST)
    public void testCallWithApplicationError() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithApplicationError", args);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), "{ballerinax/java.jdbc}ApplicationError");
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue(), "{message:\"failed to execute stored "
                + "procedure: mismatching record type count 2 and returned result set count 1 "
                + "from the stored procedure.\"}");
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
