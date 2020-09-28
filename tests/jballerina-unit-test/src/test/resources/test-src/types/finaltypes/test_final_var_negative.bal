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

final int globalFinalInt = 10;

public function testFinalGlobalVariable() {
    int v1 = globalFinalInt;
    globalFinalInt = 20;
}

function testLocalFinalValueWithType() {
    final string name = "Ballerina";
    name = "ABC";
}

function testLocalFinalValueWithoutType() {
    final var name = "Ballerina";
    name = "ABC";
}

function testLocalFinalValueWithTypeInitializedFromFunction() {
    final string name = getName();
    name = "ABC";
}

function testLocalFinalValueWithoutTypeInitializedFromFunction() {
    final var name = getName();
    name = "ABC";
}

function getName() returns string {
    return "Ballerina";
}

function testAssignmentToSubsequentlyInitializedFinalNoInitVar(boolean b) {
    final int i;
    i = 12; // OK, first assignment.

    i = 13; // cannot assign a value to final 'i'

    final string s;

    if b {
        s = "true";
    } else if i == 2 {
        s = "i is 2";
    } else {
        s = "else";
    }
    s = "after if-else";
}

function testAssignmentToSubsequentlyPotentiallyInitializedFinalNoInitVar() {
    final int i;
    boolean b = false;

    if b {
        i = 10;
    } else {
        b = true;
    }
    i = 12;

    final string s;

    while b {
        s = "initialized";
    }
    s = "after while";
}

function testAccessOfSubsequentlyPotentiallyInitializedFinalNoInitVar() {
    final int i;
    boolean b = false;

    if b {
        i = 10;
    } else {
        b = true;
    }
    int x = i;

    final string s;

    while b {
        s = "initialized";
    }
    string t = s; // should result in an error - see https://github.com/ballerina-platform/ballerina-lang/issues/25634
}
