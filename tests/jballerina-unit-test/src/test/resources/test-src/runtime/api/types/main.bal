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

import ballerina/test;
import testorg/runtime_api_types.objects;

objects:PublicClientObject obj = new ();

public function main() {
    testRemoteFunctionParameters();
    testFunctionToString();
    testParamTypesString();
    testConstituentTypes();
}

function testConstituentTypes() {
    int[] arr = [1, 2, 3];
    string[] types = objects:getConstituentTypes(arr.cloneReadOnly());
    test:assertEquals(types.length(), 2);
    test:assertEquals(types[0], "int[]");
    test:assertEquals(types[1], "readonly");
}

function testFunctionToString() {
    test:assertEquals(objects:getFunctionString(obj, "testFunction"), "function testFunction(int,decimal,string) returns (())");
    test:assertEquals(objects:getFunctionString(obj, "getRemoteCounter"), "remote function (int,decimal,string) returns (())");

    objects:Service serviceVal = new ();
    test:assertEquals(objects:getFunctionString(serviceVal, "remoteFunction"), "remote function (int,decimal,string) returns (())");
    test:assertEquals(objects:getFunctionString(serviceVal, "resourceFunction"), "resource function get resourceFunction(string test) returns (string)");
}

function testRemoteFunctionParameters() {
    [string, boolean, string][] parameters = objects:getParameters(obj, "getRemoteCounter");
    test:assertEquals(parameters.length(), 3);
    test:assertEquals(parameters[0], ["num", false, "int"]);
    test:assertEquals(parameters[1], ["value", false, "decimal"]);
    test:assertEquals(parameters[2], ["msg", true, "string"]);
}

function testParamTypesString() {
    //Need to be removed after removing getParamTypes() API
    test:assertEquals(objects:getParamTypesString(obj.testFunction), "int decimal string ");
}
