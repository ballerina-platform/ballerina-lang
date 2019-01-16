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

import ballerina/io;

io:ReadableByteChannel rch = new;
io:WritableByteChannel wch = new;

function initReadableChannel(string filePath) {
    rch = untaint io:openReadableFile(filePath);
}

function initWritableChannel(string filePath) {
    wch = untaint io:openWritableFile(filePath);
}

function readBytes(int numberOfBytes) returns byte[]|error {
    var result = rch.read(numberOfBytes);
    if (result is (byte[], int)) {
        var (bytes, val) = result;
        return bytes;
    } else {
        return result;
    }
}

function writeBytes(byte[] content, int startOffset) returns int|error {
    int empty = -1;
    var result = wch.write(content, startOffset);
    if (result is int) {
        return result;
    } else {
        return result;
    }
}

function closeReadableChannel() {
    var result = rch.close();
}

function closeWritableChannel() {
    var result = wch.close();
}

function testBase64EncodeByteChannel(io:ReadableByteChannel contentToBeEncoded) returns io:ReadableByteChannel|error {
    return contentToBeEncoded.base64Encode();
}

function testBase64DecodeByteChannel(io:ReadableByteChannel contentToBeDecoded) returns io:ReadableByteChannel|error {
    return contentToBeDecoded.base64Decode();
}
