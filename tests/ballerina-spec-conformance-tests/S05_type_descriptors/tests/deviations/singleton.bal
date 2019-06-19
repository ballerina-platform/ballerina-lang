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

// A singleton type is a type containing a single shape. A singleton type is described 
// using an compile-time constant expression for a single value: the type contains the 
// shape of that value. Note that it is possible for the variable-reference within the 
// simple-const-expr to reference a structured value.
// TODO: support unsupported types
// https://github.com/ballerina-platform/ballerina-lang/issues/13410
map<string> strMap = {
    one: "test string 1",
    two: "test string 2",
    three: "test string 3"
};

// type INT_POSITIVE_ONE +1;
// type FLOAT_NEGATIVE_TWO -2.0f;
// type FLOAT_TWO 2.0f;
// type FLOAT_POSITIVE_TWO +2.0f;
// type DECIMAL_NEGATIVE_THREE -3.0d;
// type DECIMAL_THREE 3.0d;
// type DECIMAL_POSITIVE_THREE +3.0d;
// type MAP_VAR_REF strMap;

@test:Config {
    groups: ["deviation"]
}
function testSingletonTypeDescriptorsBroken() {
    // INT_POSITIVE_ONE s1 = +1;
    // test:assertEquals(s1, +1, msg = "expected value to be equal to singleton value");

    // FLOAT_NEGATIVE_TWO s2 = -2.0f;
    // test:assertEquals(s2, -2.0, msg = "expected value to be equal to singleton value");

    // FLOAT_TWO s3 = 2.0f;
    // test:assertEquals(s3, 2.0, msg = "expected value to be equal to singleton value");

    // FLOAT_POSITIVE_TWO s4 = +2.0f;
    // test:assertEquals(s4, +2.0, msg = "expected value to be equal to singleton value");

    // DECIMAL_NEGATIVE_THREE s5 = -3.0d;
    // test:assertEquals(s5, -3.0d, msg = "expected value to be equal to singleton value");

    // DECIMAL_THREE s6 = 3.0d;
    // test:assertEquals(s6, 3.0d, msg = "expected value to be equal to singleton value");

    // DECIMAL_POSITIVE_THREE s7 = +3.0d;
    // test:assertEquals(s7, +3.0d, msg = "expected value to be equal to singleton value");

    // MAP_VAR_REF s8 = {
    //     one: "test string 1",
    //     two: "test string 2",
    //     three: "test string 3"
    // };
    // test:assertEquals(s7, <map<string>> { one: "test string 1", two: "test string 2", three: "test string 3" }, 
    //                   msg = "expected value to be equal to singleton value");
}
