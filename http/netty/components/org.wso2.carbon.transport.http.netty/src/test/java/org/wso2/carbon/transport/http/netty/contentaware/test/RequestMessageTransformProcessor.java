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

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Message processor for transform request message
 */
public class RequestMessageTransformProcessor implements CarbonMessageProcessor {

    private String transformedValue;

    private TransportSender transportSender;

    public RequestMessageTransformProcessor(String transformedValue) {
        this.transformedValue = transformedValue;
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        List<ByteBuffer> byteBufferList = carbonMessage.getFullMessageBody();
        carbonMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
        carbonMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);

        if (transformedValue != null) {
            byte[] array = transformedValue.getBytes("UTF-8");
            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
            byteBuffer.put(array);
            carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
            byteBuffer.flip();
            carbonMessage.addMessageBody(byteBuffer);
            carbonMessage.setEndOfMsgAdded(true);
            transportSender.send(carbonMessage, carbonCallback);
        }
        return false;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
        this.transportSender = transportSender;
    }

    @Override
    public String getId() {
        return null;
    }
}
