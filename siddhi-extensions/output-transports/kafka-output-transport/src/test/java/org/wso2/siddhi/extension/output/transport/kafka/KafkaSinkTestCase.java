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

package org.wso2.siddhi.extension.output.transport.kafka;

import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.output.mapper.text.TextSinkMapper;

public class KafkaSinkTestCase {
    static final Logger log = Logger.getLogger(KafkaSinkTestCase.class);

    //    @Test
    public void testPublisherWithKafkaTransport() throws InterruptedException {
        log.info("Creating test for publishing events for static topic with a partition");
        try {
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("sinkMapper:text", TextSinkMapper.class);
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@app:name('TestSiddhiApp') " +
                            "define stream FooStream (symbol string, price float, volume long); " +
                            "@info(name = 'query1') " +
                            "@sink(type='kafka', topic='kafka_topic', bootstrap.servers='localhost:9092', partition" +
                            ".no='0', " +
                            "@map(type='text'))" +
                            "Define stream BarStream (symbol string, price float, volume long);" +
                            "from FooStream select symbol, price, volume insert into BarStream;");
            InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"WSO2", 55.6f, 100L});
            fooStream.send(new Object[]{"IBM", 75.6f, 100L});
            fooStream.send(new Object[]{"WSO2", 57.6f, 100L});
            Thread.sleep(10000);
            siddhiAppRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }

    //    @Test
    public void testPublisherWithKafkaTransportWithDynamicTopic() throws InterruptedException {
        log.info("Creating test for publishing events for dynamic topic without partition");
        try {
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("sinkMapper:text", TextSinkMapper.class);
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@app:name('TestSiddhiApp') " +
                            "define stream FooStream (symbol string, price float, volume long); " +
                            "@info(name = 'query1') " +
                            "@sink(type='kafka', topic='{{symbol}}', bootstrap.servers='localhost:9092', " +
                            "@map(type='text'))" +
                            "Define stream BarStream (symbol string, price float, volume long);" +
                            "from FooStream select symbol, price, volume insert into BarStream; "
            );
            InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
            siddhiAppRuntime.start();
            Thread.sleep(2000);
            fooStream.send(new Object[]{"simple_topic", 55.6f, 100L});
            fooStream.send(new Object[]{"simple_topic", 75.6f, 100L});
            fooStream.send(new Object[]{"simple_topic", 57.6f, 100L});
            Thread.sleep(5000);
            siddhiAppRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }
}
