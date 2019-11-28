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

io:ReadableTextRecordChannel? rch = ();
io:WritableTextRecordChannel? wch = ();

function initReadableChannel(string filePath, string encoding, string recordSeparator,
                                    string fieldSeparator) returns @tainted error? {
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is error) {
        return byteChannel;
    } else {
        io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
        rch = <@untainted io:ReadableTextRecordChannel> new io:ReadableTextRecordChannel(charChannel, fieldSeparator, recordSeparator);
    }
}

function initWritableChannel(string filePath, string encoding, string recordSeparator,
                             string fieldSeparator) returns @tainted io:Error? {
    io:WritableByteChannel byteChannel = check io:openWritableFile(filePath);
    io:WritableCharacterChannel charChannel = new io:WritableCharacterChannel(byteChannel, encoding);
    wch = <@untainted io:WritableTextRecordChannel> new io:WritableTextRecordChannel(charChannel, fieldSeparator, recordSeparator);
}


function nextRecord() returns @tainted string[]|error {
    var cha = rch;
    if(cha is io:ReadableTextRecordChannel) {
        var result = cha.getNext();
        if (result is string[]) {
            return result;
        } else {
            return result;
        }
    }
    error e = error("Record channel not initialized properly");
    return e;
}

function writeRecord(string[] fields) {
    var cha = wch;
    if(cha is io:WritableTextRecordChannel){
        var result = cha.write(fields);
    }
}

function closeReadableChannel() {
    var cha = rch;
    if(cha is io:ReadableTextRecordChannel) {
        var err = cha.close();
    }
}

function closeWritableChannel() {
    var cha = wch;
    if(cha is io:WritableTextRecordChannel) {
        var err = cha.close();
    }
}


function hasNextRecord() returns boolean? {
    var cha = rch;
    if(cha is io:ReadableTextRecordChannel) {
        return cha.hasNext();
    }
}
