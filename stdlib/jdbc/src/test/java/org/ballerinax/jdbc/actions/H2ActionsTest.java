/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinax.jdbc.actions;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test class for H2 Connector.
 *
 * @since 0.970.0
 */
public class H2ActionsTest {

    private CompileResult result;
    private static final String DB_NAME = "TestDBH2";
    private static final String H2_TEST_GROUP = "H2_TEST";
    private SQLDBUtils.TestDatabase testDatabase;
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY_H2 + DB_NAME;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(Paths.get("test-src", "actions", "h2_actions_test.bal").toString());
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.H2,
                Paths.get("datafiles", "sql", "actions", "h2_actions_test_data.sql").toString(),
                SQLDBUtils.DB_DIRECTORY_H2, DB_NAME);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testSelect() {
        BValue[] returns = BRunUtil.invoke(result, "testSelect", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].size(), 2);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(1), 2);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdate", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testCall() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testCall", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        BString retValue = (BString) returns[0];
        final String expected = "Oliver";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsert", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertTrue(retValue.intValue() > 0);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
    }

    @Test(groups = { H2_TEST_GROUP })
    public void testUpdateInMemory() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateInMemory", args);
        Assert.assertEquals(returns.length, 2);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(),
                "[{\"customerId\":15, \"name\":\"Anne\", \"creditLimit\":1000.0, \"country\":\"UK\"}]");
    }

    @Test(groups = H2_TEST_GROUP)
    public void testInitWithNilDbOptions() {
        BValue[] returns = BRunUtil.invoke(result, "testInitWithNilDbOptions", args);
        assertInitTestReturnValues(returns);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testInitWithDbOptions() {
        BValue[] returns = BRunUtil.invoke(result, "testInitWithDbOptions", args);
        assertInitTestReturnValues(returns);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*error in sql connector configuration: "
                          + "Property INVALID_PARAM does not exist on target class org.h2.jdbcx.JdbcDataSource.*",
          groups = { H2_TEST_GROUP })
    public void testInitWithInvalidDbOptions() {
        BRunUtil.invoke(result, "testInitWithInvalidDbOptions", args);
        Assert.fail("expected exception should have been thrown by this point");
    }

    private void assertInitTestReturnValues(BValue[] returns) {
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].size(), 2);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(1), 2);
    }

    @Test(groups = { H2_TEST_GROUP })
    public void testH2MemDBUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testH2MemDBUpdate");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "[{\"ID\":15, \"NAME\":\"Anne\"}]");
    }

    @Test(dependsOnGroups = H2_TEST_GROUP)
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }

    //This method is used as a UDF
    public static ResultSet javafunc(Connection conn, String sql) throws SQLException {
        return conn.createStatement().executeQuery(sql);
    }
}
