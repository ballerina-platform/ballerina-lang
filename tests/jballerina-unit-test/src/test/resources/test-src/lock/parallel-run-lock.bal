import ballerina/runtime;

int a = 0;
int b = 0;
int c = 0;
int d = 0;
int e = 0;
int f = 0;
int g = 0;
int h = 0;

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

    runtime:sleep(80);

    if (!(a == 1 && b == 1 && c == 1 && d == 1 && e == 1 && f == 0 && g == 1 && h == 1)) {
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
