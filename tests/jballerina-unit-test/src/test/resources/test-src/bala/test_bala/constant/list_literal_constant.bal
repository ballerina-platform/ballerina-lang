// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
// under the License

import testorg/foo;

function testSimpleArrayAccess() {
    assertEqual(foo:l1, ["a", "b", "c"]);
}

function testSimpleTupleAccess() {
    assertEqual(foo:l2, [1, "d"]);
}

function test2DTupleAccess() {
    assertEqual(foo:l3, [1, ["e", 2]]);
}

function test2DArrayAccess() {
    assertEqual(foo:l4, [[1, 2, 3], [4, 5, 6]]);
}

function testFixedLengthArrayAccess() {
    assertEqual(foo:l5, [true, false, true]);
}

function testArrayWithRestAccess() {
    assertEqual(foo:l7, [1, "f", "g"]);
}

function test2DUnionArrayAccess() {
    assertEqual(foo:l8, [[1, "2", 3], [4, 5, 6]]);
    assertEqual(foo:l9, [[1, 2, 3], ["4", "5", "6"]]);
}

function assertEqual(anydata actual, anydata expected) {
    if expected == actual {
        return;
    }
    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}
