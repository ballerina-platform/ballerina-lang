/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.distributed.transport.single.client.test;

import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.output.mapper.text.TextOutputMapper;

/**
 * Created by sajith on 3/8/17.
 */
public class SingleClientDistributedTransportTestCases {
    @Test
    public void testPublisherWithPartitionedKafkaPublisher() throws InterruptedException {
        try {

            String inStreamDefinition = "" +
                    "define stream FooStream (symbol string, price float, volume int); " +

                    "@sink(type='kafka', topic='kafka_topic', bootstrap.servers='localhost:9092'," +
                        "@map(type='text')," +
                        "@distribution(publishingStrategy='roundRobin', partitionKey='symbol', " +
                    "       @destination(partition.no='1')," +
                    "       @destination(partition.no='2')))" +
                    "@info(name = 'query1')" +
                    "define stream BarStream (symbol string, price float, volume int);";

            String query = ("@info(name = 'query1') " +
                    "from FooStream " +
                    "select symbol, price, volume " +
                    "insert into BarStream;");

            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("outputmapper:text", TextOutputMapper.class);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
            executionPlanRuntime.start();
            executionPlanRuntime.addCallback("query1", new QueryCallback() {
                        @Override
                        public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                            EventPrinter.print(timeStamp, inEvents, removeEvents);
                        }
                    });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
            Thread.sleep(10000);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            //log.warn("No zookeeper may not be available.", ex);
        }
    }
}
