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
io:WritableCharacterChannel? wca = ();

function initReadableChannel(string filePath, string encoding) returns @tainted io:Error? {
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        rch = <@untainted> new io:ReadableCharacterChannel(byteChannel, encoding);
    } else {
        return byteChannel;
    }
}

function initWritableChannel(string filePath, string encoding) returns @tainted io:Error? {
    io:WritableByteChannel byteChannel = check io:openWritableFile(filePath);
    wch = <@untainted> new io:WritableCharacterChannel(byteChannel, encoding);
}

function initWritableChannelToAppend(string filePath, string encoding) returns @tainted io:Error? {
    io:WritableByteChannel byteChannel = check io:openWritableFile(filePath, true);
    wca = <@untainted> new io:WritableCharacterChannel(byteChannel, encoding);
}

function readCharacters(int numberOfCharacters) returns @tainted string|error {
    var rCha = rch;
    if(rCha is io:ReadableCharacterChannel){
        var result = rCha.read(numberOfCharacters);
        if (result is string) {
            return result;
        } else {
            return result;
        }
    }
    error e = error("Character channel not initialized properly");
    return e;
}

function readAllCharacters() returns @tainted string|io:Error? {
    int fixedSize = 500;
    boolean isDone = false;
    string result = "";
    while (!isDone) {
        var readResult = readCharacters(fixedSize);
        if (readResult is string) {
            result = result + readResult;
        } else {
            error e = readResult;
            if (e is io:EofError) {
                isDone = true;
            } else {
                io:GenericError readError = error(io:GENERIC_ERROR, message = "Error while reading the content", cause = readResult);
                return readError;
            }
        }
    }
    return result;
}

function writeCharacters(string content, int startOffset) returns int|io:Error? {
    var wCha = wch;
    if(wCha is io:WritableCharacterChannel){
        var result = wCha.write(content, startOffset);
        if (result is int) {
            return result;
        } else {
            return result;
        }
    }
    // error e = error("Character channel not initialized properly");
    io:GenericError e = error(io:GENERIC_ERROR, message = "Character channel not initialized properly");
    return e;
}

function appendCharacters(string content, int startOffset) returns int|io:Error? {
    var wCha = wca;
    if(wCha is io:WritableCharacterChannel){
        var result = wCha.write(content, startOffset);
        if (result is int) {
            return result;
        } else {
            return result;
        }
    }
    io:GenericError e = error(io:GENERIC_ERROR, message = "Character channel not initialized properly");
    return e;
}

function readJson() returns @tainted json|error {
    var rCha = rch;
    if(rCha is io:ReadableCharacterChannel){
        var result = rCha.readJson();
        if (result is json) {
            return result;
        } else {
            return result;
        }
    }
    return ();
}

function readXml() returns @tainted xml|error {
    var rCha = rch;
    if(rCha is io:ReadableCharacterChannel){
        var result = rCha.readXml();
        if (result is xml) {
            return result;
        } else {
            return result;
        }
    }
    io:GenericError e = error(io:GENERIC_ERROR, message = "Character channel not initialized properly");
    return e;
}

function writeJson(json content) {
    var wCha = wch;
    if(wCha is io:WritableCharacterChannel){
        var result = wCha.writeJson(content);
    }
}

function writeJsonWithHigherUnicodeRange() {
    json content = {
        "loop": "É"
    };
    var wCha = wch;
    if(wCha is io:WritableCharacterChannel){
        var result = wCha.writeJson(content);
        if (result is error) {
            panic <error>result;
        }
    }
}

function writeXml(xml content) {
    var wCha = wch;
    if(wCha is io:WritableCharacterChannel){
        var result = wCha.writeXml(content);
    }
}

function closeReadableChannel() {
    var rCha = rch;
    if(rCha is io:ReadableCharacterChannel){
        var err = rCha.close();
    }
}

function closeWritableChannel() {
    var wCha = wch;
    if(wCha is io:WritableCharacterChannel){
        var err = wCha.close();
    }
}

function closeWritableChannelToAppend() {
    var wCha = wch;
    if(wCha is io:WritableCharacterChannel){
        var err = wCha.close();
    }
}
