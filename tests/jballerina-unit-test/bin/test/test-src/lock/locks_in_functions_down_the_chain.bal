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

import ballerina/test;

string lockString = "";

function testLockWithinFunctionsDownTheLine() {
    string ans = lockWithinLock();
    test:assertTrue((ans == "w1w1w1vw2w2w2v") || (ans == "w2w2w2v"));
}

function lockWithinLock() returns string {
    worker w1 {
        lock {
            lockString = lockString + "w1w1";
            lockLevel2("w1v");
        }
    }

    lock {
        lockString = lockString + "w2w2";
        lockLevel2("w2v");
        return lockString;
    }
}

function lockLevel2(string s) {
    lockLevel3(s);
}

function lockLevel3(string s) {
    lock {
        lockString = lockString + s;
    }
}
