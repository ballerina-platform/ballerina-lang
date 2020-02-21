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
package org.ballerinax.mysql.actions;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.mysql.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.nio.file.Paths;

/**
 * Test class for JDBC select remote function tests.
 *
 * @since 1.0.0
 */
public class SelectTest {

    private static final String DB_NAME = "SELECT_DB";
    private static final String SELECT_TEST = "MySQLSelectTest";
    private CompileResult result;
    private DB datbase;
    private BValue[] args = {new BString(SQLDBUtils.DB_HOST), new BInteger(SQLDBUtils.DB_PORT),
            new BString(SQLDBUtils.DB_USER_NAME), new BString(SQLDBUtils.DB_USER_PW), new BString(DB_NAME)};

    @BeforeClass
    public void setup() throws ManagedProcessException, FileNotFoundException {
        result = BCompileUtil.compile(Paths.get("test-src", "actions", "select_test.bal").toString());
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(SQLDBUtils.DB_PORT);
        configBuilder.setDataDir(SQLDBUtils.DB_DIRECTORY);
        configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(true);
        datbase = DB.newEmbeddedDB(configBuilder.build());
        datbase.start();
        datbase.createDB(DB_NAME, SQLDBUtils.DB_USER_NAME, SQLDBUtils.DB_USER_PW);
        String sqlFile = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + SQLDBUtils.ACTIONS_DIR +
                File.separator + "select_test_data.sql";
        datbase.source(sqlFile, DB_NAME);
    }

    @Test(groups = SELECT_TEST)
    public void testSelectNumericData() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectNumericData", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2147483647);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 32767);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 127);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 1);
        Assert.assertEquals(((BDecimal) returns[5]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567, 0.003);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567, 0.003);
    }

    //    @Test(groups = SELECT_TEST)
//    public void testSelectNumericDataWithDBError() {
//        BValue[] returns = BRunUtil.invoke(result, "testSelectNumericDataWithDBError", args);
//        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
//        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
//        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
//        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
//        Assert.assertTrue(returns[4].stringValue().contains("failed to execute select query:"));
//        Assert.assertEquals(returns[5].stringValue(), "42501");
//        Assert.assertEquals(returns[6].stringValue(), "{ballerinax/sql}DatabaseError");
//        Assert.assertEquals(((BInteger) returns[7]).intValue(), -5501);
//    }
//
//    @Test(groups = SELECT_TEST)
//    public void testSelectNumericDataWithApplicationError() {
//        BValue[] returns = BRunUtil.invoke(result, "testSelectNumericDataWithApplicationError", args);
//        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
//        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
//        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
//        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
//        Assert.assertEquals(returns[4].stringValue(), "{ballerinax/sql}ApplicationError");
//        Assert.assertTrue(
//                returns[5].stringValue().contains("unsupported array type specified as a parameter at index 0"));
//    }
//
//    @Test(groups = SELECT_TEST)
//    public void testArrayOfQueryParameters() {
//        BValue[] returns = BRunUtil.invoke(result, "testArrayOfQueryParameters", args);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//    }
//
//    @Test(groups = SELECT_TEST)
//    public void testBoolArrayOfQueryParameters() {
//        BValue[] returns = BRunUtil.invoke(result, "testBoolArrayOfQueryParameters", args);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//    }
//
//    @Test(groups = SELECT_TEST)
//    public void testBlobArrayOfQueryParameter() {
//        BValue[] returns = BRunUtil.invoke(result, "testBlobArrayOfQueryParameter", args);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//    }
//
//    @Test(dependsOnGroups = SELECT_TEST)
//    public void testCloseConnectionPool() {
//        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
//        BInteger retValue = (BInteger) returns[0];
//        Assert.assertEquals(retValue.intValue(), 1);
//    }
//
    @AfterSuite
    public void cleanup() throws ManagedProcessException {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
        datbase.stop();
    }
}
