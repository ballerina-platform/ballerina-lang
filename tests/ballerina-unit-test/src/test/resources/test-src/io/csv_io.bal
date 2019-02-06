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

type Employee record {
    string id;
    string name;
    float salary;
};

io:ReadableCSVChannel? rch = ();
io:WritableCSVChannel? wch = ();

const string IO_ERROR_CODE = "{ballerina/io}IOError";

function initReadableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) {
    io:ReadableByteChannel byteChannel = untaint io:openReadableFile(filePath);
    io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
    rch = new io:ReadableCSVChannel(charChannel, fs = fieldSeparator);
}

function initWritableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) {
    io:WritableByteChannel byteChannel = untaint io:openWritableFile(filePath);
    io:WritableCharacterChannel charChannel = new io:WritableCharacterChannel(byteChannel, encoding);
    wch = new io:WritableCSVChannel(charChannel, fs = fieldSeparator);
}

function initOpenCsvChannel(string filePath, string encoding, io:Separator fieldSeparator, int nHeaders = 0) {
    io:ReadableByteChannel byteChannel = untaint io:openReadableFile(filePath);
    io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
    rch = new io:ReadableCSVChannel(charChannel, fs = fieldSeparator, nHeaders = nHeaders);
}

function nextRecord() returns (string[]|error) {
    var result = rch.getNext();
    if (result is string[]) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error(IO_ERROR_CODE, { message : "Record channel not initialized properly" });
        return e;
    }
}

function writeRecord(string[] fields) {
    var result = wch.write(fields);
}

function close() {
    _ = rch.close();
    _ = wch.close();
}


function hasNextRecord() returns boolean? {
    return rch.hasNext();
}

function getTable(string filePath, string encoding, io:Separator fieldSeperator) returns float|error {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
    io:ReadableCSVChannel csv = new io:ReadableCSVChannel(charChannel, fs = fieldSeperator);
    float total = 0.0;
    var tableResult = csv.getTable(Employee);
    if (tableResult is table<Employee>) {
        foreach var x in tableResult {
            total = total + x.salary;
        }
        return total;
    } else {
        return tableResult;
    }
}
