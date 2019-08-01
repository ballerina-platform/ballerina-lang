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

package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * A test case for http redirect.
 */
@Test(groups = "http-test")
public class RedirectTestCase extends HttpBaseTest {

    private final int servicePort = 9103;

    @Test(description = "Test http redirection and test whether the resolvedRequestedURI in the response is correct.")
    public void testRedirect() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "service1/"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "http://localhost:9102/redirect2", "Incorrect resolvedRequestedURI");
    }

    @Test(description = "When the maximum redirect count is reached, client should do no more redirects.")
    public void testMaxRedirect() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/maxRedirect"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "/redirect1/round5:http://localhost:9103/redirect1/round4",
                "Incorrect resolvedRequestedURI");
    }

    @Test(description = "Original request and the final redirect request goes to two different domains and the " +
            "max redirect count gets equal to current redirect count.")
    public void testCrossDomain() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/crossDomain"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "hello world:http://localhost:9102/redirect2",
                "Incorrect resolvedRequestedURI");
    }

    @Test(description = "Redirect is on, but the first response received is not a redirect.")
    public void testNoRedirect() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/noRedirect"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "hello world:http://localhost:9102/redirect2",
                "Incorrect resolvedRequestedURI");
    }

    @Test(description = "Redirect is off, but the first response received for a redirect.")
    public void testRedirectOff() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/redirectOff"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "/redirect1/round2:",
                "Incorrect resolvedRequestedURI");
    }

    @Test(description = "Include query params in relative path of a redirect location")
    public void testQPWithRelativePath() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/qpWithRelativePath"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "value:ballerina:http://localhost:9103/redirect1/" +
                "processQP?key=value&lang=ballerina", "Incorrect resolvedRequestedURI");
    }

    @Test(description = "Include query params in absolute path of a redirect location")
    public void testQPWithAbsolutePath() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/qpWithAbsolutePath"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "value:ballerina:http://localhost:9103/redirect1/" +
                "processQP?key=value&lang=ballerina", "Incorrect resolvedRequestedURI");
    }

    @Test(description = "Test original request with query params. NOTE:Query params in the original request should" +
            "be ignored while resolving redirect url.")
    public void testOriginalRequestWithQP() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/originalRequestWithQP"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "hello world:http://localhost:9102/redirect2",
                "Incorrect resolvedRequestedURI");
    }

    @Test
    public void test303Status() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/test303"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "hello world:http://localhost:9102/redirect2",
                "Incorrect resolvedRequestedURI");
    }

    @Test(description = "Test redirect to an HTTPS location.")
    public void testRedirectWithHTTPs() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "service1/httpsRedirect"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "HTTPs Result:https://localhost:9104/redirect3/result",
                "Incorrect resolvedRequestedURI");
    }
}
