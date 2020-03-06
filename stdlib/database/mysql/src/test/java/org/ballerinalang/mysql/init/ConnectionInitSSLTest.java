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

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.mysql.BaseTest;
import org.ballerinalang.mysql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This test case validates the SSL connections.
 *
 * @since 1.2.0
 */
public class ConnectionInitSSLTest {
    private static final String DB_NAME = "SSL_CONNECT_DB";
    private static final String SQL_SCRIPT = SQLDBUtils.SQL_RESOURCE_DIR + File.separator
            + SQLDBUtils.CONNECTIONS_DIR + File.separator + "connections_test_data.sql";
    private CompileResult result;

    static {
        BaseTest.addDBSchema(DB_NAME, SQL_SCRIPT);
    }

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(SQLDBUtils.getBalFilesDir(SQLDBUtils.CONNECTIONS_DIR,
                "connection_ssl_test.bal"));
    }

    @Test
    public void testSSLVerifyCert() {
        BValue[] returnVal = BRunUtil.invoke(result, "testSSLVerifyCert");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testSSLPreferred() {
        BValue[] returnVal = BRunUtil.invoke(result, "testSSLPreferred");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testSSLRequiredWithClientCert() {
        BValue[] returnVal = BRunUtil.invoke(result, "testSSLRequiredWithClientCert");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertNull(returnVal[0]);
    }

    @Test
    public void testSSLVerifyIdentity() {
        BValue[] returnVal = BRunUtil.invoke(result, "testSSLVerifyIdentity");
        Assert.assertTrue(returnVal[0] instanceof BError);
        BError error = (BError) returnVal[0];
        Assert.assertEquals(error.getReason(), SQLDBUtils.SQL_APPLICATION_ERROR_REASON);
        String errMessage = ((BMap) ((BError) returnVal[0]).getDetails())
                .get(SQLDBUtils.SQL_ERROR_MESSAGE).stringValue();
        Assert.assertTrue(errMessage.contains("The certificate Common Name 'Ballerina MySQL/Connector' " +
                "does not match with 'localhost'"), "Found error message: " + errMessage);
    }
}
