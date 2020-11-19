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

map<(function(int warmup, int benchmark) returns int)> singleExecFunctions = {};
map<function()> multiExecFunctions = {};

public function loadFunctions() {
    registerSingleExecFunctions();
    registerMultiExecFunctions();
}

function getFunction(string name) returns (function(int warmup, int benchmark) returns int)|function()|() {
    if (singleExecFunctions.hasKey(name)) {
        return singleExecFunctions[name];
    } else if (multiExecFunctions.hasKey(name)) {
        return multiExecFunctions[name];
    }
}

function addSingleExecFunction(string name, (function(int warmup, int benchmark) returns int) f) {
    if (singleExecFunctions.hasKey(name) || multiExecFunctions.hasKey(name)) {
        panic error("Unable to add function", message = "function with name `" + name + "` already exists.");
    } else {
        singleExecFunctions[name] = f;
    }
}

function addMultiExecFunction(string name, function() f) {
    if (singleExecFunctions.hasKey(name) || multiExecFunctions.hasKey(name)) {
        panic error("Unable to add function", message = "function with name `" + name + "` already exists.");
    } else {
        multiExecFunctions[name] = f;
    }
}
