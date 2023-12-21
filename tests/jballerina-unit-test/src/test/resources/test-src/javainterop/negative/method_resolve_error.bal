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

import ballerina/jballerina.java;

function hashCodeInstance(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.lang.Byte"
} external;

function hashCodeStatic(int receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.lang.Byte"
} external;

function getSeatCount(handle receiver) returns int = @java:Method {
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;

function getSeatCountWithModel(handle receiver) returns int = @java:Method {
    name: "getSeatCount",
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;

function getDescription(handle receiver, handle val) returns handle = @java:Method {
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;

function newByte(int val) returns handle = @java:Constructor {
   'class: "java.lang.Byte"
} external;

function getCarCategorization(handle receiver, handle val1, handle val2) returns handle = @java:Method {
    name: "getCategorization",
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;

function getBatteryType(handle receiver, handle val) returns handle = @java:Method {
    name: "getBatteryType",
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;
