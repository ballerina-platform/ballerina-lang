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
import ballerina/lang.'value as value;

function testCyclicCloneableTypeWithAny() {
    value:Cloneable x = error("x");
    any y = x; // Should fail since `value:Cloneable` includes `error` and `x` can hold an error.
}

class MyClass {
    int i = 1;
}

function testCloneWithTypeWithInvalidInferredType() {
    anydata x = {};
    MyClass|error y = x.cloneWithType();
    MyClass|error z = value:cloneWithType(x);
}

type Person record {
    int name;
};

function testEnsureTypeFunction() returns error? {
    Person p = {name: 12};
    int age = check p.name;
}

type RecordWithHandleField record {|
    int i;
    handle h;
|};

function testToJsonConversionError() {
    table<RecordWithHandleField> tb = table [
         {i: 12, h: java:fromString("pqr")},
         {i: 34, h: java:fromString("pqr")}
   ];

   json _ = tb.toJson();
}
