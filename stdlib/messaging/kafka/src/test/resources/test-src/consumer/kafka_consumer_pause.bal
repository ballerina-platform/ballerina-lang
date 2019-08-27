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

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:14103",
    groupId: "test-group",
    clientId: "pause-consumer",
    offsetReset: "earliest",
    topics: ["test"]
};

kafka:Consumer kafkaConsumer = new(consumerConfigs);

kafka:TopicPartition topicPartition = {
    topic: "test",
    partition: 0
};

kafka:TopicPartition invalidTopicPartition = {
    topic: "test_negative",
    partition: 1000
};

kafka:TopicPartition[] topicPartitions = [topicPartition];
kafka:TopicPartition[] invalidTopicPartitions = [invalidTopicPartition];

function funcKafkaPoll() returns int|error {
    var records = kafkaConsumer->poll(1000);
    if (records is error) {
        return records;
    } else {
        return records.length();
    }
}

function funcKafkaPause() returns error? {
    var result = kafkaConsumer->pause(topicPartitions);
    if (result is error) {
        return result;
    }
    return;
}

function funcKafkaPauseInvalidTopicPartitions() returns error? {
    var result = kafkaConsumer->pause(invalidTopicPartitions);
    if (result is error) {
        return result;
    }
    return;
}

function funcKafkaResume() returns error? {
    var result = kafkaConsumer->resume(topicPartitions);
    if (result is error) {
        return result;
    }
    return;
}

function funcKafkaResumeInvalidTopicPartitions() returns error? {
    var result = kafkaConsumer->resume(invalidTopicPartitions);
    if (result is error) {
        return result;
    }
    return;
}

function funcKafkaGetPausedPartitions() returns kafka:TopicPartition[]|error {
    return kafkaConsumer->getPausedPartitions();
}
