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

function foo() returns [int, int|error] => [1, 1];
var [x, y] = foo();
var a = y;

var m = error("Error!");

function testVarInVarDeclWithTypeIncludingErrorNegative() {
    var [x1, y1] = foo();
    var a1 = y1;

    var m1 = error("Error!");
}

error[] errs = let var e1 = error("one"), var e2 = error("two"), var e3 = [error("three")] in [e3[0]];

function testVarInLetVarWithTypeIncludingErrorNegative() {
    error[] errs2 = let var e1 = error("one"), var e2 = {a: error("two")}, var e3 = error("three") in [];
}

type MyError error<record {error err;}>;

function baz() returns record {[error|boolean, int] q; error r; MyError s; int t;} =>
    {q: [error("four"), 1], r: error("five"), s: error MyError("five", err = error("six")), t: 101};

function testOnFailClauseWithError() {
    do {
        fail error("error!");
    } on fail var x {

    }
}
