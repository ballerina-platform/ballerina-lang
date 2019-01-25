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

import ballerina/encoding;

public function encodeToHex(byte[] input) returns string {
    return encoding:encodeHex(input);
}

public function encodeToBase64(byte[] input) returns string {
    return encoding:encodeBase64(input);
}

public function decodeFromHex(string input) returns byte[]|error {
    return encoding:decodeHex(input);
}

public function decodeFromBase64(string input) returns byte[]|error {
    return encoding:decodeBase64(input);
}

function testEncodeDecode(string content) returns (byte[]|error) {
    var result = encoding:encodeBase64(content.toByteArray("UTF-8"));
    return encoding:decodeBase64(result);
}

function testBase64EncodeString(string contentToBeEncoded) returns (string|error) {
    return encoding:encodeBase64(contentToBeEncoded.toByteArray("UTF-8"));
}

function testBase64DecodeString(string contentToBeDecoded) returns (byte[]|error) {
    return encoding:decodeBase64(contentToBeDecoded);
}

function testBase16ToBase64Encoding(string str) returns string|error {
    var decodedValue = check encoding:decodeHex(str);
    return encoding:encodeBase64(decodedValue);
}

function testBase64ToBase16Encoding(string str) returns string|error {
    var decodedValue = check encoding:decodeBase64(str);
    return encoding:encodeHex(decodedValue);
}

function testByteArrayToString1(byte[] b) returns string|error {
    return encoding:byteArrayToString(b);
}

function testByteArrayToString2(byte[] b, string encoding) returns string|error {
    return encoding:byteArrayToString(b, encoding = encoding);
}
