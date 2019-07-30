// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/kafka;
import ballerina/transactions;

string topic = "commit-consumer-test-topic";

kafka:ProducerConfig producerConfigs = {
    bootstrapServers: "localhost:9094, localhost:9095, localhost:9096",
    clientId: "commit-producer",
    acks: "all",
    transactionalId: "comit-consumer-test-producer",
    noRetries: 3
};

kafka:Producer kafkaProducer = new(producerConfigs);

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:9094, localhost:9095, localhost:9096",
    groupId: "commit-consumer-test-group",
    offsetReset: "earliest",
    topics: [topic]
};

kafka:Consumer kafkaConsumer = new(consumerConfigs);

function funcTestKafkaProduce() {
    string msg = "Hello World";
    byte[] byteMsg = msg.toBytes();
    kafkaProduce(byteMsg);
}

function kafkaProduce(byte[] value) {
    transaction {
        var result = kafkaProducer->send(value, topic);
    } onretry {
         // Do nothing
     } committed {
         committedBlockExecuted = true;
     } aborted {
         // Do nothing
     }
}

function funcTestKafkaConsume() returns boolean {
    var records = kafkaConsumer->poll(3000);
    if (records is error) {
        return false;
    } else {
        var result = kafkaProducer->commitConsumer(kafkaConsumer);
        return !(result is error);
    }
}
