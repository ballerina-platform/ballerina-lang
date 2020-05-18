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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/kafka;

string topic = "advanced-service-test";

kafka:ConsumerConfiguration consumerConfigs = {
    bootstrapServers: "localhost:14141",
    groupId: "advanced-service-test-group",
    clientId: "advanced-service-consumer",
    offsetReset: "earliest",
    topics: [topic],
    autoCommit:false,
    valueDeserializerType: kafka:DES_INT
};

listener kafka:Consumer kafkaConsumer = new (consumerConfigs);

kafka:ProducerConfiguration producerConfigs = {
    bootstrapServers: "localhost:14141",
    clientId: "advanced-service-producer",
    acks: kafka:ACKS_ALL,
    retryCount: 3,
    valueSerializerType: kafka:SER_INT
};

kafka:Producer kafkaProducer = new (producerConfigs);

boolean isSuccess = false;

service kafkaService on kafkaConsumer {
    resource function onMessage(
        kafka:Consumer consumer,
        kafka:ConsumerRecord[] records,
        kafka:PartitionOffset[] offsets,
        string groupId
    ) {
        if (records.length() > 0 && groupId == "advanced-service-test-group") {
            foreach var kafkaRecord in records {
                var value = kafkaRecord.value;
                if (value is int) {
                    if (value == 1135) {
                        isSuccess = true;
                    }
                }
            }
        }
    }
}

function testGetResultText() returns boolean {
    return isSuccess;
}

function testProduce() {
    int msg = 1135;
    var result = kafkaProducer->send(msg, topic);
}
