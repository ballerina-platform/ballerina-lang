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

json j1 = {
    fname: "Mary💦",
    lname: "Jane",
    address: {
        line: "20 Palm Grove",
        city: "Colombo 03",
        country: "Sri Lanka🦾"
    }
};

function testJsonAccess() returns int {
    string s = extractFieldValue(checkpanic j1.fname);
    s+=  extractFieldValue(checkpanic j1.lname);
    s+=  extractFieldValue(checkpanic j1.address.country);
    return s.length();
}

function testJsonOptionalAccess() returns int {
    string s = extractFieldValue(checkpanic j1?.fname);
    s+=  extractFieldValue(checkpanic j1?.lname);
    return s.length();
}

function extractFieldValue(json fieldValue) returns (string) {
    any a = fieldValue;
    if a is string {
        return a;
    } else if a is json {
        return "Error";
    }
    return "";
}
