/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.connectors;

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.nativeimpl.util.SQLDBUtils;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for SQL Connector test.
 *
 * @since 0.8.0
 */
public class SQLConnectorTest {

    BLangProgram bLangProgram;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";

    @BeforeClass()
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/sqlConnectorTest.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/SQLConnectorDataFile.sql");
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableData() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testInsertTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testCreateTable() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testCreateTable");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 0);
    }

    @Test(groups = "ConnectorTest")
    public void testUpdateTableData() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGeneratedKeyOnInsert");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(retValue.intValue() > 0);
    }

    @Test(groups = "ConnectorTest")
    public void testGeneratedKeyWithColumn() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGeneratedKeyWithColumn");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(retValue.intValue() > 0);
    }

    @Test(groups = "ConnectorTest")
    public void testSelectData() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSelectData");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testCallProcedure() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testCallProcedure");
        BString retValue = (BString) returns[0];
        final String expected = "James";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testCallProcedureWithResultSet() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testCallProcedureWithResultSet");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testConnectorWithDataSource() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testConnectorWithDataSource");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testConnectionPoolProperties() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testConnectionPoolProperties");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testQueryParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testQueryParameters");
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest")
    public void testInsertTableDataWithParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testInsertTableDataWithParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testOutParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testOutParameters");
        Assert.assertEquals(returns.length, 14);
        Assert.assertEquals(((BString) returns[0]).intValue(), 10);
        Assert.assertEquals(((BString) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BString) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BString) returns[3]).doubleValue(), 2139095039D);
        Assert.assertEquals(Boolean.parseBoolean(returns[4].stringValue()), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BString) returns[6]).doubleValue(), 1234.567D);
        Assert.assertEquals(((BString) returns[7]).doubleValue(), 1234.567D);
        Assert.assertEquals(((BString) returns[8]).doubleValue(), 1234.567D);
        Assert.assertEquals(((BString) returns[9]).intValue(), 1);
        Assert.assertEquals(((BString) returns[10]).intValue(), 5555);
        Assert.assertEquals(returns[11].stringValue(), "very long text");
        Assert.assertEquals(returns[12].stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals(returns[13].stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = "ConnectorTest")
    public void testNullOutParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullOutParameters");
        Assert.assertEquals(returns.length, 14);
        Assert.assertEquals(((BString) returns[0]).intValue(), 0);
        Assert.assertEquals(((BString) returns[1]).longValue(), 0);
        Assert.assertEquals(((BString) returns[2]).floatValue(), 0.0f);
        Assert.assertEquals(((BString) returns[3]).doubleValue(), 0.0D);
        Assert.assertEquals(Boolean.parseBoolean(returns[4].stringValue()), false);
        Assert.assertEquals(returns[5].stringValue(), "");
        Assert.assertEquals(returns[6].stringValue(), "");
        Assert.assertEquals(returns[6].stringValue(), "");
        Assert.assertEquals(((BString) returns[8]).doubleValue(), 0.0D);
        Assert.assertEquals(((BString) returns[9]).intValue(), 0);
        Assert.assertEquals(((BString) returns[10]).intValue(), 0);
        Assert.assertEquals(returns[11].stringValue(), "");
        Assert.assertEquals(returns[12].stringValue(), "");
        Assert.assertEquals(returns[13].stringValue(), "");
    }

    @Test(groups = "ConnectorTest")
    public void testINParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testINParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testNullINParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullINParameters");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "ConnectorTest")
    public void testINOutParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testINOutParameters");
        Assert.assertEquals(returns.length, 14);
        Assert.assertEquals(((BString) returns[0]).intValue(), 10);
        Assert.assertEquals(((BString) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BString) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BString) returns[3]).doubleValue(), 2139095039D);
        Assert.assertEquals(Boolean.parseBoolean(returns[4].stringValue()), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BString) returns[6]).doubleValue(), 1234.567D);
        Assert.assertEquals(((BString) returns[7]).doubleValue(), 1234.567D);
        Assert.assertEquals(((BString) returns[8]).doubleValue(), 1234.567D);
        Assert.assertEquals(((BString) returns[9]).intValue(), 1);
        Assert.assertEquals(((BString) returns[10]).intValue(), 5555);
        Assert.assertEquals(returns[11].stringValue(), "very long text");
        Assert.assertEquals(returns[12].stringValue(), "YmxvYiBkYXRh");
        Assert.assertEquals(returns[13].stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = "ConnectorTest")
    public void testNullINOutParameters() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullINOutParameters");
        Assert.assertEquals(returns.length, 12);
        Assert.assertEquals(((BString) returns[0]).intValue(), 0);
        Assert.assertEquals(((BString) returns[1]).longValue(), 0);
        Assert.assertEquals(((BString) returns[2]).floatValue(), 0.0f);
        Assert.assertEquals(((BString) returns[3]).doubleValue(), 0.0D);
        Assert.assertEquals(Boolean.parseBoolean(returns[4].stringValue()), false);
        Assert.assertEquals(returns[5].stringValue(), "");
        Assert.assertEquals(returns[6].stringValue(), "");
        Assert.assertEquals(returns[6].stringValue(), "");
        Assert.assertEquals(((BString) returns[8]).doubleValue(), 0.0D);
        Assert.assertEquals(((BString) returns[9]).intValue(), 0);
        Assert.assertEquals(((BString) returns[10]).intValue(), 0);
        Assert.assertEquals(returns[11].stringValue(), "");
    }

    @Test(groups = "ConnectorTest")
    public void testEmptySQLType() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testEmptySQLType");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(dependsOnGroups = "ConnectorTest")
    public void testCloseConnectionPool() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testCloseConnectionPool");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
