// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.array as arrs;

public function testImportedModuleTypeParam1() returns int[]{
    int[] arr = [10, 20, 30, 40];
    return arrs:map(arr, function (int value) returns int { return value * 2; });
}

function testImportedModuleTypeParam2() returns int {
    int[] arr = [10, 20, 30, 40];
    return arrs:reduce(arr, function (int acc, int val) returns int { return acc + val; }, 0);
}
