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

const R1 = "first reason";
const R2 = "second reason";

type E1 error<R1>;
type E2 error<R2>;

function foo() returns E1? {
    int i = check bar(); // should fail - check returns E2, but return type is E1?
}

function bar() returns int|E2 {
    return E2();
}

public function main() {
    E1? x = foo();
}

public function baz() {
    worker w1 returns error? {
        if (true) {
            return error("generic error");
        }

        5 -> w2;
    }

    worker w2 returns E1? {
        int j = check <- w1; // should fail - the check-ed error is of type `error`
    }

    E1? res = wait w2;
}
