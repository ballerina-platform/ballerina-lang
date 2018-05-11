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

package org.ballerinalang.queue.nativeImpl.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.queue.JMSUtils;
import org.ballerinalang.queue.QueueConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * Get Type header from the JMS Message.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "queue",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "QueueEndpoint",
                             structPackage = "ballerina.queue"),
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class InitEndpoint extends AbstractBlockinAction {

    private static final Logger log = LoggerFactory.getLogger(InitEndpoint.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        log.info("Init got called");
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct clientEndpointConfig = clientEndpoint.getStructField("config");

        Map<String, String> propertyMap = JMSUtils.preProcessEndpointConfig(clientEndpointConfig);
        JMSUtils.preProcessIfWso2MB(propertyMap);
        JMSUtils.updateMappedParameters(propertyMap);

        BStruct ballerinaClientConnector
                = BLangConnectorSPIUtil.createBStruct(context.getProgramFile(), QueueConstants.PROTOCOL_PACKAGE_QUEUE,
                QueueConstants.CLIENT_CONNECTOR, UUID.randomUUID().toString(),
                clientEndpointConfig.getVMValue());
        clientEndpoint.addNativeData(QueueConstants.B_CLIENT_CONNECTOR, ballerinaClientConnector);
        if (callableUnitCallback != null) {
            callableUnitCallback.notifySuccess();
        }
    }
}
