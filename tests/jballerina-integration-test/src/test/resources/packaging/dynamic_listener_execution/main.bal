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

import ballerina/lang.runtime;
import ballerina/test;

int count = 0;
function init() {
   incrementCount();
   assertCount(1);
}

public class ABC {

    *runtime:DynamicListener;
    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
       incrementCount();
       assertCount(2);
    }

    public function gracefulStop() returns error? {
       incrementCount();
       assertCount(3);
       panic error("Stopped module A");
    }

    public function immediateStop() returns error? {
    }
}

public function incrementCount() {
    count += 1;
}
public function assertCount(int val) {
    test:assertEquals(count, val);
}

public function main() {
    ABC ep = new ABC("ModA");
    checkpanic ep.start();
    runtime:registerListener(ep);
}

