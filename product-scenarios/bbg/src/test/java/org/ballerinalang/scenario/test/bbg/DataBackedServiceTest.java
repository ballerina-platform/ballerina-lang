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
package org.ballerinalang.scenario.test.bbg;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.scenario.test.common.ScenarioTestBase;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests data-backed-service BBG.
 */
public class DataBackedServiceTest extends ScenarioTestBase {
    private static String host = System.getProperty("data.backed.service.host");
    private static String port = System.getProperty("data.backed.service.port");

    @Test(description = "Test employee creation")
    public void testCreateEmployee() throws Exception {
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String url = "http://" + host + ":" + port + "/records/employee";
        PrintStream printStream = System.out;
        printStream.println("URL: " + url);
        HttpResponse httpResponse = HttpClientRequest.doPost(url, "{\"name\":\"Alice\", \"age\":20,\"ssn\":123456789,"
                + "\"employeeId\":1}", headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200, "Response code mismatching");
        Assert.assertEquals(httpResponse.getData(), "{\"Status\":\"Data Inserted Successfully\"}");
    }
}
