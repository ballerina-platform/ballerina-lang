
int lockWithinLockInt1 = 0;

string lockWithinLockString1 = "";

blob blobValue;

boolean boolValue;

function lockWithinLock() (int, string) {
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
    return lockWithinLockInt1, lockWithinLockString1;
}

function lockWithinLockInWorkers() (int, string) {
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
    worker w3 {
        sleep(30);
        return lockWithinLockInt1, lockWithinLockString1;
    }
}

function lockInsideWhileLoop() (int) {
    worker w1 {
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
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideLock() (int, string) {
    worker w1 {
        lock {
            sleep(50);
            lockWithinLockInt1 = lockWithinLockInt1 + 1;
            lockWithinLockString1 = "hello";
            var ddd, err = <int>lockWithinLockString1;
            if (err != null) {
                throw err;
            }
        }
    }
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "second worker string";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
        return lockWithinLockInt1, lockWithinLockString1;
    }
}

function throwErrorInsideLockInsideTryFinally() (int, string) {
    worker w1 {
        try {
            lock {
                sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                var ddd, err = <int>lockWithinLockString1;
                if (err != null) {
                    throw err;
                }
            }
        } catch (error e) {
            sleep (10);
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        } finally {
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        }
        return lockWithinLockInt1, lockWithinLockString1;
    }
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string value after try catch finally";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideTryCatchFinallyInsideLock() (int, string) {
    worker w1 {
        lock {
            try {
                sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                var ddd, err = <int>lockWithinLockString1;
                if (err != null) {
                    throw err;
                }

            } catch (error e) {
                sleep (10);
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            } finally {
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            }
        }
        sleep(10);
        return lockWithinLockInt1, lockWithinLockString1;
    }
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try catch finally inside lock";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideTryFinallyInsideLock() (int, string) {
    worker w1 {
        lock {
            try {
                sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                var ddd, err = <int>lockWithinLockString1;
                if (err != null) {
                    throw err;
                }
            } finally {
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            }
        }
    }
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try finally";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
        return lockWithinLockInt1, lockWithinLockString1;
    }
}

function throwErrorInsideLockInsideTryCatch() (int, string) {
    worker w1 {
        try {
            lock {
                sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                var ddd, err = <int>lockWithinLockString1;
                if (err != null) {
                    throw err;
                }
            }
        } catch (error e) {
            sleep (10);
            lock {
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
            }
        }
        return lockWithinLockInt1, lockWithinLockString1;
    }
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string value after try catch";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}

function throwErrorInsideTryCatchInsideLock() (int, string) {
    worker w1 {
        lock {
            try {
                sleep(50);
                lockWithinLockInt1 = lockWithinLockInt1 + 1;
                lockWithinLockString1 = "hello";
                var ddd, err = <int>lockWithinLockString1;
                if (err != null) {
                    throw err;
                }

            } catch (error e) {
                sleep (10);
                lock {
                    lockWithinLockInt1 = lockWithinLockInt1 + 1;
                }
            }
        }
        sleep(10);
        return lockWithinLockInt1, lockWithinLockString1;
    }
    worker w2 {
        sleep(10);
        lock {
            lockWithinLockString1 = "worker 2 sets the string after try catch inside lock";
            lockWithinLockInt1 = lockWithinLockInt1 + 50;
        }
    }
}


function lockWithinLockInWorkersForBlobAndBoolean() (boolean, blob) {
    worker w1 {
        lock {
            boolValue = true;
            string strVal = "sample blob output";
            blobValue = strVal.toBlob("UTF-8");
            lock {
                boolValue = true;
            }
            sleep(100);
        }
    }
    worker w2 {
        sleep(20);
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
        sleep(30);
        return boolValue, blobValue;
    }
}

