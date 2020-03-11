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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Test class for JDBC batch update remote function tests.
 *
 * @since 1.0.0
 */
public class BatchUpdateTest {
    private static final String DB_NAME_HSQL = "JDBC_BATCH_UPDATE_TEST_HSQLDB";
    private SQLDBUtils.TestDatabase testDatabase;
    private CompileResult result;
    private static final String BATCH_UPDATE_TEST = "BatchUpdateTest";
    private static final String JDBC_URL = "jdbc:hsqldb:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME_HSQL;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.HSQLDB,
                Paths.get("datafiles", "sql", "actions", "batch_update_test_data.sql").toString(),
                SQLDBUtils.DB_DIRECTORY, DB_NAME_HSQL);
        result = BCompileUtil.compile(Paths.get("test-src", "actions", "batch_update_test.bal").toString());
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate", args);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(1), 1);
        Assert.assertNull(returns[1]);
        Assert.assertTrue(((BInteger) returns[2]).intValue() > 0);
        Assert.assertTrue(((BInteger) returns[3]).intValue() > 0);
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testBatchUpdateSingleValParamArray() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateSingleValParamArray", args);
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
        // Test for getting the auto generated keys from the ResultSet When the getGeneratedKey is false.
        Assert.assertEquals(returns[1].stringValue(), "");
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testBatchUpdateWithValues() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithValues", args);
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testBatchUpdateWithVariables() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithVariables", args);
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testBatchUpdateWithFailure() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithFailure", args);
        // This is the one after the failing batch. Depending on the driver this may or may not be executed hence the
        // result could be either 1 or -3
        int[] expectedResult = {1, 1, -3, -3};
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), expectedResult[0]);
        Assert.assertEquals(retValue.getInt(1), expectedResult[1]);
        Assert.assertEquals(retValue.getInt(2), expectedResult[2]);
        Assert.assertEquals(retValue.getInt(3), expectedResult[3]);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testBatchUpdateWithNullParam() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithNullParam", args);
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testFailedBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testFailedBatchUpdate", args);
        Assert.assertTrue(returns[0].stringValue().contains("failed to execute batch update:"));
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), -3);
    }

    @Test(groups = BATCH_UPDATE_TEST)
    public void testErrorWithBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorWithBatchUpdate", args);
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0].stringValue().contains("array values are -3"));
        Assert.assertTrue(returns[1].stringValue().contains("{ballerinax/java.jdbc}DatabaseError"));
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
