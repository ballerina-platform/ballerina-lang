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
    bootstrapServers: "localhost:9144, localhost:9145, localhost:9146",
    clientId: "commit-producer",
    acks: "all",
    transactionalId: "comit-consumer-test-producer",
    noRetries: 3
};

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:9144, localhost:9145, localhost:9146",
    groupId: "commit-consumer-test-group",
    offsetReset: "earliest",
    topics: [topic]
};

kafka:Consumer kafkaConsumer = new(consumerConfigs);

function funcTestKafkaProduce() {
    string msg = "Hello World";
    byte[] byteMsg = msg.toBytes();
    var result = kafkaProduce(byteMsg);
}

function kafkaProduce(byte[] value) returns boolean {
    var transactionComplete = false;
    kafka:Producer kafkaProducer = new(producerConfigs);
    transaction {
        var result = kafkaProducer->send(value, topic);
    } committed {
        transactionComplete = true;
    } aborted {
        transactionComplete = false;
    }
    return transactionComplete;
}

function funcTestKafkaConsume() returns boolean {
    kafka:Producer kafkaProducer = new(producerConfigs);
    boolean transactionComplete = false;
    var records = kafkaConsumer->poll(3000);
    if (records is error) {
        return false;
    }
    transaction {
        var result = kafkaProducer->commitConsumer(kafkaConsumer);
    } committed {
        transactionComplete = true;
    } aborted {
        transactionComplete = false;
    }
    return transactionComplete;
}
