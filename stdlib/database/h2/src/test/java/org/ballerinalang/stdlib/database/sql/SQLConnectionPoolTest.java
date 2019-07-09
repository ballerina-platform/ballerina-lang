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

package org.ballerinalang.stdlib.database.sql;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.stdlib.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
public class SQLConnectionPoolTest {
    private CompileResult result;
    private static final String POOL_TEST_GROUP = "ConnectionPoolTest";
    private static final String connectionTimeoutError = ".*Connection is not available, request timed out after "
            + "10\\d\\dms.*";

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("enableJBallerinaTests", "true");
        Path ballerinaConfPath = Paths.get("src", "test", "resources", "ballerina.conf").toAbsolutePath();
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        result = BCompileUtil.compile("test-src/sql/connection_pool_test.bal");
        setupDatabases();
    }

    private void setupDatabases() {
        String globalPoolDb1 = "TEST_SQL_CONNECTION_POOL_GLOBAL_1";
        String globalPoolDb2 = "TEST_SQL_CONNECTION_POOL_GLOBAL_2";

        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), globalPoolDb1);
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), globalPoolDb2);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, globalPoolDb1, "datafiles/sql/SQLTestConnectionPool.sql");
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, globalPoolDb2, "datafiles/sql/SQLTestConnectionPool.sql");

        for (int i = 1; i <= 9; i++) {
            String db = "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_" + i;
            SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), db);
            SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, db, "datafiles/sql/SQLTestConnectionPool.sql");
        }
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolSingleDestination() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolSingleDestination");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("1", (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        String error = (((BValueArray) returns[0])).getRefValue(10).stringValue();
        Assert.assertTrue(error.matches(connectionTimeoutError), "Actual Error: " + error);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolsMultipleDestinations() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolsMultipleDestinations");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).size(), 2);
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
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolSingleDestinationConcurrent");
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
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolConfigSingleDestination");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("1", (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        String error = (((BValueArray) returns[0])).getRefValue(5).stringValue();
        Assert.assertTrue(error.matches(connectionTimeoutError), "Actual Error: " + error);
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolConfigMultipleDestinations() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolConfigMultipleDestinations");
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
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownUnsharedLocalConnectionPool");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownSharedConnectionPool() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownSharedConnectionPool");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(),
                "[1, 1, 1, \"Client has been stopped\", \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownPoolCorrespondingToASharedPoolConfig() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownPoolCorrespondingToASharedPoolConfig");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 1, 1, \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolCreateClientAfterShutdown() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolCreateClientAfterShutdown");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 1, \"Client has been stopped\", 1]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testStopClientUsingGlobalPool() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testStopClientUsingGlobalPool");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, \"Client has been stopped\"]");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolStopInitInterleave() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolStopInitInterleave");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = POOL_TEST_GROUP)
    public void testLocalConnectionPoolShutDown() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalConnectionPoolShutDown");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 1]");
    }
}
