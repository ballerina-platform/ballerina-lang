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

// When array-length is present, the implicit initial value of the array type is a list of the
// specified length, with each member of the array having its implicit initial value; otherwise, the
// implicit initial value of an array type is an array of length 0.
// When a list value has an inherent type that is an array without an array-length, then
// attempting to write a member of a list at an index i that is greater than or equal to the current
// length of the list will first increase the length of the list to i + 1, with the newly added
// members of the array having the implicit initial value of T
@test:Config {}
function testImplicitInitialValueOfArrays() {
    // tested in S05_values_types_variables/implicit_initial_value.bal
}
