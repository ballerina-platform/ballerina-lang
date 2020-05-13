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

const duration = 1000;

const topic1 = "test-topic-1";
const topic2 = "test-topic-2";
const topic3 = "test-topic-3";

kafka:ConsumerConfiguration configs = {
    bootstrapServers: "localhost:14105",
    groupId: "topics-test-consumer-group-1",
    clientId: "topics-test-consumer-1",
    topics: [topic1, topic2]
};

kafka:ConsumerConfiguration topicPartitionConfigs = {
    bootstrapServers: "localhost:14105",
    groupId: "topics-test-consumer-group-2",
    clientId: "topics-test-consumer-2",
    topics: [topic1]
};

kafka:ConsumerConfiguration assignConsumerConfigs = {
    bootstrapServers: "localhost:14105",
    groupId: "topics-test-consumer-group-3",
    clientId: "topics-test-consumer-3"
};

kafka:Consumer consumer = new(configs);
kafka:Consumer topicPartitionConsumer = new(topicPartitionConfigs);
kafka:Consumer topicAssignConsumer = new(assignConsumerConfigs);

function testGetAvailableTopics() returns string[]|error {
    return consumer->getAvailableTopics();
}

function testGetAvailableTopicsWithDuration() returns string[]|error {
    return consumer->getAvailableTopics(duration);
}

function testGetTopicPartitions() returns kafka:TopicPartition[]|error {
    return topicPartitionConsumer->getTopicPartitions(topic1);
}

function testAssign() returns kafka:ConsumerError? {
    kafka:TopicPartition partition = { topic: topic3, partition: 0 };
    kafka:TopicPartition[] partitions = [partition];
    return topicAssignConsumer->assign(partitions);
}

function testGetAssignment() returns kafka:TopicPartition[]|error {
    return topicAssignConsumer->getAssignment();
}
