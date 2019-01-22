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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.net.jms.nativeimpl.endpoint.common;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsMessageListenerImpl;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

/**
 * Util class to create and register a message listener.
 *
 * @since 0.970
 */
public class MessageListenerHandler {

    private MessageListenerHandler() {
    }

    public static void createAndRegister(Context context) {
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        BMap<String, BValue> consumerConnector =
                (BMap<String, BValue>) ((BMap<String, BValue>) context.getRefArgument(0)).get("consumerActions");

        Resource resource = JmsUtils.extractJMSResource(service);

        Object nativeData = consumerConnector.getNativeData(JmsConstants.JMS_CONSUMER_OBJECT);
        if (nativeData instanceof MessageConsumer) {
            MessageListener listener = new JmsMessageListenerImpl(resource, consumerConnector);
            try {
                ((MessageConsumer) nativeData).setMessageListener(listener);
            } catch (JMSException e) {
                BallerinaAdapter.throwBallerinaException("Error registering the message listener for service"
                                                 + service.getPackage() + service.getName(), context, e);
            }
        }
    }
}
