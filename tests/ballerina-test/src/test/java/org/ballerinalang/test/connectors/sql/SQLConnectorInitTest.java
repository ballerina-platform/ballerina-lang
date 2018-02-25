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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test SQL Connector Initialization.
 */
public class SQLConnectorInitTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR_INIT";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/connectors/sql/sql-connector-init.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/sql/SQLTableCreate.sql");
    }

    @Test
    public void testConnectorWithDefaultPropertiesForListedDB() {
         BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDefaultPropertiesForListedDB");
         BString retValue = (BString) returns[0];
         final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
         Assert.assertEquals(retValue.stringValue(), expected);
     }

    @Test
    public void testConnectorWithDirectUrl() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDirectUrl");
        BString retValue = (BString) returns[0];
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClass() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClass");
        BString retValue = (BString) returns[0];
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassAndProps() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClassAndProps");
        BString retValue = (BString) returns[0];
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassWithoutURL() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClassWithoutURL");
        BString retValue = (BString) returns[0];
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassURLPriority() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClassURLPriority");
        BString retValue = (BString) returns[0];
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectionPoolProperties");
        BString retValue = (BString) returns[0];
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(retValue.stringValue(), expected);
    }


    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
