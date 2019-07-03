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

function testLength() returns int {
    int[] arr = [10, 20, 30, 40];
    return arr.length();
}

//function testMap() returns int[] {
//    int[] arr = [10, 20, 30, 40];
//    int[] newArr = arr.^"map"(function (int x) returns int {
//        return x/10;
//    });
//    return newArr;
//}

//function testForeach() returns string {
//    string?[] arr = ["Hello", "World!", (), "from", "Ballerina"];
//    string result = "";
//
//    arr.forEach(function (string? x) {
//        if (x is string) {
//            result += x;
//        }
//    });
//
//    return result;
//}

function testSlice() returns float[] {
    float[] arr = [12.34, 23.45, 34.56, 45.67, 56.78];
    return arr.slice(1, 4);
}

function testRemove() returns [string, string[]] {
    string[] arr = ["Foo", "Bar", "FooFoo", "BarBar"];
    string elem = arr.remove(2);
    return [elem, arr];
}
