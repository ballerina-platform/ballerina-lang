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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Calendar;

/**
 * Test class for SQL Connector actions test.
 *
 * @since 0.8.0
 */
public class SQLActionsTest {

    private static final double DELTA = 0.01;
    private CompileResult result;
    private CompileResult resultNegative;
    private CompileResult resultMirror;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/connectors/sql/sql-actions-test.bal");
        resultNegative = BCompileUtil.compile("test-src/connectors/sql/sql-actions-negative.bal");
        resultMirror = BCompileUtil.compile("test-src/connectors/sql/sql-mirror-table-test.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/sql/SQLConnectorDataFile.sql");
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testCreateTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTable");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 0);
    }

    @Test(groups = "ConnectorTest")
    public void testUpdateTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsert");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = "ConnectorTest")
    public void testGeneratedKeyWithColumn() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyWithColumn");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = "ConnectorTest")
    public void testSelectData() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectData");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest for int float types")
    public   void testSelectIntFloatData() {
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

    @Test(groups = "ConnectorTest")
    public void testCallProcedure() {
        BValue[] returns = BRunUtil.invoke(result, "testCallProcedure");
        BString retValue = (BString) returns[0];
        final String expected = "James";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testCallProcedureWithResultSet() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(result, "testCallProcedureWithResultSet", args);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testCallProcedureWithMultipleResultSets() {
        BValue[] returns = BRunUtil.invoke(result, "testCallProcedureWithMultipleResultSets");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        BString retValue2 = (BString) returns[1];
        final String expected2 = "John";
        Assert.assertEquals(retValue.stringValue(), expected);
        Assert.assertEquals(retValue2.stringValue(), expected2);
    }

    @Test(groups = "ConnectorTest")
    public void testQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testQueryParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters2");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest", enabled = false)
    public void testInsertTableDataWithParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters2");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayofQueryParameters");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testBoolArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testBoolArrayofQueryParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 10);
    }

    @Test(groups = "ConnectorTest")
    public void testOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testOutParameters");
        Assert.assertEquals(returns.length, 14);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567D, DELTA);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[10]).intValue(), 5555);
        Assert.assertEquals(returns[11].stringValue(), "very long text");
        Assert.assertEquals(returns[12].stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals(returns[13].stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = "ConnectorTest")
    public void testNullOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testNullOutParameters");
        Assert.assertEquals(returns.length, 14);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 0.0D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 0.0D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);
        Assert.assertEquals(returns[5].stringValue(), null);
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 0.0D);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 0.0D);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 0.0D);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[10]).intValue(), 0);
        Assert.assertEquals(returns[11].stringValue(), null);
        Assert.assertEquals(returns[12].stringValue(), null);
        Assert.assertEquals(returns[13].stringValue(), null);
    }

    @Test(groups = "ConnectorTest")
    public void testINParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testINParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testNullINParameterValues() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINParameterValues");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testINOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testINOutParameters");
        Assert.assertEquals(returns.length, 14);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567D, DELTA);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[10]).intValue(), 5555);
        Assert.assertEquals(returns[11].stringValue(), "very long text");
        Assert.assertEquals(returns[12].stringValue(), "YmxvYiBkYXRh");
        Assert.assertEquals(returns[13].stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = "ConnectorTest")
    public void testNullINOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINOutParameters");
        Assert.assertEquals(returns.length, 14);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 0.0D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 0.0D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);
        Assert.assertEquals(returns[5].stringValue(), null);
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 0.0D);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 0.0D);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 0.0D);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[10]).intValue(), 0);
        Assert.assertEquals(returns[11].stringValue(), null);
        Assert.assertEquals(returns[12].stringValue(), null);
        Assert.assertEquals(returns[13].stringValue(), null);
    }

    @Test(groups = "ConnectorTest")
    public void testEmptySQLType() {
        BValue[] returns = BRunUtil.invoke(result, "testEmptySQLType");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testArrayInParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayInParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);

        Assert.assertTrue(returns[1] instanceof BIntArray);
        BIntArray intArray = (BIntArray) returns[1];
        Assert.assertEquals(intArray.get(0), 1);

        Assert.assertTrue(returns[2] instanceof BIntArray);
        BIntArray longArray = (BIntArray) returns[2];
        Assert.assertEquals(longArray.get(0), 1503383034226L);
        Assert.assertEquals(longArray.get(1), 1503383034224L);
        Assert.assertEquals(longArray.get(2), 1503383034225L);

        Assert.assertTrue(returns[3] instanceof BFloatArray);
        BFloatArray doubleArray = (BFloatArray) returns[3];
        Assert.assertEquals(doubleArray.get(0), 1503383034226.23D);
        Assert.assertEquals(doubleArray.get(1), 1503383034224.43D);
        Assert.assertEquals(doubleArray.get(2), 1503383034225.123D);

        Assert.assertTrue(returns[4] instanceof BStringArray);
        BStringArray stringArray = (BStringArray) returns[4];
        Assert.assertEquals(stringArray.get(0), "Hello");
        Assert.assertEquals(stringArray.get(1), "Ballerina");

        Assert.assertTrue(returns[5] instanceof BBooleanArray);
        BBooleanArray booleanArray = (BBooleanArray) returns[5];
        Assert.assertEquals(booleanArray.get(0), 1);
        Assert.assertEquals(booleanArray.get(1), 0);
        Assert.assertEquals(booleanArray.get(2), 1);

        Assert.assertTrue(returns[6] instanceof BFloatArray);
        BFloatArray floatArray = (BFloatArray) returns[6];
        Assert.assertEquals(floatArray.get(0), 245.23);
        Assert.assertEquals(floatArray.get(1), 5559.49);
        Assert.assertEquals(floatArray.get(2), 8796.123);
    }

    @Test(groups = "ConnectorTest")
    public void testArrayOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayOutParameters");
        Assert.assertEquals(returns[0].stringValue(), "[1,2,3]");
        Assert.assertEquals(returns[1].stringValue(), "[100000000,200000000,300000000]");
        Assert.assertEquals(returns[2].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[3].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[4].stringValue(), "[true,false,true]");
        Assert.assertEquals(returns[5].stringValue(), "[Hello,Ballerina]");
    }

    @Test(groups = "ConnectorTest")
    public void testArrayInOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayInOutParameters");
        Assert.assertEquals(returns[0].stringValue(), "1");
        Assert.assertEquals(returns[1].stringValue(), "[1,2,3]");
        Assert.assertEquals(returns[2].stringValue(), "[100000000,200000000,300000000]");
        Assert.assertEquals(returns[3].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[4].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[5].stringValue(), "[true,false,true]");
        Assert.assertEquals(returns[6].stringValue(), "[Hello,Ballerina]");
    }

    @Test(groups = "ConnectorTest")
    public void testBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testBatchUpdateWithFailure() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithFailure");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
        Assert.assertEquals(retValue.get(2), -3);
        Assert.assertEquals(retValue.get(3), -3);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = "ConnectorTest")
    public void testBatchUpdateWithNullParam() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithNullParam");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTimeData() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeInParameters");
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals((int) retValue.get(0), 1);
        Assert.assertEquals((int) retValue.get(1), 1);
        Assert.assertEquals((int) retValue.get(2), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testDateTimeOutParams() {
        BValue[] args = new BValue[3];
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.HOUR, 14);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 23);
        long time = cal.getTimeInMillis();
        args[0] = new BInteger(time);

        cal.clear();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        long date = cal.getTimeInMillis();
        args[1] = new BInteger(date);

        cal.clear();
        cal.set(Calendar.HOUR, 16);
        cal.set(Calendar.MINUTE, 33);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        long timestamp = cal.getTimeInMillis();
        args[2] = new BInteger(timestamp);

        BValue[] returns = BRunUtil.invoke(result, "testDateTimeOutParams", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "ConnectorTest", description = "Check date time null in values")
    public void testDateTimeNullInValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullInValues");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"DATE_TYPE\":null,\"TIME_TYPE\":null,"
                + "\"TIMESTAMP_TYPE\":null,\"DATETIME_TYPE\":null}]");
    }

    @Test(groups = "ConnectorTest", description = "Check date time null out values")
    public void testDateTimeNullOutValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullOutValues");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "ConnectorTest", description = "Check date time null inout values")
    public void testDateTimeNullInOutValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullInOutValues");
        Assert.assertEquals(returns.length, 4);
        Assert.assertNull(returns[0].stringValue());
        Assert.assertNull(returns[1].stringValue());
        Assert.assertNull(returns[2].stringValue());
        Assert.assertNull(returns[3].stringValue());
    }


    @Test(groups = "ConnectorTest", enabled = false)
    public void testStructOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testStructOutParameters");
        BString retValue = (BString) returns[0];
        String expected = "10";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(dependsOnGroups = "ConnectorTest")
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest", description = "Check blob binary and clob types types.")
    public void testComplexTypeRetrieval() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeRetrieval", args);
        Assert.assertEquals(returns[0].stringValue(), "<results><result><ROW_ID>1</ROW_ID><INT_TYPE>10</INT_TYPE>"
                + "<LONG_TYPE>9223372036854774807</LONG_TYPE><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                + "<DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE><BOOLEAN_TYPE>true</BOOLEAN_TYPE>"
                + "<STRING_TYPE>Hello</STRING_TYPE><NUMERIC_TYPE>1234.567</NUMERIC_TYPE>"
                + "<DECIMAL_TYPE>1234.567</DECIMAL_TYPE><REAL_TYPE>1234.567</REAL_TYPE><TINYINT_TYPE>1</TINYINT_TYPE>"
                + "<SMALLINT_TYPE>5555</SMALLINT_TYPE><CLOB_TYPE>very long text</CLOB_TYPE>"
                + "<BLOB_TYPE>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</BLOB_TYPE>"
                + "<BINARY_TYPE>d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu</BINARY_TYPE></result></results>");
        Assert.assertEquals(returns[1].stringValue(), "<results><result><ROW_ID>1</ROW_ID>"
                + "<DATE_TYPE>2017-02-03</DATE_TYPE><TIME_TYPE>11:35:45</TIME_TYPE>"
                + "<DATETIME_TYPE>2017-02-03 11:53:00.000000</DATETIME_TYPE>"
                + "<TIMESTAMP_TYPE>2017-02-03 11:53:00.000000</TIMESTAMP_TYPE></result></results>");
        Assert.assertEquals(returns[2].stringValue(), "[{\"ROW_ID\":1,\"INT_TYPE\":10,"
                + "\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,\"DOUBLE_TYPE\":2.139095039E9,"
                + "\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\",\"NUMERIC_TYPE\":1234.567,\"DECIMAL_TYPE\":1234.567,"
                + "\"REAL_TYPE\":1234.567,\"TINYINT_TYPE\":1,\"SMALLINT_TYPE\":5555,\"CLOB_TYPE\":\"very long text\","
                + "\"BLOB_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\","
                + "\"BINARY_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu\"}]");
        Assert.assertEquals(returns[3].stringValue(), "[{\"ROW_ID\":1,\"DATE_TYPE\":\"2017-02-03\","
                + "\"TIME_TYPE\":\"11:35:45\",\"DATETIME_TYPE\":\"2017-02-03 11:53:00.000000\","
                + "\"TIMESTAMP_TYPE\":\"2017-02-03 11:53:00.000000\"}]");
    }

    @Test(groups = "ConnectorTest", description = "Test failed select query")
    public void testFailedSelect() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testSelectData");
        Assert.assertTrue(returns[0].stringValue().contains("execute query failed:"));
    }

    @Test(groups = "ConnectorTest", description = "Test failed update with generated id action")
    public void testFailedGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testGeneratedKeyOnInsert");
        Assert.assertTrue(returns[0].stringValue().contains("execute update with generated keys failed:"));
    }

    @Test(groups = "ConnectorTest", description = "Test failed call procedure")
    public void testFailedCallProcedure() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testCallProcedure");
        Assert.assertTrue(returns[0].stringValue().contains("execute stored procedure failed:"));
    }

    @Test(groups = "ConnectorTest", description = "Test failed batch update")
    public void testFailedBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testBatchUpdate");
        Assert.assertTrue(returns[0].stringValue().contains("execute batch update failed:"));
    }

    @Test(groups = "ConnectorTest", description = "Test failed parameter array update")
    public void testInvalidArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testInvalidArrayofQueryParameters");
        Assert.assertTrue(returns[0].stringValue()
                .contains("execute query failed: unsupported array type for parameter index 0"));
    }

    @Test(groups = "ConnectorTest",
          description = "Test failure scenario in adding data to mirrored table")
    public void testAddToMirrorTableNegative() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testAddToMirrorTableNegative");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "{message:\"execute update failed: integrity constraint "
                + "violation: unique constraint or index violation; SYS_PK_10179 table: EMPLOYEEADDNEGATIVE\", "
                + "cause:null}");
    }

    @Test(groups = "ConnectorTest",
          description = "Test adding data to mirrored table")
    public void testAddToMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testAddToMirrorTable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "{id:1, name:\"Manuri\", address:\"Sri Lanka\"}");
        Assert.assertEquals(returns[1].stringValue(), "{id:2, name:\"Devni\", address:\"Sri Lanka\"}");
    }

    @Test(groups = "ConnectorTest",
          description = "Test deleting data from mirrored table")
    public void testDeleteFromMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testDeleteFromMirrorTable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = "ConnectorTest",
          description = "Test iterating data of a mirrored table multiple times")
    public void testIterateMirrorTable() throws Exception {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(resultMirror, "testIterateMirrorTable", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "[[{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}]]");
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
