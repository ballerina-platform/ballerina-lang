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

io:ReadableCharacterChannel? rch = ();
io:WritableCharacterChannel? wch = ();

function initReadableChannel(string filePath, string encoding) {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    rch = untaint new io:ReadableCharacterChannel(byteChannel, encoding);
}

function initWritableChannel(string filePath, string encoding) {
    io:WritableByteChannel byteChannel = io:openWritableFile(filePath);
    wch = untaint new io:WritableCharacterChannel(byteChannel, encoding);
}

function readCharacters(int numberOfCharacters) returns string|error {
    var result = rch.read(numberOfCharacters);
    if (result is string) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Character channel not initialized properly");
        return e;
    }
}

function readAllCharacters() returns string|error? {
    int fixedSize = 500;
    boolean isDone = false;
    string result = "";
    while (!isDone) {
        var readResult = readCharacters(fixedSize);
        if (readResult is string) {
            result = result + readResult;
        } else {
            if (<string>readResult.detail()["message"] == "io.EOF") {
                isDone = true;
            } else {
                return readResult;
            }
        }
    }
    return result;
}

function writeCharacters(string content, int startOffset) returns int|error? {
    var result = wch.write(content, startOffset);
    if (result is int) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Character channel not initialized properly");
        return e;
    }
}

function readJson() returns json|error {
    var result = rch.readJson();
    if (result is json) {
        return result;
    } else {
        return result;
    }
}

function readXml() returns xml|error {
    var result = rch.readXml();
    if (result is xml) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Character channel not initialized properly");
        return e;
    }
}

function writeJson(json content) {
    var result = wch.writeJson(content);
}

function writeXml(xml content) {
    var result = wch.writeXml(content);
}

function closeReadableChannel() {
    var err = rch.close();
}

function closeWritableChannel() {
    var err = wch.close();
}
