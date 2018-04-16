/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.net.websub.hub;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.Message;
import org.ballerinalang.broker.BrokerUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * WebSub Subscriber representation for the Broker.
 *
 * @since 0.965.0
 */
class HubSubscriber extends Consumer {

    private final String queue;
    private final String topic;
    private final String callback;
    private final BStruct subscriptionDetails;

    HubSubscriber(String queue, String topic, String callback, BStruct subscriptionDetails) {
        this.queue = queue;
        this.topic = topic;
        this.callback = callback;
        this.subscriptionDetails = subscriptionDetails;
    }

    @Override
    protected void send(Message message) throws BrokerException {
        ProgramFile programFile = Hub.getInstance().getHubProgramFile();
        byte[] bytes = BrokerUtils.retrieveBytes(message);
        BValue[] args = {new BString(callback),
                subscriptionDetails,
                new BJSON(new String(bytes, StandardCharsets.UTF_8))};
        BLangFunctions.invokeCallable(programFile.getPackageInfo("websub.hub")
                                     .getFunctionInfo("distributeContent"), args);
    }

    @Override
    public String getQueueName() {
        return queue;
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

    @Override
    public boolean equals(Object subscriberObject) {
        if (subscriberObject instanceof HubSubscriber) {
            HubSubscriber subscriber = (HubSubscriber) subscriberObject;
            return subscriber.topic.equals(topic) && subscriber.callback.equals(callback);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, callback);
    }
}
