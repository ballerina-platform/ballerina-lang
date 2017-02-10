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

package org.wso2.ballerina.nativeimpl.connectors;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;
import org.wso2.ballerina.nativeimpl.util.SQLDBUtils;

import java.io.File;

/**
 * Test class for SQL Connector test.
 */
public class SQLConnectorTest {

    private BallerinaFile bFile;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";

    @BeforeClass()
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/sqlConnectorTest.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/SQLConnectorDataFile.sql");
    }

    @Test
    public void testInsertTableData() {
        BValue[] returns = Functions.invoke(bFile, "testInsertTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test
    public void testCreateTable() {
        BValue[] returns = Functions.invoke(bFile, "testCreateTable");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 0);
    }

    @Test
    public void testUpdateTableData() {
        BValue[] returns = Functions.invoke(bFile, "testUpdateTableData");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = Functions.invoke(bFile, "testGeneratedKeyOnInsert");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(retValue.intValue() > 0);
    }

    @Test
    public void testGeneratedKeyWithColumn() {
        BValue[] returns = Functions.invoke(bFile, "testGeneratedKeyWithColumn");
        BString retValue = (BString) returns[0];
        Assert.assertTrue(retValue.intValue() > 0);
    }

    @Test
    public void testSelectData() {
        BValue[] returns = Functions.invoke(bFile, "testSelectData");
        BString retValue = (BString) returns[0];

        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testCallProcedure() {
        BValue[] returns = Functions.invoke(bFile, "testCallProcedure");
        BString retValue = (BString) returns[0];

        final String expected = "James";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSource() {
        BValue[] returns = Functions.invoke(bFile, "testConnectorWithDataSource");
        BString retValue = (BString) returns[0];

        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties() {
        BValue[] returns = Functions.invoke(bFile, "testConnectionPoolProperties");
        BString retValue = (BString) returns[0];

        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
