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
import ballerina/log;

kafka:ProducerConfig producerConfigs = {
    // Here we create a producer configs with optional parameters client.id - used for broker side logging.
    // `acks` - number of acknowledgments for request complete,
    // `noRetries` - number of retries if record send fails.
    // `bootstrapServers` is the list of remote server endpoints of the Kafka brokers
    bootstrapServers:"localhost:9092",
    clientId:"basic-producer",
    acks:"all",
    retryCount:3,
    transactionalId:"test-transactional-id"
};

kafka:Producer kafkaProducer = new(producerConfigs);

public function main() {
    string msg1 = "Hello World Transaction Message 1";
    string msg2 = "Hello World Transaction Message 2";
    byte[] serializedMsg1 = msg1.toBytes();
    byte[] serializedMsg2 = msg2.toBytes();

    // Here we create a producer configs with optional parameter transactional.id - enable transactional message production.
    kafkaAdvancedTransactionalProduce(serializedMsg1, serializedMsg2);
}

function kafkaAdvancedTransactionalProduce(byte[] msg1, byte[] msg2) {
    // Kafka transactions allows messages to be send multiple partition atomically on KafkaProducerClient. Kafka Local transactions can only be used
    // when you are sending multiple messages using the same KafkaProducerClient instance.
    boolean transactionSuccess = false;
    transaction {
        var sendResult1 = kafkaProducer->send(msg1, "test-kafka-topic", partition = 0);
        if (sendResult1 is error) {
            log:printError("Kafka producer failed to send first message", sendResult1);
        }
        var sendResult2 = kafkaProducer->send(msg2, "test-kafka-topic", partition = 0);
        if (sendResult2 is error) {
            log:printError("Kafka producer failed to send second message", sendResult2);
        }
        transactionSuccess = true;
    }

    if (transactionSuccess) {
        io:println("Transaction committed");
    } else {
        io:println("Transaction failed");
    }
}
