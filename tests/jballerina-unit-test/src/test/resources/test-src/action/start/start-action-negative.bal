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
import ballerina/runtime;

function foo() returns int {
    return 1;
}

function bar(int i) returns int {
    return i + 1;
}

function baz(future<int> f) returns int {
    return 3;
}

function zee(future<int>|error f) returns int {
    return 4;
}

function c() {
    future<int>|error k = trap start foo();
    future<int> r1 = start foo();
    var p = baz(start foo());
    future<int> r2 = start baz(start foo());
    future<int> r3 = start zee(trap start foo());
}

function flushTestHelper(error? er) {

}

function flushTestHelper2(error? er) returns error? {
    return er;
}

function process() returns string {
    worker w1 {
        int a = 10;
        wait start foo() -> w2;
        () result = a ->> w2;
        a -> w2;
        error? e = flushTestHelper2(flush w2);
        a -> w2;
        error? e1 = wait start flushTestHelper2(flush w2);
        a = <- w2;

        foreach var i in 1 ... 5 {
        }
    }

    worker w2 {
        int b = 15;
        runtime:sleep(10);

        foreach var i in 1 ... 5 {
        }
        b = bar(<- w1)  ;
        b = wait start bar(<- w1);
        b = <- w1;
        b = <- w1;
        b -> w1;
        flushTestHelper(flush w1);
        future<int> r3;
        r3 = start foo();
    }

   wait w1;
   return "done";
}

function retList() returns int[] {
    return [1, 2];
}

function forI() {
    foreach int i in wait start retList() {

    }
}