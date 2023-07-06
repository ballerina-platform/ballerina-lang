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

import ballerina/lang.'float;

type Foo record {|
    string s;
    int i;
|};

string s = "hello";

function testVarNameFieldOfIncorrectType() {
    int s = 1; // shadows the global string `s` variable.
    boolean b = true;

    Foo f = {s, i: 1};
    map<string> m = {s, b: "world"};
}

boolean b = true;

function testUnspecifiedClosedRecordVarNameField() {
    Foo f = {s: "hello", i: 1, b};
}

function testUndeclaredVarNameField() {
    Foo f = {s: "hello", i};
    map<string> m = {a: "hello", s, c};
}

function testInvalidModuleQualifiedIdentifierAsVarNameField() {
    map<float> m = {a: 1.0, 'float:PI};
}
