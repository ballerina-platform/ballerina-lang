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
package org.ballerinalang.test.services.configuration;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.bre.Context;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompressionConfigurationTest {

    @Test(enabled = false)
    public void testOptionCompressionEnabled() {
        Context context = mock(Context.class);
        ServiceInfo serviceInfo = mock(ServiceInfo.class);
        AnnAttachmentInfo annAttachmentInfo = mock(AnnAttachmentInfo.class);
        AnnAttributeValue annAttributeValue = mock(AnnAttributeValue.class);

        when(context.getServiceInfo()).thenReturn(serviceInfo);
        when(serviceInfo
                     .getAnnotationAttachmentInfo(HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.ANN_NAME_CONFIG))
                .thenReturn(annAttachmentInfo);
        when(annAttachmentInfo.getAttributeValue(HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_ENABLED)).thenReturn(
                annAttributeValue);
        when(annAttributeValue.getBooleanValue()).thenReturn(true);

        HTTPCarbonMessage outBoundMessage = new HTTPCarbonMessage(
                new DefaultFullHttpResponse(HTTP_1_1, new HttpResponseStatus(200, "OK")));
        HttpUtil.setCompressionHeaders(context, outBoundMessage);
        Assert.assertNull(outBoundMessage.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                            "The content-encoding header should be null");
    }

    @Test(enabled = false)
    public void testOptionCompressionDisabled() {
        Context context = mock(Context.class);
        ServiceInfo serviceInfo = mock(ServiceInfo.class);
        AnnAttachmentInfo annAttachmentInfo = mock(AnnAttachmentInfo.class);
        AnnAttributeValue annAttributeValue = mock(AnnAttributeValue.class);

        when(context.getServiceInfo()).thenReturn(serviceInfo);
        when(serviceInfo
                     .getAnnotationAttachmentInfo(HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.ANN_NAME_CONFIG))
                .thenReturn(annAttachmentInfo);
        when(annAttachmentInfo.getAttributeValue(HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_ENABLED)).thenReturn(
                annAttributeValue);
        when(annAttributeValue.getBooleanValue()).thenReturn(false);

        HTTPCarbonMessage outBoundMessage = new HTTPCarbonMessage(
                new DefaultFullHttpResponse(HTTP_1_1, new HttpResponseStatus(200, "OK")));
        HttpUtil.setCompressionHeaders(context, outBoundMessage);
        Assert.assertEquals(outBoundMessage.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                            Constants.HTTP_TRANSFER_ENCODING_IDENTITY,
                            "The content-encoding header should be identity");
    }
}
