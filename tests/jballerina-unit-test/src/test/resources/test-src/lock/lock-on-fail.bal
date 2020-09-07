// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
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
int lockWithinLockInt1 = 0;

string lockWithinLockString1 = "";

byte[] blobValue = [];

boolean boolValue = false;

function failLockWithinLock() returns [int, string] {
    lock {
        lockWithinLockInt1 = 50;
        lockWithinLockString1 = "sample value";
        lock {
            lockWithinLockString1 = "second sample value";
            lockWithinLockInt1 = 99;
            lock {
                lockWithinLockInt1 = 90;
            }
            error err = error("custom error", message = "error value");
            fail err;
        }
    } on fail error e {
        lockWithinLockInt1 = 100;
        lockWithinLockString1 = "Error caught";
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}


function checkLockWithinLock() returns [int, string] {
    lock {
        lockWithinLockInt1 = 50;
        lockWithinLockString1 = "sample value";
        lock {
            lockWithinLockString1 = "second sample value";
            lockWithinLockInt1 = 99;
            lock {
                lockWithinLockInt1 = 90;
            }
            lockWithinLockInt1 = check getError();
        }
        lockWithinLockInt1 = 77;
    } on fail error e {
        lockWithinLockInt1 = 100;
        lockWithinLockString1 = "Error caught";
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function getError()  returns int|error {
    error err = error("Custom Error");
    return err;
}