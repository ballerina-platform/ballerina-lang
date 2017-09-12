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

package org.ballerinalang.nativeimpl.actions.jms.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.actions.jms.utils.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.services.dispatchers.jms.JMSUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.MessageUtil;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.jms.exception.JMSConnectorException;
import org.wso2.carbon.transport.jms.impl.JMSConnectorFactoryImpl;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.Map;

/**
 * {@code Post} is the send action implementation of the JMS Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.jms",
        actionName = "send",
        connectorName = Constants.CONNECTOR_NAME,
        args = { @Argument(name = "jmsClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "destinationName", type = TypeEnum.STRING),
                 @Argument(name = "msgType", type = TypeEnum.STRING),
                 @Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)},
        connectorArgs = {
                @Argument(name = "properties", type = TypeEnum.MAP)
        })
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "SEND action implementation of the JMS Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destinationName",
        value = "Destination Name") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "msgType",
        value = "Message Type") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "message",
        value = "Message") })
public class Send extends AbstractJMSAction {
    private static final Logger log = LoggerFactory.getLogger(Send.class);

    @Override
    public BValue execute(Context context) {

        // Extract argument values
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        BMessage bMessage = (BMessage) getRefArgument(context, 1);
        String destination = getStringArgument(context, 0);

        CarbonMessage message = bMessage.value();
        validateParams(bConnector);

        // Set return value to the current frame
        BValue valueRef = new BBoolean(true);
        context.getControlStackNew().currentFrame.returnValues[0] = valueRef;

        // Get the map of properties.
        BMap<String, BString> properties = (BMap<String, BString>) bConnector.getRefField(0);

        Map<String, String> propertyMap = JMSUtils.preProcessJmsConfig(properties);

        // Create message content according to the message type.
        String messageType = message.getHeader(org.ballerinalang.nativeimpl.lang.utils.Constants.CONTENT_TYPE);
        if (messageType == null) {
            messageType = org.ballerinalang.nativeimpl.lang.utils.Constants.OCTET_STREAM;
        }

        message.setHeader(Constants.JMS_MESSAGE_TYPE, messageType);
        switch (messageType) {
            case org.ballerinalang.nativeimpl.lang.utils.Constants.TEXT_PLAIN:
            case org.ballerinalang.nativeimpl.lang.utils.Constants.APPLICATION_JSON:
            case org.ballerinalang.nativeimpl.lang.utils.Constants.APPLICATION_XML:
                message = getTextCarbonMessage(bMessage, messageType, context);
                break;
            case org.ballerinalang.nativeimpl.lang.utils.Constants.APPLICATION_FORM:
                message = getMapCarbonMessage(bMessage, context);
                break;
            case org.ballerinalang.nativeimpl.lang.utils.Constants.OCTET_STREAM:
            default:
                message = getBlobCarbonMessage(bMessage, context);
        }

        propertyMap.put(JMSConstants.PARAM_DESTINATION_NAME, destination);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Sending " + messageType + " to " + propertyMap.get(JMSConstants.PARAM_DESTINATION_NAME));
            }

            new JMSConnectorFactoryImpl().createClientConnector().send(message, propertyMap);
        } catch (JMSConnectorException e) {
            throw new BallerinaException("Failed to send message. " + e.getMessage(), e, context);
        }
        return null;
    }

    private CarbonMessage getBlobCarbonMessage(BMessage bMessage, Context context) {
        return bMessage.value();
    }

    private CarbonMessage getMapCarbonMessage(BMessage bMessage, Context context) {

        CarbonMessage carbonMessage = bMessage.value();
        if (carbonMessage instanceof MapCarbonMessage) {
            return carbonMessage;
        }

        BallerinaMessageDataSource dataSource = bMessage.getMessageDataSource();
        if (dataSource instanceof BMap) {
            MapCarbonMessage mapCarbonMessage = MessageUtil.createMapMessageWithoutData(carbonMessage);
            BMap<String, ? extends BValue> mapData = (BMap) dataSource;
            for (String key : mapData.keySet()) {
                BValue value = mapData.get(key);
                mapCarbonMessage.setValue(key, value.stringValue());
            }
            return mapCarbonMessage;
        } else {
            throw new BallerinaException("Invalid Map Message provided", context);
        }
    }

    private CarbonMessage getTextCarbonMessage(BMessage bMessage, String messageType, Context context) {
        CarbonMessage carbonMessage = bMessage.value();
        if (carbonMessage instanceof TextCarbonMessage) {
            return carbonMessage;
        }

        BallerinaMessageDataSource dataSource = bMessage.getMessageDataSource();
        if (dataSource instanceof StringDataSource || dataSource instanceof BJSON
                || dataSource instanceof BXML) {
            carbonMessage = MessageUtil.createTextMessageWithData(carbonMessage);
        } else {
            throw new BallerinaException("Invalid message type provided. Message Type: " + messageType, context);
        }
        return carbonMessage;
    }

    @Override public void execute(Context context, BalConnectorCallback connectorCallback) {
        //Not needed for jms.
    }

    @Override public void validate(BalConnectorCallback connectorCallback) {
        //Not needed for jms.
    }

}
