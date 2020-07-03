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

# Error type specific to the `kafka:Consumer` object functions.
public type ConsumerError distinct error;

# Error type specific to the `kafka:Producer` object functions.
public type ProducerError distinct error;

# Represents a Kafka Avro related error.
public type AvroError distinct error;

function getValueTypeMismatchError(string expectedType) returns ProducerError {
    string message = "Invalid type found for Kafka value. Expected value type: '" + expectedType + "'.";
    return ProducerError(message);
}

function getKeyTypeMismatchError(string expectedType) returns ProducerError {
    string message = "Invalid type found for Kafka key. Expected key type: '" + expectedType + "'.";
    return ProducerError(message);
}

function createProducerError(string message) returns ProducerError {
    return ProducerError(message);
}

function createConsumerError(string message) returns ConsumerError {
    return ConsumerError(message);
}
