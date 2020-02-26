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
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
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
import java.nio.file.Paths;

/**
 * This test case validates the connections.
 */
public class ConnectionInitTest {
    private static final String DB_NAME = "CONNECT_DB";
    private CompileResult result;
    private DB datbase;
    private BValue[] args = {new BString(SQLDBUtils.DB_HOST), new BInteger(SQLDBUtils.DB_PORT),
            new BString(SQLDBUtils.DB_USER_NAME), new BString(SQLDBUtils.DB_USER_PW), new BString(DB_NAME)};

    @BeforeClass
    public void setup() throws ManagedProcessException {
        result = BCompileUtil.compile(Paths.get("test-src", "connection",
                "connection_init_test.bal").toString());
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(SQLDBUtils.DB_PORT);
        configBuilder.setDataDir(SQLDBUtils.DB_DIRECTORY);
        configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(true);
        datbase = DB.newEmbeddedDB(configBuilder.build());
        datbase.start();
        datbase.createDB(DB_NAME, SQLDBUtils.DB_USER_NAME, SQLDBUtils.DB_USER_PW);
        String sqlFile = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + SQLDBUtils.CONNECTIONS_DIR +
                File.separator + "connections_test_data.sql";
        datbase.source(sqlFile, DB_NAME);
    }

    @Test
    public void testWithMandatoryFields() {
        BValue[] returnVal = BRunUtil.invoke(result, "testConnectionWithNoFields");
        Assert.assertTrue(returnVal[0] instanceof BError);
        BError error = (BError) returnVal[0];
        Assert.assertEquals(error.getReason(), SQLDBUtils.SQL_APPLICATION_ERROR_REASON);
        Assert.assertTrue(((BMap) ((BError) returnVal[0]).getDetails()).get(SQLDBUtils.SQL_ERROR_MESSAGE)
                .stringValue().contains("Access denied for user ''@'localhost' (using password: NO)"));
    }

    @Test
    public void testWithURLParams() {
        BValue[] args = {new BString(SQLDBUtils.DB_HOST), new BString(SQLDBUtils.DB_USER_NAME), new BString(SQLDBUtils.DB_USER_PW),
                new BString(DB_NAME), new BInteger(SQLDBUtils.DB_PORT)};
        BValue[] returnVal = BRunUtil.invoke(result, "testWithURLParams", args);
        Assert.assertNull(returnVal[0]);
    }

//    @Test
//    public void testWithPoolOptions() {
//        BValue[] returns = BRunUtil.invoke(result, "testWithPoolOptions", args);
//        final boolean expected = true;
//        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
//    }

    @AfterSuite
    public void cleanup() throws ManagedProcessException {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
        datbase.stop();
    }
}
