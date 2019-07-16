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

// typed-binding-pattern := impliable-type-descriptor binding-pattern
// impliable-type-descriptor := type-descriptor | var

// A typed-binding-pattern combines a type-descriptor and a binding-pattern,
// and is used to create the variables occurring in the binding-pattern. If var is used instead of a type-descriptor,
// it means the type is implied. How the type is implied depends on the context of the typed-binding-pattern.
// The simplest and most common form of a typed-binding-pattern is for the binding pattern to consist of just a
// variable name. In this cases, the variable is constrained to contain only values matching the type descriptor.
@test:Config {}
function testSimpleTypedBindingPattern() {
    int var1 = 5;
    test:assertEquals(var1, 5, msg = "expected simple value to be assigned to simple binding pattern");
}

@test:Config {}
function testImpliedSimpleTypedBindingPattern() {
    var var1 = 5;
    test:assertEquals(var1, 5, msg = "expected simple value to be assigned to simple binding pattern");
}
