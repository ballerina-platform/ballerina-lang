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

import ballerina/jballerina.java;
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
        println("w1 started");
        lock {
            while (true) {
                globalVar = !globalVar;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        runtime:sleep(1);
        println("w2 started");
        lock {
            globalVar = !globalVar;
        }
    }

    worker w3 {
        println("w3 started");
        10 ->> w4;
    }

    worker w4 {
        println("w4 started");
        runtime:sleep(100);
        int x = <- w3;
    }

    worker w5 {
        20 -> function;
        println("w5 started");
        error? unionResult = flush;
    }

    worker w6 {
        println("w6 started");
        runtime:sleep(100);
        30 -> w7;
    }

    worker w7 returns int {
        println("w7 started");
        int x = <- w6;
        return x;
    }

    worker w8 {
        println("w8 started");
        int intResult = wait w7;
    }

    println("main started");
    runtime:sleep(100);
    int y = <- w5;
}

function println(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printlnInternal(stdout1, strValue);
}

function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

function printlnInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
