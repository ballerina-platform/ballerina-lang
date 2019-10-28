/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.websub;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Class to test WebSub Hub startup.
 */
public class WebSubHubStartTest {

    private static final String HUB_SUBS_URL_FIELD = "subscriptionUrl";
    private static final String STARTED_UP_HUB_FIELD = "startedUpHub";

    private CompileResult result;
    private int port = 9191;
    private BMap<String, BValue> hubStartUpObject = null;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/hub/test_hub_startup.bal");
    }

    @Test(description = "Test hub start up and URL identification")
    public void testHubStartUp() {
        BValue[] returns = BRunUtil.invoke(result, "startupHub", new BValue[]{new BInteger(port)});
        assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMap);
        hubStartUpObject = (BMap<String, BValue>) returns[0];
        assertEquals(hubStartUpObject.get(HUB_SUBS_URL_FIELD).stringValue(),
                            "http://localhost:" + port + "/websub/hub");
    }

    @Test(description = "Test hub start up call when already started", dependsOnMethods = "testHubStartUp")
    public void testHubStartUpWhenStarted() {
        BValue[] returns = BRunUtil.invoke(result, "startupHub", new BValue[]{new BInteger(9292)});
        assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMap);
        hubStartUpObject = (BMap<String, BValue>) returns[0];
        assertEquals(hubStartUpObject.get("message").stringValue(), "Ballerina Hub already started up");
        Assert.assertTrue(hubStartUpObject.get(STARTED_UP_HUB_FIELD) instanceof BMap);
        BMap<String, BValue> hubObject = (BMap<String, BValue>) hubStartUpObject.get(STARTED_UP_HUB_FIELD);
        assertEquals(hubObject.get(HUB_SUBS_URL_FIELD).stringValue(), "http://localhost:" + port + "/websub/hub");
    }

    @Test(description = "Test shut down and restart", dependsOnMethods = "testHubStartUpWhenStarted")
    public void testHubShutdownAndStart() {
        int port = 9393;
        BValue[] returns = BRunUtil.invoke(result, "stopHub", new Object[]{hubStartUpObject});
        assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue((((BBoolean) returns[0]).value()));

        returns = BRunUtil.invoke(result, "startupHub", new BValue[]{new BInteger(port)});
        assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMap);
        hubStartUpObject = (BMap<String, BValue>) returns[0];
        assertEquals(hubStartUpObject.get(HUB_SUBS_URL_FIELD).stringValue(),
                            "http://localhost:" + port + "/websub/hub");
    }

    @Test
    public void testPublisherAndSubscriptionInvalidSameResourcePath() {
        BValue[] returns = BRunUtil.invoke(result, "testPublisherAndSubscriptionInvalidSameResourcePath");
        assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue((((BBoolean) returns[0]).value()));
    }

    @AfterClass
    public void tearDown() {
        BRunUtil.invoke(result, "stopHub", new Object[]{hubStartUpObject});
    }

}
