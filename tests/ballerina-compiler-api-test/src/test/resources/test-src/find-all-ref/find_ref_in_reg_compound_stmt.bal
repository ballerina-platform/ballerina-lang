// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testWhile() {
    int k = 0;
    while next(k) < 10 {
        int i = next(k);
        k += i;
    }
}

function testForeach() {
    foreach int i in 0...9 {
        int p = i;
        string str = "";
    }

    int[] ints = [1, 2, 4, 5];
    foreach var item in ints {
        int val = 10;
        call();
    }

    foreach int[] [a, b, c] in [[1, 2, 3], [3, 4, 5]] {
        int val = a;
        call(b);
    }
}

function testLock() {
    int iVal = 0;
    lock {
        iVal += 1;
        call();
    }
}

function next(int i) returns int {
    return i + 1;
}

function call(any value = ()) {
}

function func(error? e) {
}
