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

function testInvalidIntersection() {
    var fn = function () returns int { return 1; };
    future<int> & readonly x = start fn();
}

function testUnsupportedIntersection() {
    json & int x = 1;
}

type IntersectionWithInvalidRecordTypeAfter Bar & readonly;

type Bar record {
    future<int> ft;
};

type IntersectionWithInvalidObjectTypeAfter Baz & readonly;

type Baz object {
    future<int> ft;
};

type X error<record { int i?; }> & error<record { int i; }>;
type Y readonly & record { int i; };

type Z record {
    *Y;
    X x;
    Y y;
};

type ReadonlyType readonly;
type FutureType future<int>;
type T2 FutureType & ReadonlyType;

function testReadonlyIntersectionImmutability() {
    ReadonlyType & string[] arr = ["a", "b", "c"];
    arr.push("d");
}

type Foo record {
    string p1;
    int p2?;
};

function testAssigningMutableValuesToReadonlyIntersectionsWithTypeRefForReadonly() {
    int[] numArray = [1, 2, 3, 4];
    ReadonlyType & int[] _ = numArray;
    Foo foo = {p1: "P1"};
    Foo & ReadonlyType _ = foo;
}
