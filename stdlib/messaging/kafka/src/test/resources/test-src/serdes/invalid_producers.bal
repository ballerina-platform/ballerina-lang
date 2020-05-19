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

import ballerina/kafka;

public const TOPIC = "add-person";

public type Person record {|
    string name;
    int age;
|};

kafka:ProducerConfiguration avroProducerWithoutSchemaUrlConfig = {
    bootstrapServers: "localhost:14131",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    requestTimeoutInMillis: 1000,
    retryCount: 0,
    valueSerializerType: kafka:SER_AVRO
};

kafka:ProducerConfiguration avroKeySerializerProducerWithoutSchemaUrlConfig = {
    bootstrapServers: "localhost:14131",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    requestTimeoutInMillis: 1000,
    retryCount: 0,
    keySerializerType: kafka:SER_AVRO
};

kafka:ProducerConfiguration customSerializerWithoutSerializerObject = {
    bootstrapServers: "localhost:14131",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    requestTimeoutInMillis: 1000,
    retryCount: 0,
    keySerializerType: kafka:SER_CUSTOM
};

kafka:ProducerConfiguration customValueSerializerWithoutSerializerObject = {
    bootstrapServers: "localhost:14131",
    clientId: "basic-producer",
    acks: kafka:ACKS_ALL,
    requestTimeoutInMillis: 1000,
    retryCount: 0,
    valueSerializerType: kafka:SER_CUSTOM
};

public function createAvroProducerWithoutSchemaUrlConfig() returns kafka:ProducerError? {
    var avroProducerWithoutSchemaUrl = trap new kafka:Producer(avroProducerWithoutSchemaUrlConfig);
    if (avroProducerWithoutSchemaUrl is kafka:ProducerError) {
        return avroProducerWithoutSchemaUrl;
    }
}

public function createAvroKeySerializerProducerWithoutSchemaUrlConfig() returns kafka:ProducerError? {
    var avroProducerWithoutSchemaUrl = trap new kafka:Producer(avroKeySerializerProducerWithoutSchemaUrlConfig);
    if (avroProducerWithoutSchemaUrl is kafka:ProducerError) {
        return avroProducerWithoutSchemaUrl;
    }
}

public function createCustomKeySerializerWithoutSerializerObject() returns kafka:ProducerError? {
    var avroProducerWithoutSchemaUrl = trap new kafka:Producer(customSerializerWithoutSerializerObject);
    if (avroProducerWithoutSchemaUrl is kafka:ProducerError) {
        return avroProducerWithoutSchemaUrl;
    }
}

public function createCustomValueSerializerWithoutSerializerObject() returns kafka:ProducerError? {
    var avroProducerWithoutSchemaUrl = trap new kafka:Producer(customValueSerializerWithoutSerializerObject);
    if (avroProducerWithoutSchemaUrl is kafka:ProducerError) {
        return avroProducerWithoutSchemaUrl;
    }
}
