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

string topic = "producer-test-topic";

kafka:ProducerConfiguration producerConfigs = {
    bootstrapServers: "localhost:14111",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    maxBlock: 5000,
    requestTimeoutInMillis: 1000,
    valueSerializerType: kafka:SER_STRING,
    retryCount: 3
};

kafka:Producer kafkaProducer = new (producerConfigs);

public function testKafkaProduce() returns kafka:ProducerError? {
    string message1 = "Hello World";
    var result = kafkaProduce(message1);
    if (result is kafka:ProducerError) {
        return result;
    }
    string message2 = "Hello World 2";
    return kafkaProduce(message2);
}

public function kafkaProduce(string value) returns kafka:ProducerError? {
    return kafkaProducer->send(value, topic);
}

function testKafkaClose() returns kafka:ProducerError? {
    string message = "Test Message";
    var result = kafkaProducer->send(message, topic);
    if (result is kafka:ProducerError) {
        return result;
    }
    result = kafkaProducer->close();
    if (result is kafka:ProducerError) {
        return result;
    }
    result = kafkaProducer->send(message, topic);
    if (result is kafka:ProducerError) {
        return result;
    }
}

function testFlush() returns kafka:ProducerError? {
    string message = "Hello World";
    var result = kafkaProducer->send(message, "test");
    if (result is kafka:ProducerError) {
        return result;
    }
    return kafkaProducer->flushRecords();
}

function testPartitionInfoRetrieval(string topic) returns kafka:TopicPartition[]|kafka:ProducerError {
    return kafkaProducer->getTopicPartitions(topic);
}
