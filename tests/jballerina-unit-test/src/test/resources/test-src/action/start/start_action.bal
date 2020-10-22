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

import ballerina/test;

type FooRec record {|
    function () returns string fn = () => "FOO";
    any...;
|};

function testRecFieldFuncPointerAsyncCall() {
    FooRec fr = {};
    future<string> fs = start fr.fn();
    string result = wait fs;
    assert("FOO", result);

    fr["rec"] = <FooRec>{};
    FooRec fr2 = <FooRec>fr["rec"];
    fs = start fr2.fn();
    string result2 = wait fs;
    assert("FOO", result2);
}

client class BarObj {
    remote function getInt() returns int => 100;

    function getName() returns string => "BAR";
}

function testObjectMethodsAsAsyncCalls() {
    BarObj bo = new;
    future<int> fi = start bo->getInt();
    int resulti = wait fi;
    assert(100, resulti);

    future<string> fs = start bo.getName();
    string results = wait fs;
    assert("BAR", results);
}


// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string detail = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}

function testCast() {
    future<string> fs1 = start getMessage([1,2]);
    string result1 = wait fs1;
    test:assertEquals(result1, "The value is [1,2]");

    map<int> marks = {sam: 50, jon: 60};
    future<string> fs2 = start getMessage(marks);
    string result2 = wait fs2;
    test:assertEquals(result2, "The value is {\"sam\":50,\"jon\":60}");
}

function getMessage(any j) returns string {
   return "The value is " + j.toString();
}
