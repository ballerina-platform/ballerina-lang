/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.transport.http.netty.contractimpl.listener.http2;

import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Message holder for inbound request and push response. Keeps track of the last read or write execution time.
 */
public class InboundMessageHolder {
    private HttpCarbonMessage inboundMsg;
    private long lastReadWriteTime;
    private Http2OutboundRespListener http2OutboundRespListener;

    public InboundMessageHolder(HttpCarbonMessage inboundMsgOrPushResponse) {
        this.inboundMsg = inboundMsgOrPushResponse;
    }

    /**
     * Gets last read or write operation execution time.
     *
     * @return the last read or write operation execution time
     */
    long getLastReadWriteTime() {
        return lastReadWriteTime;
    }

    /**
     * Sets the last read or write operation execution time.
     *
     * @param lastReadWriteTime the last read or write operation execution time
     */
    void setLastReadWriteTime(long lastReadWriteTime) {
        this.lastReadWriteTime = lastReadWriteTime;
    }

    public HttpCarbonMessage getInboundMsg() {
        return inboundMsg;
    }

    public Http2OutboundRespListener getHttp2OutboundRespListener() {
        return http2OutboundRespListener;
    }

    public void setHttp2OutboundRespListener(Http2OutboundRespListener http2OutboundRespListener) {
        this.http2OutboundRespListener = http2OutboundRespListener;
    }
}
