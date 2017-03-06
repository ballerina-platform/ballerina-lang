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

package org.wso2.siddhi.extension.input.transport.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;


public class KafkaConsumer implements Runnable {

    private KafkaStream stream;
    private String evento;
    private SourceEventListener sourceEventListener;
    private static final Logger log = Logger.getLogger(KafkaConsumer.class);

    public KafkaConsumer(KafkaStream inStream, SourceEventListener sourceEventListener) {
        stream = inStream;
        this.sourceEventListener = sourceEventListener;
    }

    public void run() {
        try {
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                evento = new String(it.next().message());
                if (log.isDebugEnabled()) {
                    log.debug("Event received in Kafka Event Adaptor - " + evento);
                }
                sourceEventListener.onEvent(evento);
            }
        } catch (Throwable t) {
            log.error("Error while consuming event " + t);
        }
    }
}
