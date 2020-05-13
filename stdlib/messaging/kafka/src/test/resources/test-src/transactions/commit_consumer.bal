// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


string topic = "commit-consumer-test-topic";

kafka:ProducerConfiguration producerConfigs = {
    bootstrapServers: "localhost:14151",
    clientId: "commit-producer",
    acks: kafka:ACKS_ALL,
    transactionalId: "comit-consumer-test-producer",
    retryCount: 3,
    enableIdempotence: true
};

kafka:Producer kafkaProducer = new(producerConfigs);

kafka:ConsumerConfiguration consumerConfigs = {
    bootstrapServers: "localhost:14151",
    groupId: "commit-consumer-test-group",
    offsetReset: "earliest",
    topics: [topic]
};

kafka:Consumer kafkaConsumer = new(consumerConfigs);

function testProduce() {
    string msg = "Hello World";
    byte[] byteMsg = msg.toBytes();
    var result = kafkaProduce(byteMsg);
}

function kafkaProduce(byte[] value) returns boolean {
    var transactionComplete = false;
    transaction {
        var result = kafkaProducer->send(value, topic);
    } committed {
        transactionComplete = true;
    } aborted {
        transactionComplete = false;
    }
    return transactionComplete;
}

function testConsume() returns boolean {
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
