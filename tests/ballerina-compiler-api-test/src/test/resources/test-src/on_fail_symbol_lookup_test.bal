// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testMatchOnFail(any val) {
    match val {
        var x => {
            any a = val;
        }
        _ => {
            any f = val;
        }
    } on fail error err {
        error errRef = err;
    }
}

function testWhileOnFail(){
    int iter = 0;
    while (iter < 3) {

    } on fail error err {
        error ref = err;
    }
}

function testForEachOnFail() {
    int[] arr = [1,2,3];
    foreach int item in arr {
        fail getError();
    } on fail error err {
        error ref = err;
    }
}

function testLockOnFail(){
    lock {
        fail getError();
    } on fail error err {
        error ref = err;
    }
}

function testDoOnFail() {
    int x = 10;
    do {
        int y = x + 10;
    } on fail error e {
        string s = e.message() + x.toString();
    }
}
