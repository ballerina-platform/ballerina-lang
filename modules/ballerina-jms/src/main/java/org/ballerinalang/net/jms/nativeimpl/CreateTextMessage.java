/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.jms.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.jms.exception.JMSConnectorException;
import org.wso2.carbon.transport.jms.impl.JMSConnectorFactoryImpl;

import java.util.Map;
import javax.jms.Message;

/**
 * Create Text JMS Message.
 */
@BallerinaFunction(packageName = "ballerina.net.jms", functionName = "createTextMessage", args = {
        @Argument(name = "clientConnector", type = TypeEnum.CONNECTOR) },
                   returnType = {@ReturnType(type = TypeEnum.STRUCT, structPackage = "ballerina.net.jms", structType = "JMSMessage")},
                   isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Create JMS Text message") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "JMSMessage",
                                                                         value = "Created JMS text message") })
public class CreateTextMessage extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(CreateTextMessage.class);

    public BValue[] execute(Context context) {

        BConnector connectorStruct = ((BConnector) this.getRefArgument(context, 0));
        BMap<String, BString> properties = (BMap<String, BString>) connectorStruct.getRefField(0);
        Map<String, String> propertyMap = JMSUtils.preProcessJmsConfig(properties);

        Message jmsMessage;
        CarbonMessage carbonMessage = new TextCarbonMessage("");

        try {
            jmsMessage = new JMSConnectorFactoryImpl().createClientConnector(propertyMap)
                    .createMessage("TextMessage");
        } catch (JMSConnectorException e) {
            throw new BallerinaException("Failed to create message. " + e.getMessage(), e, context);
        }

        BStruct bStruct = ConnectorUtils.createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_JMS, "JMSMessage");

        carbonMessage.setProperty(org.ballerinalang.net.jms.Constants.JMS_API_MESSAGE, jmsMessage);
        bStruct.addNativeData(org.ballerinalang.net.jms.Constants.TRANSPORT_MESSAGE, carbonMessage);

        return this.getBValues(bStruct);
    }
}
