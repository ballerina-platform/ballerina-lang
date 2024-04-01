// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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

function testOnFailInWorker() returns int {
    int[] vals = returnIntArr();
    int key = returnOne();
    int val = 0;

    worker A {
        int? index = vals.indexOf(key);
        if index != () {
            val = vals[index];
        } else {
            check error("value not found");
        }
    } on fail {
        val = -1;
    }
    wait A;
    return val;
}

function testDoOnFailInsideWorker() returns int {
    int val = 0;
    worker A {
        do {
            val += 1;
            fail error("error in do");
        } on fail {
            val += 1;
        }
        fail error("error for worker");
    } on fail {
        val += 1;
    }
    wait A;
    return val;
}

function testReturnWithinOnFail() returns int {
    int x = returnOne();
    worker A returns string {
        if (x == 1) {
            check error("one");
        }
        return "not one";
    } on fail error e {
        return e.message();
    }
    string str = wait A;
    return str == "one" ? -1 : 0;
}

function testOnFailWorkerWithVariable() returns int {
    int x = 0;
    worker A {
        do {
            x += 1;
            fail error("error in do");
        } on fail {
            x += 1;
        }
        fail error("error in worker");
    } on fail error e {
        if e.message() == "error in worker" {
            x -= 2;
        } else {
            x -= -1;
        }
    }
    wait A;
    return x;
}

function testWorkerOnFailWithSend() returns int {
    worker A {
        int x = 1;
        x -> B;
        check error("testWorkerOnFailWithSend");
    } on fail var err {
        _ = err.message();
    }

    worker B returns int {
        return <- A;
    }
    return wait B;
}

function returnOne() returns int => 1;

function returnIntArr() returns int[] => [2, 3, 4, 5];
