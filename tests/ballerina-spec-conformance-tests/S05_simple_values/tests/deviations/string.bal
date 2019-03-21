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

// In a StringNumericEscape, CodePoint must valid Unicode code point; more precisely, it
// must be a hexadecimal numeral denoting an integer n where 0 <= n < 0xD800 or 0xDFFF <
// n <= 0x10FFFF.
// TODO: Need to support string numeric escape
// https://github.com/ballerina-platform/ballerina-lang/issues/13180
@test:Config {
    groups: ["deviation"]
}
function testStringNumericEscapeBroken() {
    // string s1 = "\u0";
    // string s2 = "\uD799";
    // string s3 = "\uEFFF";
    // string s4 = "\u10FFFF";
    // // TODO: add tests
}
