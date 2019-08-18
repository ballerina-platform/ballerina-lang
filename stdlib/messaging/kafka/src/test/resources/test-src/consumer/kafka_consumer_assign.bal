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

kafka:ConsumerConfig consumerConfig = {
    bootstrapServers: "localhost:14101",
    groupId: "test-group",
    clientId: "assign-consumer",
    offsetReset: "earliest"
};

kafka:Consumer kafkaConsumer = new(consumerConfig);

function funcKafkaAssign () {
    kafka:TopicPartition partition = { topic: "test", partition: 1 };
    kafka:TopicPartition [] partitions = [partition];
    var assignResult = kafkaConsumer->assign(partitions);
}

function funcKafkaGetTopicPartitions() returns kafka:TopicPartition[]|error {
    return kafkaConsumer->getTopicPartitions("test");
}

function funcKafkaGetAssignment() returns kafka:TopicPartition[]|error {
    return kafkaConsumer->getAssignment();
}
