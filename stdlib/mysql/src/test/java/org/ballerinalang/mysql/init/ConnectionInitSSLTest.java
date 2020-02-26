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

package org.ballerinalang.mysql.init;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.mysql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

/**
 * This test case validates the connections.
 */
public class ConnectionInitSSLTest {
    private static final String DB_NAME = "CONNECT_DB";
    private static final String CONNECTION_INIT_TEST = "MySQLSelectTest";
    private CompileResult result;
    private DB datbase;
    private BValue[] args = {new BString(SQLDBUtils.DB_HOST), new BInteger(3306),
            new BString("root"), new BString("Test@123"), new BString(DB_NAME)};

    @BeforeClass
    public void setup() throws ManagedProcessException, FileNotFoundException {
        result = BCompileUtil.compile(Paths.get("test-src", "connection", "connection_ssl_test.bal").toString());
//        https://dev.mysql.com/doc/mysql-secure-deployment-guide/5.7/en/secure-deployment-secure-connections.html

//        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
//        configBuilder.setPort(SQLDBUtils.DB_PORT);
//        configBuilder.setDataDir(SQLDBUtils.DB_DIRECTORY);
//        configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(true);
//        configBuilder.setSecurityDisabled(false);
//        datbase = DB.newEmbeddedDB(configBuilder.build());
//        datbase.start();
//        datbase.createDB(DB_NAME, SQLDBUtils.DB_USER_NAME, SQLDBUtils.DB_USER_PW);
//        String sqlFile = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + SQLDBUtils.CONNECTIONS_DIR +
//                File.separator + "connections_test_data.sql";
//        datbase.source(sqlFile, DB_NAME);
    }

    @Test(groups = CONNECTION_INIT_TEST)
    public void testWithVerifyCert() {
        BValue[] returns = BRunUtil.invoke(result, "testWithVerifyCert", args);
        final boolean expected = true;
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
    }

    @Test(groups = CONNECTION_INIT_TEST)
    public void testWithPreferredSSL() {
        BValue[] returns = BRunUtil.invoke(result, "testWithPreferredSSL", args);
        final boolean expected = true;
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
    }

    @Test(groups = CONNECTION_INIT_TEST)
    public void testWithRequiredSSL() {
        BValue[] returns = BRunUtil.invoke(result, "testWithRequiredSSL", args);
        final boolean expected = true;
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
    }

    @AfterSuite
    public void cleanup() throws ManagedProcessException {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
//        datbase.stop();
    }
}
