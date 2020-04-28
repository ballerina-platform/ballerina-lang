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

import ballerina/java;

function wrapBoolean(boolean b) returns handle = @java:Constructor {
    class: "java.lang.Boolean",
    paramTypes: ["boolean"]
} external;

function wrapByte(byte b) returns handle = @java:Constructor {
    class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

function wrapInt(int i) returns handle = @java:Constructor {
    class: "java.lang.Integer",
    paramTypes: ["int"]
} external;

function wrapFloat(float f) returns handle = @java:Constructor {
    class: "java.lang.Float",
    paramTypes: ["float"]
} external;

function wrapByteToChar(byte b) returns handle = @java:Constructor {
    class: "java.lang.Character",
    paramTypes: ["char"]
} external;

function wrapIntToChar(int b) returns handle = @java:Constructor {
    class: "java.lang.Character",
    paramTypes: ["char"]
} external;

function wrapFloatToChar(float b) returns handle = @java:Constructor {
    class: "java.lang.Character",
    paramTypes: ["char"]
} external;

function wrapFloatToShort(float b) returns handle = @java:Constructor {
    class: "java.lang.Short",
    paramTypes: ["short"]
} external;

function wrapIntToShort(int b) returns handle = @java:Constructor {
    class: "java.lang.Short",
    paramTypes: ["short"]
} external;

function wrapByteToShort(byte b) returns handle = @java:Constructor {
    class: "java.lang.Short",
    paramTypes: ["short"]
} external;

function wrapByteToLong(byte b) returns handle = @java:Constructor {
    class: "java.lang.Long",
    paramTypes: ["long"]
} external;

function wrapIntToLong(int b) returns handle = @java:Constructor {
    class: "java.lang.Long",
    paramTypes: ["long"]
} external;

function wrapFloatToLong(float b) returns handle = @java:Constructor {
    class: "java.lang.Long",
    paramTypes: ["long"]
} external;

function wrapByteToDouble(byte b) returns handle = @java:Constructor {
    class: "java.lang.Double",
    paramTypes: ["double"]
} external;

function wrapFloatToDouble(float b) returns handle = @java:Constructor {
    class: "java.lang.Double",
    paramTypes: ["double"]
} external;
