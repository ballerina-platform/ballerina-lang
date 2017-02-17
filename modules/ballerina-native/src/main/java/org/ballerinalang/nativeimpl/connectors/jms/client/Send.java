/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.nativeimpl.connectors.jms.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Connector;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.connectors.jms.utils.JMSConstants;
import org.ballerinalang.nativeimpl.connectors.jms.utils.JMSMessageUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Post} is the send action implementation of the JMS Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.jms",
        actionName = "send",
        connectorName = ClientConnector.CONNECTOR_NAME,
        args = { @Argument(name = "jmsClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "connectionFactoryName", type = TypeEnum.STRING),
                 @Argument(name = "destinationName", type = TypeEnum.STRING),
                 @Argument(name = "destinationType", type = TypeEnum.STRING),
                 @Argument(name = "msgType", type = TypeEnum.STRING),
                 @Argument(name = "message", type = TypeEnum.MESSAGE),
                 @Argument(name = "properties", type = TypeEnum.MAP) },
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)})
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "SEND action implementation of the JMS Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connectionFactoryName",
        value = "Connection Factory Name") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destinationName",
        value = "Destination Name") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destinationType",
        value = "Destination Type") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "msgType",
        value = "Message Type") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "message",
        value = "Message") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "properties",
        value = "Properties") })
public class Send extends AbstractJMSAction {
    private static final Logger log = LoggerFactory.getLogger(Send.class);

    @Override
    public BValue execute(Context context) {

        // Extracting Argument values
        BConnector bConnector = (BConnector) getArgument(context, 0);
        Connector connector = bConnector.value();
        if (!(connector instanceof ClientConnector)) {
            throw new BallerinaException("Need to use a JMSConnector as the first argument", context);
        }
        //Getting ballerina message and extract carbon message.
        BMessage bMessage = (BMessage) getArgument(context, 5);
        if (bMessage == null) {
            throw new BallerinaException("Ballerina message not found", context);
        }
        CarbonMessage message = bMessage.value();
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>();
        //Getting the map of properties.
        BMap properties = (BMap) getArgument(context, 6);

        //Creating message content according to the message type.
        String messageType = getArgument(context, 4).stringValue();
        if (messageType.equalsIgnoreCase(JMSConstants.TEXT_MESSAGE_TYPE) ||
            messageType.equalsIgnoreCase(JMSConstants.BYTES_MESSAGE_TYPE)) {
            BallerinaMessageDataSource ballerinaMessageDataSource = bMessage.getMessageDataSource();
            if (ballerinaMessageDataSource != null) {
                if (ballerinaMessageDataSource instanceof StringDataSource) {
                    message = new TextCarbonMessage(((StringDataSource) ballerinaMessageDataSource).getValue());
                } else {
                    throw new BallerinaException(
                            "If the message type is " + messageType + ", a string payload must be set", context);
                }
            } else if (!(message instanceof TextCarbonMessage)) {
                throw new BallerinaException(
                        "If the message type is " + messageType + ", either string payload should be set or " +
                        "pass a received jms text or bytes message", context);
            }
            propertyMap.put(JMSConstants.JMS_MESSAGE_TYPE, messageType);
        } else if (messageType.equalsIgnoreCase(JMSConstants.OBJECT_MESSAGE_TYPE)) {
            message = JMSMessageUtils.toSerializableCarbonMessage(bMessage);
            propertyMap.put(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.OBJECT_MESSAGE_TYPE);
        } else if (messageType.equalsIgnoreCase(JMSConstants.MAP_MESSAGE_TYPE)) {
            BValue bValue = properties.get(new BString(JMSConstants.MAP_DATA));
            if (bValue != null) {
                message = new MapCarbonMessage();
                MapCarbonMessage mapCarbonMessage = (MapCarbonMessage) message;
                if (bValue instanceof BMap) {
                    BMap mapData = (BMap) bValue;
                    for (Object o : mapData.keySet()) {
                        BValue key = (BValue) o;
                        BValue value = mapData.get(key);
                        if (key instanceof BString && value instanceof BString) {
                            mapCarbonMessage.setValue(key.stringValue(), value.stringValue());
                        }
                    }
                }
            } else if (!(message instanceof MapCarbonMessage)) {
                throw new BallerinaException(
                        "If the message type is MapMessage, either set MapData property or pass a received" +
                        " jms map message", context);
            }
            propertyMap.put(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.MAP_MESSAGE_TYPE);
        } else {
            propertyMap.put(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.GENERIC_MESSAGE_TYPE);
        }
        //Getting necessary values from the connector instance.
        propertyMap.put(JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME,
                        ((ClientConnector) connector).getInitialContextFactory());
        propertyMap.put(JMSConstants.PROVIDER_URL_PARAM_NAME,
                        ((ClientConnector) connector).getJndiProviderUrl());

        //Getting necessary parameter values.
        propertyMap.put(JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME,
                        getArgument(context, 1).stringValue());
        propertyMap.put(JMSConstants.DESTINATION_PARAM_NAME, getArgument(context, 2).stringValue());
        propertyMap.put(JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME,
                        getArgument(context, 3).stringValue());
        //Setting optional parameters.
        if (properties.get(new BString(JMSConstants.CONNECTION_USERNAME)) != null) {
            propertyMap.put(JMSConstants.CONNECTION_USERNAME,
                            properties.get(new BString(JMSConstants.CONNECTION_USERNAME))
                                      .stringValue());
        }
        if (properties.get(new BString(JMSConstants.CONNECTION_PASSWORD)) != null) {
            propertyMap.put(JMSConstants.CONNECTION_PASSWORD,
                            properties.get(new BString(JMSConstants.CONNECTION_PASSWORD)).stringValue());
        }
        if (properties.get(new BString(JMSConstants.CACHE_LEVEL)) != null) {
            propertyMap
                    .put(JMSConstants.CACHE_LEVEL, properties.get(new BString(JMSConstants.CACHE_LEVEL)).stringValue());
        } else {
            propertyMap.put(JMSConstants.CACHE_LEVEL, Integer.toString(JMSConstants.CACHE_NONE));
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Sending " + messageType + " to " +
                          propertyMap.get(JMSConstants.DESTINATION_PARAM_NAME));
            }
            //Getting the sender instance and sending the message.
            BallerinaConnectorManager.getInstance().getClientConnector("jms")
                                     .send(message, null, propertyMap);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Exception occurred while sending message.", e, context);
        }
        return null;
    }

    @Override public void execute(Context context, BalConnectorCallback connectorCallback) {
        //Not needed for jms.
    }

    @Override public void validate(BalConnectorCallback connectorCallback) {
        //Not needed for jms.
    }

}
