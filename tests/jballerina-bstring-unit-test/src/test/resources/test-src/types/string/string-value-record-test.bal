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

type Employee record {
    int id;
    string name;
    float salary;
};

function testRecordStringValue() returns int {
    string smiley = "h😀llo";
    record {| string myField; |} r = {myField: smiley};
    boolean containsKey = r.hasKey("myField");
    if (!containsKey) {
        return -1;
    }
    return r.myField.length();
}

function testRecordGetKeys() returns int {
    Employee john = {
        id: 122,
        name: "John Doe",
        salary: .50
    };
    john["smile👍"] = "smile";
    return john.keys().toString().length();
}

function testMapToKeys() returns int {
    map<string> addrMap = { line1: "No. 20", line2: "Palm Grove 🎬",
            "city🇻🇦": "Colombo 03", "country🇱🇰": "Sri Lanka" };
    return addrMap.keys().toString().length();
}
