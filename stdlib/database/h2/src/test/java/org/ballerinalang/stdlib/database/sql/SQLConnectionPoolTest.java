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
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.stdlib.utils.SQLDBUtils;
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
    private static final String DB1_NAME = "TEST_SQL_CONNECTION_POOL_GLOBAL_1";
    private static final String DB2_NAME = "TEST_SQL_CONNECTION_POOL_GLOBAL_2";
    private static final String DB3_NAME = "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1";
    private static final String DB4_NAME = "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2";
    private Path ballerinaConfPath;
    private static final String POOL_TEST_GROUP = "ConnectionPoolTest";

    @BeforeClass
    public void setup() throws Exception {
        ballerinaConfPath = Paths.get("src", "test", "resources", "ballerina.conf").toAbsolutePath();
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        result = BCompileUtil.compile("test-src/sql/connection_pool_test.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB1_NAME);
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB2_NAME);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, DB1_NAME, "datafiles/sql/SQLTestConnectionPool.sql");
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, DB2_NAME, "datafiles/sql/SQLTestConnectionPool.sql");
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, DB3_NAME, "datafiles/sql/SQLTestConnectionPool.sql");
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIRECTORY, DB4_NAME, "datafiles/sql/SQLTestConnectionPool.sql");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolSingleDestination() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolSingleDestination");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String name1 = "[{\"FIRSTNAME\":\"Peter\"}]";
        String name2 = "[{\"FIRSTNAME\":\"Dan\"}]";
        String[] expectedTableData = { name1, name2, name1, name1, name2, name2, name1, name1, name1, name1 };
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(expectedTableData[i], (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        Assert.assertTrue((((BValueArray) returns[0])).getRefValue(10).stringValue()
                .matches(".*Timeout after 10\\d\\dms of waiting for a connection.*"));
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolsMultipleDestinations() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolsMultipleDestinations");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).size(), 2);
        BValueArray jsonArray1 = ((BValueArray) (((BValueArray) returns[0]).getRefValue(0)));
        BValueArray jsonArray2 = ((BValueArray) (((BValueArray) returns[0]).getRefValue(1)));
        String name1 = "[{\"FIRSTNAME\":\"Peter\"}]";
        String name2 = "[{\"FIRSTNAME\":\"Dan\"}]";
        String[] expectedTableData = { name1, name2, name1, name1, name2, name2, name1, name1, name1, name1 };
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(expectedTableData[i], jsonArray1.getRefValue(i).stringValue());
        }
        Assert.assertTrue(jsonArray1.getRefValue(10).stringValue()
                .matches(".*Timeout after 10\\d\\dms of waiting for a connection.*"));

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(expectedTableData[i], jsonArray2.getRefValue(i).stringValue());
        }
        Assert.assertTrue(jsonArray2.getRefValue(10).stringValue()
                .matches(".*Timeout after 10\\d\\dms of waiting for a connection.*"));
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testGlobalConnectionPoolSingleDestinationConcurrent() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalConnectionPoolSingleDestinationConcurrent");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String name1 = "[{\"FIRSTNAME\":\"Peter\"}]";
        String name2 = "[{\"FIRSTNAME\":\"Dan\"}]";
        for (int i = 0; i < 5; i++) {
            BValueArray array = ((BValueArray) ((BValueArray) returns[0]).getRefValue(i));
            Assert.assertEquals(array.getRefValue(0).stringValue(), name1);
            Assert.assertEquals(array.getRefValue(1).stringValue(), name2);
        }
        BValueArray array = ((BValueArray) ((BValueArray) returns[0]).getRefValue(4));
        Assert.assertTrue(
                array.getRefValue(2).stringValue().matches(".*Timeout after 10\\d\\dms of waiting for a connection.*"));
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolConfigSingleDestination() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolConfigSingleDestination");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String name1 = "[{\"FIRSTNAME\":\"Peter\"}]";
        String name2 = "[{\"FIRSTNAME\":\"Dan\"}]";
        String[] expectedTableData = { name1, name1, name2, name1, name1 };
        for (int i = 0; i < expectedTableData.length; i++) {
            Assert.assertEquals(expectedTableData[i], (((BValueArray) returns[0])).getRefValue(i).stringValue());
        }
        Assert.assertTrue((((BValueArray) returns[0])).getRefValue(5).stringValue()
                .matches(".*Timeout after 10\\d\\dms of waiting for a connection.*"));
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolConfigMultipleDestinations() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolConfigMultipleDestinations");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray returnArray = (BValueArray) returns[0];

        String name1 = "[{\"FIRSTNAME\":\"Peter\"}]";
        String name2 = "[{\"FIRSTNAME\":\"Dan\"}]";

        String[] expectedArray1 = { name1, name1, name2 };
        String[] expectedArray2 = { name1, name2, name2 };

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(returnArray.getRefValue(i).stringValue(), expectedArray1[i]);
            Assert.assertEquals(returnArray.getRefValue(i + 4).stringValue(), expectedArray2[i]);
        }
        Assert.assertTrue(returnArray.getRefValue(3).stringValue()
                .matches(".*Timeout after 10\\d\\dms of waiting for" + " a connection.*"));
        Assert.assertTrue(returnArray.getRefValue(7).stringValue()
                .matches(".*Timeout after 10\\d\\dms of waiting for" + " a connection.*"));
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownUnsharedLocalConnectionPool() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownUnsharedLocalConnectionPool");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "([{\"FIRSTNAME\":\"Peter\"}], {\"Error\":\"Client has been "
                + "stopped\"})");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownSharedConnectionPool() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownSharedConnectionPool");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "([{\"FIRSTNAME\":\"Peter\"}], [{\"FIRSTNAME\":\"Dan\"}], "
                + "[{\"FIRSTNAME\":\"Dan\"}], {\"Error\":\"Client has been stopped\"}, {\"Error\":\"Client has been "
                + "stopped\"}, 1)");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testShutDownPoolCorrespondingToASharedPoolConfig() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testShutDownPoolCorrespondingToASharedPoolConfig");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "([{\"FIRSTNAME\":\"Peter\"}], [{\"FIRSTNAME\":\"Dan\"}], "
                + "[{\"FIRSTNAME\":\"Dan\"}], {\"Error\":\"Client has been stopped\"})");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolCreateClientAfterShutdown() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolCreateClientAfterShutdown");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "(3, 3)");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testStopClientUsingGlobalPool() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testStopClientUsingGlobalPool");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "([{\"FIRSTNAME\":\"Peter\"}], {\"Error\":\"Client has been "
                + "stopped\"})");
    }

    @Test(groups = POOL_TEST_GROUP)
    public void testLocalSharedConnectionPoolStopInitInterleave() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalSharedConnectionPoolStopInitInterleave");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(dependsOnGroups = POOL_TEST_GROUP)
    public void testLocalConnectionPoolShutDown() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testLocalConnectionPoolShutDown");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "(1, 1)");
    }

    @Test(dependsOnGroups = POOL_TEST_GROUP)
    public void testGlobalPoolLiveness() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testGlobalPoolLiveness");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "(41, 11)");
    }
}
