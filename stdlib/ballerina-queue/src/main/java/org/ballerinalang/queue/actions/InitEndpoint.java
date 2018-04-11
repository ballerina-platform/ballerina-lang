/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.queue.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.queue.JMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * {@code InitEndpoint} is the InitEndpoint action implementation of the JMS Connector.
 *
 * @since 0.9
 */
@BallerinaFunction(orgName = "ballerina", packageName = "queue",
                   functionName = "initEndpoint",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "QueueEndpoint",
                                        structPackage = "ballerina.queue"),
                   args = {
//                           @Argument(name = "epName",
//                                     type = TypeKind.STRING),
                           @Argument(name = "config",
                                     type = TypeKind.STRUCT,
                                     structType = "ClientEndpointConfiguration")
                   },
                   isPublic = true
)
public class InitEndpoint extends AbstractJMSAction {
    private static final Logger log = LoggerFactory.getLogger(InitEndpoint.class);


    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        log.info("Init got called");
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct clientEndpointConfig = clientEndpoint.getStructField("config");

        //        BConnector bConnector = (BConnector) context.getRefArgument(0);
        //        validateParams(bConnector);

        // Create the JMS Transport Client Connector and store it as a native data in the Ballerina JMS Client
        // Connector under the key of JMS_TRANSPORT_CLIENT_CONNECTOR
        // When performing an action this native object can be retrieved
        //        BStruct connectorConfig = ((BStruct) bConnector.getRefField(0));
        Map<String, String> propertyMap = JMSUtils.preProcessEndpointConfig(clientEndpointConfig);
        JMSUtils.preProcessIfWso2MB(propertyMap);
        JMSUtils.updateMappedParameters(propertyMap);

//        JMSClientConnector jmsClientConnector;
//        try {
//            jmsClientConnector = new JMSConnectorFactoryImpl().createClientConnector(propertyMap);
//            clientEndpoint.addNativeData(Constants.JMS_TRANSPORT_CLIENT_CONNECTOR, jmsClientConnector);
//        } catch (JMSConnectorException e) {
//            throw new BallerinaException("failed to create jms client connector. " + e.getMessage(), e, context);
//        }

        BStruct ballerinaClientConnector
                = BLangConnectorSPIUtil.createBStruct(context.getProgramFile(), Constants.PROTOCOL_PACKAGE_QUEUE,
                                                      Constants.CLIENT_CONNECTOR, UUID.randomUUID().toString(),
                                                      clientEndpointConfig.getVMValue());
//        ballerinaClientConnector.addNativeData(Constants.JMS_TRANSPORT_CLIENT_CONNECTOR, jmsClientConnector);
        clientEndpoint.addNativeData(Constants.B_CLIENT_CONNECTOR, ballerinaClientConnector);

        callableUnitCallback.notifySuccess();
    }

    public boolean isBlocking() {
        return false;
    }
}
