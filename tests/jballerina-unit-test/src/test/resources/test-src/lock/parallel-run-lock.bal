import ballerina/runtime;

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
            runtime:sleep(20);
            a = 1;
        }
    }

    @strand{thread:"any"}
    worker w2 {
        lock {
            runtime:sleep(20);
            b = 1;
        }
    }

    @strand{thread:"any"}
    worker w3 {
        lock {
            runtime:sleep(20);
            c = 1;
            d = 1;
        }
    }

    @strand{thread:"any"}
    worker w4 {
        lock {
            runtime:sleep(20);
            d = 1;
            e = 1;
        }
    }

    @strand{thread:"any"}
    worker w5 {
        lock {
            runtime:sleep(20);
            g = 1;
            h = 1;
        }
    }

    @strand{thread:"any"}
    worker w6 {
        lock {
            runtime:sleep(20);
            b = 1;
            e = 1;
        }
    }

    @strand{thread:"any"}
    worker w7 {
        lock {
            runtime:sleep(20);
            i = 1;
        }
    }

    runtime:sleep(100);

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
            runtime:sleep(20);
            add();
        }
    }

    @strand{thread : "any"}
    worker w2 {
        lock {
            runtime:sleep(20);
            add();
        }
    }

    runtime:sleep(20);
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
            runtime:sleep(20);
            chain(1, true);
        }
    }

    @strand{thread : "any"}
    worker w2 {
        lock {
            runtime:sleep(20);
            chain2(2, true);
        }
    }

    runtime:sleep(20);
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
            runtime:sleep(20);
            recursive(5);
        }
    }

    @strand{thread : "any"}
    worker w2 {
        lock {
            runtime:sleep(20);
            recursive(3);
        }
    }

    runtime:sleep(20);
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
                runtime:sleep(1);
                ref[ref.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        foreach var i in 1 ... 100 {
            lock {
                runtime:sleep(1);
                ref[ref.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w3 {
        toBeUpdateRef = [];
        foreach var i in 1 ... 100 {
            lock {
                runtime:sleep(1);
                toBeUpdateRef[toBeUpdateRef.length()] = 1;
            }
        }
    }

    runtime:sleep(250);
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
                runtime:sleep(1);
                refConditional[refConditional.length()] = 1;
            }
        }
    }

    @strand {thread: "any"}
    worker w2 {
        foreach var i in 1 ... 100 {
            lock {
                runtime:sleep(1);
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
                runtime:sleep(1);
                toBeUpdateRefConditional[toBeUpdateRefConditional.length()] = 1;
            }
        }
    }

    runtime:sleep(250);
    if (toBeUpdateRefConditional.length() == 100 && refConditional.length() == 200) {
        panic error("Invalid value 100 recieved in \"testForGlobalRefUpdateInsideConditional\"");
    }
}
