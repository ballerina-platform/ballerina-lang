// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/kafka;
import ballerina/log;

kafka:ProducerConfig producerConfigs1 = {
    bootstrapServers: "localhost:14110",
    clientId: "producer_1",
    acks: "all",
    retryCount: 0,
    requestTimeoutInMillis: 5000,
    retryBackoffTimeInMillis: 1000,
    metadataMaxAgeInMillis: 5000,
    transactionTimeoutInMillis: 5000,
    linger: 5000,
    maxBlock: 5000
};

public function main() {
    kafka:Producer kafkaProducer = new (producerConfigs1);
    string msg = "Hello World, Ballerina";
    byte[] serializedMsg = msg.toBytes();
    string topic = "t5";
    int i = 0;
    while (i >= 0) {
        i = i - 1;
        var sendResult = kafkaProducer->send(serializedMsg, topic);
        if (sendResult is error) {
            log:printError("Kafka producer failed to send data", sendResult);
        }
        else {
            log:printInfo("Published to " + topic);
        }
        var flushResult = kafkaProducer->flushRecords();
        if (flushResult is error) {
            log:printError("Kafka producer failed to flush the records", flushResult);
        }
    }
}

@http:WebSocketServiceConfig {
    path: "/basic/ws",
    subProtocols: ["xml", "json"],
    idleTimeoutInSeconds: 120
}

service basic on new http:Listener(9898) {
}
