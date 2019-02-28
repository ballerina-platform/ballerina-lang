
import pkg.ab;
import ballerina/runtime;

function lockWithinLockInWorkers() returns (int, string) {
    worker w1 {
        lock {
            ab:updateLockInt1(90);
            ab:updateLockString1("sample output from package");
            lock {
                ab:updateLockInt1(66);
            }
            runtime:sleep(100);
            ab:updateLockInt1(45);
        }
    }
    worker w2 {
        runtime:sleep(20);
        lock {
            ab:updateLockString1("hello");
            lock {
                ab:updateLockInt1(88);
            }
            ab:updateLockInt1(56);
        }
    }

    runtime:sleep(30);
    return (ab:getLockInt1(), ab:getLockString1());

}
