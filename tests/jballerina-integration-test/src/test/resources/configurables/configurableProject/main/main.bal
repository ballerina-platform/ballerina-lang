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

import ballerina/test;
import ballerina/java;

configurable int intVar = 5;
configurable float floatVar = 9.5;
configurable string stringVar = ?;
configurable boolean booleanVar = ?;
configurable decimal decimalVar = 10.1;

configurable int[] & readonly intArr = ?;
configurable float[] & readonly floatArr = [9.0, 5.3, 5.6];
configurable string[] & readonly stringArr = ["apple", "orange", "banana"];
configurable boolean[] & readonly booleanArr = [true, true];
configurable decimal[] & readonly decimalArr = ?;

public function main() {
    test:assertEquals(42, intVar);
    test:assertEquals(3.5, floatVar);
    test:assertEquals("abc", stringVar);
    test:assertEquals(true, booleanVar);

    decimal result = 24.87;
    test:assertEquals(result, decimalVar);

    test:assertEquals([1,2,3], intArr);
    test:assertEquals([9.0, 5.6], floatArr);
    test:assertEquals(["red", "yellow", "green" ], stringArr);
    test:assertEquals([true, false, false, true], booleanArr);

    decimal[] & readonly resultArr = [8.9, 4.5, 6.2];
    test:assertEquals(resultArr, decimalArr);

    print("Tests passed");
}

//Extern methods to verify no errors while testing
function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}
