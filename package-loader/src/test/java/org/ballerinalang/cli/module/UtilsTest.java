/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.cli.module;

import org.ballerinalang.cli.module.util.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.Proxy;

import static org.ballerinalang.cli.module.util.Utils.convertToUrl;
import static org.ballerinalang.cli.module.util.Utils.createHttpUrlConnection;

/**
 * Unit tests for Utils class.
 *
 * @since 1.2.0
 */
public class UtilsTest {

    private static final String TEST_URL = "https://api.central.ballerina.io/1.0/modules";

    @Test(description = "Test creating proxy")
    public void testGetProxy() {
        Proxy proxy;
        // Test without proxy
        proxy = Utils.getProxy("", 0, "", "");
        Assert.assertNull(proxy);
        // Test with proxy
        proxy = Utils.getProxy("http://localhost", 9090, "testUser", "testPassword");
        Assert.assertNotNull(proxy);
        Assert.assertEquals(proxy.toString(), "HTTP @ http://localhost:9090");

    }

    @Test(description = "Test create http URL connection without proxy")
    public void testCreateHttpUrlConnection() {
        HttpURLConnection conn;
        // Test without proxy
        conn = createHttpUrlConnection(convertToUrl(TEST_URL), "", 0, "", "");
        Assert.assertNotNull(conn);
        // Test with the proxy
        conn = createHttpUrlConnection(convertToUrl(TEST_URL), "http://localhost", 9090, "testUser", "testPassword");
        Assert.assertNotNull(conn);
    }

    @Test(description = "Test set request")
    public void testSetRequestMethod() {
        HttpURLConnection conn = createHttpUrlConnection(convertToUrl(TEST_URL), "", 0, "", "");
        Utils.setRequestMethod(conn, Utils.RequestMethod.POST);
        Assert.assertEquals(conn.getRequestMethod(), "POST");
    }


}
