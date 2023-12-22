// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/jballerina.java;

public client isolated class Client {

    public isolated function init() {
    }

    isolated resource function get abc/[string ...path]() returns int = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource"
    } external;
}

function testOverloadedMethods(int[] arr, string str) returns string = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name: "testOverloadedMethods"
} external;

function getPrice(handle val) returns handle = @java:Method {
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;
