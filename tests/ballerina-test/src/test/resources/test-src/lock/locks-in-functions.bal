import ballerina/runtime;

int lockWithinLockInt1 = 0;

string lockWithinLockString1 = "";

blob blobValue;

boolean boolValue;

function lockWithinLock() returns (int, string) {
    lock {
        lockWithinLockInt1 = 50;
        lockWithinLockString1 = "sample value";
        lock {
            lockWithinLockString1 = "second sample value";
            int ret = makeAsync();
            lockWithinLockInt1 = 99;
            lock {
                lockWithinLockInt1 = 90;
            }
        }
        lockWithinLockInt1 = 77;
    }
    return (lockWithinLockInt1, lockWithinLockString1);
}

function makeAsync() returns (int) {
    worker w1 {
        runtime:sleep(100);
    }
    worker w2 {
        return 6;
    }
}

function lockWithinLockInWorkers() returns (int, string) {
    worker w1 {
        lock {
            lockWithinLockInt1 = 90;
            lockWithinLockString1 = "sample output";
            lock {
                lockWithinLockInt1 = 66;
            }
            runtime:sleep(100);
            lockWithinLockInt1 = 45;
        }
    }
    worker w2 {
        runtime:sleep(20);
        lock {
            lockWithinLockString1 = "hello";
            lock {
                lockWithinLockInt1 = 88;
            }
            lockWithinLockInt1 = 56;
        }
    }
    worker w3 {
        runtime:sleep(30);
        return (lockWithinLockInt1, lockWithinLockString1);
    }
}

function lockInsideWhileLoop() returns (int) {
    worker w1 {
        int i = 0;
        while (i < 6) {
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
            i = i +1;
            runtime:sleep(10);
        }
        return lockWithinLockInt1;
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideLock() returns (int, string) {
    worker w1 {
        lock {
            runtime:sleep(50);
            lockWithinLockInt1 = lockWithinLockInt1 + 1;
            lockWithinLockString1 = "hello";
            int ddd;
            match <int>lockWithinLockString1 {
                int a => ddd = a;
                error err => throw err;
            }
        }
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            lockWithinLockString1 = "second worker string";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
        return (lockWithinLockInt1, lockWithinLockString1);
    }
}

function throwErrorInsideLockInsideTryFinally() returns (int, string) {
    worker w1 {
        try {
            lock {
                runtime:sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                int ddd;
                match <int>lockWithinLockString1 {
                    int a => ddd = a;
                    error err => throw err;
                }
            }
        } catch (error e) {
            runtime:sleep(10);
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        } finally {
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        }
        return (lockWithinLockInt1, lockWithinLockString1);
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string value after try catch finally";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideTryCatchFinallyInsideLock() returns (int, string) {
    worker w1 {
        lock {
            try {
                runtime:sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                int ddd;
                match <int>lockWithinLockString1 {
                    int a => ddd = a;
                    error err => throw err;
                }

            } catch (error e) {
                runtime:sleep(10);
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            } finally {
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            }
        }
        runtime:sleep(10);
        return (lockWithinLockInt1, lockWithinLockString1);
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try catch finally inside lock";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideTryFinallyInsideLock() returns (int, string) {
    worker w1 {
        lock {
            try {
                runtime:sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                int ddd;
                match <int>lockWithinLockString1 {
                    int a => ddd = a;
                    error err => throw err;
                }
            } finally {
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            }
        }
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try finally";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
        return (lockWithinLockInt1, lockWithinLockString1);
    }
}

function throwErrorInsideLockInsideTryCatch() returns (int, string) {
    worker w1 {
        try {
            lock {
                runtime:sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                int ddd;
                match <int>lockWithinLockString1 {
                    int a => ddd = a;
                    error err => throw err;
                }
            }
        } catch (error e) {
            runtime:sleep(10);
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        }
        return (lockWithinLockInt1, lockWithinLockString1);
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string value after try catch";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideTryCatchInsideLock() returns (int, string) {
    worker w1 {
        lock {
            try {
                runtime:sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                int ddd;
                match <int>lockWithinLockString1 {
                    int a => ddd = a;
                    error err => throw err;
                }

            } catch (error e) {
                runtime:sleep(10);
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            }
        }
        runtime:sleep(10);
        return (lockWithinLockInt1, lockWithinLockString1);
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try catch inside lock";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}


function lockWithinLockInWorkersForBlobAndBoolean() returns (boolean, blob) {
    worker w1 {
        lock {
            boolValue = true;
            string strVal = "sample blob output";
            blobValue = strVal.toBlob("UTF-8");
            lock {
                boolValue = true;
            }
            runtime:sleep(100);
        }
    }
    worker w2 {
        runtime:sleep(20);
        lock {
            boolValue = false;
            lock {
                string wrongStr = "wrong output";
                blobValue = wrongStr.toBlob("UTF-8");
            }
            boolValue = false;
        }
    }
    worker w3 {
        runtime:sleep(30);
        return (boolValue, blobValue);
    }
}

function returnInsideLock() returns (int, string) {
    worker w1 {
        string value = returnInsideLockPart();
    }
    worker w2 {
        runtime:sleep(10);
        lock {
            if (lockWithinLockInt1 == 44) {
                lockWithinLockString1 = "changed value11";
                lockWithinLockInt1 = 88;
            } else {
                lockWithinLockString1 = "wrong value";
                lockWithinLockInt1 = 34;
            }
        }
        return (lockWithinLockInt1, lockWithinLockString1);
    }
}

function returnInsideLockPart() returns (string) {
    lock {
        lockWithinLockInt1 = 44;
        lockWithinLockString1 = "value1";
        return lockWithinLockString1;
    }
}

function breakInsideLock() returns (int, string) {
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
    worker w2 {
        runtime:sleep(20);
        lock {
            if (lockWithinLockInt1 == 40) {
                lockWithinLockInt1 = 657;
                lockWithinLockString1 = "lock value inside second worker after while";
            } else {
                lockWithinLockInt1 = 3454;
                lockWithinLockString1 = "wrong value";
            }
        }
        return (lockWithinLockInt1, lockWithinLockString1);
    }
}

function nextInsideLock() returns (int, string) {
    worker w1 {
        int i = 0;
        while (i < 1) {
            i = i + 1;
            lock {
                lockWithinLockInt1 = 40;
                lockWithinLockString1 = "lock value inside while";
                if (i == 0) {
                    next;
                }
            }
        }
    }
    worker w2 {
        runtime:sleep(20);
        lock {
            if (lockWithinLockInt1 == 40) {
                lockWithinLockInt1 = 657;
                lockWithinLockString1 = "lock value inside second worker after while";
            } else {
                lockWithinLockInt1 = 3454;
                lockWithinLockString1 = "wrong value";
            }
        }
        return (lockWithinLockInt1, lockWithinLockString1);
    }
}

