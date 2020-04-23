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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * Test different HTTP status codes with respective ballerina status code functions.
 *
 * @since 0.982.0
 */
@Test(groups = "http-test")
public class HttpStatusCodeTestCase extends HttpBaseTest {

    private final int servicePort = 9223;
    private final String newResourceURI = "/newResourceURI";

    @Test(description = "Test ballerina ok() function with entity body")
    public void testOKWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "code/okWithBody"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        assertEquals(response.getData(), "OK Response", "Message content mismatched");
    }

    @Test(description = "Test ballerina ok() function without entity body")
    public void testOKWithoutBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "code/okWithoutBody"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertEquals(response.getData(), "", "Message body should be empty");
    }

    @Test(description = "Test ballerina created() function with entity body")
    public void testCreatedWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "code/createdWithBody"));
        assertEquals(response.getResponseCode(), 201, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.LOCATION.toString()), newResourceURI,
                "Incorrect location header value");
        assertEquals(response.getData(), "Created Response", "Message content mismatched");
    }

    @Test(description = "Test ballerina created() function without entity body")
    public void testCreatedWithoutBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "code/createdWithoutBody"));
        assertEquals(response.getResponseCode(), 201, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.LOCATION.toString()), newResourceURI,
                "Incorrect location header value");
        assertEquals(response.getData(), "", "Message body should be empty");
    }

    @Test(description = "Test ballerina created() function with an empty URI")
    public void testCreatedWithEmptyURI() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "code/createdWithEmptyURI"));
        assertEquals(response.getResponseCode(), 201, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.LOCATION.toString()), null,
                "No location header should be received");
        assertEquals(response.getData(), "", "Message body should be empty");
    }

    @Test(description = "Test ballerina accepted() function with entity body")
    public void testAcceptedWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "code/acceptedWithBody"));
        assertEquals(response.getResponseCode(), 202, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        assertEquals(response.getData(), "{\"msg\":\"accepted response\"}", "Message content mismatched");
    }

    @Test(description = "Test ballerina accepted() function without entity body")
    public void testAcceptedWithoutBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                "code/acceptedWithoutBody"));
        assertEquals(response.getResponseCode(), 202, "Response code mismatched");
        assertEquals(response.getData(), "", "Message body should be empty");
    }

    @Test(description = "Test ballerina noContent() function with entity body")
    public void testNoContentWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                                                                                         "code/noContentWithBody"));
        assertEquals(response.getResponseCode(), 204, "Response code mismatched");
        assertEquals(response.getHeaders().get("x-custom-header")
                , "custom-header-value", "Content-Type mismatched");
        assertEquals(response.getData(), "", "Message body should be empty");
    }

    @Test(description = "Test ballerina noContent() function without entity body")
    public void testNoContentWithoutBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                                                                                         "code/noContentWithoutBody"));
        assertEquals(response.getResponseCode(), 204, "Response code mismatched");
        assertEquals(response.getData(), "", "Message body should be empty");
    }

    @Test(description = "Test ballerina badRequest() function with entity body")
    public void testBadRequestWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                                                                                         "code/badRequestWithBody"));
        assertEquals(response.getResponseCode(), 400, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_XML, "Content-Type mismatched");
        assertEquals(response.getData(), "<test>Bad Request</test>", "Message content mismatched");
    }

    @Test(description = "Test ballerina badRequest() function without entity body", expectedExceptions =
            IOException.class, expectedExceptionsMessageRegExp = "Server returned HTTP response code: 400 .*")
    public void testBadRequestWithoutBody() throws IOException {
        HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "code/badRequestWithoutBody"), true);
    }

    @Test(description = "Test ballerina notFound() function with entity body")
    public void testNotFoundWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                                                                                         "code/notFoundWithBody"));
        assertEquals(response.getResponseCode(), 404, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_XML, "Content-Type mismatched");
        assertEquals(response.getData(), "<test>artifacts not found</test>", "Message content mismatched");
    }

    @Test(description = "Test ballerina notFound() function without entity body", expectedExceptions =
            IOException.class, expectedExceptionsMessageRegExp = "Server returned HTTP response code: 404 .*")
    public void testNotFoundWithoutBody() throws IOException {
        HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "code/notFoundWithoutBody"), true);
    }

    @Test(description = "Test ballerina internalServerError() function with entity body")
    public void testInternalServerErrWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                                                                                         "code/serverErrWithBody"));
        assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_XML, "Content-Type mismatched");
        assertEquals(response.getData(), "<test>Internal Server Error Occurred</test>",
                     "Message content mismatched");
    }

    @Test(description = "Test ballerina internalServerError() function without entity body",
          expectedExceptions = IOException.class, expectedExceptionsMessageRegExp =
                  "Server returned HTTP response code: 500 .*")
    public void testInternalServerErrWithoutBody() throws IOException {
        HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "code/serverErrWithoutBody"), true);
    }
}
