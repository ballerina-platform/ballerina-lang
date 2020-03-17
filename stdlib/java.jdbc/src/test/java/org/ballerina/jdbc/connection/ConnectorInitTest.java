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
package org.ballerina.jdbc.connection;

import org.ballerina.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

/**
 * Test JDBC Client Initialization.
 */
public class ConnectorInitTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR_INIT";

    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(Paths.get("test-src", "connection", "connector_init_test.bal").toString());
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, DB_NAME,
                Paths.get("datafiles", "sql", "connection", "connector_init_test_data.sql").toString());
    }

    @Test
    public void testConnectorWithDefaultPropertiesForListedDB() {
         BValue[] returns = BRunUtil.invokeFunction(result, "testConnectorWithDefaultPropertiesForListedDB", args);
         final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
         Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithWorkers() {
         BValue[] returns = BRunUtil.invokeFunction(result, "testConnectorWithWorkers", args);
         final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
         Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClass() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testConnectorWithDataSourceClass", args);
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassAndProps() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testConnectorWithDataSourceClassAndProps", args);
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassWithoutURL() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testConnectorWithDataSourceClassWithoutURL", args);
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectorWithDataSourceClassURLPriority() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testConnectorWithDataSourceClassURLPriority", args);
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties1() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testConnectionPoolProperties1", args);
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testConnectionPoolProperties2", args);
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testConnectionPoolProperties3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testConnectionPoolProperties3", args);
        final String expected = "[{\"FIRSTNAME\":\"Peter\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*error in sql connector configuration: Failed to initialize pool: Database "
                  + "\".*/target/tempdb/NON_EXISTING_DB\" not found.*")
    public void testConnectionFailure() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "NON_EXISTING_DB";
        BValue[] arg = { new BString(jdbcURL) };
        BRunUtil.invokeFunction(result, "testConnectionFailure", arg);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid JDBC URL: .*")
    public void testInvalidJdbcUrl1() {
        BRunUtil.invokeFunction(result, "testInvalidJdbcUrl1");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid JDBC URL: localhost:3306/testdb.*")
    public void testInvalidJdbcUrl2() {
        BRunUtil.invokeFunction(result, "testInvalidJdbcUrl2");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid JDBC URL: jdbc://dbhost.com/testdb.*")
    public void testInvalidJdbcUrl3() {
        BRunUtil.invokeFunction(result, "testInvalidJdbcUrl3");
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
