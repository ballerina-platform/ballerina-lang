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

# Default URL for NATS connections.
public const string DEFAULT_URL = "nats://localhost:4222";

# Data types supported when publishing and consuming messages.
public type Content byte[] | boolean | string | int | float | decimal | xml | json | record {};

function convertData(Content data) returns string | byte[] | error {
    string | byte[] | error converted;
    if (data is byte[]) {
        converted = data;
    } else if (data is boolean) {
        converted = data.toString();
    } else if (data is string) {
        converted = data.toString();
    } else if (data is int) {
        converted = data.toString();
    } else if (data is float) {
        converted = data.toString();
    } else if (data is decimal) {
        converted = data.toString();
    } else if (data is xml) {
        converted = data.toString();
    } else if (data is json) {
        converted = data.toJsonString();
    } else {
        json | error jsonConverted = data.cloneWithType(typedesc<json>);
        if (jsonConverted is json) {
            converted = jsonConverted.toString();
        } else {
            converted = jsonConverted;
        }
    }
    return converted;
}
