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
package org.ballerinax.jdbc;

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
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.ballerinax.jdbc.utils.SQLDBUtils.DBType;
import org.ballerinax.jdbc.utils.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinax.jdbc.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

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
        System.setProperty("enableJBallerinaTests", "true");
        testDatabase = new FileBasedTestDatabase(DBType.H2, "datafiles/sql/SQLTest_H2_Data.sql",
                SQLDBUtils.DB_DIRECTORY, DB_NAME_H2);

        result = BCompileUtil.compile("test-src/sql/sql_actions_test.bal");
        resultNegative = BCompileUtil.compile("test-src/sql/sql_actions_negative_test.bal");
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
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(((BInteger) returns[1]).intValue() > 0);
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
        Assert.assertTrue(((BInteger) returns[0]).intValue() > 0);
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
        Assert.assertEquals(returns.length, 5);
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
        Assert.assertEquals(((BDecimal) returns[4]).decimalValue(), new BigDecimal("1234.567"));
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
    public void testINParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testINParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testINParametersWithDirectValues() {
        BValue[] returns = BRunUtil.invoke(result, "testINParametersWithDirectValues");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039.1D);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BDecimal) returns[7]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567D, DELTA);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 127);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testINParametersWithDirectVariables() {
        BValue[] returns = BRunUtil.invoke(result, "testINParametersWithDirectVariables");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039.1D);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BDecimal) returns[7]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567D, DELTA);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), -128);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBlobInParameter() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobInParameter");
        BInteger retInt = (BInteger) returns[0];
        BValueArray retBytes = (BValueArray) returns[1];
        Assert.assertEquals(retInt.intValue(), 1);
        Assert.assertEquals(new String(retBytes.getBytes()), "blob data");
    }

    @Test(groups = CONNECTOR_TEST)
    public void testNullINParameterValues() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINParameterValues");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testNullINParameterBlobValue() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINParameterBlobValue");
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
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
        Assert.assertNull(returns[1]);
        Assert.assertTrue(((BInteger) returns[2]).intValue() > 0);
        Assert.assertTrue(((BInteger) returns[3]).intValue() > 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithoutGeneratedKeys() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithoutGeneratedKeys");
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
        Assert.assertNull(returns[1]);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateSingleValParamArray() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateSingleValParamArray");
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithValues() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithValues");
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
    }

    @Test(groups = CONNECTOR_TEST, description = "Test batch update operation with variable parameters")
    public void testBatchUpdateWithVariables() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithVariables");
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithFailure() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithFailure");
        // This is the one after the failing batch. Depending on the driver this may or may not be executed hence the
        // result could be either 1 or -3
        int[] expectedResult = {1, 1, -3, 1};
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), expectedResult[0]);
        Assert.assertEquals(retValue.getInt(1), expectedResult[1]);
        Assert.assertEquals(retValue.getInt(2), expectedResult[2]);
        Assert.assertEquals(retValue.getInt(3), expectedResult[3]);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithNullParam() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithNullParam");
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTimeData() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeInParameters");
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals((int) retValue.getInt(0), 1);
        Assert.assertEquals((int) retValue.getInt(1), 1);
        Assert.assertEquals((int) retValue.getInt(2), 1);
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
        Assert.assertTrue(returns[0].stringValue().contains("Failed to execute select query:"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed select query error")
    public void testErrorWithSelectData() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testErrorWithSelectData");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerinax/java.jdbc}DatabaseError"));
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed update with generated id action")
    public void testFailedGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testGeneratedKeyOnInsert");
        Assert.assertTrue(returns[0].stringValue().contains("Failed to execute update query:"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test error for failed update with generated id action")
    public void testFailedGeneratedKeyOnInsertError() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testGeneratedKeyOnInsertError");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerinax/java.jdbc}DatabaseError"));
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test(groups = { CONNECTOR_TEST }, description = "Test failed batch update")
    public void testFailedBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testBatchUpdate");
        Assert.assertTrue(returns[0].stringValue().contains("Failed to execute batch update:"));
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), -3);
    }

    @Test(groups = { CONNECTOR_TEST }, description = "Test error for failed batch update")
    public void testErrorWithBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testErrorWithBatchUpdate");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0].stringValue().contains("array values are -3"));
        Assert.assertTrue(returns[1].stringValue().contains("{ballerinax/java.jdbc}DatabaseError"));
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid update of record field: modification not allowed on readonly value.*")
    public void testUpdateResult() {
        BRunUtil.invoke(resultNegative, "testUpdateResult");
    }

    @Test(groups = { CONNECTOR_TEST }, description = "Test failed parameter array update")
    public void testInvalidArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testInvalidArrayofQueryParameters");
        Assert.assertTrue(returns[0].stringValue().contains("Failed to execute select query: Unsupported array type " +
                "specified as a parameter at index 0"));
    }

    @Test(groups = { CONNECTOR_TEST }, description = "Test error for failed parameter array update")
    public void testErrorWithInvalidArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testErrorWithInvalidArrayofQueryParameters");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0].stringValue().contains("{ballerinax/java.jdbc}ApplicationError"));
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(returns[2].stringValue().contains("Failed to execute select query: Unsupported array type " +
                "specified as a parameter at index 0"));
    }

    @Test(groups = { CONNECTOR_TEST }, description = "Test error type for application level errors")
    public void testCheckApplicationErrorType() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testCheckApplicationErrorType");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test(groups = { CONNECTOR_TEST }, description = "Test error type for database errors")
    public void testCheckDatabaseErrorType() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testCheckDatabaseErrorType");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test(groups = CONNECTOR_TEST, description = "Test stopping a database client")
    public void testStopClient() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testStopClient");
        Assert.assertNotNull(returns);
        Assert.assertNull(returns[0]);
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
