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
// under the License.

function testTupleDestructuringWithOptionalFieldBindingPattern() {
    [record {|int a; int b?;|}] l1 = [{a: 1, b: 2}];
    int a;
    int b;
    int c;
    [{a, b}]= l1;
    [int, [record {int b?;}]] l2 = [1, [{b: 1}]];
    [a, [{b}]] = l2;
}

function testTupleDestructuringWithUnspecifiedFields() {
    [record {int a; int b;}] l1 = [{a: 1, b: 2, c: 3}];
    int a;
    int b;
    int c;
    [{a, b, c}]= l1;
    [int, [record {int b;}]] l2 = [1, [{b: 1}]];
    [a, [{b, c}]] = l2;
}

function testRecordDestructuringWithOptionalFieldBindingPattern() {
    record {int a; int b?;} r1 = {a: 1, b: 2};
    int a;
    int b;
    {a, b} = r1;
    record {|int a; record {|int b?;|} bb;|} r2 = {a: 1, bb: {b: 2}};
    {a, bb: {b}} = r2;
}

function testRecordDestructuringWithUnspecifiedFields() {
    record {int a;} r1 = {a: 1};
    int a;
    int b;
    {a, b} = r1;
    record {|int a; record {||} bb;|} r2 = {a: 1, bb: {}};
    {a, bb: {b}} = r2;
}
