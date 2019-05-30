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

string simpleLockCounter = "not-started";

function simpleLock() returns string {
    worker w1 {
        lock {
            simpleLockCounter = "w1 in critical";
            busyWait();
            busyWait();
            busyWait();
            busyWait();
            busyWait();
        }
        simpleLockCounter = "w1 out of critical";
    }
    busyWait();
    if (simpleLockCounter == "not-started") {
        return "main didn't wait for w1 to enter critical";
    }
    lock {
        busyWait();
        if (simpleLockCounter == "w1 out of critical") {
            simpleLockCounter = "main in critical after w1 is out";
        }
    }
    return simpleLockCounter;
}

function busyWait() {
    int i = 0;
    while (i < 10000000) {
        i += 1;
    }
}
