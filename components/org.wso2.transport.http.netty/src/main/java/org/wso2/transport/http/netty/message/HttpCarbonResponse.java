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

package org.wso2.transport.http.netty.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Contains information available for http response
 */
public class HttpCarbonResponse extends HTTPCarbonMessage {

    private HttpResponse httpResponse;

    public HttpCarbonResponse(HttpResponse httpResponse) {
        super(httpResponse);
        this.httpResponse = (HttpResponse) this.httpMessage;
    }

    public HttpCarbonResponse(HttpResponse httpResponse, ChannelHandlerContext ctx) {
        super(httpResponse, new DefaultListener(ctx));
        this.httpResponse = (HttpResponse) this.httpMessage;
    }

    public void setStatus(HttpResponseStatus httpResponseStatus) {
        this.httpResponse.setStatus(httpResponseStatus);
    }
}
