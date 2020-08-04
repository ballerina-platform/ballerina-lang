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

import ballerina/test;

// Single or multiple groups should be enabled/disabled and the output should be verified

@test:Config{
    groups:["g1","g2"]
}
function testFunc1 () {
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups:["g1","g2","g3"]
}
function testFunc2 () {
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups:["g1","g2","g3","g4"]
}
function testFunc3 () {
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups:["g5"]
}
function testFunc4 () {
    test:assertFalse(true, msg = "errorMessage");
}

@test:Config{
    groups:["g6"]
}
function testFunc5 () {
    test:assertFalse(true, msg = "errorMessage");
}

@test:Config{}
function testFunc6 () {
    test:assertFalse(false, msg = "errorMessage");
}
