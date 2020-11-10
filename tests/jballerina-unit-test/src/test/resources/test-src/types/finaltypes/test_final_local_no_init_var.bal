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

function testSubsequentInitializationOfFinalVar() {
    final int i;
    i = 12;

    assertEquality(12, i);
}

function testSubsequentInitializationOfFinalVarInBranches() {
    assertEquality("ONE", initializeFinalVarInBranches1(1));
    assertEquality("TWO", initializeFinalVarInBranches1(2));
    assertEquality("OTHER", initializeFinalVarInBranches1(3));
    assertEquality("ONE", initializeFinalVarInBranches2(1));
    assertEquality("TWO", initializeFinalVarInBranches2(2));
    assertEquality("OTHER", initializeFinalVarInBranches2(3));
}

function initializeFinalVarInBranches1(int i) returns string {
    final string s;
    if i == 1 {
        s = "ONE";
    } else if i == 2 {
        s = "TWO";
    } else {
        s = "OTHER";
    }
    return s;
}

function initializeFinalVarInBranches2(int i) returns string {
    final string s;

    match i {
        1 => {
            s = "ONE";
        }
        2 => {
            s = "TWO";
        }
        _ => {
            s = "OTHER";
        }
    }
    return s;
}

function assertEquality(any|error expected, any|error actual) {
    if (expected is anydata && actual is anydata && expected == actual) {
        return;
    }

    if (expected === actual) {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
