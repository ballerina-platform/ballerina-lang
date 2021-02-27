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

int initCount = 0;
int count = 0;

function init() {
    initCount += 1;
    incrementAndAssertInt(1);
}

public function main() {
}

public function getInitCount() returns int {
    return initCount;
}

public class TestListener {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
        incrementAndAssert(self.name, "basic", 6, false);
        incrementAndAssert(self.name, "first dependent", 7, false);
        incrementAndAssert(self.name, "second dependent", 8, false);
        incrementAndAssert(self.name, "current", 9, false);
    }

    public function gracefulStop() returns error? {
        incrementAndAssert(self.name, "current", 10, true);
        incrementAndAssert(self.name, "second dependent", 11, true);
        incrementAndAssert(self.name, "first dependent", 12, true);
        incrementAndAssert(self.name, "basic", 13, true);
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

listener TestListener ep = new TestListener("basic");

public function incrementAndAssertInt(int val) {
    count += 1;
    test:assertEquals(count, val);
}

function incrementAndAssert(string name, string expectedName, int val, boolean err) {
    if (expectedName == name) {
        incrementAndAssertInt(val);
        if(err) {
            panic error("Stopped module " + name);
        }
    }
}
