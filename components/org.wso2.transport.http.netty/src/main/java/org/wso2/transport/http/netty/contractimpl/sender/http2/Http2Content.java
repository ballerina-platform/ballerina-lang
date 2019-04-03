/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.transport.http.netty.contractimpl.sender.http2;

import io.netty.handler.codec.http.HttpContent;

/**
 * Represents HTTP2 request content.
 */
public class Http2Content {
    private final HttpContent httpContent;
    private final OutboundMsgHolder outboundMsgHolder;

    Http2Content(HttpContent httpContent, OutboundMsgHolder outboundMsgHolder) {
        this.httpContent = httpContent;
        this.outboundMsgHolder = outboundMsgHolder;
    }

    public HttpContent getHttpContent() {
        return httpContent;
    }

    public OutboundMsgHolder getOutboundMsgHolder() {
        return outboundMsgHolder;
    }
}
