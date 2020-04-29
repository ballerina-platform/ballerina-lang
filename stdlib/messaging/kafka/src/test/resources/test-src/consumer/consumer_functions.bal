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

kafka:ConsumerConfiguration consumerConfig = {
    bootstrapServers: "localhost:14107",
    groupId: "test-group",
    clientId: "basic-consumer",
    offsetReset: "earliest",
    topics: ["test"]
};

function funcKafkaConnect() returns kafka:Consumer {
    kafka:Consumer consumer= new(consumerConfig);
    return consumer;
}

function funcKafkaClose() returns boolean {
    kafka:Consumer consumer= new(consumerConfig);
    var result = consumer->close();
    return !(result is error);
}

function funcKafkaGetSubscription() returns string[]|error {
    kafka:Consumer consumer= new(consumerConfig);
    return consumer->getSubscription();
}

function funcKafkaGetAssignment() returns kafka:TopicPartition[]|error {
    kafka:Consumer consumer= new(consumerConfig);
    return consumer->getAssignment();
}

function funcKafkaPoll() returns int|error {
    kafka:Consumer consumer= new(consumerConfig);
    var results = consumer->poll(1000);
    if (results is error) {
        return results;
    } else {
        return results.length();
    }
}

string topic1 = "consumer-unsubscribe-test-1";
string topic2 = "consumer-unsubscribe-test-2";

function funcKafkaTestUnsubscribe() returns boolean {
    kafka:Consumer kafkaConsumer = new({
            bootstrapServers: "localhost:9100",
            groupId: "test-group",
            clientId: "unsubscribe-consumer",
            offsetReset: "earliest",
            topics: [topic1, topic2]
        });
    var subscribedTopics = kafkaConsumer->getSubscription();
    if (subscribedTopics is error) {
        return false;
    }
    else {
        if (subscribedTopics.length() != 2) {
            return false;
        }
    }
    var result = kafkaConsumer->unsubscribe();
    subscribedTopics = kafkaConsumer->getSubscription();
    if (subscribedTopics is error) {
        return false;
    } else {
        if (subscribedTopics.length() != 0) {
            return false;
        }
        return true;
    }
}
