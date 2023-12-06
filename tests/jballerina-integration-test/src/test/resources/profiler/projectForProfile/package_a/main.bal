// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import foo/package_a.mod_a1;
import ballerina/jballerina.java;

public function main() returns error? {
    mod_a1:func1();
    int[] arr = createRandomIntArray(check float:pow(10,3).cloneWithType(int));
    int[] sortedArr = bubbleSort(arr);
    boolean isSorted = isSortedArray(sortedArr);
    print("Is the array sorted? " + isSorted.toString());
}

public isolated function bubbleSort(int[] arr) returns int[] {
    int n = arr.length();
    int temp = 0;
    boolean swapped = false;
    foreach int i in 0 ... n - 2 {
        foreach int j in 1 ... n - 1 - i {
            if (arr[j - 1] > arr[j]) {
                temp = arr[j - 1];
                arr[j - 1] = arr[j];
                arr[j] = temp;
                swapped = true;
            }
        }
        if (!swapped) {
            break;
        }
    }
    return arr;
}

isolated function isSortedArray(int[] sortedArr) returns boolean {
    foreach int i in 0 ..< sortedArr.length() - 1 {
        if (sortedArr[i] > sortedArr[i + 1]) {
            return false;
        }
    }
    return true;
}

isolated function createRandomIntArray(int size) returns int[] {
    int[] array = [];
    int count = 0;
    foreach int i in 0 ..< size {
        array.push(count);
        count += 1;
    }
    return array;
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
