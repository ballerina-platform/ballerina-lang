/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.input.transport.jms;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.extension.input.transport.jms.exception.JMSInputAdaptorRuntimeException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This runnable processes each JMS message and sends it to siddhi.
 */
public class JMSWorkerThread implements Runnable {
    private CarbonMessage carbonMessage;
    private CarbonCallback carbonCallback;
    private SourceEventListener sourceEventListener;

    public JMSWorkerThread(CarbonMessage cMessage, CarbonCallback cCallback, SourceEventListener sourceEventListener) {
        this.carbonMessage = cMessage;
        this.carbonCallback = cCallback;
        this.sourceEventListener = sourceEventListener;
    }

    @Override
    public void run() {
        try {
            if (carbonMessage instanceof TextCarbonMessage) {
                String event = ((TextCarbonMessage) carbonMessage).getText();
                sourceEventListener.onEvent(event);
            } else if (carbonMessage instanceof MapCarbonMessage) {
                Map<String, String> event = new HashMap<>();
                Enumeration<String> mapNames = ((MapCarbonMessage) carbonMessage).getMapNames();
                while (mapNames.hasMoreElements()) {
                    String key = mapNames.nextElement();
                    event.put(key, ((MapCarbonMessage) carbonMessage).getValue(key));
                }
                sourceEventListener.onEvent(event);
            } else {
                throw new JMSInputAdaptorRuntimeException("The message type of the JMS message" +
                        carbonMessage.getClass() + " is not supported!");
            }
            // ACK only if the event is processed i.e: no exceptions thrown from the onEvent method.
            if (carbonCallback != null) {
                carbonCallback.done(carbonMessage);
            }
        } catch (RuntimeException e) {
            throw new JMSInputAdaptorRuntimeException("Failed to process JMS message.", e);
        }
    }
}
