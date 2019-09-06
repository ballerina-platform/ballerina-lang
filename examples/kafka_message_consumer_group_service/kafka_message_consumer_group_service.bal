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
import ballerina/io;
import ballerina/lang.'string as strings;
import ballerina/log;

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:9092",
    concurrentConsumers: 2,
    groupId: "group-id",
    topics: ["test-kafka-topic"],
    pollingIntervalInMillis: 1000
};

listener kafka:Consumer consumer = new(consumerConfigs);

service kafkaService on consumer {
    resource function onMessage(kafka:Consumer kafkaConsumer, kafka:ConsumerRecord[] records) {
        // Dispatched set of Kafka records to service, We process each one by one.
        foreach var kafkaRecord in records {
            processKafkaRecord(kafkaRecord);
        }
    }
}

function processKafkaRecord(kafka:ConsumerRecord kafkaRecord) {
   byte[] serializedMsg = kafkaRecord.value;
   string|error msg = strings:fromBytes(serializedMsg);
   if (msg is string) {
        // Print the retrieved Kafka record.
        io:println("Topic: " + kafkaRecord.topic + " Partition: " + kafkaRecord.partition.toString() + " Received Message: " + msg);
   } else {
        log:printError("Error occurred while converting message data", msg);
   }
}
