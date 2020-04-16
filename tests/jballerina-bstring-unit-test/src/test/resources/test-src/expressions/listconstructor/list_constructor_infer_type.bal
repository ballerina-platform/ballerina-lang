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

type Foo record {
    string s;
    int i = 1;
};

type Bar object {
    int i;

    public function __init(int i) {
        self.i = i;
    }
};

Foo f = {s: "hello"};
Bar b = new (1);
json j = 1;

function inferSimpleTuple() {
    var x = [1, 2.0, 3.0d, false];
    typedesc<any> ta = typeof x;
    assertEquality("typedesc [int,float,decimal,boolean]", ta.toString());
}

function inferStructuredTuple() {
    var x = [f, b, xml `text`, j];
    typedesc<any> ta = typeof x;
    assertEquality("typedesc [Foo,Bar,lang.xml:Text,json]", ta.toString());
}

function inferNestedTuple() {
    int[2] arr = [1, 2];
    var x = [1, 2.0d, [3, f, [b, b]], arr, j];
    typedesc<any> ta = typeof x;
    assertEquality("typedesc [int,decimal,[int,Foo,[Bar,Bar]],int[2],json]", ta.toString());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
