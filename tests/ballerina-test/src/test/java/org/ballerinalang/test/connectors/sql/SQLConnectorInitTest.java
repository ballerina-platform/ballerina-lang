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
        result = BCompileUtil.compile("test-src/connectors/sql/sql_connector_init_test.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/sql/SQLTableCreate.sql");
    }

    @Test
    public void testConnectorWithDefaultPropertiesForListedDB() {
         BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDefaultPropertiesForListedDB");
         final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
         Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithWorkers() {
         BValue[] returns = BRunUtil.invoke(result, "testConnectorWithWorkers");
         final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
         Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClass() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClass");
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassAndProps() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClassAndProps");
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassWithoutURL() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClassWithoutURL");
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassURLPriority() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorWithDataSourceClassURLPriority");
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties1() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectionPoolProperties1");
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties2() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectionPoolProperties2");
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties3() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectionPoolProperties3");
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }


    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
