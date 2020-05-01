import ballerina/runtime;
import testOrg/mod2;

function testLockWIthInvokableChainsAccessingGlobal() {
    @strand{thread : "any"}
    worker w1 {
        lock {
            runtime:sleep(20);
            mod2:chain(1, true);
        }
    }

    @strand{thread : "any"}
    worker w2 {
        lock {
            runtime:sleep(20);
            mod2:chain2(2, true);
        }
    }

    runtime:sleep(20);
    var [recurs1, recurs2] = mod2:getValues();
    if (recurs2 == 20  || recurs1 == 20) {
        panic error("Invalid Value");
    }
}
