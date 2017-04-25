/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.input.transport.jms.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

public class JMSMessageProcessor implements CarbonMessageProcessor {
    private static final Logger log = Logger.getLogger(JMSMessageProcessor.class);

    private SourceEventListener sourceEventListener;

    public JMSMessageProcessor(SourceEventListener sourceEventListener) {
        this.sourceEventListener = sourceEventListener;
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        if (carbonMessage instanceof TextCarbonMessage) {
            String event = ((TextCarbonMessage) carbonMessage).getText().toString();
            sourceEventListener.onEvent(event);
        } else if (carbonMessage instanceof MapCarbonMessage) {

        } else if (carbonMessage instanceof BinaryCarbonMessage) {

        }
        if (carbonCallback != null) {
            carbonCallback.done(carbonMessage);
        }
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {

    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {

    }

    @Override
    public String getId() {
        return null;
    }
}
