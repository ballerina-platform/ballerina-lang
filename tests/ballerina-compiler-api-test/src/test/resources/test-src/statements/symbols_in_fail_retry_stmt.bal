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

function testRetry1() returns error? {
    string str = "string";
    int count = 0;
    error er = error error:Retriable("Error");
    retry {
        count = count + 1;
        if (count < 5) {
            str += "retry";
            func();
        }
        str = "value";
        fail er;
    }
}

function testRetry2() returns error? {
    string str = "string";
    retry<RetryMgr> (2) {
        str = "apple";
        func();
        fail func2();
    }
}

function testRetry3() returns error? {
    retry transaction {
        int value = 2000;
        check commit;
    } on fail error e {
        fail func2();
    }
}

function func() {
}

function func2() returns error {
    return error("The Error");
}

class RetryMgr {
    private int count;
    public function init(int count = 3) {
        self.count = count;
    }
    public function shouldRetry(error e) returns boolean {
        return true;
    }
}
