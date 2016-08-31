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

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.MessageUtil;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class which read and write content through streams
 */
public class RequestResponseCreationStreamingProcessor implements CarbonMessageProcessor {

    private TransportSender transportSender;

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        InputStream inputStream = carbonMessage.getInputStream();
        CarbonMessage newMsg = MessageUtil.cloneCarbonMessageWithOutData(carbonMessage);
        OutputStream outputStream = newMsg.getOutputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);
        outputStream.write(bytes);
        outputStream.flush();
        newMsg.setEndOfMsgAdded(true);
        newMsg.setProperty(Constants.HOST, TestUtil.TEST_HOST);
        newMsg.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
        EngineCallBack engineCallBack = new EngineCallBack(carbonCallback);
        transportSender.send(newMsg, engineCallBack);
        return false;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
        this.transportSender = transportSender;
    }

    @Override
    public String getId() {
        return "request-response-streaming-processor";
    }

    private class EngineCallBack implements CarbonCallback {

        CarbonCallback requestCallBack;

        EngineCallBack(CarbonCallback carbonCallback) {
            requestCallBack = carbonCallback;
        }

        @Override
        public void done(CarbonMessage carbonMessage) {
            InputStream inputStream = carbonMessage.getInputStream();

            CarbonMessage newMsg = MessageUtil.cloneCarbonMessageWithOutData(carbonMessage);
            OutputStream outputStream = newMsg.getOutputStream();
            try {
                byte[] bytes = IOUtils.toByteArray(inputStream);
                outputStream.write(bytes);
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException("Cannot read Input Stream from Response", e);
            }
            newMsg.setEndOfMsgAdded(true);
            requestCallBack.done(newMsg);
        }

    }

}
