/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
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
package org.ballerinalang.queue;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.Message;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.jms.Constants;

public class WorkflowSubscriber extends Consumer {

    String queueName;
    CallableUnitCallback callableUnitCallback;
    Context context;

    public WorkflowSubscriber(String queueName, CallableUnitCallback callableUnitCallback, Context
            context) {
        this.queueName = queueName;
        this.callableUnitCallback = callableUnitCallback;
        this.context = context;
    }

    @Override
    protected void send(Message message) throws BrokerException {
        // Inject the Message (if received) into a JMSMessage struct.
        BStruct bStruct = BLangConnectorSPIUtil
                .createBStruct(this.context, Constants.PROTOCOL_PACKAGE_QUEUE, Constants.JMS_MESSAGE_STRUCT_NAME);
        bStruct.addNativeData(org.ballerinalang.net.jms.Constants.JMS_API_MESSAGE,
                message);
        bStruct.addNativeData(Constants.INBOUND_REQUEST, Boolean.FALSE);
        BrokerUtils.removeSubscription(this);

        context.setReturnValues(bStruct);
        callableUnitCallback.notifySuccess();
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    protected void close() throws BrokerException {

    }

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }
}