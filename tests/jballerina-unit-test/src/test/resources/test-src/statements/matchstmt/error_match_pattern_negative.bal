// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type MyError1 error<record { int x; }>;
type MyError2 error<record { int y; }>;

function testNegative1() {
    int v1 = 0;
    match v1 {
        error() => {} // pattern will not be matched
    }

    any v2 = 0;
    match v2 {
        error() => {} // pattern will not be matched
    }

    MyError1 v3 = error MyError1("Message", x = 2);
    match v3 {
        error MyError2(y = 4) => {} // pattern will not be matched
    }
}

function testNegative2() {
    any|error v = 2;
    match v {
        error() | error() => {} // unreachable pattern
        error("Message") | error("Message") => {} // unreachable pattern
        error(_) | error(_) => {} // unreachable pattern
        error(var a) | error(var b) => {} // unreachable pattern
        error("Message", error("foo")) | error("Message", error("foo")) => {} // unreachable pattern
        error("Message", error("Msg"), x = 2) | error("Message", error("Msg"), x = 2) => {} // unreachable pattern
        error("Message", error("Msg"), x = 4) | error("Message", error("Msg"), x = 3) => {}
        error MyError1("Message", x = 4) | error MyError1("Message", x = 4) => {} // unreachable pattern
    }
}
