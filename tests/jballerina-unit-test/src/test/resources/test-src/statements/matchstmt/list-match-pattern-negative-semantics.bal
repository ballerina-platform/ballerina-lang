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

function testSimilarVariables() {
    any v = [2, 3];
    match v {
        [var a, var a] => {} // same variable cannot repeat in a match pattern // redeclared symbol 'a'
        [var a, [var a, 2]] => {} // same variable cannot repeat in a match pattern // redeclared symbol 'a'
    }
}

function testInvalidTypes((int|error)[][] a) {
    match a {
        [var p, ...var oth] if p is anydata => {
            string[] m = p;
            (int)[][] n = oth;
        }
    }
}

function testInvalidTypesWithJson(json j) returns [int, boolean[]] {
    match j {
        [var x, ...var y] => {
            return [x, y];
        }
        [var z] => {
            return [z, [z]];
        }
    }
    return [0, []];
}

type T readonly & S;
type S [INT, int]|[STRING, string];

const INT = 1;
const STRING = 2;

function testInvalidTypesWithImmutableIntersection(T t) {
    match t {
        [STRING, var val] => {
            int _ = val;
        }
        [INT, var val] => {
            string? _ = val;
        }
    }
}

