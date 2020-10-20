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
import pkg.callee;

public function main() {
    testCast();
}

function testCast() {
    future<string> fs1 = start callee:getValueMessage([1,2]);
    string result1 = wait fs1;
    test:assertEquals(result1, "The value is [1,2]");

    map<int> marks = {sam: 50, jon: 60};
    future<string> fs2 = start callee:getValueMessage(marks);
    string result2 = wait fs2;
    test:assertEquals(result2, "The value is {\"sam\":50,\"jon\":60}");
}
