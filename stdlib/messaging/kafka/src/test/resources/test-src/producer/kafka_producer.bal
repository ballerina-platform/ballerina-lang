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

string topic = "producer-test-topic";

kafka:ProducerConfig producerConfigs = {
    bootstrapServers: "localhost:9102",
    clientId: "basic-producer",
    acks: "all",
    requestTimeout: 1000,
    noRetries: 3
};

kafka:Producer kafkaProducer = new(producerConfigs);

public function funcTestKafkaProduce() {
    string msg = "Hello World";
    byte[] byteMsg = msg.toBytes();
    kafkaProduce(byteMsg);

    msg = "Hello World 2";
    byteMsg = msg.toBytes();
    kafkaProduce(byteMsg);
}

public function kafkaProduce(byte[] value) {
    var result = kafkaProducer->send(value, topic);
}

function funcTestKafkaClose() returns string {
    string msg = "Test Message";
    byte[] byteMsg = msg.toBytes();
    var result = kafkaProducer->send(byteMsg, topic);
    if (result is kafka:ProducerError) {
        var closeResult = kafkaProducer->close();
        return result.detail().message;
    }
    result = kafkaProducer->close();
    if (result is kafka:ProducerError) {
        var closeResult = kafkaProducer->close();
        return result.detail().message;
    }
    result = kafkaProducer->send(byteMsg, topic);
    if (result is kafka:ProducerError) {
        var closeResult = kafkaProducer->close();
        return result.detail().message;
    }
    return "";
}

function funcKafkaTestFlush() returns boolean {
    string msg = "Hello World";
    byte[] byteMsg = msg.toBytes();
    var result = kafkaProducer->send(byteMsg, "test");

    if (result is error) {
        return false;
    }

    result = kafkaProducer->flushRecords();
    if (result is error) {
        return false;
    }
    return true;
}

function funcTestPartitionInfoRetrieval(string topic) returns kafka:TopicPartition[]? {
    return getPartitionInfo(topic);
}

function getPartitionInfo(string topic) returns kafka:TopicPartition[]? {
    kafka:TopicPartition[]|error partitions = kafkaProducer->getTopicPartitions(topic);
    if (partitions is error) {
        return;
    } else {
        return partitions;
    }
}
