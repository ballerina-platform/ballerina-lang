/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Testing the HelloWorld sample located in
 * ballerina_home/samples/helloWorldService/helloWorldService.bal.
 */
public class HelloWorldSampleTestCase extends IntegrationTestCase {

    @Test(description = "Test hello world sample test case invoking base path")
    public void testHelloWorldServiceByBasePath() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(getServiceURLHttp("hello"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Hello, World!", "Message content mismatched");
    }

    @Test(description = "Test hello world sample test case")
    public void testHelloWorldServiceByResourcePath() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(getServiceURLHttp("hello/resource"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Hello, World!", "Message content mismatched");
    }
}
