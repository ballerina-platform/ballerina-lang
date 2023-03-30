// Copyright (c) 2020, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/jballerina.java;

int a = 0;
int b = 0;
int c = 0;
int d = 0;
int e = 0;
int f = 0;
int g = 0;
int h = 0;
int i = 0;

function runParallelUsingLocks() {
    @strand{thread:"any"}
    worker w1 {
        lock {
            sleep(20);
            a = 1;
        }
    }

    @strand{thread:"any"}
    worker w2 {
        lock {
            sleep(20);
            b = 1;
        }
    }

    @strand{thread:"any"}
    worker w3 {
        lock {
            sleep(20);
            c = 1;
            d = 1;
        }
    }

    @strand{thread:"any"}
    worker w4 {
        lock {
            sleep(20);
            d = 1;
            e = 1;
        }
    }

    @strand{thread:"any"}
    worker w5 {
        lock {
            sleep(20);
            g = 1;
            h = 1;
        }
    }

    @strand{thread:"any"}
    worker w6 {
        lock {
            sleep(20);
            b = 1;
            e = 1;
        }
    }

    @strand{thread:"any"}
    worker w7 {
        lock {
            sleep(20);
            i = 1;
        }
    }

    map<()> waitResult = wait {w1, w2, w3, w4, w5, w6, w7};

    if (!(a == 1 && b == 1 && c == 1 && d == 1 && e == 1 && f == 0 && g == 1 && h == 1 && i == 1)) {
        panic error("Error in parallel run using locks");
    }
}

int x = 0;
string y = "";

function add() {
    x += 10;
    y += "lockValueInFunction";
}

function testLockWithInvokableAccessingGlobal() {
    @strand{thread : "any"}
    worker w1 {
        lock {
            sleep(20);
            add();
        }
    }

    @strand{thread : "any"}
    worker w2 {
        lock {
            sleep(20);
            add();
        }
    }

    sleep(20);
    if (y == "lockValueInFunctionlockValueInFunction" || x == 20) {
        panic error("Invalid Value");
    }
}

int recurs1 = 0;
int recurs2 = 0;

function chain(int chainBreak, boolean startFunc) {
    if (chainBreak == 1 && !startFunc) {
        return;
    }

    recurs1 += 10;
    chain2(chainBreak, false);
}

function chain2(int chainBreak, boolean startFunc) {
    if (chainBreak == 2 && !startFunc) {
        return;
    }

    recurs2 += 10;
    chain3(chainBreak, false);
}

function chain3(int chainBreak, boolean startFunc) {
    if (chainBreak == 3 && !startFunc) {
        return;
    }

    chain(chainBreak, false);
}

function testLockWIthInvokableChainsAccessingGlobal() {
    @strand{thread : "any"}
    worker w1 {
        lock {
            sleep(20);
            chain(1, true);
        }
    }

    @strand{thread : "any"}
    worker w2 {
        lock {
            sleep(20);
            chain2(2, true);
        }
    }

    sleep(20);
    if (recurs2 == 20  || recurs1 == 20) {
        panic error("Invalid Value");
    }
}

int recurs3 = 0;

function recursive(int breakCondition) {
    if (breakCondition == 0) {
        return;
    }

    recurs3 += 10;
    recursive(breakCondition - 1);
}

function testLockWIthInvokableRecursiveAccessGlobal() {
    @strand{thread : "any"}
    worker w1 {
        lock {
            sleep(20);
            recursive(5);
        }
    }

    @strand{thread : "any"}
    worker w2 {
        lock {
            sleep(20);
            recursive(3);
        }
    }

    sleep(20);
    if (recurs3 == 80) {
        panic error("Invalid Value");
    }
}

int[] values = [];
int[] numbers = values;

function testLocksWhenGlobalVariablesReferToSameValue() {
    @strand {thread: "any"}
    worker w1 {
        foreach var i in 1 ... 1000 {
            lock {
                values[values.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        foreach var i in 1 ... 1000 {
            lock {
                values[values.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w3 {
        foreach var i in 1 ... 1000 {
            lock {
                values[values.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w4 {
        foreach var i in 1 ... 1000 {
            lock {
                numbers[numbers.length()] = 1;
            }
        }
    }
    var result = wait {w1, w2, w3, w4};

    int length = numbers.length();
    if ( length != 4000) {
        panic error("Expected 4000, but found " + length.toString());
    }

}

int[] ref = [];
int[] toBeUpdateRef = ref;
function testForGlobalRefUpdateInsideWorker() {
    ref.removeAll();
    toBeUpdateRef.removeAll();

    @strand {thread: "any"}
    worker w1 {
        foreach var i in 1 ... 100 {
            lock {
                sleep(1);
                ref[ref.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        foreach var i in 1 ... 100 {
            lock {
                sleep(1);
                ref[ref.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w3 {
        toBeUpdateRef = [];
        foreach var i in 1 ... 100 {
            lock {
                sleep(1);
                toBeUpdateRef[toBeUpdateRef.length()] = 1;
            }
        }
    }

    sleep(250);
    if (toBeUpdateRef.length() == 100 && ref.length() == 200) {
        panic error("Invalid value 1000 recieved in \"testForGlobalRefUpdateInsideWorker\"");
    }
}

int[] refConditional = [];
int[] toBeUpdateRefConditional = refConditional;
function testForGlobalRefUpdateInsideConditional() {
    boolean updateRef = true;

    @strand {thread: "any"}
    worker w1 {
        foreach var i in 1 ... 100 {
            lock {
                sleep(1);
                refConditional[refConditional.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        foreach var i in 1 ... 100 {
            lock {
                sleep(1);
                refConditional[refConditional.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w3 {
        if (updateRef) {
            toBeUpdateRefConditional = [];
        }
        foreach var i in 1 ... 100 {
            lock {
                sleep(1);
                toBeUpdateRefConditional[toBeUpdateRefConditional.length()] = 1;
            }
        }
    }

    sleep(250);
    if (toBeUpdateRefConditional.length() == 100 && refConditional.length() == 200) {
        panic error("Invalid value 100 recieved in \"testForGlobalRefUpdateInsideConditional\"");
    }
}

const rounds = 1000;
const workers = 200;

int counter = 1;

function testPanicIfInLockConcurrently() {
    future<()> counterFuture = @strand{thread : "any"} start check_counter();

    foreach int i in 0 ..< workers {
        _ = @strand{thread : "any"} start increment_counter();
    }

    error? result = wait counterFuture;
}

function increment_counter() {
    foreach int i in 0 ..< rounds {
        lock {
            counter += 1;
        }
    }
}

function check_counter() {
    while (while_condition()) {}
}

function while_condition() returns boolean {
    lock {
        return counter < rounds * workers;
    }
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
