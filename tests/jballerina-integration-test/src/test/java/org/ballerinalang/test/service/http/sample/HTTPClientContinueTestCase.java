/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 *  Test case for verifying the client-side 100-continue behaviour.
 */
@Test(groups = "http-test")
public class HTTPClientContinueTestCase extends HttpBaseTest {

    @Test(description = "Test 100 continue for http client")
    public void testContinueAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9242,
                "continue"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Hello World!", "Message content mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
    }

    @Test(description = "Negative test case for 100 continue of http client")
    public void testNegativeContinueAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9242,
                "continue/failure"));
        Assert.assertEquals(response.getResponseCode(), 417, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
    }

    @Test(description = "Test 100 continue for http client")
    public void testContinueActionWithMain() throws BallerinaTestException {
        String balFilepath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "http" + File.separator + "src" + File.separator + "httpservices")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "36_http_client_100_continue.bal" }, balFilepath);
        Assert.assertTrue(output.contains("Hello World!"));
        Assert.assertTrue(output.contains("200"));
    }
}
