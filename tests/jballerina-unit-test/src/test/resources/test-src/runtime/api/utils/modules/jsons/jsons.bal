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

    var jval_result = convertJSON(j, Shop);
    test:assertEquals(jval_result.toString(), expectedOutput);
}

type numUnion1 int|decimal|float;
type singleton1 1|"1"|true|"one";
type singleton2 2|"2"|true|"two";
type singletonunion singleton1|singleton2|false;
type union1 string|byte;
type union2 int|int:Signed16;
type union3 singletonunion|union1|union2;
type intArray int[];
type stringMap map<string>;

function testConvertJSON() {
    json jval = "apple";
    var jval_result = trap convertJSON(jval, numUnion1);
    test:assertEquals(jval_result is error, true);

    jval = 10.0;
    jval_result = trap convertJSON(jval, numUnion1);
    test:assertEquals(jval_result, 10.0);

    jval = 650000000;
    jval_result = trap convertJSON(jval, union3);
    test:assertEquals(jval_result, 650000000);

    jval = "one";
    jval_result = trap convertJSON(jval, singleton1);
    test:assertEquals(jval_result, "one");

    jval = 3;
    jval_result = trap convertJSON(jval, singleton2);
    test:assertEquals(jval_result is error, true);

    jval = -13.726728468d;
    jval_result = trap convertJSON(jval, decimal);
    test:assertEquals(jval_result, -13.726728468d);

    jval = true;
    jval_result = trap convertJSON(jval, boolean);
    test:assertEquals(jval_result, true);

    jval = {"a": true};
    jval_result = trap convertJSON(jval, json);
    test:assertEquals(jval_result, {"a": true});

    jval = [12,-1,0,15];
    jval_result = trap convertJSON(jval, intArray);
    test:assertEquals(jval_result, [12,-1,0,15]);

    jval = {"a": "true"};
    jval_result = trap convertJSON(jval, stringMap);
    test:assertEquals(jval_result, {"a": "true"});

    jval = null;
    jval_result = trap convertJSON(jval, typeof ());
    test:assertEquals(jval_result, ());
}

function convertJSONToRecord(anydata v, typedesc<anydata> t) returns map<anydata> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues",
    name: "testConvertJSONToRecord"
} external;

function convertJSON(anydata v, typedesc<anydata> t) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues",
    name: "testConvertJSON"
} external;

public function validateAPI() {
    testConvertJSONToRecord();
    testConvertJSON();
}

type Address record {
    string country;
    string city;
    string street;
};

public function validateStringAPI() {
    json j = {
        "val": "a\"b\\c\td",
        "arr": ["a\"b\\c\td", 12],
        "map": {"value": "a\"b\\c\td"},
        "arr_map": ["foo", {"value": "a\"b\\c\td"}, 87]
    };
    string result = convertJSONToString(j);
    json|error jValue = checkpanic result.fromJsonString();
    test:assertTrue(jValue is json);
    test:assertTrue(jValue is map<json>);
    map<json> mapResult = checkpanic jValue.ensureType();
    test:assertEquals(mapResult.toString(), "{\"val\":\"a\"b\\c\td\",\"arr\":[\"a\"b\\c\td\",12]," +
    "\"map\":{\"value\":\"a\"b\\c\td\"},\"arr_map\":[\"foo\",{\"value\":\"a\"b\\c\td\"},87]}");

    anydata tab = table [
            {a: 1, b: " 2"},
            {c: "3", d: "4"}
        ];
    string|error res = convertJSONToString(tab);
    test:assertTrue(res is string);
    test:assertEquals(res, "[{\"a\":1, \"b\":\" 2\"}, {\"c\":\"3\", \"d\":\"4\"}]");

    Address addr = {country: "x", city: "y", "street": "z", "no": 3, "tab": tab};
    table<Address> tab1 = table [];
    tab1.add(addr);
    res = trap convertJSONToString(tab1);
    test:assertTrue(res is error);
    error err = <error>res;
    test:assertEquals(<string>checkpanic err.detail()["message"], "'table<utils.jsons:Address>' value cannot be" +
    " converted to 'json': cannot construct json object from 'table<map<anydata>>' type data");
    test:assertEquals(err.message(), "{ballerina/lang.value}ConversionError");

    err = error("error");
    res = trap convertJSONToString(err);
    test:assertTrue(res is error);
    err = <error>res;
    test:assertEquals(<string>checkpanic err.detail()["message"], "'error' value cannot be converted to 'json'");
    test:assertEquals(err.message(), "{ballerina/lang.value}ConversionError");

}

function convertJSONToString(any|error v) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues",
    name: "convertJSONToString"
} external;

