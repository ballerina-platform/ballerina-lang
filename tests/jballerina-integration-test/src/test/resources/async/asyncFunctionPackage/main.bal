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


import ballerina/jballerina.java;
import ballerina/test;
import testOrg/functionsLib.mod1 as functionsLib;
import asyncFunctionPackage.mod1;

public function main() {
    testAsyncFunction();
    mod1:testAsyncFunction();
    print("Tests passed");
}

function testAsyncFunction() {
    future<()> f1 = start functionsLib:functionWithNoParam();
    error? r1 = wait f1;
    test:assertEquals(r1 is (), true);

    future<()> f2 = start functionsLib:functionWithRestArgs("hello", "world");
    error? r2 = wait f2;
    test:assertEquals(r2 is (), true);

    future<()> f3 = start functionsLib:functionWithArgs(123456, "hello world");
    error? r3 = wait f3;
    test:assertEquals(r3 is (), true);

    future<int[]> f4 = start functionsLib:functionReturnVargs(1, 2, 3);
    error|int[] r4 = wait f4;
    test:assertEquals(r4 is int[], true);
    if (r4 is int[]) {
        test:assertEquals(r4, [1, 2, 3]);
    }

    future<functionsLib:Person> f5 = start functionsLib:functionReturnRecord({id: 14, name: "waruna"});
    error|functionsLib:Person r5 = wait f5;
    test:assertEquals(r5 is functionsLib:Person, true);
    if (r5 is functionsLib:Person) {
        test:assertEquals(r5, {id: 14, name: "waruna"});
    }

    future<anydata> f6 = start functionsLib:functionReturnAnydata(123.456);
    error|anydata r6 = wait f6;
    test:assertEquals(r6 is anydata, true);
    if (r6 is anydata) {
        test:assertEquals(r6, 123.456);
    }
}

function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
