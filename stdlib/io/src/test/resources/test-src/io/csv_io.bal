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

type PerDiem record {
    int id;
    string name;
    int age;
    int? beverageAllowance;
    float? total;
    string? department;
};

type CommonApp record {
    string appId;
    string createdDt;
    string exportDt;
    string firstName;
    string middleName;
    string lastName;
    string gender;
    string birthDate;
    string address1;
    string address2;
    string city;
    string state;
    string zip;
    string country;
    string email;
    string phoneNumber;
    string hispLatino;
    string citizenship;
    string schoolLookup;
};

io:ReadableCSVChannel? rch = ();
io:WritableCSVChannel? wch = ();

function initReadableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) returns @tainted error? {
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(<@untainted>byteChannel, encoding);
        rch = new io:ReadableCSVChannel(charChannel, fieldSeparator);
    } else {
        return byteChannel;
    }
}

function initWritableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) returns error? {
    io:WritableByteChannel byteChannel = check <@untainted>io:openWritableFile(filePath);
    io:WritableCharacterChannel charChannel = new io:WritableCharacterChannel(byteChannel, encoding);
    wch = new io:WritableCSVChannel(charChannel, fieldSeparator);
}

function initOpenCsvChannel(string filePath, string encoding, io:Separator fieldSeparator, int nHeaders = 0) returns error? {
    var byteChannel = <@untainted>io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
        rch = new io:ReadableCSVChannel(charChannel, fieldSeparator, nHeaders);
    } else {
        return byteChannel;
    }
}

function nextRecord() returns @tainted string[] | error {
    var cha = rch;
    if (cha is io:ReadableCSVChannel) {
        var result = cha.getNext();
        if (result is string[]) {
            return result;
        } else if (result is error) {
            return result;
        }
    }
    error e = error(io:GENERIC_ERROR, message = "Record channel not initialized properly");
    return e;
}

function writeRecord(string[] fields) {
    var cha = wch;
    if (cha is io:WritableCSVChannel) {
        var result = cha.write(fields);
    }
}

function close() {
    var rcha = rch;
    var wcha = wch;
    if (rcha is io:ReadableCSVChannel) {
        checkpanic rcha.close();
    }
    if (wcha is io:WritableCSVChannel) {
        checkpanic wcha.close();
    }
}

function hasNextRecord() returns boolean? {
    var rcha = rch;
    if (rcha is io:ReadableCSVChannel) {
        return rcha.hasNext();
    }
}

function getTable(string filePath, string encoding, io:Separator fieldSeparator) returns @tainted float | error {
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
        io:ReadableCSVChannel csv = new io:ReadableCSVChannel(charChannel, fieldSeparator);
        float total = 0.0;
        var tableResult = csv.getTable(Employee);
        if (tableResult is table<record {}>) {
            table<Employee> tb = <table<Employee>> tableResult;
            foreach var x in tb {
                total = total + x.salary;
            }
            error? closeResult = byteChannel.close();
            return total;
        } else {
            return tableResult;
        }
    } else {
        return byteChannel;
    }
}

function getTableWithNill(string filePath) returns @tainted [string, string] | error {
    string name = "";
    string dep = "";
    var rCsvChannel = io:openReadableCsvFile(filePath, skipHeaders = 1);
    if (rCsvChannel is io:ReadableCSVChannel) {
        var tblResult = rCsvChannel.getTable(PerDiem);
        if (tblResult is table<record {}>) {
            table<PerDiem> tb = <table<PerDiem>> tblResult;
            foreach var rec in tb {
                name = name + rec.name;
                dep = dep + (rec.department ?: "-1");
            }
            error? closeResult = rCsvChannel.close();
        } else {
            return tblResult;
        }
    }
    return [name, dep];
}

function getTableWithHeader(string filePath) returns @tainted string[] | error {
    var rCsvChannel = io:openReadableCsvFile(filePath, skipHeaders = 0);
    string[] keys = [];
    if (rCsvChannel is io:ReadableCSVChannel) {
        var tblResult = rCsvChannel.getTable(CommonApp);
        if (tblResult is table<record {}>) {
            table<CommonApp> tb = <table<CommonApp>> tblResult;
            foreach var rec in tb {
                keys.push(rec.appId);
            }
            error? closeResult = rCsvChannel.close();
        } else {
            return tblResult;
        }
    }
    return keys;
}
