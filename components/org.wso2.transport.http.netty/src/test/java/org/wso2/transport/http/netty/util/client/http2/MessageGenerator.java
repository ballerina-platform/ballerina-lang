/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.util.client.http2;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * A utility class which generates HTTP/2.0 requests.
 */
public class MessageGenerator {

    public static HTTPCarbonMessage generateRequest(HttpMethod httpMethod, String payload) {
        HTTPCarbonMessage httpCarbonMessage = new HttpCarbonRequest(new DefaultHttpRequest(
                new HttpVersion(Constants.DEFAULT_VERSION_HTTP_1_1, true), httpMethod,
                "http://" + TestUtil.TEST_HOST + ":" + TestUtil.HTTP_SERVER_PORT));
        httpCarbonMessage.setProperty(Constants.HTTP_METHOD, httpMethod.toString());
        httpCarbonMessage.setProperty(Constants.HTTP_HOST, TestUtil.TEST_HOST);
        httpCarbonMessage.setProperty(Constants.HTTP_PORT, TestUtil.HTTP_SERVER_PORT);
        httpCarbonMessage.setHeader("Host", TestUtil.TEST_HOST + ":" + TestUtil.HTTP_SERVER_PORT);
        if (payload != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(payload.getBytes(Charset.forName("UTF-8")));
            httpCarbonMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        } else {
            httpCarbonMessage.addHttpContent(new DefaultLastHttpContent());
        }
        return httpCarbonMessage;
    }

    public static HTTPCarbonMessage generateResponse(String response) throws UnsupportedEncodingException {
        return generateResponse(response, null);
    }

    public static HTTPCarbonMessage generateResponse(String response, HttpResponseStatus status)
            throws UnsupportedEncodingException {
        if (status == null) {
            status = HttpResponseStatus.OK;
        }
        HTTPCarbonMessage httpResponse =
                new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1, status));
        httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
        httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
        httpResponse.setProperty(Constants.HTTP_STATUS_CODE, status.code());

        if (response != null) {
            byte[] responseByteValues = response.getBytes("UTF-8");
            ByteBuffer responseValueByteBuffer = ByteBuffer.wrap(responseByteValues);
            httpResponse.
                    addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(responseValueByteBuffer)));
        } else {
            httpResponse.completeMessage();
        }
        return httpResponse;
    }
}
