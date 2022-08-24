// Copyright (c) (2020-2022), WSO2 Inc. (http://www.wso2.com).
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

type ClosedArr int[3];

type OpenArr int[];

type Tuple [int, string, float, boolean];

public function validateAPI() {

    anydata[] arr1 = getIntArray(ClosedArr);
    test:assertEquals(arr1.length(), 3);
    test:assertEquals(arr1, [1, 2, 3]);
    test:assertTrue(arr1 is ClosedArr);

    anydata[] arr2 = getIntArray(OpenArr);
    test:assertEquals(arr2.length(), 3);
    test:assertEquals(arr2, [1, 2, 3]);
    test:assertTrue(arr2 is OpenArr);

    anydata[] arr3 = getIntArrayWithInitialValues(ClosedArr, [4, 5, 6]);
    test:assertEquals(arr3.length(), 3);
    test:assertEquals(arr3, [4, 5, 6]);
    test:assertTrue(arr3 is ClosedArr);

    anydata[] arr4 = getIntArrayWithInitialValues(OpenArr, [7, 8]);
    test:assertEquals(arr4.length(), 2);
    test:assertEquals(arr4, [7, 8]);
    test:assertTrue(arr4 is OpenArr);

    anydata[] arr5 = getTupleWithInitialValues(Tuple, [7, "hello", 2.3, true]);
    test:assertEquals(arr5.length(), 4);
    test:assertEquals(arr5, [7, "hello", 2.3, true]);
    test:assertTrue(arr5 is Tuple);
}

function getIntArray(typedesc<any> t) returns anydata[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getIntArrayWithInitialValues(typedesc<any> t, anydata value) returns anydata[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getTupleWithInitialValues(typedesc<any> t, anydata value) returns anydata[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
