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

function testBasicLValueFillRead() {
    [string, int[][]] itup = ["int"];
    itup[1][2][4] = 10;

    [string, float[][]] ftup = ["float"];
    ftup[1][4][3] = 12.34;
    ftup[1][2][2] = 45.67;
    ftup[1][2][5] = 6.78;

    [string, decimal[][]] dtup = ["decimal"];
    dtup[1][1][3] = 12.34;
    dtup[1][2][2] = 45.67;
    dtup[1][2][5] = 6.78;

    [string, boolean[][]] btup = ["boolean"];
    btup[1][3][3] = true;

    [string, string[][]] stup = ["string"];
    stup[1][3][3] = "foo";

    assert(<[string, int[][]]>["int", [[], [], [0, 0, 0, 0, 10]]], itup);
    assert(<[string, float[][]]>["float", [[], [], [0, 0, 45.67, 0, 0, 6.78], [], [0, 0, 0, 12.34]]], ftup);
    assert(<[string, decimal[][]]>["decimal", [[], [0, 0, 0, 12.34], [0, 0, 45.67, 0, 0, 6.78]]], dtup);
    assert(<[string, boolean[][]]>["boolean", [[], [], [], [false, false, false, true]]], btup);
    assert(<[string, string[][]]>["string", [[], [], [], ["", "", "", "foo"]]], stup);
}

type Person record {|
    string name = "Pubudu";
|};

function testRestMemberFill() {
    [string, int, Person[]...] tup = ["records", 100];
    tup[3][1] = {name: "John Doe"};
    tup[3][2].name = "Jane Doe";

    assert(<[string, int, Person[]...]>["records", 100, [], [{name: "Pubudu"}, {name: "John Doe"}, {name: "Jane Doe"}]], tup);
}

type Person2 record {|
    string name;
|};

function testRecordsWithoutFillerValues() {
    [string, Person2[]...] arr = [];
    arr[2][1] = {name: "Pubudu"};
}


// Util functions

function assert(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                        + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error("{AssertionError}", message = msg);
}
