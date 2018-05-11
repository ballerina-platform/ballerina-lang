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
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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
    private static final String TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD = ".*Trying to assign a Nil value to a "
            + "non-nillable field.*";
    private static final String INVALID_UNION_FIELD_ASSIGNMENT = ".*Corresponding Union type in the record is not an "
            + "assignable nillable type.*";
    private static final String TRYING_TO_ASSIGN_TO_A_MISMATCHING_FIELD = ".*Trying to assign to a mismatching field.*";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/connectors/sql/sql_actions_test.bal");
        resultNegative = BCompileUtil.compile("test-src/connectors/sql/sql_actions_negative_test.bal");
        resultMirror = BCompileUtil.compile("test-src/connectors/sql/sql_mirror_table_test.bal");
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

    @Test(groups = "ConnectorTest")
    public void testCallProcedure() {
        BValue[] returns = BRunUtil.invoke(result, "testCallProcedure");
        BString retValue = (BString) returns[0];
        final String expected = "James";
        Assert.assertEquals(retValue.stringValue(), expected);
        Assert.assertEquals(returns[1].stringValue(), "nil");
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
        BString retValue3 = (BString) returns[2];
        final String expected3 = "Watson";
        Assert.assertEquals(retValue.stringValue(), expected);
        Assert.assertEquals(retValue2.stringValue(), expected2);
        Assert.assertEquals(retValue3.stringValue(), expected3);
    }

    @Test(groups = "ConnectorTest")
    public void testCallProcedureWithMultipleResultSetsAndLowerConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndLowerConstraintCount");
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 1 and returned result set count: 2 from the stored procedure\""));
    }

    @Test(groups = "ConnectorTest")
    public void testCallProcedureWithMultipleResultSetsAndNilConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndNilConstraintCount");
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 0 and returned result set count: 2 from the stored procedure\""));
    }

    @Test(groups = "ConnectorTest")
    public void testCallProcedureWithMultipleResultSetsAndHigherConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndHigherConstraintCount");
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 3 and returned result set count: 2 from the stored procedure\""));
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

    @Test(groups = "ConnectorTest")
    public void testInsertTableDataWithParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters2");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableDataWithParameters3() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters3");
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
    public void testINParametersWithDirectValues() {
        BValue[] returns = BRunUtil.invoke(result, "testINParametersWithDirectValues");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039.1D);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567D);
        Assert.assertTrue(returns[9].stringValue().equals(returns[10].stringValue()));
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

    @Test(groups = "ConnectorTest")
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

    @Test(groups = "ConnectorTest", description = "Test failure scenario in adding data to mirrored table")
    public void testAddToMirrorTableNegative() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testAddToMirrorTableNegative");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "{message:\"execute update failed: integrity constraint "
                + "violation: unique constraint or index violation; SYS_PK_10179 table: EMPLOYEEADDNEGATIVE\", "
                + "cause:null}");
    }

    @Test(groups = "ConnectorTest", description = "Test adding data to mirrored table")
    public void testAddToMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testAddToMirrorTable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "{id:1, name:\"Manuri\", address:\"Sri Lanka\"}");
        Assert.assertEquals(returns[1].stringValue(), "{id:2, name:\"Devni\", address:\"Sri Lanka\"}");
    }

    @Test(groups = "ConnectorTest", description = "Test deleting data from mirrored table")
    public void testDeleteFromMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testDeleteFromMirrorTable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a mirrored table multiple times")
    public void testIterateMirrorTable() throws Exception {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(resultMirror, "testIterateMirrorTable", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}])");
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a mirrored table after closing")
    public void testIterateMirrorTableAfterClose() throws Exception {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(resultMirror, "testIterateMirrorTableAfterClose", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}], {message:\"Trying to perform hasNext operation over a "
                + "closed table\", cause:null})");
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a table loaded to memory multiple times")
    public void testSelectLoadToMemory() throws Exception {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelectLoadToMemory", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2,"
                + " name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}])");
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a table loaded to memory after closing")
    public void testLoadToMemorySelectAfterTableClose() throws Exception {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(result, "testLoadToMemorySelectAfterTableClose", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}], {message:\"Trying to perform hasNext operation over a "
                + "closed table\", cause:null})");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping to nillable type fields")
    public void testMappingToNillableTypeFields() {
        BValue[] returns = BRunUtil.invoke(result, "testMappingToNillableTypeFields");
        Assert.assertNotNull(returns);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039, DELTA);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 1234.567);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[10]).intValue(), 5555);
        Assert.assertEquals(returns[11].stringValue(), "very long text");
        Assert.assertEquals(returns[12].stringValue(), "wso2 ballerina blob test.");
        Assert.assertEquals(returns[13].stringValue(), "wso2 ballerina binary test.");
        Assert.assertEquals(returns[14].stringValue(), "{time:1486060200000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
        Assert.assertEquals(returns[15].stringValue(), "{time:41745000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
        Assert.assertEquals(returns[16].stringValue(), "{time:1486122780000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
        Assert.assertEquals(returns[17].stringValue(), "{time:1486122780000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping to nillable type fields")
    public void testMappingDatesToTimeType() {
        BValue[] returns = BRunUtil.invoke(result, "testMappingDatesToTimeType");
        Assert.assertNotNull(returns);

        Assert.assertEquals(returns[0].stringValue(), "{time:1486060200000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
        Assert.assertEquals(returns[1].stringValue(), "{time:41745000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
        Assert.assertEquals(returns[2].stringValue(), "{time:1486122780000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
        Assert.assertEquals(returns[3].stringValue(), "{time:1486122780000, zone:{zoneId:\"UTC\", zoneOffset:0}}");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping to nillable type fields")
    public void testMappingDatesToIntType() {
        BValue[] returns = BRunUtil.invoke(result, "testMappingDatesToIntType");
        Assert.assertNotNull(returns);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1486060200000L);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 41745000L);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1486122780000L);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1486122780000L);
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping date to nillable int field")
    public void testMappingDatesToNillableIntType() {
        BValue[] returns = BRunUtil.invoke(result, "testMappingDatesToNillableIntType");
        Assert.assertNotNull(returns);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1486060200000L);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 41745000L);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1486122780000L);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1486122780000L);
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping date to nillable string field")
    public void testMappingDatesToNillableStringType() {
        BValue[] returns = BRunUtil.invoke(result, "testMappingDatesToNillableStringType");
        Assert.assertNotNull(returns);

        Assert.assertEquals(returns[0].stringValue(), "2017-02-03+05:30");
        Assert.assertEquals(returns[1].stringValue(), "17:05:45.000+05:30");
        Assert.assertEquals(returns[2].stringValue(), "2017-02-03T17:23:00.000+05:30");
        Assert.assertEquals(returns[3].stringValue(), "2017-02-03T17:23:00.000+05:30");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping to nillable type fields")
    public void testMappingNullToNillableTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testMappingNullToNillableTypes");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 18);
        Assert.assertNull(returns[0]);
        Assert.assertNull(returns[1]);
        ;
        Assert.assertNull(returns[2]);
        Assert.assertNull(returns[3]);
        Assert.assertNull(returns[4]);
        Assert.assertNull(returns[5]);
        Assert.assertNull(returns[6]);
        Assert.assertNull(returns[7]);
        Assert.assertNull(returns[8]);
        Assert.assertNull(returns[9]);
        Assert.assertNull(returns[10]);
        Assert.assertNull(returns[11]);
        Assert.assertNull(returns[12]);
        Assert.assertNull(returns[13]);
        Assert.assertNull(returns[14]);
        Assert.assertNull(returns[15]);
        Assert.assertNull(returns[16]);
        Assert.assertNull(returns[17]);
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable int field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableInt() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableInt");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable long field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableLong() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableLong");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable float field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableFloat() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableFloat");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable double field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDouble() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableDouble");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable boolean field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableBoolean() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableBoolean");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable string field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableString() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableString");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable numeric field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableNumeric() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableNumeric");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable small-int field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableSmallint() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableSmallint");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable tiny-int field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableTinyInt() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableTinyInt");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable decimal field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDecimal() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableDecimal");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable real field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableReal() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableReal");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable clob field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableClob() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableClob");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable blob field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableBlob() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableBlob");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable binary field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableBinary() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableBinary");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable date field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDate() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableDate");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable time field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableTime() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableTime");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable datetime field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDateTime() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableDateTime");
    }

    @Test(groups = "ConnectorTest",
          description = "Test mapping nil to non-nillable timestamp field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableTimeStamp() {
        BRunUtil.invoke(resultNegative, "testAssignNilToNonNillableTimeStamp");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionInt() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionInt");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionLong() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionLong");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionFloat() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionFloat");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDouble() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionDouble");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionBoolean() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionBoolean");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionString() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionString");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionNumeric() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionNumeric");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionSmallint() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionSmallint");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionTinyInt() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionTinyInt");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDecimal() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionDecimal");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionReal() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionReal");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionClob() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionClob");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionBlob() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionBlob");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionBinary() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionBinary");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDate() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionDate");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =  INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionTime() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionTime");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =  INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDateTime() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionDateTime");
    }

    @Test(groups = "ConnectorTest",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =  INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionTimeStamp() {
        BRunUtil.invoke(resultNegative, "testAssignToInvalidUnionTimeStamp");
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
