/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.scenario.test.bbg.http;

import org.ballerinalang.scenario.test.common.ScenarioTestBase;
import org.ballerinalang.scenario.test.common.http.HttpClientRequest;
import org.ballerinalang.scenario.test.common.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests pass-through-messaging-service BBG.
 */
public class PassthroughMessagingServiceTest extends ScenarioTestBase {
    private static String host = System.getProperty("http.service.host");
    private static String port = System.getProperty("http.service.port");

    @Test(description = "Test employee creation")
    public void testCreateEmployee() throws Exception {
        String url = "http://" + host + ":" + port + "/OnlineShopping";
        HttpResponse httpResponse = HttpClientRequest.doGet(url);
        Assert.assertEquals(httpResponse.getResponseCode(), 200, "Response code mismatching");
        Assert.assertEquals(httpResponse.getData(), "Welcome to Local Shop! Please put your order here.....");
    }
}
