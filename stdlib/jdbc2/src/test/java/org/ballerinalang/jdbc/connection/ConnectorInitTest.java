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
package org.ballerinalang.jdbc.connection;

import org.ballerinalang.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test JDBC Client Initialization.
 */
public class ConnectorInitTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR_INIT";
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME;
    private BValue[] args = {new BString(JDBC_URL), new BString("sa"), new BString("")};

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir("connection", "connector_init_test.bal"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME,
                SQLDBUtils.getSQLResourceDir("connection", "connector_init_test_data.sql"));
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
        BValue[] args = {new BString(JDBC_URL)};
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


    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIR));
    }
}
