/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql.connection;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

/**
 * Test SQL sample client initialization.
 *
 * @since 1.3.0
 */
public class ConnectorInitTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR_INIT";
    private static final String URL = SQLDBUtils.URL_PREFIX + DB_NAME;
    private BValue[] args = {new BString(URL), new BString(SQLDBUtils.DB_USER), new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compile(SQLDBUtils.getMockModuleDir(), "connection");
        SQLDBUtils.initHsqlDatabase(DB_NAME, SQLDBUtils.getSQLResourceDir("connection",
                "connector-init-test-data.sql"));
    }

    @Test
    public void testConnection1() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testConnection1", args);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testConnection2() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testConnection2", args);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testConnectionNoUserPassword() {
        BValue[] args = {new BString(URL)};
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testConnectionNoUserPassword", args);
        Assert.assertTrue(returnVal[0] instanceof BError);
    }

    @Test
    public void testConnectionWithValidDriver() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testConnectionWithValidDriver", args);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testConnectionWithInvalidDriver() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testConnectionWithInvalidDriver", args);
        Assert.assertTrue(returnVal[0] instanceof BError);
    }

    @Test
    public void testConnectionWithDatasourceOptions() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testConnectionWithDatasourceOptions", args);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testConnectionWithDatasourceInvalidProperty() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testConnectionWithDatasourceInvalidProperty", args);
        Assert.assertTrue(returnVal[0] instanceof BError);
        BError error = (BError) returnVal[0];
        Assert.assertTrue(error.getMessage().contains("Property invalidProperty does not exist on target class"));
    }

    @Test
    public void testWithConnectionPool() {
        BValue[] returnVal = BRunUtil.invoke(result, "testWithConnectionPool", args);
        Assert.assertFalse(returnVal[0] instanceof BError);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        BMap connectionPool = (BMap) returnVal[0];
        Assert.assertEquals(connectionPool.get(
                Constants.ConnectionPool.MAX_CONNECTION_LIFE_TIME_SECONDS.getValue()).stringValue(), "1800");
        Assert.assertEquals(connectionPool.get(
                Constants.ConnectionPool.MAX_OPEN_CONNECTIONS.getValue()).stringValue(), "25");
    }

    @Test
    public void testWithSharedConnPool() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testWithSharedConnPool", args);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testWithAllParams() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testWithAllParams", args);
        Assert.assertNull(returnVal[0]);
    }
}
