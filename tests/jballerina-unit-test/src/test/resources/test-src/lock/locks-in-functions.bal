// Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/lang.'int as ints;
import ballerina/jballerina.java;
import ballerina/test;

int lockWithinLockInt1 = 0;

string lockWithinLockString1 = "";

byte[] blobValue = [];

boolean boolValue = false;

function resetlockWithinLockInt1() {
    lock {
        lockWithinLockInt1 = 0;
    }
}

function testLockWithinLock() {
    test:assertEquals(lockWithinLock(), [77, "second sample value"]);
}

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

function testLockWithinLockInWorkers() {
    test:assertEquals(lockWithinLockInWorkers(), [66, "sample output"]);
}

function lockWithinLockInWorkers() returns [int, string] {
    @strand {thread: "any"}
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

    @strand {thread: "any"}
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

function testLockInsideWhileLoop() {
    test:assertEquals(lockInsideWhileLoop(), 56);
}

function lockInsideWhileLoop() returns (int) {
    resetlockWithinLockInt1();

    @strand {thread: "any"}
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
        i = i + 1;
        sleep(10);
    }
    return lockWithinLockInt1;
}

function convertStringToInt() {
    lock {
        sleep(50);
        lockWithinLockInt1 = lockWithinLockInt1 + 1;
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

function testThrowErrorInsideLock() {
    var [intResult, stringResult] = throwErrorInsideLock();
    test:assertEquals(intResult, 51);
    test:assertTrue(stringResult is "second worker string" || stringResult is "hello");
}

function throwErrorInsideLock() returns [int, string] {
    resetlockWithinLockInt1();

    @strand {thread: "any"}
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
    test:assertTrue(waitResult is error);
    if (waitResult is error) {
        test:assertEquals(waitResult.message(), "{ballerina/lang.int}NumberParsingError");
    }
    lock {
        return [lockWithinLockInt1, lockWithinLockString1];
    }
}

function errorPanicInsideLock() {
    lock {
        convertStringToInt();
    }
}

function testThrowErrorInsideLockInsideTrap() {
    test:assertEquals(throwErrorInsideLockInsideTryFinally(), [53,
    "worker 2 sets the string value after try catch finally"]);
}

function throwErrorInsideLockInsideTryFinally() returns [int, string] {
    resetlockWithinLockInt1();

    @strand {thread: "any"}
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

function testThrowErrorInsideTryCatchFinallyInsideLock() {
    test:assertEquals(throwErrorInsideTryCatchFinallyInsideLock(), [53,
    "worker 2 sets the string after try catch finally inside lock"]);
}

function throwErrorInsideTryCatchFinallyInsideLock() returns [int, string] {
    resetlockWithinLockInt1();

    @strand {thread: "any"}
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

function testThrowErrorInsideTryFinallyInsideLock() {
    test:assertEquals(throwErrorInsideTryFinallyInsideLock(), [552, "worker 2 sets the string after try finally"]);
}

function throwErrorInsideTryFinallyInsideLock() returns [int, string] {
    lock {
        lockWithinLockInt1 = 500;
    }

    @strand {thread: "any"}
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
    error? w1Result = trap wait w1;
    test:assertTrue(w1Result is error);
    lock {
        lockWithinLockString1 = "worker 2 sets the string after try finally";
        lockWithinLockInt1 = lockWithinLockInt1 + 50;
    }
    lock {
        return [lockWithinLockInt1, lockWithinLockString1];
    }
}

function testThrowErrorInsideLockInsideTryCatch() {
    test:assertEquals(throwErrorInsideLockInsideTryCatch(), [52, "worker 2 sets the string value after try catch"]);
}

function throwErrorInsideLockInsideTryCatch() returns [int, string] {
    resetlockWithinLockInt1();

    @strand {thread: "any"}
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

function testThrowErrorInsideTryCatchInsideLock() {
    test:assertEquals(throwErrorInsideTryCatchInsideLock(), [52,
    "worker 2 sets the string after try catch inside lock"]);
}

function throwErrorInsideTryCatchInsideLock() returns [int, string] {
    resetlockWithinLockInt1();

    @strand {thread: "any"}
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

function testLockWithinLockInWorkersForBlobAndBoolean() {
    test:assertEquals(lockWithinLockInWorkersForBlobAndBoolean(), [true, "sample blob output".toBytes()]);
}

function lockWithinLockInWorkersForBlobAndBoolean() returns [boolean, byte[]] {
    @strand {thread: "any"}
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

    @strand {thread: "any"}
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

function testReturnInsideLock() {
    test:assertEquals(returnInsideLock(), [88, "changed value11"]);
}

function returnInsideLock() returns [int, string] {
    @strand {thread: "any"}
    worker w1 {
        test:assertEquals(returnInsideLockPart(), "value1");
    }

    sleep(10);
    wait w1;

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

function testBreakInsideLock() {
    test:assertEquals(breakInsideLock(), [657, "lock value inside second worker after while"]);
}

function breakInsideLock() returns [int, string] {
    @strand {thread: "any"}
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

function testNextInsideLock() {
    test:assertEquals(nextInsideLock(), [657, "lock value inside second worker after while"]);
}

function nextInsideLock() returns [int, string] {
    @strand {thread: "any"}
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
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
