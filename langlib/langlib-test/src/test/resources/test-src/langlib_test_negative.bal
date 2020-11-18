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

public function testDiagnostics() {
    map<string> words = { a: "ant", b: "bear"};
    var s = words.'map(s => s.toLowerAscii()).indexOf("bear");
}

json j = {
    name: "apple", 
    color: "red", 
    price: [
        1.22,
        4, 
        3.5
    ], 
    quality: true
};

function testEnsureTypeWithArray() returns error? {
    float[] age = check j.price;
}

function testEnsureTypeWithUnion1() returns error? {
    int|string|float[] name = check j.name;
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
