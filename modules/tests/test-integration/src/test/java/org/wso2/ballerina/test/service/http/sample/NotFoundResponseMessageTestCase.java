/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.test.service.http.sample;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.test.IntegrationTestCase;
import org.wso2.ballerina.test.util.HttpClientRequest;
import org.wso2.ballerina.test.util.HttpResponse;

import java.io.IOException;

/**
 * Testing not found response messages.
 */
public class NotFoundResponseMessageTestCase extends IntegrationTestCase {

    @Test(description = "Test service not found response")
    public void testServiceNotFoundResponse() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(getServiceURLHttp(""));
        Assert.assertEquals(response.getResponseCode(), 404, "Response code mismatched");
        Assert.assertEquals(response.getData(), "no service found to handle incoming request received to : " +
                "/", "Message content mismatched");
    }

    @Test(description = "Test resource not found response")
    public void testResourceNotFoundResponse() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(getServiceURLHttp("ecommerceservice/foo"));
        Assert.assertEquals(response.getResponseCode(), 404, "Response code mismatched");
        Assert.assertEquals(response.getData(), "no resource found to handle the request to Service : " +
                "Ecommerce : no matching resource found for Path : /foo , Method : GET",
                "Message content mismatched");
    }
}
