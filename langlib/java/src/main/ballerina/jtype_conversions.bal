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

// Returns a Java Boolean handle for a Ballerina boolean
isolated function wrapBooleanToBoolean(boolean b) returns handle = @Constructor {
    'class: "java.lang.Boolean",
    paramTypes: ["boolean"]
} external;

// Returns a Java Integer handle for a Ballerina integer
isolated function wrapIntToInt(int i) returns handle = @Constructor {
    'class: "java.lang.Integer",
    paramTypes: ["int"]
} external;

// Returns a Java Character handle for a Ballerina integer
isolated function wrapIntToChar(int b) returns handle = @Constructor {
    'class: "java.lang.Character",
    paramTypes: ["char"]
} external;

// Returns a Java Short handle for a Ballerina integer
isolated function wrapIntToShort(int b) returns handle = @Constructor {
    'class: "java.lang.Short",
    paramTypes: ["short"]
} external;

// Returns a Java Long handle for a Ballerina integer
isolated function wrapIntToLong(int b) returns handle = @Constructor {
    'class: "java.lang.Long",
    paramTypes: ["long"]
} external;

// Returns a Java Byte handle for a Ballerina integer
isolated function wrapIntToByte(int b) returns handle = @Constructor {
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

// Returns a Java Float handle for a Ballerina float
isolated function wrapFloatToFloat(float b) returns handle = @Constructor {
    'class: "java.lang.Float",
    paramTypes: ["float"]
} external;

// Returns a Java Character handle for a Ballerina float
isolated function wrapFloatToChar(float b) returns handle = @Constructor {
    'class: "java.lang.Character",
    paramTypes: ["char"]
} external;

// Returns a Java Short handle for a Ballerina float
isolated function wrapFloatToShort(float b) returns handle = @Constructor {
    'class: "java.lang.Short",
    paramTypes: ["short"]
} external;

// Returns a Java Long handle for a Ballerina float
isolated function wrapFloatToLong(float b) returns handle = @Constructor {
    'class: "java.lang.Long",
    paramTypes: ["long"]
} external;

// Returns a Java Double handle for a Ballerina float
isolated function wrapFloatToDouble(float b) returns handle = @Constructor {
    'class: "java.lang.Double",
    paramTypes: ["double"]
} external;

// Returns a Java Byte handle for a Ballerina float
isolated function wrapFloatToByte(float b) returns handle = @Constructor {
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

// Returns a Java Integer handle for a Ballerina float
isolated function wrapFloatToInt(float b) returns handle = @Constructor {
    'class: "java.lang.Integer",
    paramTypes: ["int"]
} external;

// Returns a Java Byte handle for a Ballerina byte
isolated function wrapByteToByte(byte b) returns handle = @Constructor {
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

// Returns a Java Float handle for a Ballerina byte
isolated function wrapByteToFloat(byte b) returns handle = @Constructor {
    'class: "java.lang.Float",
    paramTypes: ["float"]
} external;

// Returns a Java Character handle for a Ballerina byte
isolated function wrapByteToChar(byte b) returns handle = @Constructor {
    'class: "java.lang.Character",
    paramTypes: ["char"]
} external;

// Returns a Java Short handle for a Ballerina byte
isolated function wrapByteToShort(byte b) returns handle = @Constructor {
    'class: "java.lang.Short",
    paramTypes: ["short"]
} external;

// Returns a Java Long handle for a Ballerina byte
isolated function wrapByteToLong(byte b) returns handle = @Constructor {
    'class: "java.lang.Long",
    paramTypes: ["long"]
} external;

// Returns a Java Double handle for a Ballerina byte
isolated function wrapByteToDouble(byte b) returns handle = @Constructor {
    'class: "java.lang.Double",
    paramTypes: ["double"]
} external;

// Returns a Java Integer handle for a Ballerina byte
isolated function wrapByteToInt(byte b) returns handle = @Constructor {
    'class: "java.lang.Integer",
    paramTypes: ["int"]
} external;

// Returns a Ballerina boolean from a Java Boolean handle
isolated function getBBooleanFromJBoolean(handle receiver) returns boolean = @Method {
    name:"booleanValue",
    'class:"java.lang.Boolean"
} external;

// Returns a Ballerina integer from a Java Integer handle
isolated function getBIntFromJInt(handle receiver) returns int = @Method {
    name:"longValue",
    'class:"java.lang.Integer"
} external;

// Returns a Ballerina integer from a Java Byte handle
isolated function getBIntFromJByte(handle receiver) returns int = @Method {
    name:"longValue",
    'class:"java.lang.Byte"
} external;

// Returns a Ballerina integer from a Java Short handle
isolated function getBIntFromJShort(handle receiver) returns int = @Method {
    name:"longValue",
    'class:"java.lang.Short"
} external;

// Returns a Ballerina integer from a Java Long handle
isolated function getBIntFromJLong(handle receiver) returns int = @Method {
    name:"longValue",
    'class:"java.lang.Long"
} external;

// Returns a Ballerina integer from a Java Character handle
isolated function getBIntFromJChar(handle receiver) returns int = @Method {
    name:"charValue",
    'class:"java.lang.Character"
} external;

// Returns a Ballerina float from a Java Float handle
isolated function getBFloatFromJFloat(handle receiver) returns float = @Method {
    name:"doubleValue",
    'class:"java.lang.Float"
} external;

// Returns a Ballerina float from a Java Short handle
isolated function getBFloatFromJShort(handle receiver) returns float = @Method {
    name:"doubleValue",
    'class:"java.lang.Short"
} external;

// Returns a Ballerina float from a Java Byte handle
isolated function getBFloatFromJByte(handle receiver) returns float = @Method {
    name:"doubleValue",
    'class:"java.lang.Byte"
} external;

// Returns a Ballerina float from a Java Double handle
isolated function getBFloatFromJDouble(handle receiver) returns float = @Method {
    name:"doubleValue",
    'class:"java.lang.Double"
} external;

// Returns a Ballerina float from a Java Character handle
isolated function getBFloatFromJChar(handle receiver) returns float = @Method {
    name:"charValue",
    'class:"java.lang.Character"
} external;

// Returns a Ballerina float from a Java Long handle
isolated function getBFloatFromJLong(handle receiver) returns float = @Method {
    name:"doubleValue",
    'class:"java.lang.Long"
} external;

// Returns a Ballerina float from a Java Integer handle
isolated function getBFloatFromJInt(handle receiver) returns float = @Method {
    name:"doubleValue",
    'class:"java.lang.Integer"
} external;

// Returns a Ballerina byte from a Java Byte handle
isolated function getBByteFromJByte(handle receiver) returns byte = @Method {
    name:"byteValue",
    'class:"java.lang.Byte"
} external;
