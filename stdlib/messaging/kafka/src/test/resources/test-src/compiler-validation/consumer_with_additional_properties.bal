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

string consumerTopic = "consumer-with-additional-properties-topic";
string[] consumerTopics = [consumerTopic];
string producerTopic = "producer-with-additional-properties-topic";

map<string> additionalProperties = {
    "bootstrap.servers": "localhost:14191"
};

kafka:ConsumerConfiguration consumerConfigurations = {
    // Set invalid broker value, this will be replaced by the additionalProperties map
    bootstrapServers: "invalid",
    groupId: "test-group",
    clientId: "basic-consumer",
    offsetReset: "earliest",
    valueDeserializerType: kafka:DES_STRING,
    autoCommit: true,
    properties: additionalProperties
};

kafka:ProducerConfiguration producerConfigurations = {
    // Set invalid broker value, this will be replaced by the additionalProperties map
    bootstrapServers: "invalid",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    maxBlock: 5000,
    requestTimeoutInMillis: 1000,
    valueSerializerType: kafka:SER_STRING,
    properties: additionalProperties,
    retryCount: 3
};

kafka:Consumer consumer = new (consumerConfigurations);
kafka:Producer producer = new (producerConfigurations);

function testConsumerWithAdditionalProperties() returns boolean|kafka:ConsumerError {
    string expectedMessage = "Hello, Ballerina";
    var subscribeResult = consumer->subscribe(consumerTopics);
    if (subscribeResult is kafka:ConsumerError) {
        return subscribeResult;
    }
    var results = consumer->poll(1000);
    if (results is error) {
        return results;
    } else {
        if (results.length() > 0) {
            string receivedMessage = <string> results[0].value;
            return (receivedMessage == expectedMessage);
        }
    }
    return false;
}

function testProducerWithAdditionalProperties() returns kafka:ProducerError? {
    string message = "Hello, from Ballerina";
    return producer->send(message, producerTopic);
}
