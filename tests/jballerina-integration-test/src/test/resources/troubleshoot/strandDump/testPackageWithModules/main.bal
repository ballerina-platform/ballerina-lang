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
import testPackageWithModules.utils;
import testPackageWithModules.anotherutils;

boolean globalVar = false;

public function main() {
    future<()> futureResult1 = @strand {thread: "any"} start balfunc();
    future<()> futureResult2 = start utils:entryfunc();
    anotherutils:println("main main");
    error? unionResult = wait futureResult2;
    future<()> futureResult3 = @strand {thread: "any"} start anotherutils:entryfunc();
    foobar();
}

public function foobar() {
    foo();
}

function foo() {
    bar();
}

function bar() {
    worker w1 {
        sleep_and_wait();
        5 -> w2;
    }

    worker w2 {
        anotherutils:println("bar w2");
        int x = <- w1;
    }

    anotherutils:println("bar main");
    wait w2;
}

function sleep_and_wait() {
    sleep_and_wait_nested();
}

function sleep_and_wait_nested() {
    anotherutils:println("sleep_and_wait_nested main");
    runtime:sleep(100);
}

function balfunc() {

    @strand {thread: "any"}
    worker w1 {
        anotherutils:println("balfunc w1");
        lock {
            while (true) {
                globalVar = !globalVar;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        runtime:sleep(1);
        anotherutils:println("balfunc w2");
        lock {
            globalVar = !globalVar;
        }
    }

    worker w3 {
        anotherutils:println("balfunc w3");
        10 ->> w4;
    }

    worker w4 {
        anotherutils:println("balfunc w4");
        runtime:sleep(100);
        int x = <- w3;
    }

    worker w5 {
        20 -> function;
        anotherutils:println("balfunc w5");
        error? unionResult = flush;
    }

    worker w6 {
        anotherutils:println("balfunc w6");
        runtime:sleep(100);
        30 -> w7;
    }

    worker w7 returns int {
        anotherutils:println("balfunc w7");
        int x = <- w6;
        return x;
    }

    worker w8 {
        anotherutils:println("balfunc w8");
        int intResult = wait w7;
    }

    anotherutils:println("balfunc main");
    runtime:sleep(100);
    int y = <- w5;
}
