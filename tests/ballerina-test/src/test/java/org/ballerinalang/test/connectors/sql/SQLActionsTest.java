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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Calendar;

import static org.ballerinalang.test.connectors.sql.SQLActionsTest.DBType.HSQLDB;
import static org.ballerinalang.test.connectors.sql.SQLActionsTest.DBType.MYSQL;
import static org.ballerinalang.test.connectors.sql.SQLActionsTest.DBType.POSTGRES;

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
    private DBType dbType;
    private MySQLContainer mysql;
    private PostgreSQLContainer postgres;
    private BValue[] connectionArgs = new BValue[3];

    @Parameters({"dataClientTestDBType"})
    public SQLActionsTest(@Optional("HSQLDB") DBType dataClientTestDBType) {
        this.dbType = dataClientTestDBType;
    }

    @BeforeClass
    public void setup() {
        String jdbcURL, username, password;
        switch (dbType) {
        case MYSQL:
            mysql = new MySQLContainer();
            mysql.start();
            jdbcURL = mysql.getJdbcUrl();
            username = mysql.getUsername();
            password = mysql.getPassword();
            SQLDBUtils.initDatabase(jdbcURL, username, password, "datafiles/sql/SQLConnectorMYSQLDataFile.sql");
            break;
        case POSTGRES:
            postgres = new PostgreSQLContainer();
            postgres.start();
            jdbcURL = postgres.getJdbcUrl();
            username = postgres.getUsername();
            password = postgres.getPassword();
            SQLDBUtils.initDatabase(jdbcURL, username, password, "datafiles/sql/SQLConnectorPostgresDataFile.sql");
            break;
        default:
            SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
            jdbcURL = "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR";
            username = "SA";
            password = "";
            SQLDBUtils.initDatabase(jdbcURL, username, password, "datafiles/sql/SQLConnectorDataFile.sql");
            break;
        }
        connectionArgs[0] = new BString(jdbcURL);
        connectionArgs[1] = new BString(username);
        connectionArgs[2] = new BString(password);

        result = BCompileUtil.compile("test-src/connectors/sql/sql_actions_test.bal");
        resultNegative = BCompileUtil.compile("test-src/connectors/sql/sql_actions_negative_test.bal");
        resultMirror = BCompileUtil.compile("test-src/connectors/sql/sql_mirror_table_test.bal");
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableData", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testCreateTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTable", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 0);
    }

    @Test(groups = "ConnectorTest")
    public void testUpdateTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateTableData", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsert", connectionArgs);
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = "ConnectorTest")
    public void testGeneratedKeyWithColumn() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyWithColumn", connectionArgs);
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = "ConnectorTest")
    public void testSelectData() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectData", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest for int float types")
    public void testSelectIntFloatData() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectIntFloatData", connectionArgs);
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
        BValue[] returns = BRunUtil.invoke(result, "testCallProcedure", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "James";
        Assert.assertEquals(retValue.stringValue(), expected);
        if (dbType == POSTGRES) {
            // In postgres, there are no procedures but functions. When we call a function internally a select
            // happens eg: select InsertPersonData(100, 'J'); which returns a table containing an empty string
            Assert.assertEquals(returns[1].stringValue(), "table");
        } else {
            Assert.assertEquals(returns[1].stringValue(), "nil");
        }
    }

    @Test(groups = {"ConnectorTest", "MySQLNotSupported", "PostgresNotSupported"})
    public void testCallProcedureWithResultSet() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testCallProcedureWithResultSet", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = {"ConnectorTest", "MySQLNotSupported", "HSQLDBNotSupported"})
    public void testCallFunctionWithRefCursor() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testCallFunctionWithReturningRefcursor", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = {"ConnectorTest", "MySQLNotSupported", "PostgresNotSupported"})
    public void testCallProcedureWithMultipleResultSets() {
        BValue[] returns = BRunUtil.invoke(result, "testCallProcedureWithMultipleResultSets", connectionArgs);
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

    @Test(groups = {"ConnectorTest", "PostgresNotSupported"})
    public void testCallProcedureWithMultipleResultSetsAndLowerConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndLowerConstraintCount",
                        connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 1 and returned result set count: 2 from the stored procedure\""));
    }

    @Test(groups = {"ConnectorTest", "PostgresNotSupported"})
    public void testCallProcedureWithMultipleResultSetsAndNilConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndNilConstraintCount", connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 0 and returned result set count: 2 from the stored procedure\""));
    }

    @Test(groups = {"ConnectorTest", "PostgresNotSupported"})
    public void testCallProcedureWithMultipleResultSetsAndHigherConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndHigherConstraintCount",
                        connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 3 and returned result set count: 2 from the stored procedure\""));
    }

    @Test(groups = "ConnectorTest")
    public void testQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testQueryParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters2", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableDataWithParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters2", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableDataWithParameters3() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters3", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayofQueryParameters", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testBoolArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testBoolArrayofQueryParameters", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 10);
    }

    @Test(groups = "ConnectorTest")
    public void testOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testOutParameters", connectionArgs);
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

    @Test(groups = {"ConnectorTest"})
    public void testNullOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testNullOutParameters", connectionArgs);
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
        BValue[] returns = BRunUtil.invoke(result, "testINParameters", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testINParametersWithDirectValues() {
        BValue[] returns = BRunUtil.invoke(result, "testINParametersWithDirectValues", connectionArgs);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039.1D);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567D, DELTA);
        Assert.assertTrue(returns[9].stringValue().equals(returns[10].stringValue()));
    }

    @Test(groups = "ConnectorTest")
    public void testINParametersWithDirectVariables() {
        BValue[] returns = BRunUtil.invoke(result, "testINParametersWithDirectVariables", connectionArgs);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039.1D);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567D);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567D, DELTA);
        Assert.assertTrue(returns[9].stringValue().equals(returns[10].stringValue()));
    }

    @Test(groups = "ConnectorTest")
    public void testNullINParameterValues() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINParameterValues", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testINOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testINOutParameters", connectionArgs);
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
        BValue[] returns = BRunUtil.invoke(result, "testNullINOutParameters", connectionArgs);
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
        BValue[] returns = BRunUtil.invoke(result, "testEmptySQLType", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = {"ConnectorTest", "MySQLNotSupported"})
    public void testArrayInParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayInParameters", connectionArgs);
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

    @Test(groups = {"ConnectorTest", "MySQLNotSupported"})
    public void testArrayOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayOutParameters", connectionArgs);
        Assert.assertEquals(returns[0].stringValue(), "[1,2,3]");
        Assert.assertEquals(returns[1].stringValue(), "[100000000,200000000,300000000]");
        Assert.assertEquals(returns[2].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[3].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[4].stringValue(), "[true,false,true]");
        Assert.assertEquals(returns[5].stringValue(), "[Hello,Ballerina]");
    }

    @Test(groups = {"ConnectorTest", "MySQLNotSupported"})
    public void testArrayInOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayInOutParameters", connectionArgs);
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
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testBatchUpdateWithValues() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithValues", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = "ConnectorTest", description = "Test batch update operation with variable parameters")
    public void testBatchUpdateWithVariables() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithVariables", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testBatchUpdateWithFailure() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithFailure", connectionArgs);
        // This is the one after the failing batch. Depending on the driver this may or may not be executed hence the
        // result could be either 1 or -3
        int[] expectedResult = {1, 1, -3, -3};
        int totalUpdatedCount = 2;
        if (dbType == MYSQL) {
            expectedResult[3] = 1;
            totalUpdatedCount = 3;
        } else if (dbType == POSTGRES) {
            expectedResult[0] = -3;
            expectedResult[1] = -3;
            totalUpdatedCount = 0;
        }
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), expectedResult[0]);
        Assert.assertEquals(retValue.get(1), expectedResult[1]);
        Assert.assertEquals(retValue.get(2), expectedResult[2]);
        Assert.assertEquals(retValue.get(3), expectedResult[3]);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), totalUpdatedCount);
    }

    @Test(groups = "ConnectorTest")
    public void testBatchUpdateWithNullParam() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithNullParam", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTimeData() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeInParameters", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals((int) retValue.get(0), 1);
        Assert.assertEquals((int) retValue.get(1), 1);
        Assert.assertEquals((int) retValue.get(2), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testDateTimeOutParams() {
        BValue[] args = new BValue[6];
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

        args[3] = connectionArgs[0];
        args[4] = connectionArgs[1];
        args[5] = connectionArgs[2];

        BValue[] returns = BRunUtil.invoke(result, "testDateTimeOutParams", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "ConnectorTest", description = "Check date time null in values")
    public void testDateTimeNullInValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullInValues", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"DATE_TYPE\":null,\"TIME_TYPE\":null,"
                + "\"TIMESTAMP_TYPE\":null,\"DATETIME_TYPE\":null}]");
    }

    @Test(groups = "ConnectorTest", description = "Check date time null out values")
    public void testDateTimeNullOutValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullOutValues", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "ConnectorTest", description = "Check date time null inout values")
    public void testDateTimeNullInOutValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullInOutValues", connectionArgs);
        Assert.assertEquals(returns.length, 4);
        Assert.assertNull(returns[0].stringValue());
        Assert.assertNull(returns[1].stringValue());
        Assert.assertNull(returns[2].stringValue());
        Assert.assertNull(returns[3].stringValue());
    }

    @Test(groups = {"ConnectorTest", "MySQLNotSupported"})
    public void testStructOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testStructOutParameters", connectionArgs);
        BString retValue = (BString) returns[0];
        String expected = "10";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(dependsOnGroups = "ConnectorTest")
    public void testCloseConnectionPool() {
        BValue connectionCountQuery;
        if (dbType == MYSQL) {
            connectionCountQuery = new BString("SELECT COUNT(*) FROM information_schema.PROCESSLIST");
        } else {
            connectionCountQuery = new BString("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA"
                    + ".SYSTEM_SESSIONS");
        }
        BValue[] args = new BValue[4];
        System.arraycopy(connectionArgs, 0, args, 0, 3);
        args[3] = connectionCountQuery;
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest", description = "Check blob binary and clob types types.")
    public void testComplexTypeRetrieval() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeRetrieval", connectionArgs);
        String expected0, expected1, expected2, expected3;
        if (dbType == MYSQL) {
            expected0 = "<results><result><row_id>1</row_id><int_type>10</int_type><long_type>9223372036854774807"
                    + "</long_type><float_type>123.34</float_type><double_type>2.139095039E9</double_type>"
                    + "<boolean_type>true</boolean_type><string_type>Hello</string_type>"
                    + "<numeric_type>1234.567</numeric_type><decimal_type>1234.567</decimal_type>"
                    + "<real_type>1234.567</real_type><tinyint_type>1</tinyint_type>"
                    + "<smallint_type>5555</smallint_type><clob_type>very long text</clob_type>"
                    + "<blob_type>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</blob_type>"
                    + "<binary_type>d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu</binary_type></result></results>";
            expected1 = "<results><result><row_id>1</row_id>"
                    + "<date_type>2017-02-03</date_type><time_type>11:35:45</time_type>"
                    + "<datetime_type>2017-02-03 11:53:00.0</datetime_type>"
                    + "<timestamp_type>2017-02-03 11:53:00.0</timestamp_type></result></results>";
            expected2 = "[{\"row_id\":1,\"int_type\":10,\"long_type\":9223372036854774807,\"float_type\":123.34,"
                    + "\"double_type\":2.139095039E9,\"boolean_type\":true,\"string_type\":\"Hello\","
                    + "\"numeric_type\":1234.567,\"decimal_type\":1234.567,\"real_type\":1234.567,\"tinyint_type\":1,"
                    + "\"smallint_type\":5555,\"clob_type\":\"very long text\","
                    + "\"blob_type\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\","
                    + "\"binary_type\":\"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu\"}]";
            expected3 = "[{\"row_id\":1,\"date_type\":\"2017-02-03\",\"time_type\":\"11:35:45\","
                    + "\"datetime_type\":\"2017-02-03 11:53:00.0\",\"timestamp_type\":\"2017-02-03 11:53:00.0\"}]";
        } else {
            expected0 = "<results><result><ROW_ID>1</ROW_ID><INT_TYPE>10</INT_TYPE>"
                    + "<LONG_TYPE>9223372036854774807</LONG_TYPE><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                    + "<DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE><BOOLEAN_TYPE>true</BOOLEAN_TYPE>"
                    + "<STRING_TYPE>Hello</STRING_TYPE><NUMERIC_TYPE>1234.567</NUMERIC_TYPE>"
                    + "<DECIMAL_TYPE>1234.567</DECIMAL_TYPE><REAL_TYPE>1234.567</REAL_TYPE><TINYINT_TYPE>1"
                    + "</TINYINT_TYPE><SMALLINT_TYPE>5555</SMALLINT_TYPE><CLOB_TYPE>very long text</CLOB_TYPE>"
                    + "<BLOB_TYPE>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</BLOB_TYPE>"
                    + "<BINARY_TYPE>d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu</BINARY_TYPE></result></results>";
            expected1 = "<results><result><ROW_ID>1</ROW_ID>"
                    + "<DATE_TYPE>2017-02-03</DATE_TYPE><TIME_TYPE>11:35:45</TIME_TYPE>"
                    + "<DATETIME_TYPE>2017-02-03 11:53:00.000000</DATETIME_TYPE>"
                    + "<TIMESTAMP_TYPE>2017-02-03 11:53:00.000000</TIMESTAMP_TYPE></result></results>";
            expected2 = "[{\"ROW_ID\":1,\"INT_TYPE\":10,"
                    + "\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,\"DOUBLE_TYPE\":2.139095039E9,"
                    + "\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\",\"NUMERIC_TYPE\":1234.567,"
                    + "\"DECIMAL_TYPE\":1234.567,\"REAL_TYPE\":1234.567,\"TINYINT_TYPE\":1,\"SMALLINT_TYPE\":5555,"
                    + "\"CLOB_TYPE\":\"very long text\",\"BLOB_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\","
                    + "\"BINARY_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu\"}]";
            expected3 = "[{\"ROW_ID\":1,\"DATE_TYPE\":\"2017-02-03\","
                    + "\"TIME_TYPE\":\"11:35:45\",\"DATETIME_TYPE\":\"2017-02-03 11:53:00.000000\","
                    + "\"TIMESTAMP_TYPE\":\"2017-02-03 11:53:00.000000\"}]";
        }

        Assert.assertEquals(returns[0].stringValue(), expected0);
        Assert.assertEquals(returns[1].stringValue(), expected1);
        Assert.assertEquals(returns[2].stringValue(), expected2);
        Assert.assertEquals(returns[3].stringValue(), expected3);
    }

    @Test(groups = "ConnectorTest", description = "Test failed select query")
    public void testFailedSelect() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testSelectData", connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("execute query failed:"));
    }

    @Test(groups = "ConnectorTest", description = "Test failed update with generated id action")
    public void testFailedGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testGeneratedKeyOnInsert", connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("execute update with generated keys failed:"));
    }

    @Test(groups = "ConnectorTest", description = "Test failed call procedure")
    public void testFailedCallProcedure() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testCallProcedure", connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("execute stored procedure failed:"));
    }

    @Test(groups = "ConnectorTest", description = "Test failed batch update")
    public void testFailedBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testBatchUpdate", connectionArgs);
        if (dbType == HSQLDB) {
            Assert.assertTrue(returns[0].stringValue().contains("execute batch update failed:"));
        } else {
            Assert.assertTrue(returns[0].stringValue().contains("failure"));
        }
    }

    @Test(groups = "ConnectorTest", description = "Test failed parameter array update")
    public void testInvalidArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testInvalidArrayofQueryParameters", connectionArgs);
        Assert.assertTrue(returns[0].stringValue()
                .contains("execute query failed: unsupported array type for parameter index 0"));
    }

    @Test(groups = "ConnectorTest", description = "Test failure scenario in adding data to mirrored table")
    public void testAddToMirrorTableNegative() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testAddToMirrorTableNegative", connectionArgs);
        String errorMessage = "execute update failed: integrity constraint violation: unique constraint or index "
                + "violation";
        if (dbType == MYSQL) {
            errorMessage = "execute update failed: Duplicate entry '1' for key 'PRIMARY'";
        } else if (dbType == POSTGRES) {
            errorMessage = "{message:\"execute update failed: ERROR: duplicate key value violates unique constraint "
                    + "\"employeeaddnegative_pkey\"\n"
                    + "  Detail: Key (id)=(1) already exists.\", cause:null}";
        }
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0].stringValue().contains(errorMessage));
    }

    @Test(groups = "ConnectorTest", description = "Test adding data to mirrored table")
    public void testAddToMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testAddToMirrorTable", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "{id:1, name:\"Manuri\", address:\"Sri Lanka\"}");
        Assert.assertEquals(returns[1].stringValue(), "{id:2, name:\"Devni\", address:\"Sri Lanka\"}");
    }

    @Test(groups = "ConnectorTest", description = "Test deleting data from mirrored table")
    public void testDeleteFromMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invoke(resultMirror, "testDeleteFromMirrorTable", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a mirrored table multiple times")
    public void testIterateMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(resultMirror, "testIterateMirrorTable", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}])");
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a mirrored table after closing")
    public void testIterateMirrorTableAfterClose() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(resultMirror, "testIterateMirrorTableAfterClose", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}], {message:\"Trying to perform hasNext operation over a "
                + "closed table\", cause:null})");
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a table loaded to memory multiple times")
    public void testSelectLoadToMemory() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelectLoadToMemory", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2,"
                + " name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}])");
    }

    @Test(groups = "ConnectorTest", description = "Test iterating data of a table loaded to memory after closing")
    public void testLoadToMemorySelectAfterTableClose() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLoadToMemorySelectAfterTableClose", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{id:1, name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, "
                + "name:\"Devni\", address:\"Sri Lanka\"}, {id:3, name:\"Thurani\", address:\"Sri Lanka\"}], [{id:1, "
                + "name:\"Manuri\", address:\"Sri Lanka\"}, {id:2, name:\"Devni\", address:\"Sri Lanka\"}, {id:3, "
                + "name:\"Thurani\", address:\"Sri Lanka\"}], {message:\"Trying to perform hasNext operation over a "
                + "closed table\", cause:null})");
    }

    @AfterSuite
    public void cleanup() {
        switch (dbType) {
        case HSQLDB:
            SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
            break;
        case MYSQL:
            if (mysql != null) {
                mysql.stop();
            }
            break;
        case POSTGRES:
            if (postgres != null) {
                postgres.stop();
            }
            break;
        }
    }

    enum DBType {
        MYSQL, ORACLE, POSTGRES, HSQLDB
    }
}
