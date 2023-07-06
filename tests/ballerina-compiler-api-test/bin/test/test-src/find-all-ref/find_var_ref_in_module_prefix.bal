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

import testorg/testproject;
import ballerina/lang.'string as str;

testproject:Digit zero = 0;

function test() {
    testproject:Person p = {name: "John Doe", age: 24};

    if (true) {
        int x = 'int:fromString("100");

        if (false) {
            int d = 40;

            if (true) {
                int e = 'int:max(d, x);
            }

            int f = 60;
            byte[] bytes = str:toBytes(p.name);
            var pObj = new testproject:PersonObj("Jane", 20);
        }

        float circum  = 2 * testproject:PI * 10;
        int xum = testproject:add(10, 20);
    }
}

@testproject:Config {host: "localhost""}
function foo() {
}
