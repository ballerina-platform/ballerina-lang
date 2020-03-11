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

kafka:ConsumerConfiguration consumerConfigs = {
    bootstrapServers: "localhost:14104",
    groupId: "test-group",
    clientId: "seek-consumer",
    offsetReset: "earliest",
    topics: ["test"]
};

kafka:Consumer kafkaConsumer = new(consumerConfigs);

kafka:TopicPartition topicPartition = {
    topic: "test",
    partition: 0
};

kafka:PartitionOffset partitionOffset = {
    partition: topicPartition,
    offset: 5
};

kafka:TopicPartition[] topicPartitions = [topicPartition];

function funcKafkaPoll() returns int|error {
    var records = kafkaConsumer->poll(1000);
    if (records is error) {
        return records;
    } else {
        return records.length();
    }
}

function funcKafkaGetPositionOffset() returns int|error {
    return kafkaConsumer->getPositionOffset(topicPartition);
}

function funcKafkaSeekOffset() returns error? {
    return kafkaConsumer->seek(partitionOffset);
}

function funcKafkaSeekToBegin() returns error? {
    return kafkaConsumer->seekToBeginning(topicPartitions);
}

function funcKafkaBeginOffsets() returns kafka:PartitionOffset[]|error {
    return kafkaConsumer->getBeginningOffsets(topicPartitions);
}

function funcKafkaSeekToEnd() returns error? {
    return kafkaConsumer->seekToEnd(topicPartitions);
}

function funcKafkaEndOffsets() returns kafka:PartitionOffset[]|error {
    return kafkaConsumer->getEndOffsets(topicPartitions);
}
