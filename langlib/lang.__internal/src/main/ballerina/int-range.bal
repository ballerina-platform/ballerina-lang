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

import ballerina/lang.'object as lang_object;

# The `createIntRange` function creates a `object:IntRange` object and returns it. This function is used to replace the binary
# integer range expression in Desugar phase.
#
# + s - The lower bound of the integer range inclusive
# + e - The upper bound if the integer range inclusive
# + return - `object:IntRange` object
public isolated function createIntRange(int s, int e) returns lang_object:IntRange {
    lang_object:IntRange intRange = new(s, e);
    return intRange;
}
