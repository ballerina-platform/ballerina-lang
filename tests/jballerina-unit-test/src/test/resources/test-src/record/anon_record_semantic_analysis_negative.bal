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

function testAnonClosedRecordNegative1() {
    record {| int i = "foo"; |}[] a1;
    [record {| int i = "foo"; |}] a2;
    record {| int i = "foo"; |}|string a3;
    map<record {| int i = "foo"; |}> a4;
    table<record {| int i = "foo"; |}> a5;
    record {| int i = 1; string j = 2; record {| int x = "foo"; |} k; |}[] a6;
    record {| int i = 1; record {| int x = "foo"; |}[] j; |}[] a7;
    [map<record {| int i = "foo"; |}|map<record {| int i = "foo"; |}[]>>, record {| int j = "foo"; |}...] a8;
}

function testAnonOpenRecordNegative2() {
    record { int i = "foo"; }[] a1;
    [record { int i = "foo"; }] a2;
    record { int i = "foo"; }|string a3;
    map<record { int i = "foo"; }> a4;
    table<record { int i = "foo"; }> a5;
    record {| int i = 1; string j = 2; record { int x = "foo"; } k; |}[] a6;
    record {| int i = 1; record { int x = "foo"; }[] j; |}[] a7;
}

function foo() returns record {| int i = "ABC"; |}[] {
    panic error("Bad Sad!");
}

type AA record {
    string a;
    int b;
    record {| int i = "ABC"; |} c;
};

class BB {
    public string a = "Ballerina";
    record {| int i = "ABC"; |} c = {};
}

function testIntersectionTypeNegative() {
    record {| int i = "foo"; |} & readonly a1;
    (record {| int i = "foo"; |} & readonly)[] a2;
    [record {| int i = "foo"; |} & readonly] a3;
    (record {| int i = "foo"; |} & readonly)|string a4 = {};
    map<record {| int i = "foo"; |} & readonly> a5 = {};
    table<record {| int i = "foo"; |} & readonly> a6;
    record {| int i = 1; record {| int x = "foo"; |} & readonly k; |}[] a7 = [];
    record {| int i = 1; (record {| int x = "foo"; |} & readonly)[] j; |}[] a8;
    [map<record {| int i = "foo"; |} & readonly>, record {| int j = "foo"; |} & readonly...] a9;
}

type CC record {
    string a;
    map<record {| int i = "foo"; |} & readonly> b = {};
};
