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
package org.ballerinalang.test.connectors.h2;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
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
    public static final String DB_DIRECTORY_H2 = "./target/H2Client/";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/connectors/h2/h2-client-actions-test.bal");
        SQLDBUtils.deleteFiles(new File(DB_DIRECTORY_H2), DB_NAME);
        SQLDBUtils.initH2Database(DB_DIRECTORY_H2, DB_NAME, "datafiles/sql/H2ConnectorTableCreate.sql");
    }

    @Test
    public void testSelect() {
        BValue[] returns = BRunUtil.invoke(result, "testSelect");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BIntArray);
        Assert.assertEquals(((BIntArray) returns[0]).size(), 2);
        Assert.assertEquals(((BIntArray) returns[0]).get(0), 1);
        Assert.assertEquals(((BIntArray) returns[0]).get(1), 2);
    }

    @Test
    public void testUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdate");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test
    public void testCall() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(result, "testCall", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        BString retValue = (BString) returns[0];
        final String expected = "Oliver";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test
    public void testGeneratedKeyOnInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testGeneratedKeyOnInsert");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        BString retValue = (BString) returns[0];
        Assert.assertTrue(Integer.parseInt(retValue.stringValue()) > 0);
    }

    @Test
    public void testBatchUpdate() {
        BValue[] returns = BRunUtil.invoke(result, "testBatchUpdate");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BIntArray);
        BIntArray retValue = (BIntArray) returns[0];
        Assert.assertEquals(retValue.get(0), 1);
        Assert.assertEquals(retValue.get(1), 1);
    }

    @Test
    public void testAddToMirrorTable() throws Exception {
        BValue[] returns = BRunUtil.invoke(result, "testAddToMirrorTable");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(returns[0].stringValue(),
                "{customerId:40, name:\"Manuri\", creditLimit:1000.0, country:\"Sri Lanka\"}");
        Assert.assertEquals(returns[1].stringValue(),
                "{customerId:41, name:\"Devni\", creditLimit:1000.0, country:\"Sri Lanka\"}");
    }

    @Test
    public void testUpdateInMemory() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateInMemory");
        Assert.assertEquals(returns.length, 2);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(),
                "[{\"customerId\":15,\"name\":\"Anne\",\"creditLimit\":1000.0," + "\"country\":\"UK\"}]");
    }

    @Test
    public void testInitWithNilDbOptions() {
        BValue[] returns = BRunUtil.invoke(result, "testInitWithNilDbOptions");
        assertInitTestReturnValues(returns);
    }

    @Test
    public void testInitWithDbOptions() {
        BValue[] returns = BRunUtil.invoke(result, "testInitWithDbOptions");
        assertInitTestReturnValues(returns);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error in sql connector configuration:Failed to initialize pool: "
                  + "Unsupported connection setting \"INVALID_PARAM\".*")
    public void testInitWithInvalidDbOptions() {
        BRunUtil.invoke(result, "testInitWithInvalidDbOptions");
        Assert.fail("Expected exception should have been thrown by this point");
    }

    private void assertInitTestReturnValues(BValue[] returns) {
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BIntArray);
        Assert.assertEquals(((BIntArray) returns[0]).size(), 2);
        Assert.assertEquals(((BIntArray) returns[0]).get(0), 1);
        Assert.assertEquals(((BIntArray) returns[0]).get(1), 2);
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
