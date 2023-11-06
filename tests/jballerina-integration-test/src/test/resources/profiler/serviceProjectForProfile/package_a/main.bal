// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

int count = 0;

function init() {
    incrementCount();
    assertCount(1);
}

listener ABC ep = new ABC("ModA");

public class ABC {

    private string name = "";

    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
        incrementCount();
        assertCount(2);
    }

    public function gracefulStop() returns error? {
        incrementCount();
        assertCount(3);
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]? name) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

public function incrementCount() {
    count += 1;
}

public function assertCount(int val) {
    test:assertEquals(count, val);
}
