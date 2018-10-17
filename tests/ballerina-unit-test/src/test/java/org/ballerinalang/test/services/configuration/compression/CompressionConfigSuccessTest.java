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
package org.ballerinalang.test.services.configuration.compression;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.transport.http.netty.contract.Constants.ENCODING_DEFLATE;
import static org.wso2.transport.http.netty.contract.Constants.ENCODING_GZIP;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_TRANSFER_ENCODING_IDENTITY;

/**
 * Unit tests for 'Compression' service annotation. Since this back end is just a mock, these tests only check the
 * correct response is sent to the backend. Integration test cover the full set of scenarios.
 *
 * @since 0.966.0
 */
public class CompressionConfigSuccessTest {

    private CompileResult serviceResult;
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/services/configuration/compression/compression-annotation-test.bal";
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test Compression.AUTO, with no Accept-Encoding header. The response here means the one " +
            "that should be sent to transport, not to end user.")
    public void testAutoCompress() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/autoCompress",
                HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertNull(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                "The content-encoding header should be null and the identity which means no compression " +
                        "should be done to the response");
    }

    @Test(description = "Test Compression.AUTO, with Accept-Encoding header. The response here means the one " +
            "that should be sent to transport, not to end user.")
    public void testAutoCompressWithAcceptEncoding() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header(HttpHeaderNames.ACCEPT_ENCODING.toString(), ENCODING_GZIP));
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/autoCompress",
                HttpConstants.HTTP_METHOD_GET, headers, "hello");
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertNull(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                "The content-encoding header should be null and the original value of Accept-Encoding should " +
                        "be used for compression from the backend");
    }

    @Test(description = "Test Compression.AUTO, with contentTypes and without Accept-Encoding header. + " +
            "The response here means the one that should be sent to transport, not to end user.")
    public void testAutoCompressWithContentTypes() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/autoCompressWithContentType",
                                                                        HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertNull(response.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING.toString()),
                          "The content-encoding header should be null and the identity which means no compression " +
                                  "should be done to the response");
    }

    @Test(description = "Test Compression.ALWAYS, with no Accept-Encoding header. The response here means the one " +
            "that should be sent to transport, not to end user.")
    public void testAlwaysCompress() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/alwaysCompress",
                HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()), ENCODING_GZIP,
                "The content-encoding header should be gzip.");
    }

    @Test(description = "Test Compression.ALWAYS, with Accept-Encoding header. The response here means the one " +
            "that should be sent to transport, not to end user.")
    public void testAlwaysCompressWithAcceptEncoding() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header(HttpHeaderNames.ACCEPT_ENCODING.toString(), ENCODING_DEFLATE));
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/alwaysCompress",
                HttpConstants.HTTP_METHOD_GET, headers, "hello");
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertNull(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                "The content-encoding header should be set to null and the transport will use the original" +
                        "Accept-Encoding value for compression.");
    }

    @Test(description = "Test Compression.ALWAYS, with contentTypes and without Accept-Encoding header. " +
            "The response here means the one that should be sent to transport, not to end user.")
    public void testAlwaysCompressWithContentTypes() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/alwaysCompressWithContentType",
                                                                        HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()), ENCODING_GZIP,
                            "The content-encoding header should be gzip.");
    }

    @Test(description = "Test Compression.NEVER, with no Accept-Encoding header. The response here means the one " +
            "that should be sent to transport, not to end user.")
    public void testNeverCompress() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/neverCompress",
                HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                HTTP_TRANSFER_ENCODING_IDENTITY, "The content-encoding header of the response that was sent " +
                        "to transport should be set to identity.");
    }

    @Test(description = "Test Compression.NEVER, with a user overridden content-encoding header. The response here " +
            "means the one that should be sent to transport, not to end user.")
    public void testNeverCompressWithAcceptEncoding() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header(HttpHeaderNames.ACCEPT_ENCODING.toString(), ENCODING_GZIP));
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/userOverridenValue",
                HttpConstants.HTTP_METHOD_GET, headers, "hello");
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                ENCODING_DEFLATE, "The content-encoding header of the response that was sent " +
                        "to transport should be set to identity.");
    }

    @Test(description = "Test Compression.NEVER, with contentTypes. The response here means the one " +
            "that should be sent to transport, not to end user.")
    public void testNeverCompressWithContentTypes() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/neverCompressWithContentType",
                                                                        HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                            HTTP_TRANSFER_ENCODING_IDENTITY, "The content-encoding header of the response " +
                                    "that was sent to transport should be set to identity.");
    }

    @Test(description = "Test Compression.AUTO, with incompatible contentTypes. + " +
            "The response here means the one that should be sent to transport, not to end user.")
    public void testAutoCompressWithIncompatibleContentTypes() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/autoCompressWithInCompatibleContentType",
                                                                        HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                            HTTP_TRANSFER_ENCODING_IDENTITY, "The content-encoding header of the response " +
                                    "that was sent to transport should be set to identity.");
    }

    @Test(description = "Test Compression.ALWAYS, with empty contentTypes. + " +
            "The response here means the one that should be sent to transport, not to end user.")
    public void testAlwaysCompressWithEmptyContentTypes() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/alwaysCompressWithEmptyContentType",
                                                                        HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()), ENCODING_GZIP,
                            "The content-encoding header should be gzip.");
    }
}
