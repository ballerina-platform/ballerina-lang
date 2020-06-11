/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jdbc.pool;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * This validates the functionality of the connection pool.
 *
 * @since 1.2.0
 */

public class ConnectionPoolTest {
    private CompileResult result;
    private static final String DB_NAME1 = "TEST_SQL_CONN_POOL_1";
    private static final String JDBC_URL1 = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME1;
    private static final String DB_NAME2 = "TEST_SQL_CONN_POOL_2";
    private static final String JDBC_URL2 = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME2;
    private static final String CONNECTION_TIMEOUT_ERROR_STRING =
            "Connection is not available, request timed out after";

    @BeforeClass
    public void setup() throws IOException, SQLException {
        String poolSubDir = "pool";
        Path ballerinaConfPath = SQLDBUtils.getResourcePath("ballerina.conf");
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir(poolSubDir,
                "connection-pool-test.bal"));

        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME1);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME1,
                SQLDBUtils.getSQLResourceDir(poolSubDir, "connection-pool-test-data.sql"));

        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME2);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME2,
                SQLDBUtils.getSQLResourceDir(poolSubDir, "connection-pool-test-data.sql"));
    }

    @Test
    public void testGlobalConnectionPoolSingleDestination() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolSingleDestination",
                args);
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertEquals(returnVal[0].size(), 11, "11 elements are expected as a return value");
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals((((BValueArray) returnVal[0])).getRefValue(i).stringValue(), "1");
        }
        String error = (((BValueArray) returnVal[0])).getRefValue(10).stringValue();
        Assert.assertTrue(error.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error);
    }

    @Test
    public void testGlobalConnectionPoolsMultipleDestinations() {
        BValue[] args = {new BString(JDBC_URL1), new BString(JDBC_URL2)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolsMultipleDestinations"
                , args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].size(), 2);
        BValueArray returnVal1 = ((BValueArray) (((BValueArray) returns[0]).getRefValue(0)));
        BValueArray returnVal2 = ((BValueArray) (((BValueArray) returns[0]).getRefValue(1)));
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("1", returnVal1.getRefValue(i).stringValue());
        }
        String error1 = returnVal1.getRefValue(10).stringValue();
        Assert.assertTrue(error1.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error1);

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("1", returnVal2.getRefValue(i).stringValue());
        }
        String error2 = returnVal2.getRefValue(10).stringValue();
        Assert.assertTrue(error2.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error2);
    }

    @Test
    public void testGlobalConnectionPoolSingleDestinationConcurrent() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil.invokeFunction(result,
                "testGlobalConnectionPoolSingleDestinationConcurrent", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 5; i++) {
            BValueArray array = ((BValueArray) ((BValueArray) returns[0]).getRefValue(i));
            Assert.assertEquals(array.getRefValue(0).stringValue(), "1");
            Assert.assertEquals(array.getRefValue(1).stringValue(), "1");
        }
        BValueArray array = ((BValueArray) ((BValueArray) returns[0]).getRefValue(4));
        String error = array.getRefValue(2).stringValue();
        Assert.assertTrue(error.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error);
    }

    @Test
    public void testLocalSharedConnectionPoolConfigSingleDestination() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolConfigSingleDestination", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("1", (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        String error = (((BValueArray) returns[0])).getRefValue(5).stringValue();
        Assert.assertTrue(error.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error);
    }

    @Test
    public void testLocalSharedConnectionPoolConfigDifferentDbOptions() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolConfigDifferentDbOptions", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            Assert.assertEquals(returnArray.getRefValue(i + 4).stringValue(), "1");
        }
        String error1 = returnArray.getRefValue(3).stringValue();
        Assert.assertTrue(error1.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error1);
        String error2 = returnArray.getRefValue(7).stringValue();
        Assert.assertTrue(error2.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error2);

    }

    @Test
    public void testLocalSharedConnectionPoolConfigMultipleDestinations() {
        BValue[] args = {new BString(JDBC_URL1), new BString(JDBC_URL2)};
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolConfigMultipleDestinations", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            Assert.assertEquals(returnArray.getRefValue(i + 4).stringValue(), "1");
        }
        String error1 = returnArray.getRefValue(3).stringValue();
        Assert.assertTrue(error1.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error1);
        String error2 = returnArray.getRefValue(7).stringValue();
        Assert.assertTrue(error2.contains(CONNECTION_TIMEOUT_ERROR_STRING), "Actual Error: " + error2);
    }

    @Test
    public void testLocalSharedConnectionPoolCreateClientAfterShutdown() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolCreateClientAfterShutdown", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];

        for (int i = 0; i < 4; i++) {
            if (i != 2) {
                Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            }
        }
        String error1 = returnArray.getRefValue(2).stringValue();
        Assert.assertTrue(error1.contains("Client is already closed"), "Actual Error: " + error1);
    }

    @Test
    public void testLocalSharedConnectionPoolStopInitInterleave() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolStopInitInterleave",
                args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testShutDownUnsharedLocalConnectionPool() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownUnsharedLocalConnectionPool", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];
        Assert.assertEquals(returnArray.getRefValue(0).stringValue(), "1");
        String error1 = returnArray.getRefValue(1).stringValue();
        Assert.assertTrue(error1.contains("Client is already closed"), "Actual Error: " + error1);
    }

    @Test
    public void testShutDownSharedConnectionPool() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownSharedConnectionPool", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];
        for (int i = 0; i < 5; i++) {
            if (i > 2) {
                String error1 = returnArray.getRefValue(i).stringValue();
                Assert.assertTrue(error1.contains("Client is already closed"), "Actual Error: " + error1);
            } else {
                Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            }
        }
    }

    @Test
    public void testShutDownPoolCorrespondingToASharedPoolConfig() {
        BValue[] args = {new BString(JDBC_URL1), new BString(JDBC_URL2)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownPoolCorrespondingToASharedPoolConfig", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                String error1 = returnArray.getRefValue(i).stringValue();
                Assert.assertTrue(error1.contains("Client is already closed"), "Actual Error: " + error1);
            } else {
                Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            }
        }
    }

    @Test
    public void testStopClientUsingGlobalPool() {
        BValue[] args = {new BString(JDBC_URL1)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testStopClientUsingGlobalPool", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];
        for (int i = 0; i < 2; i++) {
            if (i == 1) {
                String error1 = returnArray.getRefValue(i).stringValue();
                Assert.assertTrue(error1.contains("Client is already closed"), "Actual Error: " + error1);
            } else {
                Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            }
        }
    }

    @Test
    public void testLocalConnectionPoolShutDown() {
        BValue[] args = {new BString(JDBC_URL1), new BString(JDBC_URL2)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalConnectionPoolShutDown", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];
        String connections1 = returnArray.getRefValue(0).stringValue();
        String connections2 = returnArray.getRefValue(1).stringValue();
        Assert.assertTrue(connections1.equalsIgnoreCase(connections2), "Connections are not equal. Connections1: "
                + connections1 + " , connections2: " + connections2);
    }
}
