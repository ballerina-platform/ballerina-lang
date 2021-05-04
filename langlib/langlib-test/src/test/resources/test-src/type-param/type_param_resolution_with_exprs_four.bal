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

type Map readonly & map<int>;

function test() returns error? {
    Map[] a = [];
    a.push(check getMap());

    stream<int>[] b = [];
    b.push(check  getStream());

    table<record {| int i; |}>[] c = [];
    c.push(check getTableOne());

    (table<record {| readonly int i; int j; |}> key (i))[] d = [];
    d.push(check getTableTwo());

    typedesc<anydata>[] e = [];
    e.push(check getTypedesc());

    [int, string...][] f = [];
    f.push(check getTuple());

    function ()[] g = [];
    g.push(getFunction());

    error<Detail>[] h = [];
    h.push(getError());
}

function getMap() returns Map|error => {};

function getStream() returns stream<int, ()>|error => (<int[]> [1, 2]).toStream();

function getTableOne() returns error|table<record {| int i; |}> => table [{i: 1}, {i: 2}];

function getTableTwo() returns (table<record {| readonly int i; int j; |}> key (i))|error => table [{i: 1, j: 101}, {i: 2, j: 202}];

function getTypedesc() returns error|typedesc<int> => int;

function getTuple() returns [int, string...]|error => [1, "hello"];

function getFunction() returns function () => function () {};

type Detail record {|
    int code;
|};

function getError() returns error<Detail> => error("error!", code = 123);

function testTypeParamResolutionWithExpression() {
    error? res = test();

    if res is () {
        return;
    }

    panic error("expected nil, found '" + (<error> res).toString() + "'");
}
