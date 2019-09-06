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

import ballerina/io;
import ballerina/kafka;
import ballerina/lang.'string as strings;
import ballerina/log;

// `bootstrapServers` is the list of remote server endpoints of the Kafka brokers
kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers:"localhost:9092",
    groupId:"group-id",
    offsetReset:"earliest",
    topics:["test-kafka-topic"]
};

kafka:Consumer consumer = new(consumerConfigs);

public function main() {
    // polling consumer for messages
    var results = consumer->poll(1000);
    if (results is error) {
        log:printError("Error occurred while polling ", err = results);
    } else {
        foreach var kafkaRecord in results {
           // convert byte[] to string
           byte[] serializedMsg = kafkaRecord.value;
           string|error msg = strings:fromBytes(serializedMsg);
           if (msg is string) {
                // Print the retrieved Kafka record.
                io:println("Topic: " + kafkaRecord.topic + " Received Message: " + msg);
           } else {
                log:printError("Error occurred while converting message data", msg);
           }
        }
    }
}
