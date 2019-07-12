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
        rch = <@untainted io:ReadableTextRecordChannel> new io:ReadableTextRecordChannel(charChannel, fs = fieldSeparator, rs = recordSeparator);
    }
}

function initWritableChannel(string filePath, string encoding, string recordSeparator,
                             string fieldSeparator) returns io:IOError? {
    io:WritableByteChannel byteChannel = check io:openWritableFile(filePath);
    io:WritableCharacterChannel charChannel = new io:WritableCharacterChannel(byteChannel, encoding);
    wch = <@untainted io:WritableTextRecordChannel> new io:WritableTextRecordChannel(charChannel, fs = fieldSeparator, rs = recordSeparator);
}


function nextRecord() returns @tainted string[]|error {
    var result = rch.getNext();
    if (result is string[]) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Record channel not initialized properly");
        return e;
    }
}

function writeRecord(string[] fields) {
    var result = wch.write(fields);
}

function closeReadableChannel() {
    var err = rch.close();
}

function closeWritableChannel() {
    var err = wch.close();
}


function hasNextRecord() returns boolean? {
    return rch.hasNext();
}
