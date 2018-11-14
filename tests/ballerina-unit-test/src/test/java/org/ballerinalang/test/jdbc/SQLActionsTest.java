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
package org.ballerinalang.test.jdbc;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.test.utils.SQLDBUtils.DBType;
import org.ballerinalang.test.utils.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinalang.test.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for SQL Connector actions test.
 *
 * @since 0.8.0
 */
public class SQLActionsTest {

    private static final double DELTA = 0.01;
    private CompileResult result;
    private CompileResult resultNegative;
    private static final String DB_NAME_H2 = "TEST_SQL_CONNECTOR_H2";
    private TestDatabase testDatabase;
    private static final String CONNECTOR_TEST = "ConnectorTest";

    @BeforeClass
    public void setup() {
        testDatabase = new FileBasedTestDatabase(DBType.H2, "datafiles/sql/SQLTest_H2_Data.sql",
                SQLDBUtils.DB_DIRECTORY, DB_NAME_H2);

        result = BCompileUtil.compile("test-src/jdbc/sql_actions_test.bal");
        resultNegative = BCompileUtil.compile("test-src/jdbc/sql_actions_negative_test.bal");
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testCreateTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTable");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testUpdateTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsert");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testGeneratedKeyOnInsertEmptyResults() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsertEmptyResults");
        BInteger retValue = (BInteger) returns[0];
        int columnCount = 0;
        Assert.assertEquals(retValue.intValue(), columnCount);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testGeneratedKeyWithColumn() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyWithColumn");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testSelectData() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectData");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest for int float types")
    public void testSelectIntFloatData() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectIntFloatData");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BFloat.class);
        BInteger intVal = (BInteger) returns[0];
        BInteger longVal = (BInteger) returns[1];
        BFloat floatVal = (BFloat) returns[2];
        BFloat doubleVal = (BFloat) returns[3];
        long intExpected = 10;
        long longExpected = 9223372036854774807L;
        double floatExpected = 123.34;
        double doubleExpected = 2139095039;
        Assert.assertEquals(intVal.intValue(), intExpected);
        Assert.assertEquals(longVal.intValue(), longExpected);
        Assert.assertEquals(floatVal.floatValue(), floatExpected, DELTA);
        Assert.assertEquals(doubleVal.floatValue(), doubleExpected);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testQueryParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters2");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableDataWithParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters2");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableDataWithParameters3() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters3");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayofQueryParameters");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBoolArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testBoolArrayofQueryParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 10);
    }

    @Test(groups = {CONNECTOR_TEST})
    public void testBlobArrayOfQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobArrayQueryParameter");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 7);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testEmptySQLType() {
        BValue[] returns = BRunUtil.invoke(result, "testEmptySQLType");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithValues() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithValues");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = CONNECTOR_TEST, description = "Test batch update operation with variable parameters")
    public void testBatchUpdateWithVariables() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithVariables");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithFailure() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithFailure");
        // This is the one after the failing batch. Depending on the driver this may or may not be executed hence the
        // result could be either 1 or -3
        int[] expectedResult = {1, 1, -3, 1};
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), expectedResult[0]);
        Assert.assertEquals(retValue.get(1), expectedResult[1]);
        Assert.assertEquals(retValue.get(2), expectedResult[2]);
        Assert.assertEquals(retValue.get(3), expectedResult[3]);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithNullParam() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithNullParam");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTimeData() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeInParameters");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals((int) retValue.get(0), 1);
        Assert.assertEquals((int) retValue.get(1), 1);
        Assert.assertEquals((int) retValue.get(2), 1);
    }

    @Test(groups = CONNECTOR_TEST, description = "Check date time null in values")
    public void testDateTimeNullInValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullInValues");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"DATE_TYPE\":null, \"TIME_TYPE\":null, "
                + "\"TIMESTAMP_TYPE\":null, \"DATETIME_TYPE\":null}]");
    }

    @Test(dependsOnGroups = CONNECTOR_TEST)
    public void testCloseConnectionPool() {
        BValue connectionCountQuery;
        connectionCountQuery = new BString("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS");
        BValue[] args = { connectionCountQuery };
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST, description = "Check blob binary and clob types types.")
    public void testComplexTypeRetrieval() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeRetrieval");
        String expected0, expected1, expected2, expected3;
        expected0 = "<results><result><ROW_ID>1</ROW_ID><BLOB_TYPE>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</BLOB_TYPE>"
                + "</result></results>";
        expected1 = "<results><result><ROW_ID>1</ROW_ID><DATE_TYPE>2017-02-03</DATE_TYPE>"
                + "<TIME_TYPE>11:35:45</TIME_TYPE><DATETIME_TYPE>2017-02-03 11:53:00</DATETIME_TYPE>"
                + "<TIMESTAMP_TYPE>2017-02-03 11:53:00</TIMESTAMP_TYPE></result></results>";
        expected2 = "[{\"ROW_ID\":1, \"BLOB_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\"}]";
        expected3 = "[{\"ROW_ID\":1, \"DATE_TYPE\":\"2017-02-03\", \"TIME_TYPE\":\"11:35:45\", "
                + "\"DATETIME_TYPE\":\"2017-02-03 11:53:00\", \"TIMESTAMP_TYPE\":\"2017-02-03 11:53:00\"}]";

        Assert.assertEquals(returns[0].stringValue(), expected0);
        Assert.assertEquals(returns[1].stringValue(), expected1);
        Assert.assertEquals(returns[2].stringValue(), expected2);
        Assert.assertEquals(returns[3].stringValue(), expected3);
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed select query")
    public void testFailedSelect() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testSelectData");
        Assert.assertTrue(returns[0].stringValue().contains("execute query failed:"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed update with generated id action")
    public void testFailedGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testGeneratedKeyOnInsert");
        Assert.assertTrue(returns[0].stringValue().contains("execute update with generated keys failed:"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed batch update")
    public void testFailedBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testBatchUpdate");
        Assert.assertTrue(returns[0].stringValue().contains("execute batch update failed:"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed parameter array update")
    public void testInvalidArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testInvalidArrayofQueryParameters");
        Assert.assertTrue(returns[0].stringValue()
                .contains("execute query failed: unsupported array type for parameter index 0"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test iterating data of a table loaded to memory multiple times")
    public void testSelectLoadToMemory() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelectLoadToMemory");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, "
                + "{FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}])");
    }

    @Test(groups = CONNECTOR_TEST, description = "Test iterating data of a table loaded to memory after closing")
    public void testLoadToMemorySelectAfterTableClose() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLoadToMemorySelectAfterTableClose");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "("
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "Trying to perform hasNext operation over a closed table {})");
    }

    @Test(groups = CONNECTOR_TEST, description = "Test re-init endpoint")
    public void testReInitEndpoint() {
        TestDatabase testDatabase2;
        String validationQuery;

        testDatabase2 = new FileBasedTestDatabase(DBType.H2,
                "." + File.separator + "target" + File.separator + "H2Client2" + File.separator,
                "TEST_SQL_CONNECTOR_H2_2");
        validationQuery = "SELECT 1";

        BValue[] args = { new BString(validationQuery) };

        BValue[] returns = BRunUtil.invoke(result, "testReInitEndpoint", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        testDatabase2.stop();
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
