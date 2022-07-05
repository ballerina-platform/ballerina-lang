// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


function testInvalidTypes(map<map<int|error>> a) {
    match a {
        {x: var p, ...var oth} if p is anydata => {
            map<error> m = p;
            map<map<int>> n = oth;
        }
    }
}

function testInvalidTypesWithJson(json j) returns [int, map<boolean>] {
    match j {
        {x: var x, ...var y} => {
            return [x, y];
        }
        {a: var z} => {
            return [z, {a: z}];
        }
    }
    return [0, {}];
}

type Person record {|
    int id;
    string name;
    boolean employed;
|};

function testInvalidTypesWithClosedRecord(Person person) {
    match person {
        {id: var x, ...var rest} => {
            boolean a = x;
            map<boolean> b = rest;
        }
    }
}

type ClosedRecordWithOneField record {|
    int i;
|};

type EmptyClosedRecord record {|
|};

function testInvalidTypesWithClosedRecordUnion(ClosedRecordWithOneField|EmptyClosedRecord rec) {
    match rec {
        {i: var x, ...var rest} => {
            string m = x;
            boolean n = rest;
        }
        {...var rest} => {
            string n = rest;
        }
    }
}

type T readonly & (S1|S2);

type S1 record {|
    1 x;
    3 z;
    string y1;
    int y2;
    decimal y3;
|};

type S2 record {|
    2 x;
    4 z;
    string y1;
    int y2;
    decimal y3;
|};

function testMatchNarrowing() {
    T t = {x: 1, z: 3, y1: "s", y2: 12, y3: 3};
    match t {
        {x: 1, z: 3, y1: var val1, y2: var val2, y3: var val3} => {
            () _ = val1;
            string _ = val2;
            string _ = val3;
        }
    }
}
