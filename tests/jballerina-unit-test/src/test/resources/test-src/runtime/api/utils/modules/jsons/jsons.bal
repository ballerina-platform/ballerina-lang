// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// Prints `Hello, World!`.

import ballerina/jballerina.java;
import ballerina/test;

enum Status {
    OPEN,
    CLOSE
}

type num 1|2|3;
type ftype1 "foo";
type ftype2 "bar";
type funion ftype1|ftype2;

type Shop record {
    string name;
    Status status;
    num number;
    ftype1 foo_status;
    funion union_status;
};

function testConvertJSONToRecord() {
    json j = {
        "name": "My Shop",
        "status": "OPEN",
        "number": 1,
        "foo_status": "foo",
        "union_status": "bar"
    };

    map<anydata> recordValue = convertJSONToRecord(j, Shop);
    string expectedOutput = "{\"name\":\"My Shop\",\"status\":\"OPEN\",\"number\":1,\"foo_status\":" +
    "\"foo\",\"union_status\":\"bar\"}";
    test:assertEquals(recordValue.toString(), expectedOutput);
}

function convertJSONToRecord(anydata v, typedesc<anydata> t) returns map<anydata> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Jsons",
    name: "testConvertJSONToRecord"
} external;

public function validateAPI() {
    testConvertJSONToRecord();
}

public function validateStringAPI() {
    json j = {
        "val": "a\"b\\c\td",
        "arr": ["a\"b\\c\td", 12],
        "map": {"value": "a\"b\\c\td"},
        "arr_map": ["foo", {"value": "a\"b\\c\td"}, 87]
    };
    string result = convertJSONToString(j);
    json|error jValue = checkpanic  result.fromJsonString();
    test:assertTrue(jValue is json);
    test:assertTrue(jValue is map<json>);
    map<json> mapResult = checkpanic jValue.ensureType();
    test:assertEquals(mapResult.toString(), "{\"val\":\"a\"b\\c\td\",\"arr\":[\"a\"b\\c\td\",12]," +
    "\"map\":{\"value\":\"a\"b\\c\td\"},\"arr_map\":[\"foo\",{\"value\":\"a\"b\\c\td\"},87]}");
}

function convertJSONToString(anydata v) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Jsons",
    name: "convertJSONToString"
} external;

