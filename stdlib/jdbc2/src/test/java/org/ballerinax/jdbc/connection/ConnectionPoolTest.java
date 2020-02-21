/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.jdbc.connection;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Tests scenarios of global pool, local shared pool, local unshared pool usage.
 */
public class ConnectionPoolTest {
    private CompileResult result;
    private static final String POOL_TEST_GROUP = "ConnectionPoolTest";
    // Setting the expected error message to have any 4 digit number since it has been observed on Windows that
    // sometimes the actual timeout value goes a bit beyond(around 2000ms) the expected timeout range (around 1000ms)
    // Asked a question on HikariCP GitHub issues brettwooldridge/HikariCP#1443 for clarification.
    private static final String connectionTimeoutError = ".*Connection is not available, request timed out after "
            + "\\d\\d\\d\\dms.*";

    @BeforeClass
    public void setup() throws Exception {
        Path ballerinaConfPath = Paths.get("src", "test", "resources", "ballerina.conf").toAbsolutePath();
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        result = BCompileUtil.compile(Paths.get("test-src", "connection", "connection_pool_test.bal").toString());
        setupDatabases();
    }

    private void setupDatabases() {
        String globalPoolDb1 = "TEST_SQL_CONNECTION_POOL_GLOBAL_1";
        String globalPoolDb2 = "TEST_SQL_CONNECTION_POOL_GLOBAL_2";

        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), globalPoolDb1);
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), globalPoolDb2);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, globalPoolDb1,
                Paths.get("datafiles", "sql", "connection", "connection_pool_test_data.sql").toString());
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, globalPoolDb2,
                Paths.get("datafiles", "sql", "connection", "connection_pool_test_data.sql").toString());

        for (int i = 1; i <= 9; i++) {
            String db = "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_" + i;
            SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), db);
            SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, db,
                    Paths.get("datafiles", "sql", "connection", "connection_pool_test_data.sql").toString());
        }
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolSingleDestination() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_GLOBAL_1";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolSingleDestination", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("1", (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        String error = (((BValueArray) returns[0])).getRefValue(10).stringValue();
        Assert.assertTrue(error.matches(connectionTimeoutError), "Actual Error: " + error);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolsMultipleDestinations() {
        String jdbcURL1 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_GLOBAL_1";
        String jdbcURL2 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_GLOBAL_2";
        BValue[] args = { new BString(jdbcURL1), new BString(jdbcURL2)};
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolsMultipleDestinations", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].size(), 2);
        BValueArray jsonArray1 = ((BValueArray) (((BValueArray) returns[0]).getRefValue(0)));
        BValueArray jsonArray2 = ((BValueArray) (((BValueArray) returns[0]).getRefValue(1)));
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("1", jsonArray1.getRefValue(i).stringValue());
        }
        String error1 = jsonArray1.getRefValue(10).stringValue();
        Assert.assertTrue(error1.matches(connectionTimeoutError), "Actual Error: " + error1);

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("1", jsonArray2.getRefValue(i).stringValue());
        }
        String error2 = jsonArray2.getRefValue(10).stringValue();
        Assert.assertTrue(error2.matches(connectionTimeoutError), "Actual Error: " + error2);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolSingleDestinationConcurrent() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_GLOBAL_1";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolSingleDestinationConcurrent", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 5; i++) {
            BValueArray array = ((BValueArray) ((BValueArray) returns[0]).getRefValue(i));
            Assert.assertEquals(array.getRefValue(0).stringValue(), "1");
            Assert.assertEquals(array.getRefValue(1).stringValue(), "1");
        }
        BValueArray array = ((BValueArray) ((BValueArray) returns[0]).getRefValue(4));
        String error = array.getRefValue(2).stringValue();
        Assert.assertTrue(error.matches(connectionTimeoutError), "Actual Error: " + error);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolConfigSingleDestination() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolConfigSingleDestination", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("1", (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        String error = (((BValueArray) returns[0])).getRefValue(5).stringValue();
        Assert.assertTrue(error.matches(connectionTimeoutError), "Actual Error: " + error);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolConfigSingleDestinationWithEqualDbOptions() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolConfigSingleDestinationWithEqualDbOptions", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("1", (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        String error = (((BValueArray) returns[0])).getRefValue(5).stringValue();
        Assert.assertTrue(error.matches(connectionTimeoutError), "Actual Error: " + error);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolConfigDifferentDbOptions() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolConfigDifferentDbOptions", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            Assert.assertEquals(returnArray.getRefValue(i + 4).stringValue(), "1");
        }
        String error1 = returnArray.getRefValue(3).stringValue();
        Assert.assertTrue(error1.matches(connectionTimeoutError), "Actual Error: " + error1);
        String error2 = returnArray.getRefValue(7).stringValue();
        Assert.assertTrue(error2.matches(connectionTimeoutError), "Actual Error: " + error2);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolConfigMultipleDestinations() {
        String jdbcURL1 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2";
        String jdbcURL2 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_3";
        BValue[] args = { new BString(jdbcURL1), new BString(jdbcURL2) };
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolConfigMultipleDestinations", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(returnArray.getRefValue(i).stringValue(), "1");
            Assert.assertEquals(returnArray.getRefValue(i + 4).stringValue(), "1");
        }
        String error1 = returnArray.getRefValue(3).stringValue();
        Assert.assertTrue(error1.matches(connectionTimeoutError), "Actual Error: " + error1);
        String error2 = returnArray.getRefValue(7).stringValue();
        Assert.assertTrue(error2.matches(connectionTimeoutError), "Actual Error: " + error2);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownUnsharedLocalConnectionPool() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_6";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownUnsharedLocalConnectionPool", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownSharedConnectionPool() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_7";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownSharedConnectionPool", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(),
                "[1, 1, 1, \"Client has been stopped\", \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownPoolCorrespondingToASharedPoolConfig() {
        String jdbcURL1 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_8";
        String jdbcURL2 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_9";
        BValue[] args = { new BString(jdbcURL1), new BString(jdbcURL2) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownPoolCorrespondingToASharedPoolConfig", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 1, 1, \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolCreateClientAfterShutdown() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_4";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil
                .invokeFunction(result, "testLocalSharedConnectionPoolCreateClientAfterShutdown", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 1, \"Client has been stopped\", 1]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testStopClientUsingGlobalPool() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_GLOBAL_1";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testStopClientUsingGlobalPool", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolStopInitInterleave() {
        String jdbcURL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_5";
        BValue[] args = { new BString(jdbcURL) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolStopInitInterleave", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = POOL_TEST_GROUP)
    public void testLocalConnectionPoolShutDown() {
        String jdbcURL1 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1";
        String jdbcURL2 = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2";
        BValue[] args = { new BString(jdbcURL1), new BString(jdbcURL2) };
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalConnectionPoolShutDown", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 1]");
    }
}
