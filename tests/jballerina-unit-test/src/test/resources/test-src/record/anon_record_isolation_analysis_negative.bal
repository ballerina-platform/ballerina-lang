// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testRecordWithClosureTypeMutabilityNegative() {
    int j = 9;
    string n = "A";
    record {| int i = j; |} _ = {};
    record {| int i = j; |}[] _ = [];
    [record {| int i = j; |}] _ = [];
    table<record {| int i = j; |}> _ = table [];
    record {| int i = j; record {| string m = n; |} k;|}[] _ = [];
    record {| int i = j; record {| string m = n; |}[] k;|}[] _ = [];
    record {| int i = j; |}|string _ = {};
    [map<record {| int i = j; |}|record {| string m = n; |}[]>, record {| int i = j; |}...] _ = [];
    map<record {| int i = j; |}> a9;
    stream<record {| int i = j; |}> a10;
    stream<record {| int i = j; |}[]> a11;
    stream<record {| int i = j; |}?> a12;
}

function testRecordWithClosureTypeMutabilityNegative2() {
    int j = 9;
    string n = "A";
    record {| int i = j; |} a1;
    record {| int i = j; |}[] a2;
    [record {| int i = j; |}] a3;
    table<record {| int i = j; |}> a4;
    record {| int i = j; record {| string m = n; |} k;|}[] a5;
    record {| int i = j; record {| string m = n; |}[] k;|}[] a6;
    record {| int i = j; |}|string a7;
    [map<record {| int i = j; |}|record {| string m = n; |}[]>, record {| int i = j; |}...] a8;
    stream<record { int i = j; }> a9;
    stream<record { int i = j; }[]> a10;
    stream<record { int i = j; }?> a11;
}

int x = 12;

function foo() returns record {| int i = x; |}[] {
    panic error("Bad Sad!");
}

type AA record {
    string a;
    int b;
    record {| int i = x; |} c;
};

class BB {
    public string a = "Ballerina";
    record {| int i = x; |} c = {};
}

function testIntersectionTypeNegative() {
    int j = 9;
    record {| int i = j; |} & readonly a1;
    (record {| int i = j; |} & readonly)[] a2;
    [record {| int i = j; |} & readonly] a3;
    (record {| int i = j; |} & readonly)|string a4;
    map<record {| int i = j; |} & readonly> a5;
    table<record {| int i = j; |} & readonly> a6;
    record {| int i = 1; record {| int x = j; |} & readonly k; |}[] a7;
    record {| int i = 1; (record {| int x = j; |} & readonly)[] j; |}[] a8;
    [map<record {| int i = j; |} & readonly>, record {| int i = j; |} & readonly...] a9;
    stream<record {| int i = j; |} & readonly> a10;
    stream<(record {| int i = j; |} & readonly)[]> a11;
    stream<(record {| int i = j; |} & readonly)?> a12;
}

type CC record {
    string a;
    map<record {| int i = x; |} & readonly> b = {};
};

type EE object {
    int a;
    map<record {| int i = x; |}> b;
};

function testAnonTypeNegativeScenarios() {
    function (record {| int i = x; |}[]) returns [record {| int i = x; |}] a1;
    error<record {| int i = x; |}> a2;
    typedesc<record {| int i = x; |}> a3;
    typedesc<record {| int i = x; |}[]> a4;
    future<record {| int i = x; |}[]> a5;
    future<record {| int i = 1; record {| int i = x; |} j = {}; |}[]> a6;
    function (record {| int i = x; |}...) a7;
    function (map<record {| int i = x; |}>...) a8;
}
