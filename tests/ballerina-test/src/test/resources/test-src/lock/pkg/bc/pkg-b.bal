package pkg.bc;

import pkg.ab;

function lockWithinLockInWorkers() (int, string) {
    worker w1 {
        lock {
            ab:lockInt1 = 90;
            ab:lockString1 = "sample output from package";
            lock {
                ab:lockInt1 = 66;
            }
            sleep(100);
            ab:lockInt1 = 45;
        }
    }
    worker w2 {
        sleep(20);
        lock {
            ab:lockString1 = "hello";
            lock {
                ab:lockInt1 = 88;
            }
            ab:lockInt1 = 56;
        }
    }
    worker w3 {
        sleep(30);
        return ab:lockInt1, ab:lockString1;
    }
}
