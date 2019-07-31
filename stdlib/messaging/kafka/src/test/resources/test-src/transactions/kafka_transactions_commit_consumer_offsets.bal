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
import ballerina/transactions;

string topic = "commit-consumer-offsets-topic";

kafka:ProducerConfig producerConfigs = {
    bootstrapServers: "localhost:9144, localhost:9145, localhost:9146",
    clientId: "commit-consumer-offsets-producer",
    acks: "all",
    transactionalId: "comit-consumer-offset-test-producer",
    noRetries: 3
};

kafka:Producer kafkaProducer = new(producerConfigs);

kafka:ConsumerConfig consumerConfigs1 = {
    bootstrapServers: "localhost:9144, localhost:9145, localhost:9146",
    groupId: "commit-consumer-offsets-test-group-1",
    offsetReset: "earliest",
    topics: [topic],
    autoCommit: false
};

kafka:Consumer kafkaConsumer1 = new(consumerConfigs1);

kafka:ConsumerConfig consumerConfigs2 = {
    bootstrapServers: "localhost:9144, localhost:9145, localhost:9146",
    groupId: "commit-consumer-offsets-test-group-2",
    offsetReset: "earliest",
    topics: [topic],
    autoCommit: false
};

kafka:Consumer kafkaConsumer2 = new(consumerConfigs2);

function funcTestKafkaProduce() {
    string msg = "test-msg";
    byte[] byteMsg = msg.toBytes();
    kafkaProduce(byteMsg);
}

function kafkaProduce(byte[] value) {
    boolean committedBlockExecuted = false;
    transaction {
        var result = kafkaProducer->send(value, topic);
    } onretry {
         // Do nothing
     } committed {
         committedBlockExecuted = true;
     } aborted {
         // Do nothing
     }
}

function funcTestKafkaCommitOffsets() returns boolean {
    var results = funcGetPartitionOffset(kafkaConsumer1);
    boolean isSuccess = false;
    kafka:PartitionOffset[] offsets;
    if (results is error) {
        return false;
    } else {
        if (results.length() == 0) {
            return false;
        }
        offsets = results;
    }
    transaction {
        var result = kafkaProducer->commitConsumerOffsets(offsets, "commit-consumer-offsets-test-group-1");
        isSuccess = !(result is error);
    } committed {
        isSuccess = true;
    } aborted {
        isSuccess = false;
    }
    return isSuccess;
}

function funcTestPollAgain() returns boolean {
    var results = funcGetPartitionOffset(kafkaConsumer2);
    if (results is error) {
        return false;
    } else {
        if (results.length() == 0) {
            return true;
        } else {
            return false; // This should not receive any records as they are already committed.
        }
    }
}

function funcGetPartitionOffset(kafka:Consumer consumer) returns kafka:PartitionOffset[]|error {
    error|kafka:ConsumerRecord[] result = consumer->poll(2000);
    if (result is error) {
        return result;
    } else {
        kafka:PartitionOffset[] offsets = [];
        int i = 0;
        foreach kafka:ConsumerRecord kafkaRecord in result {
            kafka:TopicPartition partition = { topic: kafkaRecord.topic, partition: kafkaRecord.partition };
            kafka:PartitionOffset offset = { partition: partition, offset: kafkaRecord.offset };
            offsets[i] = offset;
            i += 1;
        }
        return offsets;
    }
}
