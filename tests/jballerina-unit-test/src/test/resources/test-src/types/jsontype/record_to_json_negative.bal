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

function testNegatives() {
    record {
        string name;
    } person = {name: "John"};

    json j1 = person; // open with anydata record to json

    record {|
        map<any> m;
    |} r1 = {m: {}};

    json j2 = r1;

    record {|
        string name = "";
        record {|
            record {|
                int x = 10;
                any y = ();
            |} nestedL2 = {};
        |} nestedL1 = {};
    |} r2 = {};

    json j3 = r2;

    record {|
        string name = "";
        record {|
            record {|
                int x = 10;
                (int|string|typedesc<any>)...;
            |} nestedL2 = {};
        |} nestedL1 = {};
    |} r3 = {};

    json j4 = r3;
}

