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
package org.ballerinalang.stdlib.database.h2;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.stdlib.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test class for H2 Connector.
 *
 * @since 0.970.0
 */
public class H2ClientActionsTest {

    private CompileResult result;
    private static final String DB_NAME = "TestDBH2";
    private static final String DB_DIRECTORY_H2 = "./target/H2Client/";
    private static final String H2_TEST_GROUP = "H2_TEST";

    @BeforeClass
    public void setup() {
        System.setProperty("enableJBallerinaTests", "true");
        result = BCompileUtil.compile("test-src/h2/h2_actions_test.bal");
        SQLDBUtils.deleteFiles(new File(DB_DIRECTORY_H2), DB_NAME);
        SQLDBUtils.initH2Database(DB_DIRECTORY_H2, DB_NAME, "datafiles/sql/H2ConnectorTableCreate.sql");
    }

    @Test(groups = H2_TEST_GROUP)
    public void testSelect() {
        BValue[] returns = BRunUtil.invoke(result, "testSelect");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).size(), 2);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(1), 2);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdate");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = { H2_TEST_GROUP, "broken" })
    public void testCall() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(result, "testCall", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        BString retValue = (BString) returns[0];
        final String expected = "Oliver";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsert");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertTrue(retValue.intValue() > 0);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray retValue = (BValueArray) returns[0];
        Assert.assertEquals(retValue.getInt(0), 1);
        Assert.assertEquals(retValue.getInt(1), 1);
    }

    //TODO: #16033
    @Test(groups = { H2_TEST_GROUP, "broken" })
    public void testUpdateInMemory() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateInMemory");
        Assert.assertEquals(returns.length, 2);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(),
                "[{\"customerId\":15, \"name\":\"Anne\", \"creditLimit\":1000.0, \"country\":\"UK\"}]");
    }

    @Test(groups = H2_TEST_GROUP)
    public void testInitWithNilDbOptions() {
        BValue[] returns = BRunUtil.invoke(result, "testInitWithNilDbOptions");
        assertInitTestReturnValues(returns);
    }

    @Test(groups = H2_TEST_GROUP)
    public void testInitWithDbOptions() {
        BValue[] returns = BRunUtil.invoke(result, "testInitWithDbOptions");
        assertInitTestReturnValues(returns);
    }

    // TODO: #16033
    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*error in sql connector configuration:Failed to initialize pool: "
                  + "Unsupported connection setting \"INVALID_PARAM\".*", groups = { H2_TEST_GROUP, "broken" })
    public void testInitWithInvalidDbOptions() {
        BRunUtil.invoke(result, "testInitWithInvalidDbOptions");
        Assert.fail("Expected exception should have been thrown by this point");
    }

    private void assertInitTestReturnValues(BValue[] returns) {
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).size(), 2);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(1), 2);
    }

    //TODO: #16033
    @Test(groups = { H2_TEST_GROUP, "broken" })
    public void testH2MemDBUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testH2MemDBUpdate");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "[{\"ID\":15, \"NAME\":\"Anne\"}]");
    }

    @Test(dependsOnGroups = H2_TEST_GROUP)
    public void testCloseConnectionPool() {
        BValue connectionCountQuery = new BString("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS");
        BValue[] args = { connectionCountQuery };
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(DB_DIRECTORY_H2));
    }

    //This method is used as a UDF
    public static ResultSet javafunc(Connection conn, String sql) throws SQLException {
        return conn.createStatement().executeQuery(sql);
    }
}
