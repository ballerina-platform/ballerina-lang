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

string topic = "consumer-pause-resume-test-topic";

kafka:ConsumerConfiguration consumerConfigs = {
    bootstrapServers: "localhost:14104",
    groupId: "test-group",
    clientId: "pause-consumer",
    valueDeserializerType: kafka:DES_STRING,
    offsetReset: "earliest",
    topics: [topic]
};

kafka:Consumer kafkaConsumer = new (consumerConfigs);

kafka:TopicPartition topicPartition = {
    topic: topic,
    partition: 0
};

kafka:TopicPartition invalidTopicPartition = {
    topic: "test_negative",
    partition: 1000
};

kafka:TopicPartition[] topicPartitions = [topicPartition];
kafka:TopicPartition[] invalidTopicPartitions = [invalidTopicPartition];

function testPoll() returns string|kafka:ConsumerError {
    var result = kafkaConsumer->poll(1000);
    if (result is kafka:ConsumerError) {
        return result;
    } else {
        if (result.length() == 0) {
            return "";
        } else {
            return <string> result[0].value;
        }
    }
}

function testPause() returns kafka:ConsumerError? {
    return kafkaConsumer->pause(topicPartitions);
}

function testPauseInvalidTopicPartitions() returns kafka:ConsumerError? {
    return kafkaConsumer->pause(invalidTopicPartitions);
}

function testResume() returns kafka:ConsumerError? {
    return kafkaConsumer->resume(topicPartitions);
}

function testResumeInvalidTopicPartitions() returns kafka:ConsumerError? {
    return kafkaConsumer->resume(invalidTopicPartitions);
}

function testGetPausedPartitions() returns kafka:TopicPartition[]|error {
    return kafkaConsumer->getPausedPartitions();
}
