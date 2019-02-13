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

import ballerina/test;

// An XML value is iterable as a sequence of its items, where each character item is
// represented by a string with a single code point and other items are represented by a
// singleton XML value.
// TODO: fix XML character item iteration, expected to be iterated by single code point
// https://github.com/ballerina-platform/ballerina-lang/issues/13190
@test:Config {
    groups: ["deviation"]
}
function testXmlIterationBroken() {
    xml xmlCharacters = xml `Hello, world!`;

    anydata[] arr = [];
    int index = 0;

    foreach xml|string item in xmlCharacters {
        arr[index] = item;
        index += 1;
    }

    test:assertEquals(arr.length(), 1, msg = "expected iteration to result in one element");
    test:assertEquals(arr[0], xmlCharacters, msg = "expected iteration to produce the same element");
    // string[] stringArray = <string[]> string[].convert(arr);
    // string actualContent = "";
    // foreach string s in stringArray {
    //     actualContent += s;
    // }
    // test:assertEquals(actualContent, "Hello, world!", msg = "expected iteration over all code points");
}
