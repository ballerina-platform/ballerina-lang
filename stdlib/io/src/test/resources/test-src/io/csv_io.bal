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

const string IO_ERROR_CODE = "{ballerina/io}Error";

function initReadableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) returns @tainted error? {
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel( <@untainted> byteChannel, encoding);
        rch = new io:ReadableCSVChannel(charChannel, fieldSeparator);
    } else {
        return byteChannel;
    }
}

function initWritableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) {
    io:WritableByteChannel byteChannel = <@untainted io:WritableByteChannel> io:openWritableFile(filePath);
    io:WritableCharacterChannel charChannel = new io:WritableCharacterChannel(byteChannel, encoding);
    wch = new io:WritableCSVChannel(charChannel, fieldSeparator);
}

function initOpenCsvChannel(string filePath, string encoding, io:Separator fieldSeparator, int nHeaders = 0) returns error? {
    var byteChannel =  <@untainted> io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
        rch = new io:ReadableCSVChannel(charChannel, fieldSeparator, nHeaders);
    } else {
        return byteChannel;
    }
}

function nextRecord() returns @tainted string[]|error {
    var cha = rch;
    if(cha is io:ReadableCSVChannel){
        var result = cha.getNext();
        if (result is string[]) {
            return result;
        } else if(result is error) {
            return result;
        }
    }
    error e = error(IO_ERROR_CODE, message = "Record channel not initialized properly");
    return e;
}

function writeRecord(string[] fields) {
    var cha = wch;
    if(cha is io:WritableCSVChannel){
        var result = cha.write(fields);
    }
}

function close() {
    var rcha = rch;
    var wcha = wch;
    if(rcha is io:ReadableCSVChannel) {
        checkpanic rcha.close();
    }
    if(wcha is io:WritableCSVChannel) {
        checkpanic wcha.close();
    }
}

function hasNextRecord() returns boolean? {
    var rcha = rch;
    if(rcha is io:ReadableCSVChannel) {
        return rcha.hasNext();
    }
}

function getTable(string filePath, string encoding, io:Separator fieldSeparator) returns @tainted float|error {
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
        io:ReadableCSVChannel csv = new io:ReadableCSVChannel(charChannel, fieldSeparator);
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
    } else {
        return byteChannel;
    }
}
