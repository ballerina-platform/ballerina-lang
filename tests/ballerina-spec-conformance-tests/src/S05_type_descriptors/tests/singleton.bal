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

// singleton-type-descriptor := simple-const-expr
// simple-const-expr :=
//   nil-literal
//   | boolean
//   | [Sign] int-literal
//   | [floating-point-type] [Sign] floating-point-literal
//   | string-literal
//   | variable-reference
// floating-point-type := < (decimal|float) >

// A singleton type is a type containing a single shape. A singleton type is described 
// using an compile-time constant expression for a single value: the type contains the 
// shape of that value. Note that it is possible for the variable-reference within the 
// simple-const-expr to reference a structured value.
type NIL ();
type BOOLEAN_TRUE true;
type BOOLEAN_FALSE false;
type INT_ZERO 0;
type INT_NEGATIVE_ONE -1;
type FLOAT_NEGATIVE_ONE -1.0;
type STRING "SINGLETON";

@test:Config {}
function testSingletonTypeDescriptors() {
    NIL s1 = ();
    test:assertEquals(s1, (), msg = "expected value to be equal to singleton value");
    
    BOOLEAN_TRUE s2 = true;
    test:assertEquals(s2, true, msg = "expected value to be equal to singleton value");

    BOOLEAN_FALSE s3 = false;
    test:assertEquals(s3, false, msg = "expected value to be equal to singleton value");

    INT_ZERO s4 = 0;
    test:assertEquals(s4, 0, msg = "expected value to be equal to singleton value");

    INT_NEGATIVE_ONE s5 = -1;
    test:assertEquals(s5, -1, msg = "expected value to be equal to singleton value");

    FLOAT_NEGATIVE_ONE s6 = -1.0;
    test:assertEquals(s6, -1.0, msg = "expected value to be equal to singleton value");

    STRING s7 = "SINGLETON";
    test:assertEquals(s7, "SINGLETON", msg = "expected value to be equal to singleton value");
}
