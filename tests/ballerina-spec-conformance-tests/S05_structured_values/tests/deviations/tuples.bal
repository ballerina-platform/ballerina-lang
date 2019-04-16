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

// A member of a list can be referenced by an integer index representing its position in the list.
// For a list of length n, the indices of the members of the list, from first to last, are 0,1,...,n - 1.
// TODO: Need to support tuple indexing with variable references
// https://github.com/ballerina-platform/ballerina-lang/issues/13164
@test:Config {
    groups: ["deviation"]
}
function testTupleMemberReferenceByInvalidIntegerIndex() {
    //(float, float, float) tuple = (1.1, 0.0, 2.20);
    //
    //int index = -1;
    //assertErrorReason(trap tuple[index], "{ballerina}IndexOutOfRange",
    //    "invalid reason on access by negative index");
    //
    //index = tuple.length();
    //assertErrorReason(trap tuple[index], "{ballerina}IndexOutOfRange",
    //    "invalid reason on tuple by index == tuple length");
    //
    //index = tuple.length() + 3;
    //assertErrorReason(trap tuple[index], "{ballerina}IndexOutOfRange",
    //    "invalid reason on access by index > tuple length");
}

// Note that a tuple type where all the member-type-descriptors are the same is
// equivalent to a an array-type-descriptor with a length.
// TODO: Tuple type descriptor should be equal to array type descriptor
// https://github.com/ballerina-platform/ballerina-lang/issues/13167
//@test:Config {
//    groups: ["deviation"]
//}
//function testTupleAndArrayTypeDescriptorBroken() {
//    int[3] a = (1, 2, 3);
//    (int, int, int) b = [1, 2, 3];
//}
