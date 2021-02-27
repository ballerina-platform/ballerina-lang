/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.connectionpool;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;

import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_MANAGER;

/**
 * Test connection pool configurations.
 *
 * @since 0.995.0
 */
public class ConnectionPoolTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/connection-pool/client-connection-pool.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
    }

    @Test
    public void testGlobalPoolConfig() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGlobalPoolConfig");
        Assert.assertEquals(returns.length, 3);
        verifyResults((BMap<String, BValue>) returns[0]);
        verifyResults((BMap<String, BValue>) returns[1]);
        verifyResults((BMap<String, BValue>) returns[2]);
    }

    @Test
    public void testSharedConfig() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSharedConfig");
        Assert.assertEquals(returns.length, 2);
        ConnectionManager connectionManager1 = verifyPoolConfig(returns[0]);
        ConnectionManager connectionManager2 = verifyPoolConfig(returns[1]);
        Assert.assertEquals(connectionManager1, connectionManager2,
                            "Both the clients should have same connection manager");
    }

    @Test
    public void testPoolPerClient() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPoolPerClient");
        Assert.assertEquals(returns.length, 2);
        ConnectionManager connectionManager1 = verifyPoolConfig(returns[0]);
        ConnectionManager connectionManager2 = verifyPoolConfig(returns[1]);
        Assert.assertNotEquals(connectionManager1, connectionManager2,
                               "Both the clients should have their own connection manager");
    }

    private void verifyResults(BMap<String, BValue> client) {
        BValue userDefinedPoolConfig = client.get(HttpConstants.USER_DEFINED_POOL_CONFIG.getValue());
        Assert.assertNull(userDefinedPoolConfig, "Client shouldn't have pool config defined by the user");
    }

    private ConnectionManager verifyPoolConfig(BValue aReturn) {
        BMap<String, BValue> result = (BMap<String, BValue>) aReturn;
        BMap<String, BValue> clientConfig = (BMap) result.get(CLIENT_ENDPOINT_CONFIG.getValue());
        BMap<String, BValue> userDefinedPoolConfig = (BMap) clientConfig.get(
                HttpConstants.USER_DEFINED_POOL_CONFIG.getValue());
        Assert.assertNotNull(userDefinedPoolConfig, "Client should have a pool config defined by the user");
        return (ConnectionManager) userDefinedPoolConfig.getNativeData(CONNECTION_MANAGER);
    }
}
