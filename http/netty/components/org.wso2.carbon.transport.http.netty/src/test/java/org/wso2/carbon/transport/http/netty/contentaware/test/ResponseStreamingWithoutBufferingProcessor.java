/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.contentaware.test;

import io.netty.handler.codec.http.HttpHeaders;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.nio.ByteBuffer;

/**
 * A Message Processor which respond in streaming manner without buffering.
 */
public class ResponseStreamingWithoutBufferingProcessor implements CarbonMessageProcessor {

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback callback) throws Exception {
        CarbonMessage cMsg = new DefaultCarbonMessage(false);
        cMsg.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        cMsg.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        cMsg.setHeader(HttpHeaders.Names.CONTENT_TYPE, Constants.TEXT_PLAIN);
        cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
        callback.done(cMsg);
        while (!(carbonMessage.isEmpty() && carbonMessage.isEndOfMsgAdded())) {
            ByteBuffer byteBuffer = carbonMessage.getMessageBody();
            if (byteBuffer != null) {
                cMsg.addMessageBody(byteBuffer);
            }
        }
        cMsg.setEndOfMsgAdded(true);
        return false;
    }

    @Override
    public void setTransportSender(TransportSender sender) {

    }

    @Override
    public String getId() {
        return "ResponseWritingWithoutBufferingProcessor";
    }
}
