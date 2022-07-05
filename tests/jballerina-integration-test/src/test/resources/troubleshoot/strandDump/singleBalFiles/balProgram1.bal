// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

boolean globalVar = false;

public function main() {
    foo();
}

function foo() {
    bar();
}

function bar() {

    @strand {thread: "any"}
    worker w1 {
        lock {
            while (true) {
                globalVar = !globalVar;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        runtime:sleep(1.5);
        lock {
            globalVar = !globalVar;
        }
    }

    worker w3 {
        10 ->> w4;
    }

    worker w4 {
        runtime:sleep(100);
        int x = <- w3;
    }

    worker w5 {
        20 -> function;
        error? unionResult = flush;
    }

    worker w6 {
        runtime:sleep(100);
        30 -> w7;
    }

    worker w7 returns int {
        int x = <- w6;
        return x;
    }

    worker w8 {
        int intResult = wait w7;
    }

    runtime:sleep(100);
    int y = <- w5;
}
