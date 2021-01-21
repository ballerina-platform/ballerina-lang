import ballerina/lang.'int as ints;
import ballerina/jballerina.java;

int lockWithinLockInt1 = 0;

string lockWithinLockString1 = "";

byte[] blobValue = [];

boolean boolValue = false;

function lockWithinLock() returns [int, string] {
    lock {
        lockWithinLockInt1 = 50;
        lockWithinLockString1 = "sample value";
        lock {
            lockWithinLockString1 = "second sample value";
            lockWithinLockInt1 = 99;
            lock {
                lockWithinLockInt1 = 90;
            }
        }
        lockWithinLockInt1 = 77;
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function makeAsync() returns (int) {
    worker w1 {
        sleep(100);
    }

    return 6;
}

function lockWithinLockInWorkers() returns [int, string] {
    @strand{thread:"any"}
    worker w1 {
        lock {
            lockWithinLockInt1 = 90;
            lockWithinLockString1 = "sample output";
            lock {
                lockWithinLockInt1 = 66;
            }
            sleep(100);
            lockWithinLockInt1 = 45;
        }
    }

    @strand{thread:"any"}
    worker w2 {
        sleep(20);
        lock {
            lockWithinLockString1 = "hello";
            lock {
                lockWithinLockInt1 = 88;
            }
            lockWithinLockInt1 = 56;
        }
    }

    sleep(30);
    return [lockWithinLockInt1, lockWithinLockString1];
}

function lockInsideWhileLoop() returns (int) {
    @strand{thread:"any"}
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }

    int i = 0;
    while (i < 6) {
        lock {
            lockWithinLockInt1 = lockWithinLockInt1 + 1;
        }
        i = i +1;
        sleep(10);
    }
    return lockWithinLockInt1;
}

function convertStringToInt() {
    lock {
        sleep(50);
        lockWithinLockInt1 = lockWithinLockInt1 +1;
        lockWithinLockString1 = "hello";
        int ddd;
        var result = ints:fromString(lockWithinLockString1);
        if (result is int) {
            ddd = result;
        } else {
            panic result;
        }
    }
}

function throwErrorInsideLock() returns [int, string] {
    @strand{thread:"any"}
    worker w1 {
        lock {
            convertStringToInt();
        }
    }

    sleep(10);
    lock {
        lockWithinLockString1 = "second worker string";
        lockWithinLockInt1 = lockWithinLockInt1 + 50;
    }
    error? waitResult = trap wait w1;
    return [lockWithinLockInt1, lockWithinLockString1];
}

function errorPanicInsideLock() {
    lock {
        convertStringToInt();
    }
}

function throwErrorInsideLockInsideTryFinally() returns [int, string] {
    @strand{thread:"any"}
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string value after try catch finally";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }

    var err = trap errorPanicInsideLock();
    if (err is error) {
        sleep(10);
        lock {
            lockWithinLockInt1 = lockWithinLockInt1 + 1;
        }
    }
    lock {
        lockWithinLockInt1 = lockWithinLockInt1 + 1;
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function throwErrorInsideTryCatchFinallyInsideLock() returns [int, string] {
    @strand{thread:"any"}
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try catch finally inside lock";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }

    lock {
        var err = trap convertStringToInt();
        if (err is error) {
            sleep(10);
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        }
        lock {
            lockWithinLockInt1 = lockWithinLockInt1 + 1;
        }
    }
    sleep(10);
    return [lockWithinLockInt1, lockWithinLockString1];
}

function throwErrorInsideTryFinallyInsideLock() returns [int, string] {
    @strand{thread:"any"}
    worker w1 {
        lock {
            sleep(50);
            lockWithinLockInt1 = lockWithinLockInt1 + 1;
            lockWithinLockString1 = "hello";
            int ddd;
            var result = ints:fromString(lockWithinLockString1);
            if (result is int) {
                ddd = result;
            } else {
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
                panic result;
            }

        }
    }

    sleep(10);
    lock {
        lockWithinLockString1 = "worker 2 sets the string after try finally";
        lockWithinLockInt1 = lockWithinLockInt1 + 50;
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function throwErrorInsideLockInsideTryCatch() returns [int, string] {
    @strand{thread:"any"}
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string value after try catch";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }

    var err = trap errorPanicInsideLock();
    if (err is error) {
        sleep(10);
        lock {
            lockWithinLockInt1 = lockWithinLockInt1 + 1;
        }
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function throwErrorInsideTryCatchInsideLock() returns [int, string] {
    @strand{thread:"any"}
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try catch inside lock";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }

    lock {
        var err = trap convertStringToInt();
        if (err is error) {
            sleep(10);
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        }
    }
    sleep(10);
    return [lockWithinLockInt1, lockWithinLockString1];
}


function lockWithinLockInWorkersForBlobAndBoolean() returns [boolean, byte[]] {
    @strand{thread:"any"}
    worker w1 {
        lock {
            boolValue = true;
            string strVal = "sample blob output";
            blobValue = strVal.toBytes();
            lock {
                boolValue = true;
            }
            sleep(100);
        }
    }

    @strand{thread:"any"}
    worker w2 {
        sleep(20);
        lock {
            boolValue = false;
            lock {
                string wrongStr = "wrong output";
                blobValue = wrongStr.toBytes();
            }
            boolValue = false;
        }
    }

    sleep(30);
    return [boolValue, blobValue];
}

function returnInsideLock() returns [int, string] {
    @strand{thread:"any"}
    worker w1 {
        string value = returnInsideLockPart();
    }

    sleep(10);
    lock {
        if (lockWithinLockInt1 == 44) {
            lockWithinLockString1 = "changed value11";
            lockWithinLockInt1 = 88;
        } else {
            lockWithinLockString1 = "wrong value";
            lockWithinLockInt1 = 34;
        }
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function returnInsideLockPart() returns (string) {
    lock {
        lockWithinLockInt1 = 44;
        lockWithinLockString1 = "value1";
        return lockWithinLockString1;
    }
}

function breakInsideLock() returns [int, string] {
    @strand{thread:"any"}
    worker w1 {
        int i = 0;
        while (i < 3) {
            lock {
                lockWithinLockInt1 = 40;
                lockWithinLockString1 = "lock value inside while";
                if (i == 0) {
                    break;
                }
            }
            i = i + 1;
        }
    }
    sleep(20);
    lock {
        if (lockWithinLockInt1 == 40) {
            lockWithinLockInt1 = 657;
            lockWithinLockString1 = "lock value inside second worker after while";
        } else {
            lockWithinLockInt1 = 3454;
            lockWithinLockString1 = "wrong value";
        }
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function nextInsideLock() returns [int, string] {
    @strand{thread:"any"}
    worker w1 {
        int i = 0;
        while (i < 1) {
            i = i + 1;
            lock {
                lockWithinLockInt1 = 40;
                lockWithinLockString1 = "lock value inside while";
                if (i == 0) {
                    continue;
                }
            }
        }
    }

    sleep(20);
    lock {
        if (lockWithinLockInt1 == 40) {
            lockWithinLockInt1 = 657;
            lockWithinLockString1 = "lock value inside second worker after while";
        } else {
            lockWithinLockInt1 = 3454;
            lockWithinLockString1 = "wrong value";
        }
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Sleep"
} external;
