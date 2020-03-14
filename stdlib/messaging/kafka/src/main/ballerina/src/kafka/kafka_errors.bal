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

# Represents the details of an error.
#
# + message - The error message.
# + cause - Cause of the error.
public type Detail record {
    string message;
    error cause?;
};

# Defines a Kafka consumer related error
public const CONSUMER_ERROR = "{ballerina/kafka}ConsumerError";

# Represents a Kafka consumer related error
public type ConsumerError error<CONSUMER_ERROR, Detail>;

# Represents a Kafka producer related error
public const PRODUCER_ERROR = "{ballerina/kafka}ProducerError";

# Represents a Kafka producer related error
public type ProducerError error<PRODUCER_ERROR, Detail>;

function getValueTypeMismatchError(string expectedType) returns ProducerError {
    string message = "Invalid type found for Kafka value. Expected value type: '" + expectedType + "'.";
    return createProducerError(message);
}

function getKeyTypeMismatchError(string expectedType) returns ProducerError {
    string message = "Invalid type found for Kafka key. Expected key type: '" + expectedType + "'.";
    return createProducerError(message);
}

function createProducerError(string message) returns ProducerError {
    return error(PRODUCER_ERROR, message = message);
}
