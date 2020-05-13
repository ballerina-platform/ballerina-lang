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

kafka:AuthenticationConfiguration authConfigValid = {
    mechanism: kafka:AUTH_SASL_PLAIN,
    username: "ballerina",
    password: "ballerina-secret"
};

kafka:AuthenticationConfiguration authConfigInvalidPassword = {
    mechanism: kafka:AUTH_SASL_PLAIN,
    username: "ballerina",
    password: "invalid-secret"
};

kafka:ConsumerConfiguration consumerConfigsValid = {
    bootstrapServers: "localhost:14121",
    valueDeserializerType: kafka:DES_STRING,
    authenticationConfiguration: authConfigValid
};

kafka:ConsumerConfiguration consumerConfigsInvalidPassword = {
    bootstrapServers: "localhost:14121",
    valueDeserializerType: kafka:DES_STRING,
    authenticationConfiguration: authConfigInvalidPassword
};

public function getTopicsForValidConsumer() returns string[]|kafka:ConsumerError {
    kafka:Consumer kafkaConsumer = new (consumerConfigsValid);
    return kafkaConsumer->getAvailableTopics(1000);
}

public function getTopicsForInvalidPasswordConsumer() returns string[]|kafka:ConsumerError {
    kafka:Consumer kafkaConsumer = new (consumerConfigsInvalidPassword);
    return kafkaConsumer->getAvailableTopics(1000);
}

