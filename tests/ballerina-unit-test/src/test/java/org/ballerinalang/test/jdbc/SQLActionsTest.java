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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.test.utils.SQLDBUtils.ContainerizedTestDatabase;
import org.ballerinalang.test.utils.SQLDBUtils.DBType;
import org.ballerinalang.test.utils.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinalang.test.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Calendar;

import static org.ballerinalang.test.utils.SQLDBUtils.DBType.H2;
import static org.ballerinalang.test.utils.SQLDBUtils.DBType.HSQLDB;
import static org.ballerinalang.test.utils.SQLDBUtils.DBType.MYSQL;
import static org.ballerinalang.test.utils.SQLDBUtils.DBType.POSTGRES;

/**
 * Test class for SQL Connector actions test.
 *
 * @since 0.8.0
 */
public class SQLActionsTest {

    private static final double DELTA = 0.01;
    private CompileResult result;
    private CompileResult resultNegative;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";
    private static final String DB_NAME_H2 = "TEST_SQL_CONNECTOR_H2";
    private static final String DB_DIRECTORY_H2 = "./target/H2Client/";
    private DBType dbType;
    private TestDatabase testDatabase;
    private BValue[] connectionArgs = new BValue[3];
    private static final String CONNECTOR_TEST = "ConnectorTest";
    private static final String H2_NOT_SUPPORTED = "H2NotSupported";
    private static final String MYSQL_NOT_SUPPORTED = "MySQLNotSupported";
    private static final String POSTGRES_NOT_SUPPORTED = "PostgresNotSupported";
    private static final String HSQLDB_NOT_SUPPORTED = "HSQLDBNotSupported";

    @Parameters({ "dataClientTestDBType" })
    public SQLActionsTest(@Optional("HSQLDB") DBType dataClientTestDBType) {
        this.dbType = dataClientTestDBType;
    }

    @BeforeClass
    public void setup() {
        switch (dbType) {
        case MYSQL:
            testDatabase = new ContainerizedTestDatabase(dbType, "datafiles/sql/SQLTest_Mysql_Data.sql");
            break;
        case POSTGRES:
            testDatabase = new ContainerizedTestDatabase(dbType, "datafiles/sql/SQLTest_Postgres_Data.sql");
            break;
        case H2:
            testDatabase = new FileBasedTestDatabase(dbType, "datafiles/sql/SQLTest_H2_Data.sql",
                    DB_DIRECTORY_H2, DB_NAME_H2);
            break;
        case HSQLDB:
            testDatabase = new FileBasedTestDatabase(dbType, "datafiles/sql/SQLTest_HSQL_Data.sql",
                    SQLDBUtils.DB_DIRECTORY, DB_NAME);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }

        connectionArgs[0] = new BString(testDatabase.getJDBCUrl());
        connectionArgs[1] = new BString(testDatabase.getUsername());
        connectionArgs[2] = new BString(testDatabase.getPassword());

        result = BCompileUtil.compile("test-src/jdbc/sql_actions_test.bal");
        resultNegative = BCompileUtil.compile("test-src/jdbc/sql_actions_negative_test.bal");
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableData", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testCreateTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateTable", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testUpdateTableData() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateTableData", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsert", connectionArgs);
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testGeneratedKeyOnInsertEmptyResults() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsertEmptyResults", connectionArgs);
        BInteger retValue = (BInteger) returns[0];

        // Postgres returns all columns when there is no returning clause
        int columnCount = (dbType == POSTGRES) ? 5 : 0;
        Assert.assertEquals(retValue.intValue(), columnCount);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testGeneratedKeyWithColumn() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyWithColumn", connectionArgs);
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test(groups = CONNECTOR_TEST)
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

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testCallProcedure() {
        BValue[] returns = BRunUtil.invoke(result, "testCallProcedure", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "James";
        Assert.assertEquals(retValue.stringValue(), expected);
        Assert.assertEquals(returns[1].stringValue(), "nil");
    }

    @Test(groups = {CONNECTOR_TEST, MYSQL_NOT_SUPPORTED, POSTGRES_NOT_SUPPORTED, H2_NOT_SUPPORTED})
    public void testCallProcedureWithResultSet() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testCallProcedureWithResultSet", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = {CONNECTOR_TEST, MYSQL_NOT_SUPPORTED, HSQLDB_NOT_SUPPORTED, H2_NOT_SUPPORTED})
    public void testCallFunctionWithRefCursor() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testCallFunctionWithReturningRefcursor", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = {CONNECTOR_TEST, MYSQL_NOT_SUPPORTED, POSTGRES_NOT_SUPPORTED, H2_NOT_SUPPORTED})
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

    @Test(groups = {CONNECTOR_TEST, POSTGRES_NOT_SUPPORTED, H2_NOT_SUPPORTED})
    public void testCallProcedureWithMultipleResultSetsAndLowerConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndLowerConstraintCount",
                        connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 1 and returned result set count: 2 from the stored procedure\""));
    }

    @Test(groups = {CONNECTOR_TEST, POSTGRES_NOT_SUPPORTED, H2_NOT_SUPPORTED})
    public void testCallProcedureWithMultipleResultSetsAndNilConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndNilConstraintCount", connectionArgs);
        Assert.assertEquals(returns[0].stringValue(), "nil");
    }

    @Test(groups = {CONNECTOR_TEST, POSTGRES_NOT_SUPPORTED, H2_NOT_SUPPORTED})
    public void testCallProcedureWithMultipleResultSetsAndHigherConstraintCount() {
        BValue[] returns = BRunUtil
                .invoke(resultNegative, "testCallProcedureWithMultipleResultSetsAndHigherConstraintCount",
                        connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("message:\"execute stored procedure failed: Mismatching "
                + "record type count: 3 and returned result set count: 2 from the stored procedure\""));
    }

    @Test(groups = CONNECTOR_TEST)
    public void testQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testQueryParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testQueryParameters2", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableDataWithParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableDataWithParameters2() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters2", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTableDataWithParameters3() {
        BValue[] returns = BRunUtil.invoke(result, "testInsertTableDataWithParameters3", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayofQueryParameters", connectionArgs);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBoolArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testBoolArrayofQueryParameters", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 10);
    }

    // This is rather doesn't make sense to test for postgresql than not being supported. Because, in official
    // postgresql driver when setting a blob value while preparing a statement, an OID is created so as it will
    // always be a new one, IN clause would never be evaluated to true
    @Test(groups = {CONNECTOR_TEST, POSTGRES_NOT_SUPPORTED})
    public void testBlobArrayOfQueryParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobArrayQueryParameter", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 7);
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testOutParameters", connectionArgs);
        Assert.assertEquals(returns.length, 13);
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
        Assert.assertEquals(returns[12].stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED, POSTGRES_NOT_SUPPORTED})
    public void testBlobOutInOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobOutInOutParameters", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals(returns[1].stringValue(), "YmxvYiBkYXRh");
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testNullOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testNullOutParameters", connectionArgs);
        Assert.assertEquals(returns.length, 13);
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
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testINParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testINParameters", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testBlobInParameter() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobInParameter", connectionArgs);
        BInteger retInt = (BInteger) returns[0];
        BByteArray retBytes = (BByteArray) returns[1];
        Assert.assertEquals(retInt.intValue(), 1);
        Assert.assertEquals(new String(retBytes.getBytes()), "blob data");
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
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
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
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
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testNullINParameterValues() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINParameterValues", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testNullINParameterBlobValue() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINParameterBlobValue", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testINOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testINOutParameters", connectionArgs);
        Assert.assertEquals(returns.length, 13);
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
        Assert.assertEquals(returns[12].stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
    public void testNullINOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testNullINOutParameters", connectionArgs);
        Assert.assertEquals(returns.length, 13);
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
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED, POSTGRES_NOT_SUPPORTED})
    public void testNullOutInOutBlobParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testNullOutInOutBlobParameters", connectionArgs);
        Assert.assertEquals(returns[0].stringValue(), null);
        Assert.assertEquals(returns[1].stringValue(), null);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testEmptySQLType() {
        BValue[] returns = BRunUtil.invoke(result, "testEmptySQLType", connectionArgs);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = {CONNECTOR_TEST, MYSQL_NOT_SUPPORTED, H2_NOT_SUPPORTED})
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

    @Test(groups = {CONNECTOR_TEST, MYSQL_NOT_SUPPORTED, H2_NOT_SUPPORTED})
    public void testArrayOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayOutParameters", connectionArgs);
        Assert.assertEquals(returns[0].stringValue(), "[1,2,3]");
        Assert.assertEquals(returns[1].stringValue(), "[100000000,200000000,300000000]");
        Assert.assertEquals(returns[2].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[3].stringValue(), "[245.23,5559.49,8796.123]");
        Assert.assertEquals(returns[4].stringValue(), "[true,false,true]");
        Assert.assertEquals(returns[5].stringValue(), "[Hello,Ballerina]");
    }

    @Test(groups = {CONNECTOR_TEST, MYSQL_NOT_SUPPORTED, H2_NOT_SUPPORTED})
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

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithValues() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithValues", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = CONNECTOR_TEST, description = "Test batch update operation with variable parameters")
    public void testBatchUpdateWithVariables() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithVariables", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithFailure() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithFailure", connectionArgs);
        // This is the one after the failing batch. Depending on the driver this may or may not be executed hence the
        // result could be either 1 or -3
        int[] expectedResult = {1, 1, -3, -3};
        if (dbType == MYSQL || dbType == H2) {
            expectedResult[3] = 1;
        } else if (dbType == POSTGRES) {
            expectedResult[0] = -3;
            expectedResult[1] = -3;
        }
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), expectedResult[0]);
        Assert.assertEquals(retValue.get(1), expectedResult[1]);
        Assert.assertEquals(retValue.get(2), expectedResult[2]);
        Assert.assertEquals(retValue.get(3), expectedResult[3]);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testBatchUpdateWithNullParam() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdateWithNullParam", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
    }

    @Test(groups = CONNECTOR_TEST)
    public void testInsertTimeData() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeInParameters", connectionArgs);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals((int) retValue.get(0), 1);
        Assert.assertEquals((int) retValue.get(1), 1);
        Assert.assertEquals((int) retValue.get(2), 1);
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED})
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

    @Test(groups = CONNECTOR_TEST, description = "Check date time null in values")
    public void testDateTimeNullInValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullInValues", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"DATE_TYPE\":null, \"TIME_TYPE\":null, "
                + "\"TIMESTAMP_TYPE\":null, \"DATETIME_TYPE\":null}]");
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED}, description = "Check date time null out values")
    public void testDateTimeNullOutValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullOutValues", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED}, description = "Check date time null inout values")
    public void testDateTimeNullInOutValues() {
        BValue[] returns = BRunUtil.invoke(result, "testDateTimeNullInOutValues", connectionArgs);
        Assert.assertEquals(returns.length, 4);
        Assert.assertNull(returns[0].stringValue());
        Assert.assertNull(returns[1].stringValue());
        Assert.assertNull(returns[2].stringValue());
        Assert.assertNull(returns[3].stringValue());
    }

    @Test(groups = {CONNECTOR_TEST, MYSQL_NOT_SUPPORTED, H2_NOT_SUPPORTED, POSTGRES_NOT_SUPPORTED})
    public void testStructOutParameters() {
        BValue[] returns = BRunUtil.invoke(result, "testStructOutParameters", connectionArgs);
        BString retValue = (BString) returns[0];
        String expected = "10";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(dependsOnGroups = CONNECTOR_TEST)
    public void testCloseConnectionPool() {
        BValue connectionCountQuery;
        if (dbType == MYSQL) {
            connectionCountQuery = new BString("SELECT COUNT(*) FROM information_schema.PROCESSLIST");
        } else if (dbType == H2) {
            connectionCountQuery = new BString("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS");
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

    @Test(groups = CONNECTOR_TEST, description = "Check blob binary and clob types types.")
    public void testComplexTypeRetrieval() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeRetrieval", connectionArgs);
        String expected0, expected1, expected2, expected3;
        if (dbType == MYSQL) {
            expected0 = "<results><result><row_id>1</row_id>"
                    + "<blob_type>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</blob_type>"
                    + "</result></results>";
            expected1 = "<results><result><row_id>1</row_id>"
                    + "<date_type>2017-02-03</date_type><time_type>11:35:45</time_type>"
                    + "<datetime_type>2017-02-03 11:53:00.0</datetime_type>"
                    + "<timestamp_type>2017-02-03 11:53:00.0</timestamp_type></result></results>";
            expected2 = "[{\"row_id\":1, \"blob_type\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\"}]";
            expected3 = "[{\"row_id\":1, \"date_type\":\"2017-02-03\", \"time_type\":\"11:35:45\", "
                    + "\"datetime_type\":\"2017-02-03 11:53:00.0\", \"timestamp_type\":\"2017-02-03 11:53:00.0\"}]";
        } else if (dbType == H2) {
            expected0 = "<results><result><ROW_ID>1</ROW_ID><BLOB_TYPE>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</BLOB_TYPE>"
                    + "</result></results>";
            expected1 = "<results><result><ROW_ID>1</ROW_ID><DATE_TYPE>2017-02-03</DATE_TYPE>"
                    + "<TIME_TYPE>11:35:45</TIME_TYPE><DATETIME_TYPE>2017-02-03 11:53:00</DATETIME_TYPE>"
                    + "<TIMESTAMP_TYPE>2017-02-03 11:53:00</TIMESTAMP_TYPE></result></results>";
            expected2 = "[{\"ROW_ID\":1, \"BLOB_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\"}]";
            expected3 = "[{\"ROW_ID\":1, \"DATE_TYPE\":\"2017-02-03\",\"TIME_TYPE\":\"11:35:45\", "
                    + "\"DATETIME_TYPE\":\"2017-02-03 11:53:00\", \"TIMESTAMP_TYPE\":\"2017-02-03 11:53:00\"}]";
        } else if (dbType == POSTGRES) {
            // When retrieving value from OID column postgres driver supports both getLong and getBlob.
            // In table -> JSON/XML conversion of Ballerina data client implementation, the OID is returned ATM.
            // However, when mapping a result set to a record, depending on whether the field type is int or byte[]
            // getBlob/getLong are called appropriately.
            expected0 = "<results><result><row_id>1</row_id><blob_type>16458</blob_type></result></results>";
            expected1 = "<results><result><row_id>1</row_id><date_type>2017-02-03 "
                    + "00:00:00</date_type><time_type>11:35:45</time_type><datetime_type>2017-02-03 "
                    + "11:53:00</datetime_type><timestamp_type>2017-02-03 "
                    + "11:53:00+05:30</timestamp_type></result></results>";
            expected2 = "[{\"row_id\":1, \"blob_type\":16458}]";
            expected3 = "[{\"row_id\":1, \"date_type\":\"2017-02-03 00:00:00\", \"time_type\":\"11:35:45\", "
                    + "\"datetime_type\":\"2017-02-03 11:53:00\", \"timestamp_type\":\"2017-02-03 11:53:00+05:30\"}]";
        } else {
            expected0 = "<results><result><ROW_ID>1</ROW_ID><BLOB_TYPE>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</BLOB_TYPE>"
                    + "</result></results>";
            expected1 = "<results><result><ROW_ID>1</ROW_ID>"
                    + "<DATE_TYPE>2017-02-03</DATE_TYPE><TIME_TYPE>11:35:45</TIME_TYPE>"
                    + "<DATETIME_TYPE>2017-02-03 11:53:00.000000</DATETIME_TYPE>"
                    + "<TIMESTAMP_TYPE>2017-02-03 11:53:00.000000</TIMESTAMP_TYPE></result></results>";
            expected2 = "[{\"ROW_ID\":1, \"BLOB_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\"}]";
            expected3 = "[{\"ROW_ID\":1, \"DATE_TYPE\":\"2017-02-03\", "
                    + "\"TIME_TYPE\":\"11:35:45\", \"DATETIME_TYPE\":\"2017-02-03 11:53:00.000000\", "
                    + "\"TIMESTAMP_TYPE\":\"2017-02-03 11:53:00.000000\"}]";
        }

        Assert.assertEquals(returns[0].stringValue(), expected0);
        Assert.assertEquals(returns[1].stringValue(), expected1);
        Assert.assertEquals(returns[2].stringValue(), expected2);
        Assert.assertEquals(returns[3].stringValue(), expected3);
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed select query")
    public void testFailedSelect() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testSelectData", connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("execute query failed:"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed update with generated id action")
    public void testFailedGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testGeneratedKeyOnInsert", connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("execute update with generated keys failed:"));
    }

    @Test(groups = {CONNECTOR_TEST, H2_NOT_SUPPORTED}, description = "Test failed call procedure")
    public void testFailedCallProcedure() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testCallProcedure", connectionArgs);
        Assert.assertTrue(returns[0].stringValue().contains("execute stored procedure failed:"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed batch update")
    public void testFailedBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testBatchUpdate", connectionArgs);
        if (dbType == HSQLDB || dbType == H2) {
            Assert.assertTrue(returns[0].stringValue().contains("execute batch update failed:"));
        } else {
            Assert.assertTrue(returns[0].stringValue().contains("failure"));
        }
    }

    @Test(groups = CONNECTOR_TEST, description = "Test failed parameter array update")
    public void testInvalidArrayofQueryParameters() {
        BValue[] returns = BRunUtil.invoke(resultNegative, "testInvalidArrayofQueryParameters", connectionArgs);
        Assert.assertTrue(returns[0].stringValue()
                .contains("execute query failed: unsupported array type for parameter index 0"));
    }

    @Test(groups = CONNECTOR_TEST, description = "Test iterating data of a table loaded to memory multiple times")
    public void testSelectLoadToMemory() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelectLoadToMemory", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "([{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, "
                + "{FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}])");
    }

    @Test(groups = CONNECTOR_TEST, description = "Test iterating data of a table loaded to memory after closing")
    public void testLoadToMemorySelectAfterTableClose() throws Exception {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLoadToMemorySelectAfterTableClose", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "("
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "[{FIRSTNAME:\"Peter\", LASTNAME:\"Stuart\"}, {FIRSTNAME:\"John\", LASTNAME:\"Watson\"}], "
                + "{message:\"Trying to perform hasNext operation over a closed table\", cause:null})");
    }

    @Test(groups = CONNECTOR_TEST, description = "Test re-init endpoint")
    public void testReInitEndpoint() {
        TestDatabase testDatabase2;
        String validationQuery;

        switch (dbType) {
        case MYSQL:
            testDatabase2 = new ContainerizedTestDatabase(dbType);
            validationQuery = "SELECT 1";
            break;
        case POSTGRES:
            testDatabase2 = new ContainerizedTestDatabase(dbType);
            validationQuery = "SELECT 1";
            break;
        case H2:
            testDatabase2 = new FileBasedTestDatabase(dbType,
                    "." + File.separator + "target" + File.separator + "H2Client2" + File.separator,
                    "TEST_SQL_CONNECTOR_H2_2");
            validationQuery = "SELECT 1";
            break;
        case HSQLDB:
            testDatabase2 = new FileBasedTestDatabase(dbType,
                    "." + File.separator + "target" + File.separator + "HSQLDBClient2" + File.separator,
                    "TEST_SQL_CONNECTOR_2");
            validationQuery = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
            break;
        default:
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }

        BValue[] args = new BValue[5];
        System.arraycopy(connectionArgs, 0, args, 0, 3);
        args[3] = new BString(testDatabase2.getJDBCUrl());
        args[4] = new BString(validationQuery);

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
