/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.input.transport.kafka;

import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.transport.PassThroughInputMapper;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.Subscription;
import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;

public class KafkaInputTransportTestCase {
    private static final Logger log = Logger.getLogger(KafkaInputTransportTestCase.class);

    @Test
    public void testCreatingKafkaSubscription() throws InterruptedException {
        try{
            Subscription subscription = Subscription.Subscribe(Transport.transport("kafka")
                    .option("topic", "test22")
                    .option("threads", "1")
                    .option("group.id", "group11")
                    .option("zookeeper.connect", "localhost")
                    .option("adapter.name", "org.wso2.cep.kafka.receiver"));
            subscription.map(Mapping.format("passThrough"));
            subscription.insertInto("FooStream");

            ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
            executionPlan.defineStream(StreamDefinition.id("FooStream")
                    .attribute("symbol", Attribute.Type.STRING)
                    .attribute("price", Attribute.Type.FLOAT)
                    .attribute("volume", Attribute.Type.INT));
            executionPlan.addSubscription(subscription);
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("inputmapper:passThrough", PassThroughInputMapper.class);

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
            executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                }
            });
            executionPlanRuntime.start();
            Thread.sleep(10000);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }
}

