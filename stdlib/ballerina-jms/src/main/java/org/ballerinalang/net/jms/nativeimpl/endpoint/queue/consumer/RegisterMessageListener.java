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

package org.ballerinalang.net.jms.nativeimpl.endpoint.queue.consumer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.JmsMessageListenerImpl;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

/**
 * Register JMS listener for a consumer endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "jms",
        functionName = "registerListener",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "QueueConsumer", structPackage = "ballerina.jms"),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPEDESC),
                @Argument(name = "connector", type = TypeKind.STRUCT, structType = "QueueConsumerConnector")
        },
        isPublic = true
)
public class RegisterMessageListener implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Struct queueConsumerBObject = BallerinaAdapter.getReceiverStruct(context);
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        BStruct consumerConnector = (BStruct) context.getRefArgument(2);

        Resource resource = JMSUtils.extractJMSResource(service);

        Object nativeData = consumerConnector.getNativeData(Constants.JMS_CONSUMER_OBJECT);
        if (nativeData instanceof MessageConsumer) {
            MessageListener listener = new JmsMessageListenerImpl(resource, queueConsumerBObject.getVMValue());
            try {
                ((MessageConsumer) nativeData).setMessageListener(listener);
            } catch (JMSException e) {
                throw new BallerinaException("Error registering the message listener for service"
                                                     + service.getPackage() + service.getName());
            }
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
