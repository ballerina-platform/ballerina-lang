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

const TOPIC = "test";

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:9095",
    groupId: "abcd",
    clientId: "manual-commit-consumer",
    offsetReset: "earliest",
    autoCommit: false,
    topics: [TOPIC]
};

kafka:Consumer consumer = new(consumerConfigs);

kafka:TopicPartition topicPartition = {
    topic: TOPIC,
    partition: 0
};

kafka:TopicPartition topicPartitionNegative = {
    topic: "topic_negative",
    partition: 100
};

function funcKafkaPoll() returns int|error {
    var records = consumer->poll(1000);
    if (records is error) {
        return records;
    } else {
        return records.length();
    }
}

function funcKafkaGetCommittedOffset() returns kafka:PartitionOffset|error {
    return consumer->getCommittedOffset(topicPartition);
}

function funcKafkaGetCommittedOffsetForNonExistingTopic() returns kafka:PartitionOffset|error {
    return consumer->getCommittedOffset(topicPartitionNegative);
}

function funcKafkaGetPositionOffset() returns int|error {
     return consumer->getPositionOffset(topicPartition);
 }

 function funcKafkaGetPositionOffsetForNonExistingTopic() returns int|error {
     return consumer->getPositionOffset(topicPartitionNegative);
 }

function funcKafkaCommit() returns boolean {
    var result = consumer->commit();
    return !(result is error);
}
