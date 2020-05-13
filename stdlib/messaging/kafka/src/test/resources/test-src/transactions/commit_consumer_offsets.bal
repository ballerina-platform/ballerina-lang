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


string topic = "commit-consumer-offsets-topic";

kafka:ProducerConfiguration producerConfigs = {
    bootstrapServers: "localhost:14151",
    clientId: "commit-consumer-offsets-producer",
    acks: kafka:ACKS_ALL,
    transactionalId: "comit-consumer-offset-test-producer",
    retryCount: 3,
    enableIdempotence: true
};

kafka:Producer kafkaProducer = new (producerConfigs);

kafka:ConsumerConfiguration consumerConfigs1 = {
    bootstrapServers: "localhost:14151",
    groupId: "commit-consumer-offsets-test-group-1",
    offsetReset: "earliest",
    topics: [topic],
    autoCommit: false
};

kafka:Consumer kafkaConsumer1 = new (consumerConfigs1);

kafka:ConsumerConfiguration consumerConfigs2 = {
    bootstrapServers: "localhost:14151",
    groupId: "commit-consumer-offsets-test-group-2",
    offsetReset: "earliest",
    topics: [topic],
    autoCommit: false
};

kafka:Consumer kafkaConsumer2 = new (consumerConfigs2);

function testProduce() {
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

function testCommitOffsets() returns boolean {
    var results = getPartitionOffset(kafkaConsumer1);
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

function testPollAgain() returns boolean {
    var results = getPartitionOffset(kafkaConsumer2);
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

function getPartitionOffset(kafka:Consumer consumer) returns kafka:PartitionOffset[]|error {
    var result = consumer->poll(1000);
    if (result is error) {
        return result;
    } else {
        kafka:PartitionOffset[] offsets = [];
        int i = 0;
        foreach var kafkaRecord in result {
            kafka:TopicPartition partition = { topic: kafkaRecord.topic, partition: kafkaRecord.partition };
            kafka:PartitionOffset offset = { partition: partition, offset: kafkaRecord.offset };
            offsets[i] = offset;
            i += 1;
        }
        return offsets;
    }
}
