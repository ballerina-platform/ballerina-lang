package pkg.bc;

import pkg.ab;
import ballerina/runtime;

function lockWithinLockInWorkers() returns (int, string) {
    worker w1 {
        lock {
            ab:lockInt1 = 90;
            ab:lockString1 = "sample output from package";
            lock {
                ab:lockInt1 = 66;
            }
            runtime:sleep(100);
            ab:lockInt1 = 45;
        }
    }
    worker w2 {
        runtime:sleep(20);
        lock {
            ab:lockString1 = "hello";
            lock {
                ab:lockInt1 = 88;
            }
            ab:lockInt1 = 56;
        }
    }
    worker w3 {
        runtime:sleep(30);
        return (ab:lockInt1, ab:lockString1);
    }
}
